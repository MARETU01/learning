package main

import (
	"fmt"
	"sync"
	"time"
)

func worker(id int, jobs <-chan int, results chan<- int, wg *sync.WaitGroup) {
	defer wg.Done()
	for j := range jobs {
		// 假装做点计算
		time.Sleep(500 * time.Millisecond)
		results <- j * j
		fmt.Printf("worker %d processing job %d\n", id, j)
	}
}

func main() {
	startTime := time.Now()
	jobs := make(chan int, 5)
	results := make(chan int, 5)

	var wg sync.WaitGroup
	workerN := 3
	wg.Add(workerN)

	// 启动 worker，总共 3 个 goroutine
	for i := 0; i < workerN; i++ {
		go worker(i, jobs, results, &wg)
	}

	// 投递任务
	for j := 1; j <= 5; j++ {
		jobs <- j
	}
	close(jobs) // 不再投递任务

	// 等待所有 worker 完成
	wg.Wait()
	close(results) // worker 都退出后再关 results

	// 收集结果
	for r := range results {
		fmt.Println("result:", r)
	}

	fmt.Println(time.Since(startTime))
}
