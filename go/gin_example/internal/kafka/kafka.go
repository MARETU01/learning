package kafka

import (
	"context"
	"encoding/json"
	"fmt"
	"sync"

	"github.com/IBM/sarama"
)

// Client Kafka客户端
type Client struct {
	producer sarama.SyncProducer
	consumer sarama.ConsumerGroup
	brokers  []string
	topic    string
}

// Message 消息结构
type Message struct {
	Key   string      `json:"key"`
	Value interface{} `json:"value"`
}

// ConsumerHandler 消费者处理器接口
type ConsumerHandler interface {
	HandleMessage(msg *Message) error
}

// clientInstance 单例实例
var clientInstance *Client
var once sync.Once
var startedConsumers map[string]bool
var consumersMutex sync.Mutex

// NewClient 创建Kafka客户端（单例模式）
func NewClient(brokers []string, topic string) (*Client, error) {
	var initErr error
	once.Do(func() {
		// 初始化消费者追踪map
		startedConsumers = make(map[string]bool)

		// 创建生产者配置
		config := sarama.NewConfig()
		config.Producer.RequiredAcks = sarama.WaitForAll
		config.Producer.Retry.Max = 5
		config.Producer.Return.Successes = true
		config.Consumer.Group.Rebalance.GroupStrategies = []sarama.BalanceStrategy{sarama.NewBalanceStrategyRoundRobin()}
		config.Consumer.Offsets.Initial = sarama.OffsetNewest

		// 创建生产者
		producer, err := sarama.NewSyncProducer(brokers, config)
		if err != nil {
			initErr = fmt.Errorf("创建Kafka生产者失败: %w", err)
			return
		}

		clientInstance = &Client{
			producer: producer,
			brokers:  brokers,
			topic:    topic,
		}
	})

	if initErr != nil {
		return nil, initErr
	}
	return clientInstance, nil
}

// GetClient 获取客户端实例
func GetClient() *Client {
	return clientInstance
}

// Produce 发送消息到Kafka
func (c *Client) Produce(key string, value interface{}) error {
	if c == nil || c.producer == nil {
		return fmt.Errorf("Kafka客户端未初始化")
	}

	// 序列化消息值
	valueBytes, err := json.Marshal(value)
	if err != nil {
		return fmt.Errorf("序列化消息失败: %w", err)
	}

	msg := &sarama.ProducerMessage{
		Topic: c.topic,
		Key:   sarama.StringEncoder(key),
		Value: sarama.ByteEncoder(valueBytes),
	}

	partition, offset, err := c.producer.SendMessage(msg)
	if err != nil {
		return fmt.Errorf("发送消息失败: %w", err)
	}

	fmt.Printf("消息已发送到分区 %d, 偏移量 %d\n", partition, offset)
	return nil
}

// ProduceWithAck 发送消息到Kafka并返回ack确认信息
func (c *Client) ProduceWithAck(key string, value interface{}) (int32, int64, error) {
	if c == nil || c.producer == nil {
		return 0, 0, fmt.Errorf("Kafka客户端未初始化")
	}

	// 序列化消息值
	valueBytes, err := json.Marshal(value)
	if err != nil {
		return 0, 0, fmt.Errorf("序列化消息失败: %w", err)
	}

	msg := &sarama.ProducerMessage{
		Topic: c.topic,
		Key:   sarama.StringEncoder(key),
		Value: sarama.ByteEncoder(valueBytes),
	}

	partition, offset, err := c.producer.SendMessage(msg)
	if err != nil {
		return 0, 0, fmt.Errorf("发送消息失败: %w", err)
	}

	return partition, offset, nil
}

// Consume 消费消息（非阻塞，在后台运行）
func (c *Client) Consume(ctx context.Context, groupID string, handler ConsumerHandler) error {
	if c == nil {
		return fmt.Errorf("Kafka客户端未初始化")
	}

	consumersMutex.Lock()
	defer consumersMutex.Unlock()

	// 检查该group是否已经启动
	if startedConsumers[groupID] {
		return nil // 已经启动，直接返回
	}

	// 创建消费者配置
	config := sarama.NewConfig()
	config.Consumer.Group.Rebalance.Strategy = sarama.BalanceStrategyRoundRobin
	config.Consumer.Offsets.Initial = sarama.OffsetNewest

	// 创建消费者组
	consumerGroup, err := sarama.NewConsumerGroup(c.brokers, groupID, config)
	if err != nil {
		return fmt.Errorf("创建消费者组失败: %w", err)
	}

	c.consumer = consumerGroup
	startedConsumers[groupID] = true

	// 启动消费者
	go func() {
		consumer := &consumerHandler{handler: handler}
		for {
			select {
			case <-ctx.Done():
				fmt.Printf("[Kafka] 消费者 %s 收到停止信号\n", groupID)
				return
			default:
				if err := consumerGroup.Consume(ctx, []string{c.topic}, consumer); err != nil {
					fmt.Printf("[Kafka] 消费错误 (%s): %v\n", groupID, err)
				}
			}
		}
	}()

	return nil
}

// StartConsumer 启动消费者（用于应用启动时调用）
func StartConsumer(ctx context.Context, groupID string, handler ConsumerHandler) error {
	client := GetClient()
	if client == nil {
		return fmt.Errorf("Kafka客户端未初始化")
	}

	fmt.Printf("[Kafka] 正在启动消费者，GroupID: %s, Topic: %s\n", groupID, client.topic)
	return client.Consume(ctx, groupID, handler)
}

// IsConsumerStarted 检查消费者是否已启动
func IsConsumerStarted(groupID string) bool {
	consumersMutex.Lock()
	defer consumersMutex.Unlock()
	return startedConsumers[groupID]
}

// consumerHandler 消费者处理器实现
type consumerHandler struct {
	handler ConsumerHandler
}

// Setup 会话开始前调用
func (h *consumerHandler) Setup(sarama.ConsumerGroupSession) error {
	return nil
}

// Cleanup 会话结束后调用
func (h *consumerHandler) Cleanup(sarama.ConsumerGroupSession) error {
	return nil
}

// ConsumeClaim 消费消息
func (h *consumerHandler) ConsumeClaim(session sarama.ConsumerGroupSession, claim sarama.ConsumerGroupClaim) error {
	for msg := range claim.Messages() {
		var value interface{}
		if err := json.Unmarshal(msg.Value, &value); err != nil {
			fmt.Printf("反序列化消息失败: %v\n", err)
			continue
		}

		message := &Message{
			Key:   string(msg.Key),
			Value: value,
		}

		if err := h.handler.HandleMessage(message); err != nil {
			fmt.Printf("处理消息失败: %v\n", err)
		}

		session.MarkMessage(msg, "")
	}
	return nil
}

// Close 关闭客户端
func (c *Client) Close() error {
	if c == nil {
		return nil
	}
	var errs []error
	if c.producer != nil {
		if err := c.producer.Close(); err != nil {
			errs = append(errs, err)
		}
	}
	if c.consumer != nil {
		if err := c.consumer.Close(); err != nil {
			errs = append(errs, err)
		}
	}
	if len(errs) > 0 {
		return fmt.Errorf("关闭Kafka客户端出错: %v", errs)
	}
	return nil
}
