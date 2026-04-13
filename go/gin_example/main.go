package main

import (
	"context"
	"fmt"
	"log"
	"strings"

	"gin_example/internal/config"
	"gin_example/internal/database"
	"gin_example/internal/handlers"
	"gin_example/internal/kafka"
	"gin_example/internal/routes"
	"github.com/gin-gonic/gin"
)

func main() {
	// 加载配置
	cfg, err := config.LoadConfig()
	if err != nil {
		log.Fatalf("加载配置失败: %v", err)
	}

	// 初始化Kafka客户端
	brokers := strings.Split(cfg.KafkaBrokers, ",")
	kafkaClient, err := kafka.NewClient(brokers, cfg.KafkaTopic)
	if err != nil {
		log.Printf("警告: 初始化Kafka客户端失败: %v", err)
	} else {
		defer kafkaClient.Close()
		fmt.Println("✅ Kafka客户端初始化成功")

		// 启动消费者（应用启动时立即开始消费）
		ctx := context.Background()

		// 启动普通消费者
		if err := kafka.StartConsumer(ctx, "gin-example-group", &handlers.MessageHandler{}); err != nil {
			log.Printf("警告: 启动普通消费者失败: %v", err)
		} else {
			fmt.Println("✅ Kafka普通消费者已启动 (gin-example-group)")
		}

		// 启动带ACK的消费者
		if err := kafka.StartConsumer(ctx, "gin-example-ack-group", &handlers.AckMessageHandler{}); err != nil {
			log.Printf("警告: 启动ACK消费者失败: %v", err)
		} else {
			fmt.Println("✅ Kafka ACK消费者已启动 (gin-example-ack-group)")
		}
	}

	// 初始化MySQL连接
	mysqlConfig := &database.MySQLConfig{
		Host:     cfg.MySQLHost,
		Port:     cfg.MySQLPort,
		User:     cfg.MySQLUser,
		Password: cfg.MySQLPassword,
		Database: cfg.MySQLDatabase,
	}
	db, err := database.InitMySQL(mysqlConfig)
	if err != nil {
		log.Printf("警告: 初始化MySQL失败: %v", err)
	} else {
		defer func() {
			sqlDB, _ := db.DB()
			sqlDB.Close()
		}()
	}

	// 设置Gin模式
	gin.SetMode(gin.ReleaseMode)

	// 创建Gin引擎
	r := gin.Default()

	// 设置路由
	routes.SetupRoutes(r, db)

	// 启动服务器
	addr := fmt.Sprintf(":%s", cfg.ServerPort)
	fmt.Printf("🚀 服务器启动成功，监听端口: %s\n", addr)
	fmt.Println("📚 API文档:")
	fmt.Println("  Kafka消息队列:")
	fmt.Println("    POST   /api/v1/kafka/produce       - 生产Kafka消息")
	fmt.Println("    GET    /api/v1/kafka/consume        - 消费Kafka消息")
	fmt.Println("    POST   /api/v1/kafka/produce/ack    - 生产消息(带ACK确认)")
	fmt.Println("    GET    /api/v1/kafka/consume/ack    - 消费消息(带ACK确认)")
	fmt.Println("  用户管理:")
	fmt.Println("    GET    /api/v1/users                - 获取用户列表")
	fmt.Println("    GET    /api/v1/users/:id            - 获取单个用户")
	fmt.Println("    POST   /api/v1/users                - 创建用户")
	fmt.Println("    PUT    /api/v1/users/:id            - 更新用户")
	fmt.Println("    DELETE /api/v1/users/:id            - 删除用户")

	if err := r.Run(addr); err != nil {
		log.Fatalf("服务器启动失败: %v", err)
	}
}
