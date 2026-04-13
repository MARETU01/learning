package config

import (
	"os"
	"strconv"

	"github.com/joho/godotenv"
)

// Config 应用配置结构体
type Config struct {
	ServerPort string
	// Kafka配置
	KafkaBrokers string
	KafkaTopic   string
	// MySQL配置
	MySQLHost     string
	MySQLPort     string
	MySQLUser     string
	MySQLPassword string
	MySQLDatabase string
}

// LoadConfig 加载配置
func LoadConfig() (*Config, error) {
	// 加载.env文件
	err := godotenv.Load()
	if err != nil {
		// 如果.env文件不存在，使用环境变量
	}

	config := &Config{
		ServerPort:    getEnv("SERVER_PORT", "8080"),
		KafkaBrokers:  getEnv("KAFKA_BROKERS", "localhost:9092"),
		KafkaTopic:    getEnv("KAFKA_TOPIC", "test-topic"),
		MySQLHost:     getEnv("MYSQL_HOST", "localhost"),
		MySQLPort:     getEnv("MYSQL_PORT", "3306"),
		MySQLUser:     getEnv("MYSQL_USER", "root"),
		MySQLPassword: getEnv("MYSQL_PASSWORD", "root123"),
		MySQLDatabase: getEnv("MYSQL_DATABASE", "demo"),
	}

	return config, nil
}

func getEnv(key, defaultValue string) string {
	if value, exists := os.LookupEnv(key); exists {
		return value
	}
	return defaultValue
}

// GetServerPortAsInt 获取服务器端口为整数
func (c *Config) GetServerPortAsInt() int {
	port, err := strconv.Atoi(c.ServerPort)
	if err != nil {
		return 8080
	}
	return port
}
