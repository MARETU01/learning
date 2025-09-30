package com.learning.messagequeue;

import com.learning.messagequeue.producer.KafkaProducer;
import com.learning.messagequeue.producer.RabbitMQProducer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 消息队列应用测试类
 * 
 * 测试消息发送和接收功能
 */
@SpringBootTest
public class MessageQueueApplicationTest {

    @Autowired
    private RabbitMQProducer rabbitMQProducer;

    @Autowired
    private KafkaProducer kafkaProducer;

    /**
     * 测试RabbitMQ消息发送
     */
    @Test
    void testRabbitMQMessageSending() {
        System.out.println("=== RabbitMQ消息发送测试 ===");
        
        // 发送简单消息
        rabbitMQProducer.sendSimpleMessage("Hello RabbitMQ!");
        
        // 发送工作队列消息
        rabbitMQProducer.sendWorkMessage("Work message");
        
        // 发送扇形交换器消息
        rabbitMQProducer.sendFanoutMessage("Fanout message");
        
        // 发送直连交换器消息
        rabbitMQProducer.sendDirectMessage("Direct message");
        
        // 发送主题交换器消息
        rabbitMQProducer.sendTopicMessage("Topic message");
        
        // 发送延迟消息
        rabbitMQProducer.sendDelayedMessage("Delayed message", 5000);
        
        // 发送确认消息
        rabbitMQProducer.sendConfirmMessage("Confirm message");
        
        // 发送批量消息
        rabbitMQProducer.sendBatchMessages(5);
        
        // 发送优先级消息
        rabbitMQProducer.sendPriorityMessage("High priority message", 10);
        
        // 发送带有回调的消息
        rabbitMQProducer.sendCallbackMessage("Callback message");
        
        System.out.println("RabbitMQ消息发送测试完成");
    }

    /**
     * 测试Kafka消息发送
     */
    @Test
    void testKafkaMessageSending() {
        System.out.println("=== Kafka消息发送测试 ===");
        
        // 发送简单消息
        kafkaProducer.sendSimpleMessage("Hello Kafka!");
        
        // 发送异步消息
        kafkaProducer.sendAsyncMessage("Async message");
        
        // 发送到指定分区的消息
        kafkaProducer.sendPartitionMessage("Partition message", 0);
        
        // 发送带有key的消息
        kafkaProducer.sendKeyedMessage("test-key", "Keyed message");
        
        // 发送批量消息
        kafkaProducer.sendBatchMessages(5);
        
        // 发送事务消息
        kafkaProducer.sendTransactionMessage("Transaction message");
        
        // 发送重试消息
        kafkaProducer.sendRetryMessage("Retry message");
        
        // 发送死信消息
        kafkaProducer.sendDeadLetterMessage("Dead letter message");
        
        // 发送带有回调的消息
        kafkaProducer.sendCallbackMessage("Kafka callback message");
        
        // 发送有序消息
        kafkaProducer.sendOrderedMessage("ordered-key", "Ordered message");
        
        // 发送高性能消息
        kafkaProducer.sendHighPerformanceMessage("High performance message");
        
        // 发送可靠消息
        kafkaProducer.sendReliableMessage("Reliable message");
        
        // 发送监控消息
        kafkaProducer.sendMonitorMessage("Monitor message");
        
        // 发送压缩消息
        kafkaProducer.sendCompressedMessage("Compressed message");
        
        System.out.println("Kafka消息发送测试完成");
    }

    /**
     * 测试消息队列性能
     */
    @Test
    void testMessageQueuePerformance() {
        System.out.println("=== 消息队列性能测试 ===");
        
        int messageCount = 100;
        
        // 测试RabbitMQ性能
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < messageCount; i++) {
            rabbitMQProducer.sendSimpleMessage("Performance test message " + i);
        }
        long rabbitMQTime = System.currentTimeMillis() - startTime;
        System.out.println("RabbitMQ发送" + messageCount + "条消息耗时: " + rabbitMQTime + "ms");
        
        // 测试Kafka性能
        startTime = System.currentTimeMillis();
        for (int i = 0; i < messageCount; i++) {
            kafkaProducer.sendSimpleMessage("Performance test message " + i);
        }
        long kafkaTime = System.currentTimeMillis() - startTime;
        System.out.println("Kafka发送" + messageCount + "条消息耗时: " + kafkaTime + "ms");
        
        System.out.println("性能测试完成");
    }

    /**
     * 测试错误处理
     */
    @Test
    void testErrorHandling() {
        System.out.println("=== 错误处理测试 ===");
        
        try {
            // 测试RabbitMQ死信队列
            rabbitMQProducer.sendDeadLetterMessage("Test dead letter message");
            
            // 测试Kafka死信队列
            kafkaProducer.sendDeadLetterMessage("Test dead letter message");
            
            // 测试重试机制
            rabbitMQProducer.sendRetryMessage("Test retry message");
            kafkaProducer.sendRetryMessage("Test retry message");
            
            System.out.println("错误处理测试完成");
            
        } catch (Exception e) {
            System.err.println("错误处理测试失败: " + e.getMessage());
        }
    }

    /**
     * 测试消息可靠性
     */
    @Test
    void testMessageReliability() {
        System.out.println("=== 消息可靠性测试 ===");
        
        try {
            // 测试RabbitMQ事务消息
            rabbitMQProducer.sendTransactionMessage("Test transaction message");
            
            // 测试Kafka事务消息
            kafkaProducer.sendTransactionMessage("Test transaction message");
            
            // 测试确认机制
            rabbitMQProducer.sendConfirmMessage("Test confirm message");
            kafkaProducer.sendReliableMessage("Test reliable message");
            
            System.out.println("消息可靠性测试完成");
            
        } catch (Exception e) {
            System.err.println("消息可靠性测试失败: " + e.getMessage());
        }
    }
}
