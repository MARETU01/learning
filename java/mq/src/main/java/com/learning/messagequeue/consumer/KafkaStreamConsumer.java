package com.learning.messagequeue.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.learning.messagequeue.model.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Kafka流式消费者
 * 
 * 演示使用Java Stream API进行流式消息消费和处理
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaStreamConsumer {

    private final ObjectMapper objectMapper;
    
    // 用于流式处理的缓存结构
    private final Map<Long, List<Message>> windowCache = new ConcurrentHashMap<>();
    private final Map<Integer, List<Message>> groupCache = new ConcurrentHashMap<>();
    private final NavigableMap<Integer, List<Message>> priorityQueue = new ConcurrentSkipListMap<>(Collections.reverseOrder());
    private final AtomicLong processedCount = new AtomicLong(0);
    private final Set<String> processedMessageIds = Collections.newSetFromMap(new ConcurrentHashMap<>());

    /**
     * 流式消费简单消息
     * 使用Stream进行简单的消息处理
     */
    @KafkaListener(topics = "stream-topic", groupId = "stream-simple-group")
    public void consumeSimpleStreamMessages(@Payload List<String> messages,
                                          @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        log.info("流式消费简单消息: 消息数量={}", messages.size());
        
        // 使用Stream处理消息
        List<Message> processedMessages = messages.stream()
            .map(this::parseMessage)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .peek(this::processSimpleMessage)
            .collect(Collectors.toList());
        
        log.info("简单流式消息处理完成: 成功处理{}条消息", processedMessages.size());
    }

    /**
     * 流式消费分区消息
     * 按分区进行流式处理
     */
    @KafkaListener(topics = "stream-partition-topic", groupId = "stream-partition-group")
    public void consumePartitionedStreamMessages(@Payload List<String> messages,
                                               @Header(KafkaHeaders.RECEIVED_PARTITION) List<Integer> partitions,
                                               @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        log.info("流式消费分区消息: 消息数量={}", messages.size());
        
        // 按分区分组并流式处理
        Map<Integer, List<Message>> messagesByPartition = IntStream.range(0, messages.size())
            .mapToObj(i -> {
                Optional<Message> messageOpt = parseMessage(messages.get(i));
                return messageOpt.map(message -> 
                    new AbstractMap.SimpleEntry<>(partitions.get(i), message)
                );
            })
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.groupingBy(
                Map.Entry::getKey,
                Collectors.mapping(Map.Entry::getValue, Collectors.toList())
            ));
        
        // 流式处理每个分区的消息
        messagesByPartition.forEach((partition, partitionMessages) -> {
            log.info("处理分区{}的{}条消息", partition, partitionMessages.size());
            
            partitionMessages.stream()
                .forEach(message -> {
                    processPartitionedMessage(message, partition);
                });
        });
        
        log.info("分区流式消息处理完成");
    }

    /**
     * 流式消费Keyed消息
     * 按key进行流式处理，保证相同key的消息顺序处理
     */
    @KafkaListener(topics = "stream-keyed-topic", groupId = "stream-keyed-group")
    public void consumeKeyedStreamMessages(@Payload List<String> messages,
                                         @Header(KafkaHeaders.RECEIVED_KEY) List<String> keys,
                                         @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        log.info("流式消费Keyed消息: 消息数量={}", messages.size());
        
        // 按key分组并流式处理
        Map<String, List<Message>> messagesByKey = IntStream.range(0, messages.size())
            .mapToObj(i -> {
                Optional<Message> messageOpt = parseMessage(messages.get(i));
                return messageOpt.map(message -> 
                    new AbstractMap.SimpleEntry<>(keys.get(i), message)
                );
            })
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.groupingBy(
                Map.Entry::getKey,
                Collectors.mapping(Map.Entry::getValue, Collectors.toList())
            ));
        
        // 流式处理每个key的消息
        messagesByKey.forEach((key, keyMessages) -> {
            log.info("处理key='{}'的{}条消息", key, keyMessages.size());
            
            keyMessages.stream()
                .forEach(message -> {
                    processKeyedMessage(message, key);
                });
        });
        
        log.info("Keyed流式消息处理完成");
    }

    /**
     * 流式消费批量消息
     * 使用Stream进行批量消息处理
     */
    @KafkaListener(topics = "stream-batch-topic", groupId = "stream-batch-group")
    public void consumeBatchStreamMessages(@Payload List<String> messages,
                                         @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        log.info("流式消费批量消息: 消息数量={}", messages.size());
        
        // 使用Stream进行批量处理
        BatchProcessingResult result = messages.stream()
            .map(this::parseMessage)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(
                () -> new BatchProcessingResult(),
                (batchResult, message) -> {
                    processBatchMessage(message);
                    batchResult.successCount++;
                    batchResult.totalSize += message.getContent().length();
                },
                (batchResult1, batchResult2) -> {
                    batchResult1.successCount += batchResult2.successCount;
                    batchResult1.totalSize += batchResult2.totalSize;
                }
            );
        
        log.info("批量流式消息处理完成: 成功处理{}条消息, 总大小={}字节", 
            result.successCount, result.totalSize);
    }

    /**
     * 流式消费优先级消息
     * 使用优先级队列进行流式处理
     */
    @KafkaListener(topics = "stream-priority-topic", groupId = "stream-priority-group")
    public void consumePriorityStreamMessages(@Payload List<String> messages,
                                            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        log.info("流式消费优先级消息: 消息数量={}", messages.size());
        
        // 将消息按优先级分组
        messages.stream()
            .map(this::parseMessage)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .forEach(message -> {
                int priority = message.getPriority();
                priorityQueue.computeIfAbsent(priority, k -> new ArrayList<>()).add(message);
            });
        
        // 按优先级顺序处理消息
        priorityQueue.entrySet().stream()
            .forEach(entry -> {
                int priority = entry.getKey();
                List<Message> priorityMessages = entry.getValue();
                
                log.info("处理优先级{}的{}条消息", priority, priorityMessages.size());
                
                priorityMessages.stream()
                    .forEach(this::processPriorityMessage);
                
                // 清空已处理的优先级队列
                entry.clear();
            });
        
        log.info("优先级流式消息处理完成");
    }

    /**
     * 流式消费窗口化消息
     * 实现时间窗口流式处理
     */
    @KafkaListener(topics = "stream-window-topic", groupId = "stream-window-group")
    public void consumeWindowedStreamMessages(@Payload List<String> messages,
                                           @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        log.info("流式消费窗口化消息: 消息数量={}", messages.size());
        
        long currentTime = System.currentTimeMillis();
        long windowSizeMs = 5000; // 5秒窗口
        
        // 将消息分配到窗口
        messages.stream()
            .map(this::parseMessage)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .forEach(message -> {
                long windowId = message.getWindowId();
                windowCache.computeIfAbsent(windowId, k -> new ArrayList<>()).add(message);
            });
        
        // 处理已完成的窗口
        windowCache.entrySet().stream()
            .filter(entry -> {
                long windowEndTime = entry.getKey() * windowSizeMs + windowSizeMs;
                return currentTime >= windowEndTime;
            })
            .forEach(entry -> {
                long windowId = entry.getKey();
                List<Message> windowMessages = entry.getValue();
                
                log.info("处理窗口{}的{}条消息", windowId, windowMessages.size());
                
                // 流式处理窗口消息
                WindowProcessingResult result = windowMessages.stream()
                    .collect(
                        () -> new WindowProcessingResult(windowId),
                        (windowResult, message) -> {
                            processWindowedMessage(message, windowId);
                            windowResult.messageCount++;
                        },
                        (windowResult1, windowResult2) -> {
                            windowResult1.messageCount += windowResult2.messageCount;
                        }
                    );
                
                log.info("窗口{}处理完成: 处理{}条消息", result.windowId, result.messageCount);
                
                // 清空已处理的窗口
                entry.clear();
            });
        
        log.info("窗口化流式消息处理完成");
    }

    /**
     * 流式消费聚合消息
     * 实现分组聚合流式处理
     */
    @KafkaListener(topics = "stream-aggregate-topic", groupId = "stream-aggregate-group")
    public void consumeAggregatedStreamMessages(@Payload List<String> messages,
                                              @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        log.info("流式消费聚合消息: 消息数量={}", messages.size());
        
        // 将消息按组ID分组
        messages.stream()
            .map(this::parseMessage)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .forEach(message -> {
                int groupId = message.getGroupId();
                groupCache.computeIfAbsent(groupId, k -> new ArrayList<>()).add(message);
            });
        
        // 处理已完成的组
        groupCache.entrySet().stream()
            .filter(entry -> entry.getValue().size() >= 5) // 假设每组5条消息
            .forEach(entry -> {
                int groupId = entry.getKey();
                List<Message> groupMessages = entry.getValue();
                
                log.info("处理组{}的{}条消息", groupId, groupMessages.size());
                
                // 流式聚合处理
                AggregationResult result = groupMessages.stream()
                    .collect(
                        () -> new AggregationResult(groupId),
                        (aggResult, message) -> {
                            processAggregatedMessage(message, groupId);
                            aggResult.messageCount++;
                            aggResult.totalLength += message.getContent().length();
                        },
                        (aggResult1, aggResult2) -> {
                            aggResult1.messageCount += aggResult2.messageCount;
                            aggResult1.totalLength += aggResult2.totalLength;
                        }
                    );
                
                log.info("组{}聚合处理完成: 消息数={}, 总长度={}", 
                    result.groupId, result.messageCount, result.totalLength);
                
                // 清空已处理的组
                entry.clear();
            });
        
        log.info("聚合流式消息处理完成");
    }

    /**
     * 流式消费过滤消息
     * 实现过滤和转换流式处理
     */
    @KafkaListener(topics = "stream-filter-topic", groupId = "stream-filter-group")
    public void consumeFilteredStreamMessages(@Payload List<String> messages,
                                            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        log.info("流式消费过滤消息: 消息数量={}", messages.size());
        
        // 使用Stream进行过滤和转换处理
        FilterProcessingResult result = messages.stream()
            .map(this::parseMessage)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .filter(message -> message.getShouldInclude()) // 过滤
            .peek(this::processFilteredMessage) // 处理
            .collect(
                () -> new FilterProcessingResult(),
                (filterResult, message) -> {
                    filterResult.processedCount++;
                    if (message.getContent().contains("包含")) {
                        filterResult.matchedCount++;
                    }
                },
                (filterResult1, filterResult2) -> {
                    filterResult1.processedCount += filterResult2.processedCount;
                    filterResult1.matchedCount += filterResult2.matchedCount;
                }
            );
        
        log.info("过滤流式消息处理完成: 处理{}条消息, 匹配{}条消息", 
            result.processedCount, result.matchedCount);
    }

    /**
     * 消息解析方法
     */
    private Optional<Message> parseMessage(String messageStr) {
        try {
            Message message = objectMapper.readValue(messageStr, Message.class);
            return Optional.of(message);
        } catch (Exception e) {
            log.error("消息解析失败: {}", messageStr, e);
            return Optional.empty();
        }
    }

    /**
     * 处理简单消息
     */
    private void processSimpleMessage(Message message) {
        if (processedMessageIds.contains(message.getId())) {
            return; // 避免重复处理
        }
        
        log.debug("处理简单流式消息: {}", message.getId());
        
        // 模拟处理逻辑
        try {
            Thread.sleep(10);
            processedMessageIds.add(message.getId());
            processedCount.incrementAndGet();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 处理分区消息
     */
    private void processPartitionedMessage(Message message, int partition) {
        log.debug("处理分区流式消息: partition={}, message={}", partition, message.getId());
        
        try {
            Thread.sleep(10);
            processedCount.incrementAndGet();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 处理Keyed消息
     */
    private void processKeyedMessage(Message message, String key) {
        log.debug("处理Keyed流式消息: key={}, message={}", key, message.getId());
        
        try {
            Thread.sleep(10);
            processedCount.incrementAndGet();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 处理批量消息
     */
    private void processBatchMessage(Message message) {
        log.debug("处理批量流式消息: {}", message.getId());
        
        try {
            Thread.sleep(5); // 批量处理通常更快
            processedCount.incrementAndGet();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 处理优先级消息
     */
    private void processPriorityMessage(Message message) {
        log.debug("处理优先级流式消息: priority={}, message={}", message.getPriority(), message.getId());
        
        try {
            Thread.sleep(15); // 高优先级消息可能需要更多处理时间
            processedCount.incrementAndGet();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 处理窗口化消息
     */
    private void processWindowedMessage(Message message, long windowId) {
        log.debug("处理窗口化流式消息: windowId={}, message={}", windowId, message.getId());
        
        try {
            Thread.sleep(10);
            processedCount.incrementAndGet();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 处理聚合消息
     */
    private void processAggregatedMessage(Message message, int groupId) {
        log.debug("处理聚合流式消息: groupId={}, message={}", groupId, message.getId());
        
        try {
            Thread.sleep(10);
            processedCount.incrementAndGet();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 处理过滤消息
     */
    private void processFilteredMessage(Message message) {
        log.debug("处理过滤流式消息: {}", message.getId());
        
        try {
            Thread.sleep(10);
            processedCount.incrementAndGet();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 获取已处理消息计数
     */
    public long getProcessedCount() {
        return processedCount.get();
    }

    /**
     * 重置处理计数器
     */
    public void resetProcessedCount() {
        processedCount.set(0);
        processedMessageIds.clear();
    }

    /**
     * 获取窗口缓存状态
     */
    public Map<Long, Integer> getWindowCacheStatus() {
        return windowCache.entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> entry.getValue().size()
            ));
    }

    /**
     * 获取组缓存状态
     */
    public Map<Integer, Integer> getGroupCacheStatus() {
        return groupCache.entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> entry.getValue().size()
            ));
    }

    // 内部结果类
    private static class BatchProcessingResult {
        int successCount = 0;
        long totalSize = 0;
    }

    private static class WindowProcessingResult {
        final long windowId;
        int messageCount = 0;

        WindowProcessingResult(long windowId) {
            this.windowId = windowId;
        }
    }

    private static class AggregationResult {
        final int groupId;
        int messageCount = 0;
        long totalLength = 0;

        AggregationResult(int groupId) {
            this.groupId = groupId;
        }
    }

    private static class FilterProcessingResult {
        int processedCount = 0;
        int matchedCount = 0;
    }
}
