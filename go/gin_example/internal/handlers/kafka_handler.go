package handlers

import (
	"fmt"
	"gin_example/internal/kafka"
	"net/http"
	"sync"
	"time"

	"github.com/gin-gonic/gin"
)

// KafkaProduceRequest 生产消息请求
type KafkaProduceRequest struct {
	Key   string      `json:"key" binding:"required"`
	Value interface{} `json:"value" binding:"required"`
}

// KafkaProduceResponse 生产消息响应
type KafkaProduceResponse struct {
	Success bool   `json:"success"`
	Message string `json:"message"`
}

// KafkaConsumeResponse 消费消息响应
type KafkaConsumeResponse struct {
	Success  bool             `json:"success"`
	Messages []*kafka.Message `json:"messages,omitempty"`
	Message  string           `json:"message,omitempty"`
}

// consumedMessages 存储消费到的消息
var consumedMessages []*kafka.Message
var messageMutex sync.Mutex

// ProduceMessage 生产消息到Kafka
func ProduceMessage(c *gin.Context) {
	var req KafkaProduceRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{
			"success": false,
			"message": "请求参数错误: " + err.Error(),
		})
		return
	}

	client := kafka.GetClient()
	if client == nil {
		c.JSON(http.StatusInternalServerError, gin.H{
			"success": false,
			"message": "Kafka客户端未初始化",
		})
		return
	}

	if err := client.Produce(req.Key, req.Value); err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{
			"success": false,
			"message": "发送消息失败: " + err.Error(),
		})
		return
	}

	c.JSON(http.StatusOK, KafkaProduceResponse{
		Success: true,
		Message: "消息发送成功",
	})
}

// ConsumeMessage 消费Kafka消息
func ConsumeMessage(c *gin.Context) {
	client := kafka.GetClient()
	if client == nil {
		c.JSON(http.StatusInternalServerError, gin.H{
			"success": false,
			"message": "Kafka客户端未初始化",
		})
		return
	}

	// 消费者已在应用启动时启动，这里只返回消费到的消息
	// 等待一段时间让消息被消费
	time.Sleep(100 * time.Millisecond)

	// 返回当前消费到的消息
	messageMutex.Lock()
	messages := make([]*kafka.Message, len(consumedMessages))
	copy(messages, consumedMessages)
	messageMutex.Unlock()

	c.JSON(http.StatusOK, KafkaConsumeResponse{
		Success:  true,
		Messages: messages,
		Message:  fmt.Sprintf("已消费 %d 条消息", len(messages)),
	})
}

// MessageHandler 消息处理器实现（导出供main.go使用）
type MessageHandler struct{}

// HandleMessage 处理消费到的消息
func (h *MessageHandler) HandleMessage(msg *kafka.Message) error {
	messageMutex.Lock()
	defer messageMutex.Unlock()

	consumedMessages = append(consumedMessages, msg)
	fmt.Printf("消费到消息: Key=%s, Value=%v\n", msg.Key, msg.Value)
	return nil
}

// ClearMessages 清空已消费的消息（用于测试）
func ClearMessages() {
	messageMutex.Lock()
	defer messageMutex.Unlock()
	consumedMessages = nil
}

// KafkaProduceWithAckResponse 带ack的生产消息响应
type KafkaProduceWithAckResponse struct {
	Success   bool   `json:"success"`
	Message   string `json:"message"`
	Partition int32  `json:"partition,omitempty"`
	Offset    int64  `json:"offset,omitempty"`
}

// KafkaConsumeWithAckResponse 带ack的消费消息响应
type KafkaConsumeWithAckResponse struct {
	Success       bool             `json:"success"`
	Message       string           `json:"message"`
	Messages      []*kafka.Message `json:"messages,omitempty"`
	Acknowledged  int              `json:"acknowledged,omitempty"`
	ConsumerGroup string           `json:"consumer_group,omitempty"`
}

// ProduceMessageWithAck 生产消息到Kafka（带ack确认和日志）
func ProduceMessageWithAck(c *gin.Context) {
	var req KafkaProduceRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		fmt.Printf("[Kafka-Ack] 请求参数错误: %v\n", err)
		c.JSON(http.StatusBadRequest, gin.H{
			"success": false,
			"message": "请求参数错误: " + err.Error(),
		})
		return
	}

	client := kafka.GetClient()
	if client == nil {
		fmt.Println("[Kafka-Ack] Kafka客户端未初始化")
		c.JSON(http.StatusInternalServerError, gin.H{
			"success": false,
			"message": "Kafka客户端未初始化",
		})
		return
	}

	fmt.Printf("[Kafka-Ack] 开始生产消息: Key=%s, Value=%v\n", req.Key, req.Value)

	// 调用带ack的生产方法
	partition, offset, err := client.ProduceWithAck(req.Key, req.Value)
	if err != nil {
		fmt.Printf("[Kafka-Ack] 发送消息失败: %v\n", err)
		c.JSON(http.StatusInternalServerError, gin.H{
			"success": false,
			"message": "发送消息失败: " + err.Error(),
		})
		return
	}

	fmt.Printf("[Kafka-Ack] 消息发送成功并收到ACK确认: Partition=%d, Offset=%d\n", partition, offset)

	c.JSON(http.StatusOK, KafkaProduceWithAckResponse{
		Success:   true,
		Message:   "消息发送成功，已收到broker确认",
		Partition: partition,
		Offset:    offset,
	})
}

// ConsumeMessageWithAck 消费Kafka消息（带ack确认和日志）
func ConsumeMessageWithAck(c *gin.Context) {
	client := kafka.GetClient()
	if client == nil {
		fmt.Println("[Kafka-Ack] Kafka客户端未初始化")
		c.JSON(http.StatusInternalServerError, gin.H{
			"success": false,
			"message": "Kafka客户端未初始化",
		})
		return
	}

	groupID := "gin-example-ack-group"

	// 消费者已在应用启动时启动，这里只返回消费到的消息
	// 等待一段时间让消息被消费
	time.Sleep(100 * time.Millisecond)

	// 返回当前消费到的消息
	messageMutex.Lock()
	messages := make([]*kafka.Message, len(consumedMessages))
	copy(messages, consumedMessages)
	ackCount := len(messages)
	messageMutex.Unlock()

	fmt.Printf("[Kafka-Ack] 返回消费结果: 消息数=%d, 消费者组=%s\n", ackCount, groupID)

	c.JSON(http.StatusOK, KafkaConsumeWithAckResponse{
		Success:       true,
		Message:       fmt.Sprintf("已消费并ACK确认 %d 条消息", ackCount),
		Messages:      messages,
		Acknowledged:  ackCount,
		ConsumerGroup: groupID,
	})
}

// AckMessageHandler 带ack的消息处理器实现（导出供main.go使用）
type AckMessageHandler struct{}

// HandleMessage 处理消费到的消息（带ack）
func (h *AckMessageHandler) HandleMessage(msg *kafka.Message) error {
	messageMutex.Lock()
	defer messageMutex.Unlock()

	consumedMessages = append(consumedMessages, msg)
	fmt.Printf("[Kafka-Ack] 成功消费并ACK消息: Key=%s, Value=%v\n", msg.Key, msg.Value)
	return nil
}
