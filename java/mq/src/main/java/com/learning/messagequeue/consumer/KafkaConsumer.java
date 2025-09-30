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

/**
 * Kafka消息消费者
 * 
 * 演示不同类型的Kafka消息消费模式
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaConsumer {

    private final ObjectMapper objectMapper;

    /**
     * 消费简单消息
     */
    @KafkaListener(topics = "simple-topic", groupId = "simple-group")
    public void consumeSimpleMessage(@Payload String messageStr,
                                   @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                   @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                                   @Header(KafkaHeaders.OFFSET) long offset) {
        try {
            Message message = objectMapper.readValue(messageStr, Message.class);
            log.info("消费简单消息: topic={}, partition={}, offset={}, message={}", 
                topic, partition, offset, message);
            
            processMessage(message);
            
        } catch (Exception e) {
            log.error("处理简单消息失败", e);
            throw new RuntimeException("处理失败", e);
        }
    }

    /**
     * 消费多分区消息
     */
    @KafkaListener(topics = "multi-partition-topic", groupId = "multi-partition-group")
    public void consumeMultiPartitionMessage(@Payload String messageStr,
                                            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                                            @Header(KafkaHeaders.OFFSET) long offset) {
        try {
            Message message = objectMapper.readValue(messageStr, Message.class);
            log.info("消费多分区消息: topic={}, partition={}, offset={}, message={}", 
                topic, partition, offset, message);
            
            processMessage(message);
            
        } catch (Exception e) {
            log.error("处理多分区消息失败", e);
            throw new RuntimeException("处理失败", e);
        }
    }

    /**
     * 手动确认消息
     */
    @KafkaListener(topics = "simple-topic", groupId = "manual-ack-group")
    public void consumeManualAckMessage(@Payload String messageStr,
                                      @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                      @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                                      @Header(KafkaHeaders.OFFSET) long offset,
                                      Acknowledgment acknowledgment) {
        try {
            Message message = objectMapper.readValue(messageStr, Message.class);
            log.info("手动确认消费消息: topic={}, partition={}, offset={}, message={}", 
                topic, partition, offset, message);
            
            processMessage(message);
            
            // 手动提交偏移量
            acknowledgment.acknowledge();
            
        } catch (Exception e) {
            log.error("处理手动确认消息失败", e);
            // 不提交偏移量，消息会被重新消费
        }
    }

    /**
     * 批量消费消息
     */
    @KafkaListener(topics = "simple-topic", groupId = "batch-group", 
                  containerFactory = "batchKafkaListenerContainerFactory")
    public void consumeBatchMessages(java.util.List<String> messages,
                                   @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                   @Header(KafkaHeaders.RECEIVED_PARTITION) java.util.List<Integer> partitions,
                                   @Header(KafkaHeaders.OFFSET) java.util.List<Long> offsets) {
        log.info("批量消费消息: topic={}, 消息数量={}", topic, messages.size());
        
        for (int i = 0; i < messages.size(); i++) {
            try {
                Message message = objectMapper.readValue(messages.get(i), Message.class);
                log.debug("处理批量消息中的第{}条: partition={}, offset={}, message={}", 
                    i + 1, partitions.get(i), offsets.get(i), message);
                
                processMessage(message);
                
            } catch (Exception e) {
                log.error("批量处理第{}条消息失败", i + 1, e);
                // 可以选择跳过错误消息继续处理其他消息
            }
        }
    }

    /**
     * 消费重试消息
     */
    @KafkaListener(topics = "retry-topic", groupId = "retry-group")
    public void consumeRetryMessage(@Payload String messageStr,
                                  @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                  @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                                  @Header(KafkaHeaders.OFFSET) long offset) {
        try {
            Message message = objectMapper.readValue(messageStr, Message.class);
            log.info("消费重试消息: topic={}, partition={}, offset={}, message={}", 
                topic, partition, offset, message);
            
            // 检查重试次数
            if (message.getRetryCount() >= 3) {
                log.warn("消息重试次数过多，发送到死信队列: {}", message);
                // 可以发送到死信队列
                // kafkaTemplate.send("dead-letter-topic", message.toString());
                return;
            }
            
            // 增加重试次数
            message.setRetryCount(message.getRetryCount() + 1);
            
            // 重新处理
            processMessage(message);
            
        } catch (Exception e) {
            log.error("处理重试消息失败", e);
            throw new RuntimeException("重试处理失败", e);
        }
    }

    /**
     * 消费死信消息
     */
    @KafkaListener(topics = "dead-letter-topic", groupId = "dead-letter-group")
    public void consumeDeadLetterMessage(@Payload String messageStr,
                                       @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                       @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                                       @Header(KafkaHeaders.OFFSET) long offset) {
        try {
            Message message = objectMapper.readValue(messageStr, Message.class);
            log.warn("消费死信消息: topic={}, partition={}, offset={}, message={}", 
                topic, partition, offset, message);
            
            // 分析死信原因
            analyzeDeadLetter(message);
            
            // 处理死信消息
            handleDeadLetter(message);
            
        } catch (Exception e) {
            log.error("处理死信消息失败", e);
            // 死信消息处理失败通常需要人工介入
        }
    }

    /**
     * 消费事务消息
     */
    @KafkaListener(topics = "simple-topic", groupId = "transaction-group")
    public void consumeTransactionMessage(@Payload String messageStr,
                                        @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                        @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                                        @Header(KafkaHeaders.OFFSET) long offset) {
        try {
            Message message = objectMapper.readValue(messageStr, Message.class);
            log.info("消费事务消息: topic={}, partition={}, offset={}, message={}", 
                topic, partition, offset, message);
            
            // 模拟事务处理
            processMessageWithTransaction(message);
            
        } catch (Exception e) {
            log.error("处理事务消息失败", e);
            throw new RuntimeException("事务处理失败", e);
        }
    }

    /**
     * 消费有序消息
     */
    @KafkaListener(topics = "multi-partition-topic", groupId = "ordered-group")
    public void consumeOrderedMessage(@Payload String messageStr,
                                    @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                    @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                                    @Header(KafkaHeaders.OFFSET) long offset) {
        try {
            Message message = objectMapper.readValue(messageStr, Message.class);
            log.info("消费有序消息: topic={}, partition={}, offset={}, message={}", 
                topic, partition, offset, message);
            
            // 确保顺序处理
            processOrderedMessage(message);
            
        } catch (Exception e) {
            log.error("处理有序消息失败", e);
            throw new RuntimeException("有序处理失败", e);
        }
    }

    /**
     * 消费监控消息
     */
    @KafkaListener(topics = "simple-topic", groupId = "monitor-group")
    public void consumeMonitorMessage(@Payload String messageStr,
                                    @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                    @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                                    @Header(KafkaHeaders.OFFSET) long offset,
                                    @Header(KafkaHeaders.TIMESTAMP) long timestamp) {
        long startTime = System.currentTimeMillis();
        
        try {
            Message message = objectMapper.readValue(messageStr, Message.class);
            log.info("消费监控消息: topic={}, partition={}, offset={}, message={}", 
                topic, partition, offset, message);
            
            processMessage(message);
            
            long endTime = System.currentTimeMillis();
            long processingTime = endTime - startTime;
            long latency = endTime - timestamp;
            
            log.info("监控消息处理完成: 处理时间={}ms, 端到端延迟={}ms", processingTime, latency);
            
        } catch (Exception e) {
            log.error("处理监控消息失败", e);
            throw new RuntimeException("监控处理失败", e);
        }
    }

    /**
     * 错误处理消费者
     */
    @KafkaListener(topics = "simple-topic", groupId = "error-handler-group")
    public void consumeWithErrorHandling(@Payload String messageStr,
                                      @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                      @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                                      @Header(KafkaHeaders.OFFSET) long offset) {
        try {
            Message message = objectMapper.readValue(messageStr, Message.class);
            log.info("消费错误处理消息: topic={}, partition={}, offset={}, message={}", 
                topic, partition, offset, message);
            
            // 模拟可能失败的处理
            if (Math.random() < 0.1) { // 10%的概率失败
                throw new RuntimeException("随机模拟处理失败");
            }
            
            processMessage(message);
            
        } catch (Exception e) {
            log.error("处理错误处理消息失败，将进行重试", e);
            // 抛出异常会触发重试机制
            throw new RuntimeException("处理失败", e);
        }
    }

    /**
     * 处理消息的通用方法
     */
    private void processMessage(Message message) {
        log.info("开始处理消息: {}", message.getId());
        
        try {
            // 模拟业务逻辑处理
            Thread.sleep(100);
            
            log.info("消息处理完成: {}", message.getId());
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("消息处理被中断", e);
            throw new RuntimeException("处理被中断", e);
        }
    }

    /**
     * 事务处理消息
     */
    private void processMessageWithTransaction(Message message) {
        log.info("开始事务处理消息: {}", message.getId());
        
        try {
            // 模拟事务性操作
            Thread.sleep(100);
            
            log.info("事务处理完成: {}", message.getId());
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("事务处理被中断", e);
            throw new RuntimeException("事务处理被中断", e);
        }
    }

    /**
     * 顺序处理消息
     */
    private synchronized void processOrderedMessage(Message message) {
        log.info("开始顺序处理消息: {}", message.getId());
        
        try {
            // 模拟需要保证顺序的处理
            Thread.sleep(100);
            
            log.info("顺序处理完成: {}", message.getId());
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("顺序处理被中断", e);
            throw new RuntimeException("顺序处理被中断", e);
        }
    }

    /**
     * 分析死信原因
     */
    private void analyzeDeadLetter(Message message) {
        log.warn("死信分析 - 消息ID: {}, 重试次数: {}, 类型: {}", 
            message.getId(), message.getRetryCount(), message.getType());
    }

    /**
     * 处理死信消息
     */
    private void handleDeadLetter(Message message) {
        // 可以选择：
        // 1. 记录到数据库
        // 2. 发送告警通知
        // 3. 人工介入处理
        // 4. 定期重试
        
        log.warn("死信消息已记录，等待人工处理: {}", message.getId());
    }
}
