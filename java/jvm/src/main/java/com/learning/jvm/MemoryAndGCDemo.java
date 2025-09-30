package com.learning.jvm;

import java.lang.management.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * JVM内存模型与垃圾回收演示
 */
public class MemoryAndGCDemo {
    
    public static void memoryAreasDemo() {
        System.out.println("=== JVM内存区域演示 ===");
        
        // 获取内存管理信息
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        
        // 堆内存信息
        MemoryUsage heapMemory = memoryMXBean.getHeapMemoryUsage();
        System.out.println("堆内存使用情况:");
        printMemoryUsage("堆内存", heapMemory);
        
        // 非堆内存信息（方法区、代码缓存等）
        MemoryUsage nonHeapMemory = memoryMXBean.getNonHeapMemoryUsage();
        System.out.println("\n非堆内存使用情况:");
        printMemoryUsage("非堆内存", nonHeapMemory);
        
        // 详细的内存池信息
        List<MemoryPoolMXBean> memoryPools = ManagementFactory.getMemoryPoolMXBeans();
        System.out.println("\n详细内存池信息:");
        for (MemoryPoolMXBean pool : memoryPools) {
            System.out.println("内存池: " + pool.getName());
            System.out.println("  类型: " + pool.getType());
            if (pool.isUsageThresholdSupported()) {
                MemoryUsage usage = pool.getUsage();
                if (usage != null) {
                    printMemoryUsage("  " + pool.getName(), usage);
                }
            }
            System.out.println();
        }
    }
    
    private static void printMemoryUsage(String name, MemoryUsage usage) {
        System.out.println(name + ":");
        System.out.println("  初始大小: " + formatBytes(usage.getInit()));
        System.out.println("  已使用: " + formatBytes(usage.getUsed()));
        System.out.println("  已提交: " + formatBytes(usage.getCommitted()));
        System.out.println("  最大大小: " + formatBytes(usage.getMax()));
        if (usage.getMax() > 0) {
            double usagePercent = (double) usage.getUsed() / usage.getMax() * 100;
            System.out.println("  使用率: " + String.format("%.2f%%", usagePercent));
        }
    }
    
    private static String formatBytes(long bytes) {
        if (bytes == -1) return "未定义";
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.2f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.2f MB", bytes / (1024.0 * 1024));
        return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
    }
    
    public static void gcBehaviorDemo() {
        System.out.println("=== 垃圾回收行为演示 ===");
        
        // 获取GC信息
        List<GarbageCollectorMXBean> gcBeans = ManagementFactory.getGarbageCollectorMXBeans();
        
        System.out.println("当前JVM的垃圾收集器:");
        for (GarbageCollectorMXBean gcBean : gcBeans) {
            System.out.println("GC名称: " + gcBean.getName());
            System.out.println("  内存池: " + Arrays.toString(gcBean.getMemoryPoolNames()));
            System.out.println("  收集次数: " + gcBean.getCollectionCount());
            System.out.println("  收集总时间: " + gcBean.getCollectionTime() + "ms");
            System.out.println();
        }
        
        // 记录GC前的状态
        long beforeCollections = getTotalGCCollections();
        long beforeTime = getTotalGCTime();
        
        System.out.println("开始创建对象触发GC...");
        
        // 创建大量对象触发GC
        createLotsOfObjects();
        
        // 显式触发GC
        System.gc();
        
        // 等待GC完成
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // 记录GC后的状态
        long afterCollections = getTotalGCCollections();
        long afterTime = getTotalGCTime();
        
        System.out.println("GC统计:");
        System.out.println("  发生GC次数: " + (afterCollections - beforeCollections));
        System.out.println("  GC耗时: " + (afterTime - beforeTime) + "ms");
    }
    
    private static void createLotsOfObjects() {
        List<byte[]> objects = new ArrayList<>();
        try {
            for (int i = 0; i < 1000; i++) {
                // 创建1MB的字节数组
                byte[] bigObject = new byte[1024 * 1024];
                objects.add(bigObject);
                
                if (i % 100 == 0) {
                    System.out.println("已创建 " + (i + 1) + " 个1MB对象");
                }
            }
        } catch (OutOfMemoryError e) {
            System.out.println("内存不足，已创建 " + objects.size() + " 个对象");
        }
        
        // 清除引用，使对象可被回收
        objects.clear();
    }
    
    private static long getTotalGCCollections() {
        return ManagementFactory.getGarbageCollectorMXBeans()
                .stream()
                .mapToLong(GarbageCollectorMXBean::getCollectionCount)
                .sum();
    }
    
    private static long getTotalGCTime() {
        return ManagementFactory.getGarbageCollectorMXBeans()
                .stream()
                .mapToLong(GarbageCollectorMXBean::getCollectionTime)
                .sum();
    }
    
    public static void referenceTypesDemo() {
        System.out.println("=== Java引用类型演示 ===");
        
        // 强引用 - 永远不会被GC回收（除非显式设为null）
        System.out.println("1. 强引用演示:");
        String strongRef = new String("强引用对象");
        System.out.println("强引用对象: " + strongRef);
        
        // 软引用 - 内存不足时才被回收
        System.out.println("\n2. 软引用演示:");
        java.lang.ref.SoftReference<String> softRef = new java.lang.ref.SoftReference<>(new String("软引用对象"));
        System.out.println("软引用对象: " + softRef.get());
        
        // 弱引用 - 下次GC时被回收
        System.out.println("\n3. 弱引用演示:");
        java.lang.ref.WeakReference<String> weakRef = new java.lang.ref.WeakReference<>(new String("弱引用对象"));
        System.out.println("GC前弱引用对象: " + weakRef.get());
        
        System.gc();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        System.out.println("GC后弱引用对象: " + weakRef.get());
        
        // 虚引用 - 用于跟踪对象被GC的时机
        System.out.println("\n4. 虚引用演示:");
        java.lang.ref.ReferenceQueue<String> queue = new java.lang.ref.ReferenceQueue<>();
        java.lang.ref.PhantomReference<String> phantomRef = 
            new java.lang.ref.PhantomReference<>(new String("虚引用对象"), queue);
        
        System.out.println("虚引用对象总是null: " + phantomRef.get());
        System.out.println("引用队列是否为空: " + (queue.poll() == null));
    }
    
    public static void memoryLeakDemo() {
        System.out.println("=== 内存泄漏演示 ===");
        
        System.out.println("注意：以下是内存泄漏的常见模式，仅用于学习！");
        
        // 1. 集合类内存泄漏
        System.out.println("\n1. 集合类内存泄漏模式:");
        Map<String, Object> cache = new HashMap<>();
        
        // 模拟不断添加到缓存但从不清理
        for (int i = 0; i < 10; i++) {
            cache.put("key" + i, new byte[1024]); // 1KB对象
        }
        System.out.println("缓存大小: " + cache.size());
        
        // 正确做法：定期清理或使用弱引用
        cache.clear();
        System.out.println("清理后缓存大小: " + cache.size());
        
        // 2. 监听器未移除
        System.out.println("\n2. 监听器内存泄漏模式:");
        EventSource eventSource = new EventSource();
        EventListener listener = new EventListener() {
            @Override
            public void onEvent(String event) {
                System.out.println("收到事件: " + event);
            }
        };
        
        eventSource.addListener(listener);
        System.out.println("监听器数量: " + eventSource.getListenerCount());
        
        // 正确做法：记得移除监听器
        eventSource.removeListener(listener);
        System.out.println("移除后监听器数量: " + eventSource.getListenerCount());
        
        // 3. 静态集合持有对象引用
        System.out.println("\n3. 静态集合内存泄漏模式:");
        StaticHolder.addObject(new Object());
        System.out.println("静态集合大小: " + StaticHolder.getSize());
        
        // 注意：静态集合在程序生命周期内不会被回收
        StaticHolder.clear();
        System.out.println("清理后静态集合大小: " + StaticHolder.getSize());
    }
    
    // 事件源类
    static class EventSource {
        private List<EventListener> listeners = new ArrayList<>();
        
        public void addListener(EventListener listener) {
            listeners.add(listener);
        }
        
        public void removeListener(EventListener listener) {
            listeners.remove(listener);
        }
        
        public int getListenerCount() {
            return listeners.size();
        }
    }
    
    // 事件监听器接口
    interface EventListener {
        void onEvent(String event);
    }
    
    // 静态持有者类
    static class StaticHolder {
        private static List<Object> objects = new ArrayList<>();
        
        public static void addObject(Object obj) {
            objects.add(obj);
        }
        
        public static int getSize() {
            return objects.size();
        }
        
        public static void clear() {
            objects.clear();
        }
    }
    
    public static void outOfMemoryDemo() {
        System.out.println("=== 内存溢出类型演示 ===");
        
        System.out.println("注意：以下演示可能导致程序崩溃，请在测试环境运行！");
        
        // 1. 堆内存溢出 (java.lang.OutOfMemoryError: Java heap space)
        System.out.println("\n1. 堆内存溢出演示（已注释，取消注释需谨慎）:");
        System.out.println("// List<byte[]> list = new ArrayList<>();");
        System.out.println("// while (true) {");
        System.out.println("//     list.add(new byte[1024 * 1024]); // 1MB");
        System.out.println("// }");
        
        /*
        // 取消注释以下代码来演示堆内存溢出
        try {
            List<byte[]> list = new ArrayList<>();
            while (true) {
                list.add(new byte[1024 * 1024]); // 1MB
            }
        } catch (OutOfMemoryError e) {
            System.out.println("堆内存溢出: " + e.getMessage());
        }
        */
        
        // 2. 栈溢出 (java.lang.StackOverflowError)
        System.out.println("\n2. 栈溢出演示:");
        try {
            recursiveMethod(0);
        } catch (StackOverflowError e) {
            System.out.println("栈溢出: " + e.getClass().getSimpleName());
        }
        
        // 3. 方法区溢出演示（较难触发，在较新的JVM中）
        System.out.println("\n3. 方法区溢出演示（需要特殊JVM参数）:");
        System.out.println("可以通过动态生成大量类或使用大量常量来触发");
        System.out.println("JVM参数示例: -XX:MetaspaceSize=10m -XX:MaxMetaspaceSize=10m");
    }
    
    private static void recursiveMethod(int depth) {
        if (depth % 1000 == 0) {
            System.out.println("递归深度: " + depth);
        }
        recursiveMethod(depth + 1);
    }
    
    public static void gcTuningTips() {
        System.out.println("=== GC调优建议 ===");
        
        System.out.println("1. 监控GC指标:");
        System.out.println("   - GC频率和耗时");
        System.out.println("   - 各内存区域使用率");
        System.out.println("   - 应用暂停时间");
        
        System.out.println("\n2. 常用GC参数:");
        System.out.println("   堆大小设置:");
        System.out.println("     -Xms<size>    初始堆大小");
        System.out.println("     -Xmx<size>    最大堆大小");
        System.out.println("     -Xmn<size>    年轻代大小");
        
        System.out.println("\n   GC收集器选择:");
        System.out.println("     -XX:+UseG1GC           使用G1收集器");
        System.out.println("     -XX:+UseZGC            使用ZGC收集器(JDK11+)");
        System.out.println("     -XX:+UseParallelGC     使用并行收集器");
        
        System.out.println("\n   GC日志:");
        System.out.println("     -Xlog:gc*:gc.log       记录GC日志");
        System.out.println("     -XX:+PrintGCDetails    打印GC详细信息");
        
        System.out.println("\n3. 调优策略:");
        System.out.println("   - 根据应用特性选择合适的GC算法");
        System.out.println("   - 合理设置堆大小，避免过小或过大");
        System.out.println("   - 监控和分析GC日志");
        System.out.println("   - 减少对象创建，复用对象");
        System.out.println("   - 避免内存泄漏");
    }
    
    public static void main(String[] args) {
        System.out.println("JVM内存和GC学习演示");
        System.out.println("====================");
        
        memoryAreasDemo();
        System.out.println("\n" + "=".repeat(50) + "\n");
        
        gcBehaviorDemo();
        System.out.println("\n" + "=".repeat(50) + "\n");
        
        referenceTypesDemo();
        System.out.println("\n" + "=".repeat(50) + "\n");
        
        memoryLeakDemo();
        System.out.println("\n" + "=".repeat(50) + "\n");
        
        outOfMemoryDemo();
        System.out.println("\n" + "=".repeat(50) + "\n");
        
        gcTuningTips();
    }
}