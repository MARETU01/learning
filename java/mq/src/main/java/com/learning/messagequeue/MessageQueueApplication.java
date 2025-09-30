package com.learning.messagequeue;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 消息队列学习应用主类
 * 
 * 该应用演示了RabbitMQ和Kafka的基本使用方法
 * 包括消息生产者、消费者、消息确认、重试机制等
 */
@SpringBootApplication
public class MessageQueueApplication {

    public static void main(String[] args) {
        SpringApplication.run(MessageQueueApplication.class, args);
    }
}
