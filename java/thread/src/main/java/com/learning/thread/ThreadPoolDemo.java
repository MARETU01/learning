package com.learning.thread;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 线程池学习示例：
 * 1. Executors 常见工厂方法
 * 2. 自定义 ThreadPoolExecutor
 * 3. 提交 Runnable / Callable 任务
 * 4. 使用 ScheduledExecutorService 定时任务
 * 5. 使用 CompletableFuture 进行异步编排
 * 6. 一个并行计算的小示例供测试使用
 *
 * 注意：生产中建议优先使用自定义 ThreadPoolExecutor，而不是 Executors 的快捷工厂方法，
 * 主要是为了显式控制队列长度与拒绝策略，避免 OOM。
 */
public class ThreadPoolDemo {

    /**
     * 自定义线程工厂，给线程命名，便于排查问题。
     */
    private static class NamedThreadFactory implements ThreadFactory {
        private final AtomicInteger index = new AtomicInteger();
        private final String prefix;

        NamedThreadFactory(String prefix) {
            this.prefix = prefix;
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, prefix + index.incrementAndGet());
            // 可以根据需要设置为守护线程: t.setDaemon(false);
            return t;
        }
    }

    /**
     * 构建一个自定义线程池：
     * corePoolSize = 2
     * maximumPoolSize = 4
     * keepAliveTime = 30s （非核心线程空闲回收）
     * workQueue = 容量 2 的 LinkedBlockingQueue
     * rejectedExecutionHandler = CallerRunsPolicy（调用线程执行）
     */
    public static ThreadPoolExecutor buildCustomExecutor() {
        return new ThreadPoolExecutor(
                2,
                4,
                30, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(2),
                new NamedThreadFactory("demo-pool-"),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }

    /**
     * 演示各种线程池的创建与基本使用。
     */
    public static void main(String[] args) throws Exception {
        demoExecutorsFactory();
        demoCustomThreadPool();
        demoScheduled();
        demoCompletableFuture();
    }

    private static void demoExecutorsFactory() throws Exception {
        System.out.println("========== Executors 工厂方法演示 ==========");
        ExecutorService fixed = Executors.newFixedThreadPool(3);
        ExecutorService cached = Executors.newCachedThreadPool();
        ExecutorService single = Executors.newSingleThreadExecutor();

        Future<Integer> f1 = fixed.submit(() -> intensiveCalc(10));
        Future<Integer> f2 = cached.submit(() -> intensiveCalc(20));
        Future<Integer> f3 = single.submit(() -> intensiveCalc(30));

        System.out.println("fixed result: " + f1.get());
        System.out.println("cached result: " + f2.get());
        System.out.println("single result: " + f3.get());

        fixed.shutdown();
        cached.shutdown();
        single.shutdown();
    }

    private static void demoCustomThreadPool() throws InterruptedException, ExecutionException {
        System.out.println("========== 自定义 ThreadPoolExecutor 演示 ==========");
        ThreadPoolExecutor executor = buildCustomExecutor();
        List<Future<String>> futures = new ArrayList<>();
        for (int i = 1; i <= 8; i++) { // 故意提交超过队列与核心线程容量，触发扩容 / 拒绝策略
            int taskId = i;
            futures.add(executor.submit(() -> {
                String name = Thread.currentThread().getName();
                TimeUnit.MILLISECONDS.sleep(200);
                return "task-" + taskId + " executed by " + name;
            }));
        }
        for (Future<String> f : futures) {
            System.out.println(f.get());
        }
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);
    }

    private static void demoScheduled() throws Exception {
        System.out.println("========== ScheduledExecutorService 演示 ==========");
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
        CountDownLatch latch = new CountDownLatch(1);
        scheduler.schedule(() -> {
            System.out.println(LocalTime.now() + " 延迟 500ms 执行的任务: " + Thread.currentThread().getName());
            latch.countDown();
        }, 500, TimeUnit.MILLISECONDS);
        // 等待任务执行完成
        if (!latch.await(2, TimeUnit.SECONDS)) {
            System.out.println("定时任务未在预期时间内完成");
        }
        scheduler.shutdown();
    }

    private static void demoCompletableFuture() throws Exception {
        System.out.println("========== CompletableFuture 异步编排演示 ==========");
        ExecutorService pool = Executors.newFixedThreadPool(4, new NamedThreadFactory("cf-"));
        CompletableFuture<Integer> a = CompletableFuture.supplyAsync(() -> intensiveCalc(5), pool);
        CompletableFuture<Integer> b = CompletableFuture.supplyAsync(() -> intensiveCalc(6), pool);
        CompletableFuture<Integer> c = a.thenCombine(b, Integer::sum)
                .thenApply(sum -> sum * 10);
        System.out.println("(5 + 6) * 10 = " + c.get());
        pool.shutdown();
    }

    /**
     * 一个 CPU 密集型的假计算
     */
    private static int intensiveCalc(int base) {
        int r = 0;
        for (int i = 0; i < 100_000; i++) {
            r = (r + base * 31 + i) ^ (r << 1);
        }
        return Math.abs(r) % 10_000; // 简化输出
    }

    /**
     * 并行计算：对输入整数列表求平方，演示如何用线程池批量提交并收集结果。
     * 提供给测试用。
     */
    public static List<Integer> parallelSquare(List<Integer> numbers) throws InterruptedException {
        if (numbers == null || numbers.isEmpty()) {
            return Collections.emptyList();
        }
        ThreadPoolExecutor executor = buildCustomExecutor();
        try {
            List<Future<Integer>> futures = new ArrayList<>(numbers.size());
            for (Integer n : numbers) {
                if (Objects.nonNull(n)) {
                    futures.add(executor.submit(() -> n * n));
                } else {
                    futures.add(CompletableFuture.completedFuture(null));
                }
            }
            List<Integer> result = new ArrayList<>(numbers.size());
            for (Future<Integer> f : futures) {
                try {
                    result.add(f.get(2, TimeUnit.SECONDS));
                } catch (ExecutionException | TimeoutException e) {
                    result.add(null); // 异常情况下填充 null
                }
            }
            return result;
        } finally {
            executor.shutdown();
            executor.awaitTermination(5, TimeUnit.SECONDS);
        }
    }
}

