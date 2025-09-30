package com.learning.messagequeue.config;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka配置类
 * 
 * 配置Kafka生产者、消费者、主题和监听器工厂
 */
@Configuration
@EnableKafka
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    /**
     * 创建Kafka主题
     */
    @Bean
    public NewTopic simpleTopic() {
        return new NewTopic("simple-topic", 1, (short) 1);
    }

    @Bean
    public NewTopic multiPartitionTopic() {
        return new NewTopic("multi-partition-topic", 3, (short) 1);
    }

    @Bean
    public NewTopic retryTopic() {
        return new NewTopic("retry-topic", 1, (short) 1);
    }

    @Bean
    public NewTopic deadLetterTopic() {
        return new NewTopic("dead-letter-topic", 1, (short) 1);
    }

    /**
     * 流式处理主题
     */
    @Bean
    public NewTopic streamTopic() {
        return new NewTopic("stream-topic", 3, (short) 1);
    }

    @Bean
    public NewTopic streamPartitionTopic() {
        return new NewTopic("stream-partition-topic", 5, (short) 1);
    }

    @Bean
    public NewTopic streamKeyedTopic() {
        return new NewTopic("stream-keyed-topic", 3, (short) 1);
    }

    @Bean
    public NewTopic streamBatchTopic() {
        return new NewTopic("stream-batch-topic", 3, (short) 1);
    }

    @Bean
    public NewTopic streamPriorityTopic() {
        return new NewTopic("stream-priority-topic", 3, (short) 1);
    }

    @Bean
    public NewTopic streamWindowTopic() {
        return new NewTopic("stream-window-topic", 5, (short) 1);
    }

    @Bean
    public NewTopic streamAggregateTopic() {
        return new NewTopic("stream-aggregate-topic", 3, (short) 1);
    }

    @Bean
    public NewTopic streamFilterTopic() {
        return new NewTopic("stream-filter-topic", 3, (short) 1);
    }

    /**
     * 生产者配置
     */
    @Bean
    public Map<String, Object> producerConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        
        // 可靠性配置
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.RETRIES_CONFIG, 3);
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        
        // 性能配置
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
        props.put(ProducerConfig.LINGER_MS_CONFIG, 1);
        props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);
        
        return props;
    }

    /**
     * 生产者工厂
     */
    @Bean
    public ProducerFactory<String, String> producerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfigs());
    }

    /**
     * KafkaTemplate
     */
    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    /**
     * 消费者配置
     */
    @Bean
    public Map<String, Object> consumerConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        
        // 消费者组ID
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "message-queue-learning-group");
        
        // 自动提交偏移量
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        
        // 从最早的消息开始消费
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        
        // 会话超时时间
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 10000);
        
        // 心跳间隔时间
        props.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, 3000);
        
        return props;
    }

    /**
     * 消费者工厂
     */
    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        return new DefaultKafkaConsumerFactory<>(consumerConfigs());
    }

    /**
     * 监听器容器工厂
     */
    @Bean
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, String>> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = 
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        
        // 设置并发消费者数量
        factory.setConcurrency(3);
        
        // 设置批量监听
        factory.setBatchListener(false);
        
        // 设置手动提交偏移量
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        
        return factory;
    }

    /**
     * 批量监听器容器工厂（用于流式处理）
     */
    @Bean
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, String>> batchKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = 
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        
        // 设置并发消费者数量
        factory.setConcurrency(1);
        
        // 设置批量监听
        factory.setBatchListener(true);
        
        // 设置手动提交偏移量
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        
        // 设置批量消费配置
        factory.getContainerProperties().setPollTimeoutMs(3000);
        
        return factory;
    }

    /**
     * 管理客户端配置
     */
    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        return new KafkaAdmin(configs);
    }
}
