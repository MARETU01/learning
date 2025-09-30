package com.learning.messagequeue.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ配置类
 * 
 * 配置队列、交换器、绑定关系以及消息转换器
 */
@Configuration
public class RabbitMQConfig {

    // 队列名称
    public static final String QUEUE_SIMPLE = "queue.simple";
    public static final String QUEUE_WORK = "queue.work";
    public static final String QUEUE_FANOUT = "queue.fanout";
    public static final String QUEUE_DIRECT = "queue.direct";
    public static final String QUEUE_TOPIC = "queue.topic";
    public static final String QUEUE_DEAD_LETTER = "queue.dead.letter";
    public static final String QUEUE_RETRY = "queue.retry";

    // 交换器名称
    public static final String EXCHANGE_FANOUT = "exchange.fanout";
    public static final String EXCHANGE_DIRECT = "exchange.direct";
    public static final String EXCHANGE_TOPIC = "exchange.topic";
    public static final String EXCHANGE_DEAD_LETTER = "exchange.dead.letter";

    // 路由键
    public static final String ROUTING_KEY_DIRECT = "routing.key.direct";
    public static final String ROUTING_KEY_TOPIC = "routing.key.topic";

    /**
     * 简单队列
     */
    @Bean
    public Queue simpleQueue() {
        return new Queue(QUEUE_SIMPLE, true);
    }

    /**
     * 工作队列
     */
    @Bean
    public Queue workQueue() {
        return new Queue(QUEUE_WORK, true);
    }

    /**
     * 扇形交换器
     */
    @Bean
    public FanoutExchange fanoutExchange() {
        return new FanoutExchange(EXCHANGE_FANOUT);
    }

    /**
     * 扇形队列1
     */
    @Bean
    public Queue fanoutQueue1() {
        return new Queue(QUEUE_FANOUT + ".1", true);
    }

    /**
     * 扇形队列2
     */
    @Bean
    public Queue fanoutQueue2() {
        return new Queue(QUEUE_FANOUT + ".2", true);
    }

    /**
     * 扇形交换器绑定队列1
     */
    @Bean
    public Binding fanoutBinding1() {
        return BindingBuilder.bind(fanoutQueue1()).to(fanoutExchange());
    }

    /**
     * 扇形交换器绑定队列2
     */
    @Bean
    public Binding fanoutBinding2() {
        return BindingBuilder.bind(fanoutQueue2()).to(fanoutExchange());
    }

    /**
     * 直连交换器
     */
    @Bean
    public DirectExchange directExchange() {
        return new DirectExchange(EXCHANGE_DIRECT);
    }

    /**
     * 直连队列
     */
    @Bean
    public Queue directQueue() {
        return new Queue(QUEUE_DIRECT, true);
    }

    /**
     * 直连交换器绑定队列
     */
    @Bean
    public Binding directBinding() {
        return BindingBuilder.bind(directQueue())
                .to(directExchange())
                .with(ROUTING_KEY_DIRECT);
    }

    /**
     * 主题交换器
     */
    @Bean
    public TopicExchange topicExchange() {
        return new TopicExchange(EXCHANGE_TOPIC);
    }

    /**
     * 主题队列
     */
    @Bean
    public Queue topicQueue() {
        return new Queue(QUEUE_TOPIC, true);
    }

    /**
     * 主题交换器绑定队列
     */
    @Bean
    public Binding topicBinding() {
        return BindingBuilder.bind(topicQueue())
                .to(topicExchange())
                .with(ROUTING_KEY_TOPIC);
    }

    /**
     * 死信队列
     */
    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(QUEUE_DEAD_LETTER)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", QUEUE_RETRY)
                .build();
    }

    /**
     * 重试队列
     */
    @Bean
    public Queue retryQueue() {
        return new Queue(QUEUE_RETRY, true);
    }

    /**
     * 消息转换器
     * 使用JSON格式序列化消息
     */
    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * RabbitTemplate配置
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter());
        
        // 设置消息发送确认回调
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            if (ack) {
                System.out.println("消息发送成功: " + correlationData);
            } else {
                System.out.println("消息发送失败: " + cause);
            }
        });
        
        // 设置消息返回回调
        rabbitTemplate.setReturnsCallback(returned -> {
            System.out.println("消息被退回: " + returned.getMessage());
        });
        
        return rabbitTemplate;
    }
}
