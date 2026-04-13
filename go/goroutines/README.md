# goroutines 示例

这个目录包含用于学习 Go goroutine、channel、sync.WaitGroup、context 取消、panic/recover、以及 worker pool 的小示例。

运行：

```bash
cd learning/goroutines
go run .
```

示例包含：
- `simpleChannelExample`：演示多个 goroutine 向 channel 发送并被主 goroutine 接收。
- `waitGroupExample`：使用 `sync.WaitGroup` 等待一组 goroutine 完成。
- `contextCancelExample`：使用 `context.WithCancel` 发送取消信号并由 goroutine 响应。
- `panicRecoverExample`：在 goroutine 内使用 `defer + recover()` 捕获 panic，避免进程退出。
- `workerPoolExample`：用 channel 实现简单的 worker pool。

代码适合作为学习和演示用途；欢迎改进或补充更多场景。
