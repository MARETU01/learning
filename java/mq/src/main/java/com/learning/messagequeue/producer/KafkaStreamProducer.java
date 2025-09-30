package com.learning.messagequeue.producer;

import com.learning.messagequeue.model.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Kafka流式生产者
 * 
 * 演示使用Java Stream API进行流式消息生产
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaStreamProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final AtomicLong messageCounter = new AtomicLong(0);

    /**
     * 流式发送简单消息
     * 使用IntStream生成一系列消息
     */
    public void streamSimpleMessages(int count) {
        log.info("开始流式发送{}条简单消息", count);
        
        IntStream.range(0, count)
            .parallel()  // 并行流提高发送效率
            .mapToObj(i -> {
                Message message = Message.create(
                    "流式消息 " + i + " - " + LocalDateTime.now(), 
                    "STREAM_SIMPLE", 
                    "KafkaStreamProducer"
                );
                return kafkaTemplate.send("stream-topic", message.getId(), message.toString());
            })
            .forEach(future -> future.whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("流式消息发送失败", ex);
                } else {
                    messageCounter.incrementAndGet();
                    if (messageCounter.get() % 100 == 0) {
                        log.info("已发送{}条流式消息", messageCounter.get());
                    }
                }
            }));
        
        log.info("流式简单消息发送完成，共发送{}条消息", count);
    }

    /**
     * 流式发送带分区的消息
     * 使用Stream的map操作为消息分配分区
     */
    public void streamPartitionedMessages(int count, int partitionCount) {
        log.info("开始流式发送{}条分区消息到{}个分区", count, partitionCount);
        
        IntStream.range(0, count)
            .mapToObj(i -> {
                int partition = i % partitionCount;
                Message message = Message.create(
                    "分区消息 " + i + " - 分区" + partition, 
                    "STREAM_PARTITIONED", 
                    "KafkaStreamProducer"
                );
                return new StreamMessageWrapper(message, partition);
            })
            .forEach(wrapper -> {
                CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(
                    "stream-partition-topic", 
                    wrapper.partition, 
                    wrapper.message.getId(), 
                    wrapper.message.toString()
                );
                
                future.whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("分区消息发送失败: partition={}", wrapper.partition, ex);
                    } else {
                        log.debug("分区消息发送成功: partition={}, offset={}", 
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset());
                    }
                });
            });
        
        log.info("流式分区消息发送完成");
    }

    /**
     * 流式发送 keyed 消息
     * 使用Stream生成带key的消息以确保相同key的消息发送到同一分区
     */
    public void streamKeyedMessages(int count, String[] keys) {
        log.info("开始流式发送{}条keyed消息，使用{}个不同的key", count, keys.length);
        
        IntStream.range(0, count)
            .mapToObj(i -> {
                String key = keys[i % keys.length];
                Message message = Message.create(
                    "Keyed消息 " + i + " - key=" + key, 
                    "STREAM_KEYED", 
                    "KafkaStreamProducer"
                );
                return new KeyedMessageWrapper(key, message);
            })
            .forEach(wrapper -> {
                CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(
                    "stream-keyed-topic", 
                    wrapper.key, 
                    wrapper.message.toString()
                );
                
                future.whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Keyed消息发送失败: key={}", wrapper.key, ex);
                    } else {
                        log.debug("Keyed消息发送成功: key={}, partition={}, offset={}", 
                            wrapper.key,
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset());
                    }
                });
            });
        
        log.info("流式Keyed消息发送完成");
    }

    /**
     * 流式发送批量消息
     * 使用Stream的批处理功能
     */
    public void streamBatchMessages(int totalCount, int batchSize) {
        log.info("开始流式发送批量消息: 总数={}, 批大小={}", totalCount, batchSize);
        
        IntStream.range(0, (totalCount + batchSize - 1) / batchSize)
            .mapToObj(batchIndex -> {
                int start = batchIndex * batchSize;
                int end = Math.min(start + batchSize, totalCount);
                return IntStream.range(start, end)
                    .mapToObj(i -> Message.create(
                        "批量消息 " + i + " - 批次" + batchIndex, 
                        "STREAM_BATCH", 
                        "KafkaStreamProducer"
                    ))
                    .toList();
            })
            .forEach(batch -> {
                log.info("发送批次消息，批次大小: {}", batch.size());
                
                batch.forEach(message -> {
                    CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(
                        "stream-batch-topic", 
                        message.getId(), 
                        message.toString()
                    );
                    
                    future.whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error("批量消息发送失败", ex);
                        } else {
                            log.debug("批量消息发送成功: offset={}", 
                                result.getRecordMetadata().offset());
                        }
                    });
                });
                
                // 批次间短暂延迟，避免过载
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        
        log.info("流式批量消息发送完成");
    }

    /**
     * 流式发送优先级消息
     * 使用Stream的sorted功能按优先级排序
     */
    public void streamPriorityMessages(int count) {
        log.info("开始流式发送{}条优先级消息", count);
        
        // 生成带优先级的消息并按优先级排序
        Stream<Message> messageStream = IntStream.range(0, count)
            .mapToObj(i -> {
                int priority = (int) (Math.random() * 10); // 优先级0-9
                Message message = Message.create(
                    "优先级消息 " + i + " - 优先级" + priority, 
                    "STREAM_PRIORITY", 
                    "KafkaStreamProducer"
                );
                message.setPriority(priority);
                return message;
            })
            .sorted((m1, m2) -> Integer.compare(m2.getPriority(), m1.getPriority())); // 降序排序
        
        messageStream.forEach(message -> {
            CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(
                "stream-priority-topic", 
                String.valueOf(message.getPriority()), // 使用优先级作为key
                message.toString()
            );
            
            future.whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("优先级消息发送失败: priority={}", message.getPriority(), ex);
                } else {
                    log.debug("优先级消息发送成功: priority={}, offset={}", 
                        message.getPriority(),
                        result.getRecordMetadata().offset());
                }
            });
        });
        
        log.info("流式优先级消息发送完成");
    }

    /**
     * 流式发送窗口化消息
     * 模拟时间窗口消息处理
     */
    public void streamWindowedMessages(int windowSizeMs, int messageCount) {
        log.info("开始流式发送窗口化消息: 窗口大小={}ms, 消息数量={}", windowSizeMs, messageCount);
        
        long windowStartTime = System.currentTimeMillis();
        
        IntStream.range(0, messageCount)
            .mapToObj(i -> {
                long currentTime = System.currentTimeMillis();
                long windowId = (currentTime - windowStartTime) / windowSizeMs;
                
                Message message = Message.create(
                    "窗口消息 " + i + " - 窗口" + windowId, 
                    "STREAM_WINDOW", 
                    "KafkaStreamProducer"
                );
                message.setWindowId(windowId);
                return new WindowedMessageWrapper(windowId, message);
            })
            .forEach(wrapper -> {
                CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(
                    "stream-window-topic", 
                    String.valueOf(wrapper.windowId), // 使用窗口ID作为key
                    wrapper.message.toString()
                );
                
                future.whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("窗口消息发送失败: windowId={}", wrapper.windowId, ex);
                    } else {
                        log.debug("窗口消息发送成功: windowId={}, offset={}", 
                            wrapper.windowId,
                            result.getRecordMetadata().offset());
                    }
                });
                
                // 控制发送速率
                try {
                    Thread.sleep(windowSizeMs / messageCount);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        
        log.info("流式窗口化消息发送完成");
    }

    /**
     * 流式发送聚合消息
     * 演示消息聚合处理
     */
    public void streamAggregatedMessages(int count, int groupSize) {
        log.info("开始流式发送聚合消息: 总数={}, 分组大小={}", count, groupSize);
        
        IntStream.range(0, count)
            .mapToObj(i -> {
                int groupId = i / groupSize;
                Message message = Message.create(
                    "聚合消息 " + i + " - 组" + groupId, 
                    "STREAM_AGGREGATE", 
                    "KafkaStreamProducer"
                );
                message.setGroupId(groupId);
                return new AggregatedMessageWrapper(groupId, message);
            })
            .forEach(wrapper -> {
                CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(
                    "stream-aggregate-topic", 
                    String.valueOf(wrapper.groupId), // 使用组ID作为key
                    wrapper.message.toString()
                );
                
                future.whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("聚合消息发送失败: groupId={}", wrapper.groupId, ex);
                    } else {
                        log.debug("聚合消息发送成功: groupId={}, offset={}", 
                            wrapper.groupId,
                            result.getRecordMetadata().offset());
                    }
                });
            });
        
        log.info("流式聚合消息发送完成");
    }

    /**
     * 流式发送过滤后的消息
     * 演示消息过滤功能
     */
    public void streamFilteredMessages(int count, String filterKeyword) {
        log.info("开始流式发送过滤消息: 总数={}, 过滤关键词='{}'", count, filterKeyword);
        
        IntStream.range(0, count)
            .mapToObj(i -> {
                boolean shouldInclude = Math.random() > 0.3; // 70%的消息包含关键词
                String content = shouldInclude ? 
                    "过滤消息 " + i + " - 包含" + filterKeyword : 
                    "过滤消息 " + i + " - 不包含";
                
                Message message = Message.create(content, "STREAM_FILTER", "KafkaStreamProducer");
                message.setShouldInclude(shouldInclude);
                return message;
            })
            .filter(Message::getShouldInclude) // 只发送应该包含的消息
            .forEach(message -> {
                CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(
                    "stream-filter-topic", 
                    message.getId(), 
                    message.toString()
                );
                
                future.whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("过滤消息发送失败", ex);
                    } else {
                        log.debug("过滤消息发送成功: offset={}", 
                            result.getRecordMetadata().offset());
                    }
                });
            });
        
        log.info("流式过滤消息发送完成");
    }

    /**
     * 获取已发送消息计数
     */
    public long getMessageCount() {
        return messageCounter.get();
    }

    /**
     * 重置消息计数器
     */
    public void resetMessageCount() {
        messageCounter.set(0);
    }

    // 内部包装类
    private static class StreamMessageWrapper {
        final Message message;
        final int partition;

        StreamMessageWrapper(Message message, int partition) {
            this.message = message;
            this.partition = partition;
        }
    }

    private static class KeyedMessageWrapper {
        final String key;
        final Message message;

        KeyedMessageWrapper(String key, Message message) {
            this.key = key;
            this.message = message;
        }
    }

    private static class WindowedMessageWrapper {
        final long windowId;
        final Message message;

        WindowedMessageWrapper(long windowId, Message message) {
            this.windowId = windowId;
            this.message = message;
        }
    }

    private static class AggregatedMessageWrapper {
        final int groupId;
        final Message message;

        AggregatedMessageWrapper(int groupId, Message message) {
            this.groupId = groupId;
            this.message = message;
        }
    }
}
