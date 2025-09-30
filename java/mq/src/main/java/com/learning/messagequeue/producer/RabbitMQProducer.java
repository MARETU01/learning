package com.learning.messagequeue.producer;

import com.learning.messagequeue.config.RabbitMQConfig;
import com.learning.messagequeue.model.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.stereotype.Service;

/**
 * RabbitMQ消息生产者
 * 
 * 演示不同类型的消息发送模式
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RabbitMQProducer {

    private final RabbitTemplate rabbitTemplate;
    private final MessageConverter messageConverter;

    /**
     * 发送简单消息
     */
    public void sendSimpleMessage(String content) {
        Message message = Message.create(content, "SIMPLE", "RabbitMQProducer");
        log.info("发送简单消息: {}", message);
        
        rabbitTemplate.convertAndSend(RabbitMQConfig.QUEUE_SIMPLE, message);
    }

    /**
     * 发送工作队列消息
     */
    public void sendWorkMessage(String content) {
        Message message = Message.create(content, "WORK", "RabbitMQProducer");
        log.info("发送工作队列消息: {}", message);
        
        rabbitTemplate.convertAndSend(RabbitMQConfig.QUEUE_WORK, message);
    }

    /**
     * 发送扇形交换器消息
     */
    public void sendFanoutMessage(String content) {
        Message message = Message.create(content, "FANOUT", "RabbitMQProducer");
        log.info("发送扇形交换器消息: {}", message);
        
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_FANOUT, "", message);
    }

    /**
     * 发送直连交换器消息
     */
    public void sendDirectMessage(String content) {
        Message message = Message.create(content, "DIRECT", "RabbitMQProducer");
        log.info("发送直连交换器消息: {}", message);
        
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_DIRECT, RabbitMQConfig.ROUTING_KEY_DIRECT, message);
    }

    /**
     * 发送主题交换器消息
     */
    public void sendTopicMessage(String content) {
        Message message = Message.create(content, "TOPIC", "RabbitMQProducer");
        log.info("发送主题交换器消息: {}", message);
        
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_TOPIC, RabbitMQConfig.ROUTING_KEY_TOPIC, message);
    }

    /**
     * 发送死信队列消息
     */
    public void sendDeadLetterMessage(String content) {
        Message message = Message.create(content, "DEAD_LETTER", "RabbitMQProducer");
        log.info("发送死信队列消息: {}", message);
        
        rabbitTemplate.convertAndSend(RabbitMQConfig.QUEUE_DEAD_LETTER, message);
    }

    /**
     * 发送延迟消息
     */
    public void sendDelayedMessage(String content, int delay) {
        Message message = Message.create(content, "DELAYED", "RabbitMQProducer");
        log.info("发送延迟消息: {}, 延迟时间: {}ms", message, delay);
        
        // 使用消息的TTL来实现延迟
        rabbitTemplate.convertAndSend(RabbitMQConfig.QUEUE_SIMPLE, message, messagePostProcessor -> {
            messagePostProcessor.getMessageProperties().setExpiration(String.valueOf(delay));
            return messagePostProcessor;
        });
    }

    /**
     * 发送确认消息
     */
    public void sendConfirmMessage(String content) {
        Message message = Message.create(content, "CONFIRM", "RabbitMQProducer");
        log.info("发送确认消息: {}", message);
        
        // 启用发送确认
        rabbitTemplate.setMandatory(true);
        rabbitTemplate.convertAndSend(RabbitMQConfig.QUEUE_SIMPLE, message);
    }

    /**
     * 发送事务消息
     */
    public void sendTransactionMessage(String content) {
        Message message = Message.create(content, "TRANSACTION", "RabbitMQProducer");
        log.info("发送事务消息: {}", message);
        
        // 开启事务
        rabbitTemplate.setChannelTransacted(true);
        
        try {
            rabbitTemplate.execute(channel -> {
                // 开启事务
                channel.txSelect();
                
                // 发送消息
                rabbitTemplate.convertAndSend(RabbitMQConfig.QUEUE_SIMPLE, message);
                
                // 提交事务
                channel.txCommit();
                return null;
            });
        } catch (Exception e) {
            log.error("发送事务消息失败", e);
            // 回滚事务
            rabbitTemplate.execute(channel -> {
                channel.txRollback();
                return null;
            });
        }
    }

    /**
     * 批量发送消息
     */
    public void sendBatchMessages(int count) {
        log.info("批量发送{}条消息", count);
        
        for (int i = 0; i < count; i++) {
            Message message = Message.create("批量消息 " + i, "BATCH", "RabbitMQProducer");
            rabbitTemplate.convertAndSend(RabbitMQConfig.QUEUE_SIMPLE, message);
        }
    }

    /**
     * 发送优先级消息
     */
    public void sendPriorityMessage(String content, int priority) {
        Message message = Message.create(content, "PRIORITY", "RabbitMQProducer");
        log.info("发送优先级消息: {}, 优先级: {}", message, priority);
        
        rabbitTemplate.convertAndSend(RabbitMQConfig.QUEUE_SIMPLE, message, messagePostProcessor -> {
            messagePostProcessor.getMessageProperties().setPriority(priority);
            return messagePostProcessor;
        });
    }

    /**
     * 发送带有回调的消息
     */
    public void sendCallbackMessage(String content) {
        Message message = Message.create(content, "CALLBACK", "RabbitMQProducer");
        log.info("发送带有回调的消息: {}", message);
        
        // 设置回调
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            if (ack) {
                log.info("消息发送确认成功: {}", correlationData);
            } else {
                log.error("消息发送确认失败: {}", cause);
            }
        });
        
        rabbitTemplate.setReturnsCallback(returned -> {
            log.warn("消息被退回: {}", returned.getMessage());
        });
        
        rabbitTemplate.convertAndSend(RabbitMQConfig.QUEUE_SIMPLE, message);
    }
}
