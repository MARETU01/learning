package com.learning.collection;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MapDemo {
    
    public static void hashMapDemo() {
        System.out.println("=== HashMap 演示 (基于哈希表，无序) ===");
        Map<String, Integer> hashMap = new HashMap<>();
        
        // 基本操作
        hashMap.put("Apple", 10);
        hashMap.put("Banana", 20);
        hashMap.put("Cherry", 30);
        hashMap.put("Date", 40);
        
        System.out.println("HashMap: " + hashMap);
        System.out.println("大小: " + hashMap.size());
        
        // 获取操作
        System.out.println("Apple的值: " + hashMap.get("Apple"));
        System.out.println("Grape的值(不存在): " + hashMap.get("Grape"));
        System.out.println("Grape的值(默认值): " + hashMap.getOrDefault("Grape", 0));
        
        // 检查操作
        System.out.println("包含键Apple? " + hashMap.containsKey("Apple"));
        System.out.println("包含值20? " + hashMap.containsValue(20));
        
        // 修改操作
        hashMap.put("Apple", 15); // 更新值
        System.out.println("更新Apple后: " + hashMap);
        
        hashMap.replace("Banana", 25); // 替换值
        System.out.println("替换Banana后: " + hashMap);
        
        hashMap.compute("Cherry", (k, v) -> v * 2); // 计算新值
        System.out.println("计算Cherry后: " + hashMap);
        
        // 删除操作
        hashMap.remove("Date");
        System.out.println("删除Date后: " + hashMap);
        
        // 批量操作
        Map<String, Integer> moreItems = Map.of("Elderberry", 50, "Fig", 60);
        hashMap.putAll(moreItems);
        System.out.println("批量添加后: " + hashMap);
        
        // 遍历方式
        System.out.println("遍历键值对:");
        hashMap.forEach((k, v) -> System.out.println(k + " -> " + v));
        
        System.out.println("遍历键:");
        for (String key : hashMap.keySet()) {
            System.out.print(key + " ");
        }
        System.out.println();
        
        System.out.println("遍历值:");
        for (Integer value : hashMap.values()) {
            System.out.print(value + " ");
        }
        System.out.println("\n");
    }
    
    public static void linkedHashMapDemo() {
        System.out.println("=== LinkedHashMap 演示 (维护插入顺序) ===");
        
        // 基本LinkedHashMap（维护插入顺序）
        Map<String, Integer> linkedHashMap = new LinkedHashMap<>();
        linkedHashMap.put("First", 1);
        linkedHashMap.put("Second", 2);
        linkedHashMap.put("Third", 3);
        linkedHashMap.put("Fourth", 4);
        
        System.out.println("LinkedHashMap (插入顺序): " + linkedHashMap);
        
        // 与HashMap对比
        Map<String, Integer> hashMap = new HashMap<>(linkedHashMap);
        System.out.println("相同元素的HashMap (无序): " + hashMap);
        
        // 访问顺序的LinkedHashMap
        Map<String, Integer> accessOrderMap = new LinkedHashMap<>(16, 0.75f, true);
        accessOrderMap.put("A", 1);
        accessOrderMap.put("B", 2);
        accessOrderMap.put("C", 3);
        
        System.out.println("访问顺序LinkedHashMap初始: " + accessOrderMap);
        accessOrderMap.get("B"); // 访问B
        accessOrderMap.get("A"); // 访问A
        System.out.println("访问B和A后: " + accessOrderMap);
        
        // 实现LRU缓存
        Map<String, String> lruCache = new LinkedHashMap<String, String>(3, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, String> eldest) {
                return size() > 3; // 当超过3个元素时删除最老的
            }
        };
        
        lruCache.put("1", "One");
        lruCache.put("2", "Two");
        lruCache.put("3", "Three");
        System.out.println("LRU缓存: " + lruCache);
        
        lruCache.get("1"); // 访问1
        lruCache.put("4", "Four"); // 添加4会删除最老的2
        System.out.println("添加4后的LRU缓存: " + lruCache);
        System.out.println();
    }
    
    public static void treeMapDemo() {
        System.out.println("=== TreeMap 演示 (基于红黑树，有序) ===");
        
        // 默认按键自然排序
        TreeMap<String, Integer> treeMap = new TreeMap<>();
        treeMap.put("Banana", 20);
        treeMap.put("Apple", 10);
        treeMap.put("Date", 40);
        treeMap.put("Cherry", 30);
        
        System.out.println("TreeMap (按键排序): " + treeMap);
        
        // TreeMap特有的导航方法
        System.out.println("第一个键: " + treeMap.firstKey());
        System.out.println("最后一个键: " + treeMap.lastKey());
        System.out.println("小于'Cherry'的最大键: " + treeMap.lowerKey("Cherry"));
        System.out.println("大于'Cherry'的最小键: " + treeMap.higherKey("Cherry"));
        System.out.println("小于等于'Cherry'的最大键: " + treeMap.floorKey("Cherry"));
        System.out.println("大于等于'Cherry'的最小键: " + treeMap.ceilingKey("Cherry"));
        
        // 子映射
        System.out.println("子映射(Apple-Cherry): " + treeMap.subMap("Apple", "Cherry"));
        System.out.println("头部映射(<Banana): " + treeMap.headMap("Banana"));
        System.out.println("尾部映射(>=Banana): " + treeMap.tailMap("Banana"));
        
        // 自定义比较器
        TreeMap<Integer, String> customTreeMap = new TreeMap<>(Collections.reverseOrder());
        customTreeMap.put(1, "One");
        customTreeMap.put(3, "Three");
        customTreeMap.put(2, "Two");
        customTreeMap.put(4, "Four");
        System.out.println("倒序TreeMap: " + customTreeMap);
        
        // 自定义对象作为键
        TreeMap<Person, String> personMap = new TreeMap<>((p1, p2) -> 
            Integer.compare(p1.age, p2.age));
        personMap.put(new Person("Alice", 25), "Engineer");
        personMap.put(new Person("Bob", 30), "Manager");
        personMap.put(new Person("Charlie", 20), "Intern");
        System.out.println("按年龄排序的Person Map: " + personMap);
        System.out.println();
    }
    
    static class Person {
        String name;
        int age;
        
        Person(String name, int age) {
            this.name = name;
            this.age = age;
        }
        
        @Override
        public String toString() {
            return name + "(" + age + ")";
        }
    }
    
    public static void concurrentHashMapDemo() {
        System.out.println("=== ConcurrentHashMap 演示 (线程安全) ===");
        ConcurrentHashMap<String, Integer> concurrentMap = new ConcurrentHashMap<>();
        
        // 基本操作
        concurrentMap.put("Key1", 1);
        concurrentMap.put("Key2", 2);
        concurrentMap.put("Key3", 3);
        
        System.out.println("ConcurrentHashMap: " + concurrentMap);
        
        // 原子操作
        concurrentMap.putIfAbsent("Key1", 100); // 只有当键不存在时才放入
        System.out.println("putIfAbsent后: " + concurrentMap);
        
        concurrentMap.compute("Key2", (k, v) -> v * 10); // 原子计算
        System.out.println("compute后: " + concurrentMap);
        
        concurrentMap.merge("Key3", 5, (oldVal, newVal) -> oldVal + newVal); // 合并值
        System.out.println("merge后: " + concurrentMap);
        
        // 并发遍历
        System.out.println("并发遍历:");
        concurrentMap.forEach((k, v) -> System.out.println(k + " -> " + v));
        
        // 并行操作
        System.out.println("并行计算:");
        int sum = concurrentMap.reduceValuesToInt(1, v -> v, 0, Integer::sum);
        System.out.println("所有值的和: " + sum);
        
        // 搜索操作
        System.out.println("搜索值大于2的键:");
        String foundKey = concurrentMap.searchKeys(1, k -> concurrentMap.get(k) > 2 ? k : null);
        System.out.println("找到的键: " + foundKey);
        System.out.println();
    }
    
    public static void weakHashMapDemo() {
        System.out.println("=== WeakHashMap 演示 (弱引用键) ===");
        
        // 创建强引用的键
        String key1 = new String("StrongKey1");
        String key2 = new String("StrongKey2");
        
        WeakHashMap<String, String> weakMap = new WeakHashMap<>();
        weakMap.put(key1, "Value1");
        weakMap.put(key2, "Value2");
        
        System.out.println("WeakHashMap初始: " + weakMap);
        
        // 移除强引用
        key1 = null;
        key2 = null;
        
        // 建议垃圾回收
        System.gc();
        
        try {
            Thread.sleep(1000); // 给垃圾回收一些时间
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        System.out.println("垃圾回收后WeakHashMap: " + weakMap);
        System.out.println("WeakHashMap适合缓存场景，当键不再被引用时自动清理");
        System.out.println();
    }
    
    public static void enumMapDemo() {
        System.out.println("=== EnumMap 演示 (专门用于枚举键) ===");
        
        enum Day {
            MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY
        }
        
        EnumMap<Day, String> schedule = new EnumMap<>(Day.class);
        schedule.put(Day.MONDAY, "Work");
        schedule.put(Day.TUESDAY, "Work");
        schedule.put(Day.WEDNESDAY, "Meeting");
        schedule.put(Day.THURSDAY, "Work");
        schedule.put(Day.FRIDAY, "Party");
        schedule.put(Day.SATURDAY, "Rest");
        schedule.put(Day.SUNDAY, "Rest");
        
        System.out.println("一周日程安排:");
        schedule.forEach((day, activity) -> System.out.println(day + ": " + activity));
        
        System.out.println("EnumMap特点:");
        System.out.println("1. 键必须是枚举类型");
        System.out.println("2. 内部使用数组存储，效率很高");
        System.out.println("3. 按枚举的自然顺序排序");
        System.out.println("4. 类型安全");
        System.out.println();
    }
    
    public static void mapOperationsDemo() {
        System.out.println("=== Map高级操作演示 ===");
        
        Map<String, Integer> map1 = new HashMap<>(Map.of("A", 1, "B", 2, "C", 3));
        Map<String, Integer> map2 = new HashMap<>(Map.of("B", 2, "C", 4, "D", 5));
        
        System.out.println("Map1: " + map1);
        System.out.println("Map2: " + map2);
        
        // 合并Map
        Map<String, Integer> merged = new HashMap<>(map1);
        map2.forEach((k, v) -> merged.merge(k, v, Integer::sum));
        System.out.println("合并后: " + merged);
        
        // 过滤Map
        Map<String, Integer> filtered = map1.entrySet().stream()
            .filter(entry -> entry.getValue() > 1)
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        System.out.println("过滤后(值>1): " + filtered);
        
        // 转换Map
        Map<String, String> transformed = map1.entrySet().stream()
            .collect(Collectors.toMap(
                entry -> entry.getKey().toLowerCase(),
                entry -> "Value_" + entry.getValue()
            ));
        System.out.println("转换后: " + transformed);
        
        // 分组操作
        List<String> words = Arrays.asList("apple", "banana", "cherry", "date", "elderberry");
        Map<Integer, List<String>> groupedByLength = words.stream()
            .collect(Collectors.groupingBy(String::length));
        System.out.println("按长度分组: " + groupedByLength);
        
        // 扁平化
        Map<String, List<Integer>> data = Map.of(
            "Group1", List.of(1, 2, 3),
            "Group2", List.of(4, 5, 6)
        );
        List<Integer> flattened = data.values().stream()
            .flatMap(List::stream)
            .collect(Collectors.toList());
        System.out.println("扁平化后: " + flattened);
        System.out.println();
    }
    
    public static void performanceComparison() {
        System.out.println("=== Map性能对比 ===");
        int size = 100000;
        
        // HashMap性能
        long start = System.currentTimeMillis();
        Map<Integer, String> hashMap = new HashMap<>();
        for (int i = 0; i < size; i++) {
            hashMap.put(i, "Value" + i);
        }
        for (int i = 0; i < size; i++) {
            hashMap.get(i);
        }
        long end = System.currentTimeMillis();
        System.out.println("HashMap " + size + " 次插入和查找耗时: " + (end - start) + "ms");
        
        // TreeMap性能
        start = System.currentTimeMillis();
        Map<Integer, String> treeMap = new TreeMap<>();
        for (int i = 0; i < size; i++) {
            treeMap.put(i, "Value" + i);
        }
        for (int i = 0; i < size; i++) {
            treeMap.get(i);
        }
        end = System.currentTimeMillis();
        System.out.println("TreeMap " + size + " 次插入和查找耗时: " + (end - start) + "ms");
        
        // LinkedHashMap性能
        start = System.currentTimeMillis();
        Map<Integer, String> linkedHashMap = new LinkedHashMap<>();
        for (int i = 0; i < size; i++) {
            linkedHashMap.put(i, "Value" + i);
        }
        for (int i = 0; i < size; i++) {
            linkedHashMap.get(i);
        }
        end = System.currentTimeMillis();
        System.out.println("LinkedHashMap " + size + " 次插入和查找耗时: " + (end - start) + "ms");
        
        // ConcurrentHashMap性能
        start = System.currentTimeMillis();
        Map<Integer, String> concurrentHashMap = new ConcurrentHashMap<>();
        for (int i = 0; i < size; i++) {
            concurrentHashMap.put(i, "Value" + i);
        }
        for (int i = 0; i < size; i++) {
            concurrentHashMap.get(i);
        }
        end = System.currentTimeMillis();
        System.out.println("ConcurrentHashMap " + size + " 次插入和查找耗时: " + (end - start) + "ms");
        System.out.println();
    }
    
    public static void mapComparison() {
        System.out.println("=== Map实现类对比总结 ===");
        System.out.println("HashMap: 哈希表实现，无序，O(1)操作，允许null键值，非线程安全");
        System.out.println("LinkedHashMap: 维护插入顺序，O(1)操作，可实现LRU缓存");
        System.out.println("TreeMap: 红黑树实现，有序，O(log n)操作，支持导航方法");
        System.out.println("ConcurrentHashMap: 线程安全，分段锁，高并发性能好");
        System.out.println("Hashtable: 线程安全但性能差，已过时，不建议使用");
        System.out.println("WeakHashMap: 弱引用键，适合缓存，自动清理");
        System.out.println("IdentityHashMap: 使用引用相等而非equals，特殊用途");
        System.out.println("EnumMap: 专门用于枚举键，高效，类型安全");
        System.out.println("Properties: 继承Hashtable，用于属性文件，已过时");
        System.out.println();
    }
    
    public static void main(String[] args) {
        hashMapDemo();
        linkedHashMapDemo();
        treeMapDemo();
        concurrentHashMapDemo();
        weakHashMapDemo();
        enumMapDemo();
        mapOperationsDemo();
        performanceComparison();
        mapComparison();
    }
}
