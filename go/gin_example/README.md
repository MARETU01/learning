# Gin + Kafka 后端示例

这是一个基于 Gin 框架和 Kafka 消息队列的后端示例项目。

## 项目结构

本项目采用标准的 Go 项目布局，使用 `internal/` 目录存放私有应用代码。

```
gin_example/
├── docker/
│   └── kafka.sh           # Kafka Docker 启动脚本
├── internal/               # 私有应用代码（不可被其他项目导入）
│   ├── config/
│   │   └── config.go      # 配置管理
│   ├── handlers/
│   │   └── kafka_handler.go # HTTP 处理器
│   ├── kafka/
│   │   └── kafka.go       # Kafka 客户端封装
│   ├── middleware/
│   │   └── middleware.go  # 中间件
│   └── routes/
│       └── routes.go      # 路由配置
├── main.go                # 程序入口
├── go.mod                 # Go 模块文件
└── go.sum                 # 依赖版本锁定
```

## 功能特性

- ✅ 使用 Gin 框架构建 RESTful API
- ✅ Kafka 消息生产与消费
- ✅ CORS 支持
- ✅ 日志和恢复中间件

## 快速开始

### 1. 安装依赖

```bash
go mod download
```

### 2. 启动 Kafka

```bash
bash docker/kafka.sh
```

### 3. 运行项目

```bash
go run main.go
```

服务器将在 `http://localhost:8080` 启动。

## API 接口

### 健康检查
```bash
curl -X GET http://localhost:8080/health
```

### 生产消息
```bash
curl -X POST http://localhost:8080/api/v1/kafka/produce \
  -H "Content-Type: application/json" \
  -d '{
    "topic": "test-topic",
    "message": "Hello Kafka"
  }'
```

### 消费消息
```bash
curl -X GET "http://localhost:8080/api/v1/kafka/consume?topic=test-topic&group=test-group"
```

## 技术栈

- **Gin**: 高性能 HTTP Web 框架
- **Kafka**: 分布式消息队列

## 响应格式

所有接口返回统一格式的 JSON 响应：

### 成功响应
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {}
}
```

### 错误响应
```json
{
  "code": 400,
  "message": "错误信息"
}
```
