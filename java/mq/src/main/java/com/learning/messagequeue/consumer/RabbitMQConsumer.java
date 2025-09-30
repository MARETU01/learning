package com.learning.messagequeue.consumer;

import com.learning.messagequeue.config.RabbitMQConfig;
import com.learning.messagequeue.model.Message;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.stereotype.Service;

/**
 * RabbitMQ消息消费者
 * 
 * 演示不同类型的消息消费模式
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RabbitMQConsumer {

    private final ObjectMapper objectMapper;
    private final MessageConverter messageConverter;

    /**
     * 消费简单队列消息
     */
    @RabbitListener(queues = RabbitMQConfig.QUEUE_SIMPLE)
    public void consumeSimpleMessage(org.springframework.amqp.core.Message amqpMessage) {
        try {
            Message message = objectMapper.readValue(amqpMessage.getBody(), Message.class);
            log.info("消费简单队列消息: {}", message);
            
            // 模拟消息处理
            processMessage(message);
            
        } catch (Exception e) {
            log.error("处理简单队列消息失败", e);
            // 可以选择重新入队或发送到死信队列
            throw new RuntimeException("处理失败", e);
        }
    }

    /**
     * 消费工作队列消息
     */
    @RabbitListener(queues = RabbitMQConfig.QUEUE_WORK)
    public void consumeWorkMessage(org.springframework.amqp.core.Message amqpMessage) {
        try {
            Message message = objectMapper.readValue(amqpMessage.getBody(), Message.class);
            log.info("消费工作队列消息: {}", message);
            
            // 模拟耗时任务
            Thread.sleep(1000);
            
            log.info("工作队列消息处理完成: {}", message.getId());
            
        } catch (Exception e) {
            log.error("处理工作队列消息失败", e);
            throw new RuntimeException("处理失败", e);
        }
    }

    /**
     * 消费扇形队列1消息
     */
    @RabbitListener(queues = RabbitMQConfig.QUEUE_FANOUT + ".1")
    public void consumeFanoutMessage1(org.springframework.amqp.core.Message amqpMessage) {
        try {
            Message message = objectMapper.readValue(amqpMessage.getBody(), Message.class);
            log.info("消费扇形队列1消息: {}", message);
            
            processMessage(message);
            
        } catch (Exception e) {
            log.error("处理扇形队列1消息失败", e);
            throw new RuntimeException("处理失败", e);
        }
    }

    /**
     * 消费扇形队列2消息
     */
    @RabbitListener(queues = RabbitMQConfig.QUEUE_FANOUT + ".2")
    public void consumeFanoutMessage2(org.springframework.amqp.core.Message amqpMessage) {
        try {
            Message message = objectMapper.readValue(amqpMessage.getBody(), Message.class);
            log.info("消费扇形队列2消息: {}", message);
            
            processMessage(message);
            
        } catch (Exception e) {
            log.error("处理扇形队列2消息失败", e);
            throw new RuntimeException("处理失败", e);
        }
    }

    /**
     * 消费直连队列消息
     */
    @RabbitListener(queues = RabbitMQConfig.QUEUE_DIRECT)
    public void consumeDirectMessage(org.springframework.amqp.core.Message amqpMessage) {
        try {
            Message message = objectMapper.readValue(amqpMessage.getBody(), Message.class);
            log.info("消费直连队列消息: {}", message);
            
            processMessage(message);
            
        } catch (Exception e) {
            log.error("处理直连队列消息失败", e);
            throw new RuntimeException("处理失败", e);
        }
    }

    /**
     * 消费主题队列消息
     */
    @RabbitListener(queues = RabbitMQConfig.QUEUE_TOPIC)
    public void consumeTopicMessage(org.springframework.amqp.core.Message amqpMessage) {
        try {
            Message message = objectMapper.readValue(amqpMessage.getBody(), Message.class);
            log.info("消费主题队列消息: {}", message);
            
            processMessage(message);
            
        } catch (Exception e) {
            log.error("处理主题队列消息失败", e);
            throw new RuntimeException("处理失败", e);
        }
    }

    /**
     * 消费死信队列消息
     */
    @RabbitListener(queues = RabbitMQConfig.QUEUE_DEAD_LETTER)
    public void consumeDeadLetterMessage(org.springframework.amqp.core.Message amqpMessage) {
        try {
            Message message = objectMapper.readValue(amqpMessage.getBody(), Message.class);
            log.warn("消费死信队列消息: {}", message);
            
            // 分析死信原因
            analyzeDeadLetter(message, amqpMessage);
            
            // 可以选择重新处理或记录到数据库
            handleDeadLetter(message);
            
        } catch (Exception e) {
            log.error("处理死信队列消息失败", e);
            // 死信队列的消息处理失败通常需要人工介入
        }
    }

    /**
     * 消费重试队列消息
     */
    @RabbitListener(queues = RabbitMQConfig.QUEUE_RETRY)
    public void consumeRetryMessage(org.springframework.amqp.core.Message amqpMessage) {
        try {
            Message message = objectMapper.readValue(amqpMessage.getBody(), Message.class);
            log.info("消费重试队列消息: {}", message);
            
            // 检查重试次数
            if (message.getRetryCount() >= 3) {
                log.warn("消息重试次数过多，发送到死信队列: {}", message);
                // 发送到死信队列
                // rabbitTemplate.convertAndSend(RabbitMQConfig.QUEUE_DEAD_LETTER, message);
                return;
            }
            
            // 增加重试次数
            message.setRetryCount(message.getRetryCount() + 1);
            
            // 重新处理
            processMessage(message);
            
        } catch (Exception e) {
            log.error("处理重试队列消息失败", e);
            throw new RuntimeException("重试处理失败", e);
        }
    }

    /**
     * 批量消费消息
     */
    @RabbitListener(queues = RabbitMQConfig.QUEUE_SIMPLE, containerFactory = "batchRabbitListenerContainerFactory")
    public void consumeBatchMessages(java.util.List<org.springframework.amqp.core.Message> messages) {
        log.info("批量消费消息，数量: {}", messages.size());
        
        for (org.springframework.amqp.core.Message amqpMessage : messages) {
            try {
                Message message = objectMapper.readValue(amqpMessage.getBody(), Message.class);
                processMessage(message);
            } catch (Exception e) {
                log.error("批量处理消息失败", e);
                // 可以选择跳过错误消息继续处理其他消息
            }
        }
    }

    /**
     * 手动确认消息
     */
    @RabbitListener(queues = RabbitMQConfig.QUEUE_SIMPLE)
    public void consumeManualAckMessage(org.springframework.amqp.core.Message amqpMessage, 
                                       com.rabbitmq.client.Channel channel) throws Exception {
        try {
            Message message = objectMapper.readValue(amqpMessage.getBody(), Message.class);
            log.info("手动确认消费消息: {}", message);
            
            processMessage(message);
            
            // 手动确认消息
            channel.basicAck(amqpMessage.getMessageProperties().getDeliveryTag(), false);
            
        } catch (Exception e) {
            log.error("处理手动确认消息失败", e);
            
            // 拒绝消息并重新入队
            channel.basicNack(amqpMessage.getMessageProperties().getDeliveryTag(), false, true);
        }
    }

    /**
     * 延迟消息消费
     */
    @RabbitListener(queues = RabbitMQConfig.QUEUE_SIMPLE)
    public void consumeDelayedMessage(org.springframework.amqp.core.Message amqpMessage) {
        try {
            Message message = objectMapper.readValue(amqpMessage.getBody(), Message.class);
            
            // 检查是否为延迟消息
            if ("DELAYED".equals(message.getType())) {
                long delay = System.currentTimeMillis() - message.getSendTime().toEpochSecond(java.time.ZoneOffset.UTC) * 1000;
                log.info("消费延迟消息: {}, 实际延迟: {}ms", message, delay);
            }
            
            processMessage(message);
            
        } catch (Exception e) {
            log.error("处理延迟消息失败", e);
            throw new RuntimeException("处理失败", e);
        }
    }

    /**
     * 处理消息的通用方法
     */
    private void processMessage(Message message) {
        log.info("开始处理消息: {}", message.getId());
        
        // 模拟业务逻辑处理
        try {
            // 这里可以添加具体的业务逻辑
            Thread.sleep(100);
            
            log.info("消息处理完成: {}", message.getId());
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("消息处理被中断", e);
            throw new RuntimeException("处理被中断", e);
        }
    }

    /**
     * 分析死信原因
     */
    private void analyzeDeadLetter(Message message, org.springframework.amqp.core.Message amqpMessage) {
        // 获取死信原因
        String reason = amqpMessage.getMessageProperties().getHeader("x-first-death-reason");
        String exchange = amqpMessage.getMessageProperties().getHeader("x-first-death-exchange");
        String routingKey = amqpMessage.getMessageProperties().getHeader("x-first-death-routing-key");
        
        log.warn("死信分析 - 消息ID: {}, 原因: {}, 交换器: {}, 路由键: {}", 
            message.getId(), reason, exchange, routingKey);
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
