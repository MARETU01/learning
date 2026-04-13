package main

import (
	"context"
	"fmt"
	"golang.org/x/sync/errgroup"
	"time"
)

func errWorker(id int) error {
	// 模拟工作耗时
	time.Sleep(time.Duration(id) * 100 * time.Millisecond)
	fmt.Printf("worker %d 完成\n", id)

	// 模拟worker 3 会出错
	if id == 3 {
		return fmt.Errorf("worker %d 遇到错误", id)
	}

	return nil
}

func workerWithContext(ctx context.Context, id int) error {
	// 检查是否被取消
	select {
	case <-ctx.Done():
		fmt.Printf("worker %d 被取消\n", id)
		return ctx.Err()
	default:
	}

	// 模拟工作耗时
	time.Sleep(200 * time.Millisecond)
	fmt.Printf("worker %d 完成\n", id)
	return nil
}

func main() {
	fmt.Println("=== 演示1: 基本用法 - 任何一个出错都会取消其他 ===")

	startTime := time.Now()
	g, ctx := errgroup.WithContext(context.Background())

	// 启动多个 goroutine
	for i := 1; i <= 5; i++ {
		i := i // 捕获变量
		g.Go(func() error {
			return errWorker(i)
		})
	}

	// 等待所有 goroutine 完成
	if err := g.Wait(); err != nil {
		fmt.Printf("errgroup 遇到错误: %v\n", err)
	}
	fmt.Printf("耗时: %v\n\n", time.Since(startTime))

	fmt.Println("=== 演示2: 限制并发数量 ===")

	startTime = time.Now()
	g, ctx = errgroup.WithContext(context.Background())
	g.SetLimit(2) // 限制最多2个并发

	// 启动10个任务，但同一时间最多2个在运行
	for i := 1; i <= 10; i++ {
		i := i
		g.Go(func() error {
			return workerWithContext(ctx, i)
		})
	}

	if err := g.Wait(); err != nil {
		fmt.Printf("errgroup 遇到错误: %v\n", err)
	}
	fmt.Printf("耗时: %v\n\n", time.Since(startTime))

	fmt.Println("=== 演示3: context 超时取消 ===")

	startTime = time.Now()
	// 设置100ms超时
	ctx, cancel := context.WithTimeout(context.Background(), 100*time.Millisecond)
	defer cancel()

	g, _ = errgroup.WithContext(ctx)

	for i := 1; i <= 5; i++ {
		i := i
		g.Go(func() error {
			return workerWithContext(ctx, i)
		})
	}

	if err := g.Wait(); err != nil {
		fmt.Printf("errgroup 因超时被取消: %v\n", err)
	}
	fmt.Printf("耗时: %v\n", time.Since(startTime))
}
