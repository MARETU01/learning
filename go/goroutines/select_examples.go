package main

import (
	"context"
	"fmt"
	"math/rand"
	"time"
)

// 这个文件是一组可运行的 Go select 例子。
//
// 运行方式（在该目录下）：
//   go run ./...
// 或者只跑这个文件：
//   go run ./select_examples.go
//
// 注意：本目录如果已经存在其它 main.go，建议用 `go run ./select_examples.go` 单独运行。

func main() {
	fmt.Println("== demo1: multiple receive (谁先来处理谁) ==")
	demoMultipleReceive()

	fmt.Println("\n== demo2: timeout with time.After ==")
	demoTimeout()

	fmt.Println("\n== demo3: cancel with context.Done ==")
	demoContextCancel()

	fmt.Println("\n== demo4: non-blocking send/receive (default) ==")
	demoNonBlocking()

	fmt.Println("\n== demo5: ticker + select (周期任务 + 可退出) ==")
	demoTicker()

	fmt.Println("\n== demo6: fan-in merge channels + disable case with nil channel ==")
	demoFanIn()
}

// demoMultipleReceive 展示：select 同时等待多个 channel 接收。
//
// 规则：
// - 没有 default：当所有 case 都不就绪时，select 会阻塞。
// - 多个 case 同时就绪：select 会“随机”选择一个执行（避免饥饿）。
func demoMultipleReceive() {
	chA := make(chan string)
	chB := make(chan string)

	go func() {
		time.Sleep(120 * time.Millisecond)
		chA <- "from A"
	}()
	go func() {
		time.Sleep(60 * time.Millisecond)
		chB <- "from B"
	}()

	select {
	case v := <-chA:
		fmt.Println("got:", v)
	case v := <-chB:
		fmt.Println("got:", v)
	}
}

// demoTimeout 展示：select + time.After 做超时控制。
// 常用于：等待下游/缓存/数据库结果，但最多等 N 毫秒。
func demoTimeout() {
	resultCh := make(chan string)

	go func() {
		// 模拟慢操作
		time.Sleep(200 * time.Millisecond)
		resultCh <- "OK"
	}()

	select {
	case v := <-resultCh:
		fmt.Println("result:", v)
	case <-time.After(80 * time.Millisecond):
		fmt.Println("timeout")
	}

	// 小提示：在高频循环中反复使用 time.After 会频繁创建 timer。
	// 如果你在 for 循环里做超时，通常用 time.NewTimer + Reset 会更省。
}

// demoContextCancel 展示：select 监听 ctx.Done() 实现取消/退出。
// 这是后台服务里最常用的退出方式之一。
func demoContextCancel() {
	ctx, cancel := context.WithCancel(context.Background())
	defer cancel()

	jobs := make(chan int)
	done := make(chan struct{})

	go func() {
		defer close(done)
		for {
			select {
			case <-ctx.Done():
				fmt.Println("worker: canceled")
				return
			case job := <-jobs:
				fmt.Println("worker: got job", job)
			}
		}
	}()

	jobs <- 1
	jobs <- 2
	cancel() // 触发退出
	<-done
}

// demoNonBlocking 展示：select 带 default 实现“非阻塞”收/发。
// 常用于：
// - 非阻塞接收：有就拿，没有就继续干别的
// - 非阻塞发送：队列满了就丢弃/降级/统计
func demoNonBlocking() {
	// 1) 非阻塞接收：此时 ch 没数据，走 default
	ch := make(chan int)
	select {
	case v := <-ch:
		fmt.Println("received:", v)
	default:
		fmt.Println("receive: no data (non-blocking)")
	}

	// 2) 非阻塞发送：buffer 满了走 default
	queue := make(chan int, 1)
	queue <- 10

	select {
	case queue <- 20:
		fmt.Println("send: enqueued")
	default:
		fmt.Println("send: queue full -> drop")
	}

	// 3) ⚠️ 常见坑：for + select + default 很容易写成 busy loop
	// 因为永远不阻塞，会让 CPU 飙高。一般用 ticker/sleep/backoff 避免空转。
}

// demoTicker 展示：Ticker + select 实现周期任务，并且监听退出。
func demoTicker() {
	ctx, cancel := context.WithTimeout(context.Background(), 350*time.Millisecond)
	defer cancel()

	ticker := time.NewTicker(100 * time.Millisecond)
	defer ticker.Stop()

	count := 0
	for {
		select {
		case <-ticker.C:
			count++
			fmt.Println("tick", count)
		case <-ctx.Done():
			fmt.Println("ticker loop exit:", ctx.Err())
			return
		}
	}
}

// demoFanIn 展示：fan-in（合并多个输入 channel）+ 关闭处理。
// 这里也展示一个技巧：当某个输入 channel 被 close 后，把它设置为 nil
// 来“禁用”这个 case（nil channel 永远不会就绪）。
func demoFanIn() {
	r := rand.New(rand.NewSource(time.Now().UnixNano()))

	a := make(chan int)
	b := make(chan int)

	go func() {
		defer close(a)
		for i := 1; i <= 3; i++ {
			time.Sleep(time.Duration(30+r.Intn(40)) * time.Millisecond)
			a <- i
		}
	}()
	go func() {
		defer close(b)
		for i := 100; i <= 102; i++ {
			time.Sleep(time.Duration(30+r.Intn(40)) * time.Millisecond)
			b <- i
		}
	}()

	out := fanIn(a, b)
	for v := range out {
		fmt.Println("out:", v)
	}
	fmt.Println("fan-in done")
}

func fanIn(a, b <-chan int) <-chan int {
	out := make(chan int)
	go func() {
		defer close(out)
		for a != nil || b != nil {
			select {
			case v, ok := <-a:
				if !ok {
					// a 被关闭后，继续 select 会立刻读到零值并 ok=false，导致空转。
					// 设置为 nil 可以禁用该 case。
					a = nil
					continue
				}
				out <- v
			case v, ok := <-b:
				if !ok {
					b = nil
					continue
				}
				out <- v
			}
		}
	}()
	return out
}
