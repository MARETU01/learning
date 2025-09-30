# 消息队列学习模块

## 项目介绍

这是一个基于Spring Boot的消息队列学习模块，旨在帮助开发者学习和理解RabbitMQ和Kafka的基本概念和使用方法。

## 技术栈

- **Spring Boot 3.2.0** - 应用框架
- **Spring AMQP** - RabbitMQ集成
- **Spring Kafka** - Kafka集成
- **Maven** - 项目构建工具
- **Java 17** - 编程语言
- **Lombok** - 代码生成工具
- **Jackson** - JSON处理
- **JUnit 5** - 单元测试

## 项目结构

```
message-queue/
├── src/main/java/com/learning/messagequeue/
│   ├── config/                 # 配置类
│   │   ├── RabbitMQConfig.java    # RabbitMQ配置
│   │   └── KafkaConfig.java        # Kafka配置
│   ├── model/                  # 数据模型
│   │   └── Message.java           # 消息模型
│   ├── producer/               # 生产者
│   │   ├── RabbitMQProducer.java  # RabbitMQ生产者
│   │   ├── KafkaProducer.java      # Kafka生产者
│   │   └── KafkaStreamProducer.java # Kafka流式生产者
│   ├── consumer/               # 消费者
│   │   ├── RabbitMQConsumer.java  # RabbitMQ消费者
│   │   ├── KafkaConsumer.java      # Kafka消费者
│   │   └── KafkaStreamConsumer.java # Kafka流式消费者
│   └── MessageQueueApplication.java # 主应用类
├── src/main/resources/
│   └── application.yml         # 应用配置
├── src/test/java/
│   └── MessageQueueApplicationTest.java # 测试类
└── pom.xml                    # Maven配置
```

## 功能特性

### RabbitMQ功能
- ✅ 简单队列模式
- ✅ 工作队列模式
- ✅ 扇形交换器(Fanout)
- ✅ 直连交换器(Direct)
- ✅ 主题交换器(Topic)
- ✅ 死信队列
- ✅ 延迟消息
- ✅ 消息确认机制
- ✅ 事务消息
- ✅ 批量消息
- ✅ 优先级消息
- ✅ 手动确认

### Kafka功能
- ✅ 简单消息发送
- ✅ 异步消息发送
- ✅ 分区消息
- ✅ 带Key消息
- ✅ 批量消息
- ✅ 事务消息
- ✅ 重试机制
- ✅ 死信队列
- ✅ 有序消息
- ✅ 高性能消息
- ✅ 可靠消息
- ✅ 监控消息
- ✅ 压缩消息
- ✅ 手动确认
- ✅ 批量消费

### Kafka流式处理功能
- ✅ 流式简单消息处理
- ✅ 流式分区消息处理
- ✅ 流式Keyed消息处理
- ✅ 流式批量消息处理
- ✅ 流式优先级消息处理
- ✅ 流式窗口化消息处理
- ✅ 流式聚合消息处理
- ✅ 流式过滤消息处理
- ✅ 并行流处理
- ✅ 实时数据流处理
- ✅ 消息缓存和状态管理

## 环境要求

- JDK 17+
- Maven 3.6+
- RabbitMQ 3.8+ (可选，用于实际测试)
- Apache Kafka 2.8+ (可选，用于实际测试)

## 快速开始

### 1. 启动RabbitMQ (可选)

使用Docker启动RabbitMQ：
```bash
docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3-management
```

### 2. 启动Kafka (可选)

使用Docker启动Kafka：
```bash
docker run -d --name kafka -p 9092:9092 \
  -e KAFKA_ADVERTISED_HOST_NAME=localhost \
  -e KAFKA_ZOOKEEPER_CONNECT=zookeeper:2181 \
  confluentinc/cp-kafka:latest
```

### 3. 构建项目

```bash
mvn clean compile
```

### 4. 运行应用

```bash
mvn spring-boot:run
```

### 5. 运行测试

```bash
mvn test
```

## 使用示例

### RabbitMQ示例

#### 发送简单消息
```java
@Autowired
private RabbitMQProducer rabbitMQProducer;

// 发送简单消息
rabbitMQProducer.sendSimpleMessage("Hello RabbitMQ!");

// 发送扇形消息
rabbitMQProducer.sendFanoutMessage("Broadcast message");

// 发送延迟消息
rabbitMQProducer.sendDelayedMessage("Delayed message", 5000);
```

#### 消费消息
```java
@RabbitListener(queues = RabbitMQConfig.QUEUE_SIMPLE)
public void consumeSimpleMessage(Message message) {
    log.info("收到消息: {}", message);
    // 处理消息
}
```

### Kafka示例

#### 发送消息
```java
@Autowired
private KafkaProducer kafkaProducer;

// 发送简单消息
kafkaProducer.sendSimpleMessage("Hello Kafka!");

// 发送异步消息
kafkaProducer.sendAsyncMessage("Async message");

// 发送分区消息
kafkaProducer.sendPartitionMessage("Partition message", 0);
```

#### 消费消息
```java
@KafkaListener(topics = "simple-topic", groupId = "simple-group")
public void consumeSimpleMessage(String messageStr) {
    Message message = objectMapper.readValue(messageStr, Message.class);
    log.info("收到消息: {}", message);
    // 处理消息
}
```

### Kafka流式处理示例

#### 流式生产者示例
```java
@Autowired
private KafkaStreamProducer streamProducer;

// 流式发送简单消息
streamProducer.streamSimpleMessages(1000);

// 流式发送分区消息
streamProducer.streamPartitionedMessages(500, 5);

// 流式发送Keyed消息
String[] keys = {"key1", "key2", "key3"};
streamProducer.streamKeyedMessages(300, keys);

// 流式发送批量消息
streamProducer.streamBatchMessages(1000, 100);

// 流式发送优先级消息
streamProducer.streamPriorityMessages(200);

// 流式发送窗口化消息
streamProducer.streamWindowedMessages(5000, 100);

// 流式发送聚合消息
streamProducer.streamAggregatedMessages(500, 10);

// 流式发送过滤消息
streamProducer.streamFilteredMessages(500, "包含");
```

#### 流式消费者示例
```java
@KafkaListener(topics = "stream-topic", groupId = "stream-simple-group")
public void consumeSimpleStreamMessages(@Payload List<String> messages,
                                      @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
    // 使用Stream处理消息
    List<Message> processedMessages = messages.stream()
        .map(this::parseMessage)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .peek(this::processSimpleMessage)
        .collect(Collectors.toList());
    
    log.info("简单流式消息处理完成: 成功处理{}条消息", processedMessages.size());
}

@KafkaListener(topics = "stream-priority-topic", groupId = "stream-priority-group")
public void consumePriorityStreamMessages(@Payload List<String> messages,
                                        @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
    // 按优先级顺序处理消息
    messages.stream()
        .map(this::parseMessage)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .forEach(message -> {
            int priority = message.getPriority();
            // 处理优先级消息
            processPriorityMessage(message);
        });
}

@KafkaListener(topics = "stream-window-topic", groupId = "stream-window-group")
public void consumeWindowedStreamMessages(@Payload List<String> messages,
                                       @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
    long currentTime = System.currentTimeMillis();
    long windowSizeMs = 5000; // 5秒窗口
    
    // 处理时间窗口消息
    messages.stream()
        .map(this::parseMessage)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .forEach(message -> {
            long windowId = message.getWindowId();
            long windowEndTime = windowId * windowSizeMs + windowSizeMs;
            
            if (currentTime >= windowEndTime) {
                // 处理已完成的窗口
                processWindowedMessage(message, windowId);
            }
        });
}
```

#### 流式处理特性
```java
// 获取流式处理状态
@Autowired
private KafkaStreamConsumer streamConsumer;

// 获取已处理消息计数
long processedCount = streamConsumer.getProcessedCount();

// 获取窗口缓存状态
Map<Long, Integer> windowCache = streamConsumer.getWindowCacheStatus();

// 获取组缓存状态
Map<Integer, Integer> groupCache = streamConsumer.getGroupCacheStatus();

// 重置计数器
streamConsumer.resetProcessedCount();
```

## 配置说明

### RabbitMQ配置

在`application.yml`中配置RabbitMQ连接参数：

```yaml
spring:
  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest
    virtual-host: /
    publisher-confirm-type: correlated
    publisher-returns: true
```

### Kafka配置

在`application.yml`中配置Kafka连接参数：

```yaml
spring:
  kafka:
    bootstrap-servers: localhost:9092
    producer:
      acks: all
      retries: 3
    consumer:
      group-id: message-queue-learning-group
      auto-offset-reset: earliest
```

## 学习要点

### RabbitMQ核心概念

1. **消息模型**
   - 生产者(Producer)：发送消息的应用
   - 消费者(Consumer)：接收消息的应用
   - 队列(Queue)：存储消息的缓冲区
   - 交换器(Exchange)：消息路由规则
   - 绑定(Binding)：交换器与队列的关系

2. **交换器类型**
   - Direct：精确匹配路由键
   - Fanout：广播到所有绑定的队列
   - Topic：模式匹配路由键
   - Headers：基于消息头属性

3. **消息确认机制**
   - Publisher Confirm：生产者确认
   - Consumer Ack：消费者确认
   - 事务消息：保证消息可靠性

### Kafka核心概念

1. **基本概念**
   - Topic：消息主题
   - Partition：分区，提高并行度
   - Producer：生产者
   - Consumer：消费者
   - Consumer Group：消费者组
   - Broker：Kafka服务器
   - Offset：消息偏移量

2. **消息保证**
   - At-least-once：至少一次
   - At-most-once：最多一次
   - Exactly-once：精确一次

3. **性能优化**
   - 批量发送
   - 消息压缩
   - 分区并行
   - 零拷贝

## 最佳实践

### RabbitMQ最佳实践

1. **消息设计**
   - 使用JSON格式序列化消息
   - 添加消息ID和时间戳
   - 控制消息大小，避免过大消息

2. **队列设计**
   - 合理设置队列持久化
   - 使用死信队列处理失败消息
   - 设置合适的TTL和过期时间

3. **消费者设计**
   - 使用手动确认机制
   - 合理设置预取数量
   - 实现重试机制

### Kafka最佳实践

1. **主题设计**
   - 合理设置分区数量
   - 使用有意义的主题名称
   - 考虑消息保留策略

2. **生产者设计**
   - 使用批量发送提高性能
   - 启用消息压缩
   - 设置合适的重试策略

3. **消费者设计**
   - 合理设置消费者组
   - 手动提交偏移量
   - 处理再平衡事件

## 监控和运维

### 健康检查

应用提供了Spring Boot Actuator健康检查端点：

```bash
curl http://localhost:8080/actuator/health
```

### 指标监控

使用Prometheus格式导出指标：

```bash
curl http://localhost:8080/actuator/prometheus
```

### 日志配置

日志文件位置：`logs/message-queue.log`

日志级别配置：
- DEBUG：详细调试信息
- INFO：一般信息
- WARN：警告信息
- ERROR：错误信息

## 故障排除

### 常见问题

1. **连接失败**
   - 检查RabbitMQ/Kafka服务是否启动
   - 验证连接参数是否正确
   - 检查网络连接

2. **消息丢失**
   - 确认启用了消息确认机制
   - 检查消费者是否正确处理消息
   - 查看死信队列中的消息

3. **性能问题**
   - 调整批量大小和线程池配置
   - 检查消息序列化性能
   - 监控系统资源使用情况

### 调试技巧

1. **启用DEBUG日志**
   ```yaml
   logging:
     level:
       org.springframework.amqp: DEBUG
       org.springframework.kafka: DEBUG
   ```

2. **使用管理界面**
   - RabbitMQ Management: http://localhost:15672
   - Kafka Tools: 使用kafka-topics.sh等工具

3. **监控指标**
   - 消息发送/接收速率
   - 队列积压情况
   - 消费者延迟

## 扩展功能

### 高级特性

1. **消息追踪**
   - 集成OpenTelemetry
   - 实现分布式追踪

2. **消息路由**
   - 动态路由规则
   - 条件路由

3. **消息转换**
   - 多种序列化格式
   - 协议转换

### 集成示例

1. **与Spring Cloud集成**
   - 使用Spring Cloud Stream
   - 集成服务发现

2. **与数据库集成**
   - 消息持久化
   - 事件溯源

3. **与监控系统集成**
   - Prometheus + Grafana
   - ELK Stack

## 贡献指南

1. Fork项目
2. 创建功能分支
3. 提交更改
4. 推送到分支
5. 创建Pull Request

## 许可证

本项目采用MIT许可证，详见LICENSE文件。

## 联系方式

如有问题或建议，请提交Issue或联系项目维护者。

---

**注意：** 这是一个学习项目，用于演示消息队列的基本概念和使用方法。在生产环境中使用时，请根据实际需求进行调整和优化。
