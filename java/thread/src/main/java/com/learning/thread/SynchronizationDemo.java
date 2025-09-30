package com.learning.thread;

public class SynchronizationDemo {
    
    private int counter = 0;
    
    public synchronized void increment() {
        counter++;
    }
    
    public synchronized int getCounter() {
        return counter;
    }
    
    public static void main(String[] args) throws InterruptedException {
        // TODO: 演示线程同步
        SynchronizationDemo demo = new SynchronizationDemo();
        
        Thread t1 = new Thread(() -> {
            for (int i = 0; i < 1000; i++) {
                System.out.println("Thread-1: " + i);
                demo.increment();
            }
        });
        
        Thread t2 = new Thread(() -> {
            for (int i = 0; i < 1000; i++) {
                System.out.println("Thread-2: " + i);
                demo.increment();
            }
        });
        
        t1.start();
        t2.start();
        
        t1.join();
        t2.join();
        
        System.out.println("最终计数器值: " + demo.getCounter());
    }
}