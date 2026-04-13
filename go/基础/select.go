package main

import (
	"fmt"
	"time"
)

// === Go select 语句 ===
// select 是 Go 并发编程的核心机制，专门用于处理多个 channel 操作
// 类似于 switch，但每个 case 必须是 channel 的发送或接收操作
// 当多个 case 同时就绪时，会随机选择一个执行

// 基本 select
func exampleBasicSelect() {
	fmt.Println("1) 基本 select:")

	ch1 := make(chan string)
	ch2 := make(chan string)

	go func() {
		time.Sleep(100 * time.Millisecond)
		ch1 <- "来自 ch1"
	}()

	go func() {
		time.Sleep(200 * time.Millisecond)
		ch2 <- "来自 ch2"
	}()

	// select 会等待任意一个 case 就绪
	select {
	case msg1 := <-ch1:
		fmt.Println("收到:", msg1)
	case msg2 := <-ch2:
		fmt.Println("收到:", msg2)
	}
	// 输出: 收到: 来自 ch1 (因为它先到达)

	fmt.Println()
}

// 超时控制
func exampleTimeout() {
	fmt.Println("2) 超时控制:")

	ch := make(chan string)

	go func() {
		time.Sleep(2 * time.Second)
		ch <- "结果"
	}()

	select {
	case res := <-ch:
		fmt.Println("收到:", res)
	case <-time.After(500 * time.Millisecond):
		fmt.Println("超时了！(等待超过500ms)")
	}

	fmt.Println()
}

// 非阻塞操作
func exampleNonBlocking() {
	fmt.Println("3) 非阻塞操作:")

	ch := make(chan int, 1)

	// 非阻塞发送
	select {
	case ch <- 42:
		fmt.Println("发送成功: 42")
	default:
		fmt.Println("channel 已满，发送失败")
	}

	// 非阻塞接收
	select {
	case val := <-ch:
		fmt.Println("接收成功:", val)
	default:
		fmt.Println("channel 为空，没有数据")
	}

	// 再次尝试接收（此时 channel 已空）
	select {
	case val := <-ch:
		fmt.Println("接收成功:", val)
	default:
		fmt.Println("channel 为空，没有数据")
	}

	fmt.Println()
}

// default 默认分支
func exampleDefaultBranch() {
	fmt.Println("4) default 默认分支:")

	ch := make(chan int, 2)
	ch <- 1
	ch <- 2

	// 使用 default 实现非阻塞
	for i := 0; i < 5; i++ {
		select {
		case val := <-ch:
			fmt.Printf("读取到: %d\n", val)
		default:
			fmt.Println("没有数据可读，执行其他操作...")
			time.Sleep(100 * time.Millisecond)
		}
	}

	fmt.Println()
}

// 退出信号
func exampleQuitSignal() {
	fmt.Println("5) 退出信号:")

	quit := make(chan bool, 1)   // 带缓冲，避免阻塞
	done := make(chan string, 1) // 带缓冲，避免阻塞

	// 启动工作 goroutine
	go func() {
		for i := 1; i <= 3; i++ {
			select {
			case <-quit:
				done <- "已停止"
				return
			default:
				fmt.Printf("工作中... 第 %d 次\n", i)
				time.Sleep(100 * time.Millisecond)
			}
		}
		done <- "工作完成"
	}()

	time.Sleep(250 * time.Millisecond)
	quit <- true // 发送退出信号

	result := <-done
	fmt.Println("结果:", result)

	fmt.Println()
}

// 随机选择
func exampleRandomSelect() {
	fmt.Println("6) 随机选择 (多个 case 同时就绪):")

	ch1 := make(chan string, 1)
	ch2 := make(chan string, 1)

	ch1 <- "数据1"
	ch2 <- "数据2"

	// 两个 channel 都有数据，select 会随机选择
	for i := 0; i < 5; i++ {
		select {
		case msg1 := <-ch1:
			fmt.Printf("第%d次: 选中 ch1 -> %s\n", i+1, msg1)
			ch1 <- "数据1" // 补回去
		case msg2 := <-ch2:
			fmt.Printf("第%d次: 选中 ch2 -> %s\n", i+1, msg2)
			ch2 <- "数据2" // 补回去
		}
	}

	fmt.Println()
}

// 循环 select
func exampleLoopSelect() {
	fmt.Println("7) 循环 select:")

	ch := make(chan int)
	quit := make(chan bool)

	// 生产者
	go func() {
		for i := 1; i <= 3; i++ {
			ch <- i
			time.Sleep(100 * time.Millisecond)
		}
		quit <- true
	}()

	// 消费者循环
	for {
		select {
		case val := <-ch:
			fmt.Printf("收到数据: %d\n", val)
		case <-quit:
			fmt.Println("收到退出信号，结束循环")
			return
		}
	}
}

// 空 select
func exampleEmptySelect() {
	fmt.Println("8) 空 select (永久阻塞):")

	fmt.Println("select {} 会永久阻塞 goroutine")
	fmt.Println("通常用于防止主 goroutine 退出")
	fmt.Println("示例: 在 main 函数末尾写 select {} 可以让程序一直运行")

	// 注释掉的代码会永久阻塞
	// select {}

	fmt.Println()
}

// select 超时重试模式
func exampleTimeoutRetry() {
	fmt.Println("9) 超时重试模式:")

	ch := make(chan string)
	maxRetries := 3

	go func() {
		time.Sleep(800 * time.Millisecond)
		ch <- "最终结果"
	}()

	for i := 1; i <= maxRetries; i++ {
		fmt.Printf("第 %d 次尝试...\n", i)

		select {
		case res := <-ch:
			fmt.Println("成功:", res)
			fmt.Println()
			return
		case <-time.After(300 * time.Millisecond):
			fmt.Println("超时，继续重试...")
		}
	}

	fmt.Println("重试次数用尽，操作失败")
	fmt.Println()
}

// select 实现心跳
func exampleHeartbeat() {
	fmt.Println("10) select 实现心跳:")

	heartbeat := make(chan bool)
	work := make(chan string, 3)

	// 工作协程
	go func() {
		ticker := time.NewTicker(100 * time.Millisecond)
		defer ticker.Stop()

		for i := 0; i < 3; i++ {
			select {
			case <-ticker.C:
				heartbeat <- true // 发送心跳
			case work <- fmt.Sprintf("任务%d", i+1):
				time.Sleep(50 * time.Millisecond)
			}
		}
		close(heartbeat)
	}()

	// 主循环
	for {
		select {
		case _, ok := <-heartbeat:
			if !ok {
				fmt.Println("心跳停止，退出")
				fmt.Println()
				return
			}
			fmt.Println("❤️ 心跳")
		case task := <-work:
			fmt.Println("执行:", task)
		}
	}
}

// select vs switch 对比
func exampleSelectVsSwitch() {
	fmt.Println("11) select vs switch:")

	fmt.Println("switch: 值的多路分支")
	fmt.Println("  switch value {")
	fmt.Println("  case 1: ...")
	fmt.Println("  case 2: ...")
	fmt.Println("  }")

	fmt.Println("\nselect: channel 的多路复用")
	fmt.Println("  select {")
	fmt.Println("  case <-ch1: ...")
	fmt.Println("  case <-ch2: ...")
	fmt.Println("  }")

	fmt.Println("\n关键区别:")
	fmt.Println("  1. select 的 case 必须是 channel 操作")
	fmt.Println("  2. select 会阻塞等待直到有 case 就绪")
	fmt.Println("  3. 多个 case 就绪时随机选择")

	fmt.Println()
}

func main() {
	fmt.Println("=== Go select 语句示例 ===\n")

	exampleBasicSelect()
	exampleTimeout()
	exampleNonBlocking()
	exampleDefaultBranch()
	exampleQuitSignal()
	exampleRandomSelect()
	exampleLoopSelect()
	exampleEmptySelect()
	exampleTimeoutRetry()
	exampleHeartbeat()
	exampleSelectVsSwitch()

	fmt.Println("=== 示例结束 ===")
}
