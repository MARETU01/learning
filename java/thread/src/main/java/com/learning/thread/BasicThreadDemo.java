package com.learning.thread;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import static java.util.concurrent.Executors.newSingleThreadExecutor;

public class BasicThreadDemo {
    // 继承Thread类方式
    public static class MyThread extends Thread {
        @Override
        public void run() {
            System.out.println("通过继承Thread类创建线程: " + Thread.currentThread().getName());
        }
    }

    // 实现Runnable接口方式
    public static class MyRunnable implements Runnable {
        @Override
        public void run() {
            System.out.println("通过实现Runnable接口创建线程: " + Thread.currentThread().getName());
        }
    }

    // 实现Callable接口方式
    public static class MyCallable implements Callable<String> {
        @Override
        public String call() {
            return "通过实现Callable接口创建线程: " + Thread.currentThread().getName();
        }
    }

    public static void main(String[] args) throws  InterruptedException, ExecutionException {
        // 演示不同创建线程的方式
        MyThread thread1 = new MyThread();
        // 这里调用的是start()方法，而不是run()方法。
        // start()会启动一个新的线程，并由JVM自动调用run()方法。
        // 如果直接调用run()，不会启动新线程，只会在主线程中执行run()方法。
        thread1.start();
        
        Thread thread2 = new Thread(new MyRunnable());
        thread2.start();

        // 使用Callable和Future方式创建线程并获取返回值
        ExecutorService executor = newSingleThreadExecutor();
        Future<String> future = executor.submit(new MyCallable());
        System.out.println(future.get());
        executor.shutdown();
    }
}