package routes

import (
	"gin_example/internal/handlers"
	"gin_example/internal/middleware"

	"github.com/gin-gonic/gin"
	"gorm.io/gorm"
)

// SetupRoutes 设置路由
func SetupRoutes(r *gin.Engine, db *gorm.DB) {
	// 应用全局中间件
	r.Use(middleware.Logger())
	r.Use(middleware.Recovery())
	r.Use(middleware.CORS())

	// 健康检查
	r.GET("/health", func(c *gin.Context) {
		c.JSON(200, gin.H{
			"status":  "ok",
			"message": "服务运行正常",
		})
	})

	// 用户处理器
	userHandler := handlers.NewUserHandler(db)

	// API路由组
	api := r.Group("/api/v1")
	{
		// Kafka路由
		kafka := api.Group("/kafka")
		{
			kafka.POST("/produce", handlers.ProduceMessage)            // 生产消息
			kafka.GET("/consume", handlers.ConsumeMessage)             // 消费消息
			kafka.POST("/produce/ack", handlers.ProduceMessageWithAck) // 生产消息（带ack确认）
			kafka.GET("/consume/ack", handlers.ConsumeMessageWithAck)  // 消费消息（带ack确认）
		}

		// 用户路由
		users := api.Group("/users")
		{
			users.GET("", userHandler.ListUsers)         // 获取用户列表
			users.GET("/:id", userHandler.GetUser)       // 获取单个用户
			users.POST("", userHandler.CreateUser)       // 创建用户
			users.PUT("/:id", userHandler.UpdateUser)    // 更新用户
			users.DELETE("/:id", userHandler.DeleteUser) // 删除用户
		}
	}
}
