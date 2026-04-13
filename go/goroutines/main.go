package main

import (
	"context"
	"fmt"
	"runtime"
	"sync"
	"time"
)

func simpleChannelExample() {
	fmt.Println("--- simpleChannelExample ---")
	ch := make(chan string)

	for i := 1; i <= 3; i++ {
		id := i
		go func() {
			// 每个 goroutine 向 channel 发送消息
			time.Sleep(time.Duration(id) * 100 * time.Millisecond)
			ch <- fmt.Sprintf("worker %d done", id)
		}()
	}

	// 接收三次，等待所有 goroutine 完成
	for i := 0; i < 3; i++ {
		msg := <-ch
		fmt.Println(msg)
	}
	fmt.Println()
}

func waitGroupExample() {
	fmt.Println("--- waitGroupExample ---")
	var wg sync.WaitGroup
	for i := 1; i <= 3; i++ {
		wg.Add(1)
		id := i
		go func() {
			defer wg.Done()
			time.Sleep(time.Duration(id) * 80 * time.Millisecond)
			fmt.Println("wg worker", id, "done")
		}()
	}
	wg.Wait()
	fmt.Println("all wg workers done")
	fmt.Println()
}

func contextCancelExample() {
	fmt.Println("--- contextCancelExample ---")
	ctx, cancel := context.WithCancel(context.Background())
	defer cancel()

	go func(ctx context.Context) {
		// 模拟一个会响应取消信号的 goroutine
		for i := 0; i < 10; i++ {
			select {
			case <-ctx.Done():
				fmt.Println("worker received cancel")
				return
			default:
				fmt.Println("working", i)
				time.Sleep(50 * time.Millisecond)
			}
		}
	}(ctx)

	// 让上面的 worker 运行一会儿，然后取消
	time.Sleep(180 * time.Millisecond)
	cancel()
	// 等一会儿让 worker 打印取消信息
	time.Sleep(60 * time.Millisecond)
	fmt.Println()
}

func panicRecoverExample() {
	fmt.Println("--- panicRecoverExample ---")
	// 在每个 goroutine 内用 defer+recover 捕获 panic，避免整个程序崩溃
	go func() {
		defer func() {
			if r := recover(); r != nil {
				fmt.Println("recovered from panic in goroutine:", r)
			}
		}()
		panic("something bad in goroutine")
	}()

	// 等个短时间，确认 panic 已被捕获并打印
	time.Sleep(50 * time.Millisecond)
	fmt.Println("other goroutines still run")
	fmt.Println()
}

func workerPoolExample() {
	fmt.Println("--- workerPoolExample ---")
	jobs := make(chan int)
	results := make(chan int)
	var wg sync.WaitGroup

	// 启动 3 个 worker
	numWorkers := 3
	for w := 1; w <= numWorkers; w++ {
		wg.Add(1)
		go func(id int) {
			defer wg.Done()
			for j := range jobs {
				fmt.Printf("worker %d processing job %d\n", id, j)
				time.Sleep(30 * time.Millisecond) // 模拟工作
				results <- j * 2
			}
		}(w)
	}

	// 发送任务并关闭 jobs
	go func() {
		for j := 1; j <= 5; j++ {
			jobs <- j
		}
		close(jobs)
	}()

	// 收集结果（因为有 5 个任务）
	for i := 0; i < 5; i++ {
		res := <-results
		fmt.Println("result:", res)
	}

	// 等待所有 worker 退出后关闭 results
	wg.Wait()
	close(results)
	// drain any remaining (should be none)
	for range results {
	}
	fmt.Println("worker pool done")
	fmt.Println()
}

func main() {
	fmt.Println("Go goroutines learning - NumCPU:", runtime.NumCPU())

	simpleChannelExample()

	waitGroupExample()

	contextCancelExample()

	panicRecoverExample()

	workerPoolExample()

	fmt.Println("learning complete")
}
