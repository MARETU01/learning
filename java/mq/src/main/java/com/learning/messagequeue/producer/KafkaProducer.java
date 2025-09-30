package com.learning.messagequeue.producer;

import com.learning.messagequeue.model.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * Kafka消息生产者
 * 
 * 演示不同类型的Kafka消息发送模式
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;

    /**
     * 发送简单消息
     */
    public void sendSimpleMessage(String content) {
        Message message = Message.create(content, "SIMPLE", "KafkaProducer");
        log.info("发送简单消息到Kafka: {}", message);
        
        kafkaTemplate.send("simple-topic", message.getId(), message.toString());
    }

    /**
     * 异步发送消息
     */
    public void sendAsyncMessage(String content) {
        Message message = Message.create(content, "ASYNC", "KafkaProducer");
        log.info("异步发送消息到Kafka: {}", message);
        
        CompletableFuture<SendResult<String, String>> future = 
            kafkaTemplate.send("simple-topic", message.getId(), message.toString());
        
        future.thenAccept(result -> {
            log.info("消息发送成功: topic={}, partition={}, offset={}", 
                result.getRecordMetadata().topic(),
                result.getRecordMetadata().partition(),
                result.getRecordMetadata().offset());
        }).exceptionally(ex -> {
            log.error("消息发送失败", ex);
            return null;
        });
    }

    /**
     * 发送到指定分区的消息
     */
    public void sendPartitionMessage(String content, int partition) {
        Message message = Message.create(content, "PARTITION", "KafkaProducer");
        log.info("发送到指定分区的消息: {}, 分区: {}", message, partition);
        
        kafkaTemplate.send("multi-partition-topic", partition, message.getId(), message.toString());
    }

    /**
     * 发送带有key的消息
     */
    public void sendKeyedMessage(String key, String content) {
        Message message = Message.create(content, "KEYED", "KafkaProducer");
        log.info("发送带有key的消息: key={}, message={}", key, message);
        
        kafkaTemplate.send("multi-partition-topic", key, message.toString());
    }

    /**
     * 批量发送消息
     */
    public void sendBatchMessages(int count) {
        log.info("批量发送{}条消息到Kafka", count);
        
        for (int i = 0; i < count; i++) {
            Message message = Message.create("批量消息 " + i, "BATCH", "KafkaProducer");
            kafkaTemplate.send("simple-topic", message.getId(), message.toString());
        }
    }

    /**
     * 发送事务消息
     */
    public void sendTransactionMessage(String content) {
        Message message = Message.create(content, "TRANSACTION", "KafkaProducer");
        log.info("发送事务消息到Kafka: {}", message);
        
        kafkaTemplate.executeInTransaction(template -> {
            template.send("simple-topic", message.getId(), message.toString());
            return true;
        });
    }

    /**
     * 发送重试消息
     */
    public void sendRetryMessage(String content) {
        Message message = Message.create(content, "RETRY", "KafkaProducer");
        log.info("发送重试消息到Kafka: {}", message);
        
        kafkaTemplate.send("retry-topic", message.getId(), message.toString());
    }

    /**
     * 发送死信消息
     */
    public void sendDeadLetterMessage(String content) {
        Message message = Message.create(content, "DEAD_LETTER", "KafkaProducer");
        log.info("发送死信消息到Kafka: {}", message);
        
        kafkaTemplate.send("dead-letter-topic", message.getId(), message.toString());
    }

    /**
     * 发送带有回调的消息
     */
    public void sendCallbackMessage(String content) {
        Message message = Message.create(content, "CALLBACK", "KafkaProducer");
        log.info("发送带有回调的消息到Kafka: {}", message);
        
        kafkaTemplate.send("simple-topic", message.getId(), message.toString())
            .addCallback(
                result -> {
                    log.info("消息发送成功: topic={}, partition={}, offset={}", 
                        result.getRecordMetadata().topic(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
                },
                ex -> {
                    log.error("消息发送失败", ex);
                }
            );
    }

    /**
     * 发送有序消息
     */
    public void sendOrderedMessage(String key, String content) {
        Message message = Message.create(content, "ORDERED", "KafkaProducer");
        log.info("发送有序消息: key={}, message={}", key, message);
        
        // 使用相同的key确保消息发送到同一个分区，从而保证顺序性
        kafkaTemplate.send("multi-partition-topic", key, message.toString());
    }

    /**
     * 发送高性能消息
     */
    public void sendHighPerformanceMessage(String content) {
        Message message = Message.create(content, "HIGH_PERFORMANCE", "KafkaProducer");
        log.info("发送高性能消息: {}", message);
        
        // 使用异步发送且不等待确认
        kafkaTemplate.send("simple-topic", message.getId(), message.toString());
    }

    /**
     * 发送可靠消息
     */
    public void sendReliableMessage(String content) {
        Message message = Message.create(content, "RELIABLE", "KafkaProducer");
        log.info("发送可靠消息: {}", message);
        
        try {
            // 同步发送，等待确认
            SendResult<String, String> result = kafkaTemplate
                .send("simple-topic", message.getId(), message.toString())
                .get();
            
            log.info("可靠消息发送成功: topic={}, partition={}, offset={}", 
                result.getRecordMetadata().topic(),
                result.getRecordMetadata().partition(),
                result.getRecordMetadata().offset());
        } catch (Exception e) {
            log.error("可靠消息发送失败", e);
            // 重试逻辑
            sendRetryMessage(content);
        }
    }

    /**
     * 发送监控消息
     */
    public void sendMonitorMessage(String content) {
        Message message = Message.create(content, "MONITOR", "KafkaProducer");
        log.info("发送监控消息: {}", message);
        
        long startTime = System.currentTimeMillis();
        
        try {
            SendResult<String, String> result = kafkaTemplate
                .send("simple-topic", message.getId(), message.toString())
                .get();
            
            long endTime = System.currentTimeMillis();
            long latency = endTime - startTime;
            
            log.info("监控消息发送成功: topic={}, partition={}, offset={}, latency={}ms", 
                result.getRecordMetadata().topic(),
                result.getRecordMetadata().partition(),
                result.getRecordMetadata().offset(),
                latency);
        } catch (Exception e) {
            log.error("监控消息发送失败", e);
        }
    }

    /**
     * 发送压缩消息
     */
    public void sendCompressedMessage(String content) {
        Message message = Message.create(content, "COMPRESSED", "KafkaProducer");
        log.info("发送压缩消息: {}", message);
        
        // Kafka支持多种压缩算法（GZIP、Snappy、LZ4、Zstandard）
        // 在生产者配置中设置compression.type
        kafkaTemplate.send("simple-topic", message.getId(), message.toString());
    }
}
