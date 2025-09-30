package com.learning.collection;

import java.util.*;
import java.util.concurrent.*;

public class QueueDemo {
    
    public static void linkedListQueueDemo() {
        System.out.println("=== LinkedList作为Queue演示 (FIFO队列) ===");
        Queue<String> queue = new LinkedList<>();
        
        // 入队操作
        queue.offer("First");   // 推荐使用，返回boolean
        queue.offer("Second");
        queue.offer("Third");
        queue.add("Fourth");    // 也可以使用，但可能抛异常
        
        System.out.println("队列: " + queue);
        System.out.println("队列大小: " + queue.size());
        
        // 查看队首元素（不移除）
        System.out.println("队首元素(peek): " + queue.peek());
        System.out.println("队首元素(element): " + queue.element());
        
        // 出队操作
        System.out.println("出队(poll): " + queue.poll());
        System.out.println("出队(remove): " + queue.remove());
        System.out.println("出队后队列: " + queue);
        
        // 清空队列后的操作对比
        queue.clear();
        System.out.println("空队列poll(): " + queue.poll());      // 返回null
        System.out.println("空队列peek(): " + queue.peek());      // 返回null
        // System.out.println("空队列remove(): " + queue.remove()); // 会抛异常
        // System.out.println("空队列element(): " + queue.element()); // 会抛异常
        System.out.println();
    }
    
    public static void arrayDequeDemo() {
        System.out.println("=== ArrayDeque演示 (双端队列，推荐的Queue实现) ===");
        ArrayDeque<Integer> deque = new ArrayDeque<>();
        
        // 双端操作
        deque.addFirst(2);
        deque.addFirst(1);
        deque.addLast(3);
        deque.addLast(4);
        System.out.println("双端队列: " + deque);
        
        // 队首操作
        System.out.println("移除队首: " + deque.removeFirst());
        System.out.println("查看队首: " + deque.peekFirst());
        
        // 队尾操作
        System.out.println("移除队尾: " + deque.removeLast());
        System.out.println("查看队尾: " + deque.peekLast());
        System.out.println("当前队列: " + deque);
        
        // 作为栈使用
        System.out.println("\n--- ArrayDeque作为栈 ---");
        ArrayDeque<String> stack = new ArrayDeque<>();
        stack.push("Bottom");
        stack.push("Middle");
        stack.push("Top");
        System.out.println("栈: " + stack);
        System.out.println("弹出: " + stack.pop());
        System.out.println("栈顶: " + stack.peek());
        
        // 作为队列使用
        System.out.println("\n--- ArrayDeque作为队列 ---");
        ArrayDeque<String> queue = new ArrayDeque<>();
        queue.offer("First");
        queue.offer("Second");
        queue.offer("Third");
        System.out.println("队列: " + queue);
        System.out.println("出队: " + queue.poll());
        System.out.println("队首: " + queue.peek());
        System.out.println();
    }
    
    public static void priorityQueueDemo() {
        System.out.println("=== PriorityQueue演示 (优先队列，堆实现) ===");
        
        // 默认最小堆
        PriorityQueue<Integer> minHeap = new PriorityQueue<>();
        minHeap.offer(5);
        minHeap.offer(2);
        minHeap.offer(8);
        minHeap.offer(1);
        minHeap.offer(9);
        
        System.out.println("优先队列(最小堆): " + minHeap);
        System.out.println("队首(最小值): " + minHeap.peek());
        
        System.out.println("按优先级出队:");
        while (!minHeap.isEmpty()) {
            System.out.print(minHeap.poll() + " ");
        }
        System.out.println();
        
        // 最大堆（自定义比较器）
        PriorityQueue<Integer> maxHeap = new PriorityQueue<>(Collections.reverseOrder());
        maxHeap.addAll(Arrays.asList(5, 2, 8, 1, 9));
        System.out.println("优先队列(最大堆): " + maxHeap);
        System.out.println("队首(最大值): " + maxHeap.peek());
        
        System.out.println("按优先级出队:");
        while (!maxHeap.isEmpty()) {
            System.out.print(maxHeap.poll() + " ");
        }
        System.out.println();
        
        // 自定义对象优先队列
        PriorityQueue<Task> taskQueue = new PriorityQueue<>((t1, t2) -> 
            Integer.compare(t1.priority, t2.priority));
        
        taskQueue.offer(new Task("Low Priority Task", 3));
        taskQueue.offer(new Task("High Priority Task", 1));
        taskQueue.offer(new Task("Medium Priority Task", 2));
        
        System.out.println("任务队列按优先级执行:");
        while (!taskQueue.isEmpty()) {
            Task task = taskQueue.poll();
            System.out.println(task.name + " (优先级: " + task.priority + ")");
        }
        System.out.println();
    }
    
    // 任务类
    static class Task {
        String name;
        int priority;
        
        Task(String name, int priority) {
            this.name = name;
            this.priority = priority;
        }
    }
    
    public static void blockingQueueDemo() {
        System.out.println("=== BlockingQueue演示 (线程安全的阻塞队列) ===");
        
        // ArrayBlockingQueue - 有界队列
        BlockingQueue<String> arrayBlockingQueue = new ArrayBlockingQueue<>(3);
        
        try {
            arrayBlockingQueue.put("Item1");
            arrayBlockingQueue.put("Item2");
            arrayBlockingQueue.put("Item3");
            
            System.out.println("ArrayBlockingQueue: " + arrayBlockingQueue);
            System.out.println("队列已满? " + (arrayBlockingQueue.remainingCapacity() == 0));
            
            // offer with timeout
            boolean added = arrayBlockingQueue.offer("Item4", 1, TimeUnit.SECONDS);
            System.out.println("超时添加成功? " + added);
            
            // take - 阻塞获取
            System.out.println("取出元素: " + arrayBlockingQueue.take());
            System.out.println("当前队列: " + arrayBlockingQueue);
            
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        // LinkedBlockingQueue - 可选有界队列
        BlockingQueue<Integer> linkedBlockingQueue = new LinkedBlockingQueue<>();
        linkedBlockingQueue.addAll(Arrays.asList(1, 2, 3, 4, 5));
        System.out.println("LinkedBlockingQueue: " + linkedBlockingQueue);
        
        // SynchronousQueue - 同步队列
        SynchronousQueue<String> synchronousQueue = new SynchronousQueue<>();
        System.out.println("SynchronousQueue容量: " + synchronousQueue.size()); // 始终为0
        
        // PriorityBlockingQueue - 无界优先队列
        PriorityBlockingQueue<Integer> priorityBlockingQueue = new PriorityBlockingQueue<>();
        priorityBlockingQueue.addAll(Arrays.asList(3, 1, 4, 1, 5));
        System.out.println("PriorityBlockingQueue: " + priorityBlockingQueue);
        System.out.println();
    }
    
    public static void concurrentLinkedQueueDemo() {
        System.out.println("=== ConcurrentLinkedQueue演示 (无锁并发队列) ===");
        ConcurrentLinkedQueue<String> concurrentQueue = new ConcurrentLinkedQueue<>();
        
        // 基本操作
        concurrentQueue.offer("Concurrent1");
        concurrentQueue.offer("Concurrent2");
        concurrentQueue.offer("Concurrent3");
        
        System.out.println("ConcurrentLinkedQueue: " + concurrentQueue);
        System.out.println("大小: " + concurrentQueue.size()); // 注意：size()操作不是常数时间
        
        // 线程安全的操作
        System.out.println("出队: " + concurrentQueue.poll());
        System.out.println("队首: " + concurrentQueue.peek());
        System.out.println("剩余: " + concurrentQueue);
        
        // 批量操作
        concurrentQueue.addAll(Arrays.asList("Batch1", "Batch2", "Batch3"));
        System.out.println("批量添加后: " + concurrentQueue);
        System.out.println();
    }
    
    public static void dequeOperations() {
        System.out.println("=== Deque接口操作演示 (双端队列) ===");
        Deque<String> deque = new ArrayDeque<>();
        
        // 头部操作
        deque.addFirst("Head1");
        deque.addFirst("Head0");
        
        // 尾部操作
        deque.addLast("Tail1");
        deque.addLast("Tail2");
        
        System.out.println("Deque: " + deque);
        
        // 访问操作
        System.out.println("第一个元素: " + deque.getFirst());
        System.out.println("最后一个元素: " + deque.getLast());
        System.out.println("查看第一个: " + deque.peekFirst());
        System.out.println("查看最后一个: " + deque.peekLast());
        
        // 删除操作
        System.out.println("删除第一个: " + deque.removeFirst());
        System.out.println("删除最后一个: " + deque.removeLast());
        System.out.println("轮询第一个: " + deque.pollFirst());
        System.out.println("轮询最后一个: " + deque.pollLast());
        
        System.out.println("最终Deque: " + deque);
        
        // Deque作为栈和队列的方法映射
        System.out.println("\n--- Deque方法映射 ---");
        System.out.println("栈操作: push() -> addFirst(), pop() -> removeFirst(), peek() -> peekFirst()");
        System.out.println("队列操作: offer() -> addLast(), poll() -> removeFirst(), peek() -> peekFirst()");
        System.out.println();
    }
    
    public static void queueComparison() {
        System.out.println("=== Queue实现类对比总结 ===");
        System.out.println("LinkedList: 实现Queue接口，双向链表，适合频繁插入删除");
        System.out.println("ArrayDeque: 推荐的Queue/Deque实现，基于数组，性能优异");
        System.out.println("PriorityQueue: 优先队列，基于堆，元素按优先级排序");
        System.out.println("ArrayBlockingQueue: 有界阻塞队列，线程安全，适合生产者消费者");
        System.out.println("LinkedBlockingQueue: 可选有界阻塞队列，链表实现");
        System.out.println("SynchronousQueue: 同步队列，容量为0，直接传递");
        System.out.println("PriorityBlockingQueue: 无界优先阻塞队列");
        System.out.println("ConcurrentLinkedQueue: 无锁并发队列，高并发场景");
        System.out.println("DelayQueue: 延时队列，元素到期后才能被取出");
        System.out.println();
    }
    
    public static void performanceComparison() {
        System.out.println("=== Queue性能对比演示 ===");
        int size = 100000;
        
        // ArrayDeque性能
        long start = System.currentTimeMillis();
        Queue<Integer> arrayDeque = new ArrayDeque<>();
        for (int i = 0; i < size; i++) {
            arrayDeque.offer(i);
        }
        for (int i = 0; i < size; i++) {
            arrayDeque.poll();
        }
        long end = System.currentTimeMillis();
        System.out.println("ArrayDeque " + size + " 次入队出队耗时: " + (end - start) + "ms");
        
        // LinkedList性能
        start = System.currentTimeMillis();
        Queue<Integer> linkedList = new LinkedList<>();
        for (int i = 0; i < size; i++) {
            linkedList.offer(i);
        }
        for (int i = 0; i < size; i++) {
            linkedList.poll();
        }
        end = System.currentTimeMillis();
        System.out.println("LinkedList " + size + " 次入队出队耗时: " + (end - start) + "ms");
        
        // PriorityQueue性能
        start = System.currentTimeMillis();
        PriorityQueue<Integer> priorityQueue = new PriorityQueue<>();
        for (int i = 0; i < size; i++) {
            priorityQueue.offer(i);
        }
        for (int i = 0; i < size; i++) {
            priorityQueue.poll();
        }
        end = System.currentTimeMillis();
        System.out.println("PriorityQueue " + size + " 次入队出队耗时: " + (end - start) + "ms");
        System.out.println();
    }
    
    public static void main(String[] args) {
        linkedListQueueDemo();
        arrayDequeDemo();
        priorityQueueDemo();
        blockingQueueDemo();
        concurrentLinkedQueueDemo();
        dequeOperations();
        queueComparison();
        performanceComparison();
    }
}