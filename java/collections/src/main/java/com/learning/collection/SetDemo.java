package com.learning.collection;

import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;

public class SetDemo {
    
    public static void hashSetDemo() {
        System.out.println("=== HashSet 演示 (基于哈希表，无序，不重复) ===");
        Set<String> hashSet = new HashSet<>();
        
        // 基本操作
        hashSet.add("Red");
        hashSet.add("Blue");
        hashSet.add("Green");
        hashSet.add("Yellow");
        boolean added = hashSet.add("Red"); // 重复元素不会被添加
        
        System.out.println("HashSet: " + hashSet);
        System.out.println("添加重复元素Red成功? " + added);
        System.out.println("包含Red? " + hashSet.contains("Red"));
        System.out.println("大小: " + hashSet.size());
        
        // 集合操作
        Set<String> otherSet = new HashSet<>(Arrays.asList("Blue", "Purple", "Orange"));
        System.out.println("另一个集合: " + otherSet);
        
        // 并集 (使用副本以保持原集合不变)
        Set<String> union = new HashSet<>(hashSet);
        union.addAll(otherSet);
        System.out.println("并集: " + union);
        
        // 交集
        Set<String> intersection = new HashSet<>(hashSet);
        intersection.retainAll(otherSet);
        System.out.println("交集: " + intersection);
        
        // 差集
        Set<String> difference = new HashSet<>(hashSet);
        difference.removeAll(otherSet);
        System.out.println("差集(hashSet-otherSet): " + difference);
        
        // 遍历方式
        System.out.println("遍历HashSet:");
        hashSet.forEach(color -> System.out.print(color + " "));
        System.out.println("\n");
    }
    
    public static void treeSetDemo() {
        System.out.println("=== TreeSet 演示 (基于红黑树，有序，不重复) ===");
        Set<Integer> treeSet = new TreeSet<>();
        
        // 添加元素（会自动排序）
        treeSet.add(5);
        treeSet.add(2);
        treeSet.add(8);
        treeSet.add(1);
        treeSet.add(9);
        treeSet.add(3);
        
        System.out.println("TreeSet (自动排序): " + treeSet);
        
        // TreeSet特有的导航方法
        TreeSet<Integer> navTreeSet = new TreeSet<>(treeSet);
        System.out.println("第一个元素: " + navTreeSet.first());
        System.out.println("最后一个元素: " + navTreeSet.last());
        System.out.println("小于5的最大元素: " + navTreeSet.lower(5));
        System.out.println("大于5的最小元素: " + navTreeSet.higher(5));
        System.out.println("小于等于4的最大元素: " + navTreeSet.floor(4));
        System.out.println("大于等于4的最小元素: " + navTreeSet.ceiling(4));
        
        // 子集操作
        System.out.println("子集(3到8): " + navTreeSet.subSet(3, 9));
        System.out.println("头部集合(小于5): " + navTreeSet.headSet(5));
        System.out.println("尾部集合(大于等于5): " + navTreeSet.tailSet(5));
        
        // 自定义排序
        TreeSet<String> customTreeSet = new TreeSet<>((s1, s2) -> s2.compareTo(s1)); // 倒序
        customTreeSet.addAll(Arrays.asList("Apple", "Banana", "Cherry", "Date"));
        System.out.println("自定义倒序TreeSet: " + customTreeSet);
        
        // 删除操作
        System.out.println("删除并返回第一个元素: " + navTreeSet.pollFirst());
        System.out.println("删除并返回最后一个元素: " + navTreeSet.pollLast());
        System.out.println("删除后的TreeSet: " + navTreeSet);
        System.out.println();
    }
    
    public static void linkedHashSetDemo() {
        System.out.println("=== LinkedHashSet 演示 (维护插入顺序的HashSet) ===");
        Set<String> linkedHashSet = new LinkedHashSet<>();
        
        linkedHashSet.add("First");
        linkedHashSet.add("Second");
        linkedHashSet.add("Third");
        linkedHashSet.add("Fourth");
        linkedHashSet.add("Second"); // 重复元素
        
        System.out.println("LinkedHashSet (保持插入顺序): " + linkedHashSet);
        
        // 与HashSet对比
        Set<String> hashSet = new HashSet<>(Arrays.asList("First", "Second", "Third", "Fourth"));
        System.out.println("相同元素的HashSet (无序): " + hashSet);
        
        // 遍历顺序比较
        System.out.println("LinkedHashSet遍历:");
        linkedHashSet.forEach(item -> System.out.print(item + " "));
        System.out.println("\nHashSet遍历:");
        hashSet.forEach(item -> System.out.print(item + " "));
        System.out.println("\n");
    }
    
    public static void enumSetDemo() {
        System.out.println("=== EnumSet 演示 (专门用于枚举类型的Set) ===");
        
        // 定义一个枚举
        enum Day {
            MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY
        }
        
        // 创建EnumSet的不同方式
        EnumSet<Day> weekdays = EnumSet.range(Day.MONDAY, Day.FRIDAY);
        System.out.println("工作日: " + weekdays);
        
        EnumSet<Day> weekend = EnumSet.of(Day.SATURDAY, Day.SUNDAY);
        System.out.println("周末: " + weekend);
        
        EnumSet<Day> allDays = EnumSet.allOf(Day.class);
        System.out.println("所有日期: " + allDays);
        
        EnumSet<Day> noDays = EnumSet.noneOf(Day.class);
        System.out.println("空集合: " + noDays);
        
        // 补集
        EnumSet<Day> notWeekend = EnumSet.complementOf(weekend);
        System.out.println("非周末: " + notWeekend);
        
        // EnumSet的高效性
        System.out.println("EnumSet使用位向量实现，非常高效");
        System.out.println();
    }
    
    public static void copyOnWriteArraySetDemo() {
        System.out.println("=== CopyOnWriteArraySet 演示 (线程安全的Set) ===");
        CopyOnWriteArraySet<String> cowSet = new CopyOnWriteArraySet<>();
        
        cowSet.add("ThreadSafe1");
        cowSet.add("ThreadSafe2");
        cowSet.add("ThreadSafe3");
        cowSet.add("ThreadSafe1"); // 重复元素
        
        System.out.println("CopyOnWriteArraySet: " + cowSet);
        System.out.println("大小: " + cowSet.size());
        
        // 适合读多写少的场景
        System.out.println("适合读多写少的并发场景");
        for (String item : cowSet) {
            System.out.println("读取: " + item);
        }
        System.out.println();
    }
    
    public static void setOperations() {
        System.out.println("=== Set集合运算演示 ===");
        
        Set<Integer> setA = new HashSet<>(Arrays.asList(1, 2, 3, 4, 5));
        Set<Integer> setB = new HashSet<>(Arrays.asList(4, 5, 6, 7, 8));
        
        System.out.println("集合A: " + setA);
        System.out.println("集合B: " + setB);
        
        // 并集 (Union): A ∪ B
        Set<Integer> union = new HashSet<>(setA);
        union.addAll(setB);
        System.out.println("并集 A ∪ B: " + union);
        
        // 交集 (Intersection): A ∩ B
        Set<Integer> intersection = new HashSet<>(setA);
        intersection.retainAll(setB);
        System.out.println("交集 A ∩ B: " + intersection);
        
        // 差集 (Difference): A - B
        Set<Integer> differenceAB = new HashSet<>(setA);
        differenceAB.removeAll(setB);
        System.out.println("差集 A - B: " + differenceAB);
        
        // 差集 (Difference): B - A
        Set<Integer> differenceBA = new HashSet<>(setB);
        differenceBA.removeAll(setA);
        System.out.println("差集 B - A: " + differenceBA);
        
        // 对称差集 (Symmetric Difference): (A ∪ B) - (A ∩ B)
        Set<Integer> symmetricDiff = new HashSet<>(union);
        symmetricDiff.removeAll(new HashSet<>(setA) {{ retainAll(setB); }});
        System.out.println("对称差集 (A ∪ B) - (A ∩ B): " + symmetricDiff);
        
        // 子集判断
        Set<Integer> subset = new HashSet<>(Arrays.asList(1, 2, 3));
        System.out.println("集合{1,2,3}是A的子集? " + setA.containsAll(subset));
        
        // 不相交判断
        Set<Integer> disjointSet = new HashSet<>(Arrays.asList(9, 10));
        System.out.println("集合{9,10}与A不相交? " + Collections.disjoint(setA, disjointSet));
        System.out.println();
    }
    
    public static void performanceComparison() {
        System.out.println("=== Set性能对比 ===");
        int size = 100000;
        
        // HashSet添加性能
        long start = System.currentTimeMillis();
        Set<Integer> hashSet = new HashSet<>();
        for (int i = 0; i < size; i++) {
            hashSet.add(i);
        }
        long end = System.currentTimeMillis();
        System.out.println("HashSet添加 " + size + " 个元素耗时: " + (end - start) + "ms");
        
        // TreeSet添加性能
        start = System.currentTimeMillis();
        Set<Integer> treeSet = new TreeSet<>();
        for (int i = 0; i < size; i++) {
            treeSet.add(i);
        }
        end = System.currentTimeMillis();
        System.out.println("TreeSet添加 " + size + " 个元素耗时: " + (end - start) + "ms");
        
        // LinkedHashSet添加性能
        start = System.currentTimeMillis();
        Set<Integer> linkedHashSet = new LinkedHashSet<>();
        for (int i = 0; i < size; i++) {
            linkedHashSet.add(i);
        }
        end = System.currentTimeMillis();
        System.out.println("LinkedHashSet添加 " + size + " 个元素耗时: " + (end - start) + "ms");
        
        // 查找性能对比
        Random random = new Random();
        start = System.currentTimeMillis();
        for (int i = 0; i < 10000; i++) {
            hashSet.contains(random.nextInt(size));
        }
        end = System.currentTimeMillis();
        System.out.println("HashSet查找10000次耗时: " + (end - start) + "ms");
        
        start = System.currentTimeMillis();
        for (int i = 0; i < 10000; i++) {
            treeSet.contains(random.nextInt(size));
        }
        end = System.currentTimeMillis();
        System.out.println("TreeSet查找10000次耗时: " + (end - start) + "ms");
        System.out.println();
    }
    
    public static void setComparison() {
        System.out.println("=== Set实现类对比总结 ===");
        System.out.println("HashSet: 基于哈希表，无序，O(1)操作，允许null，非线程安全");
        System.out.println("TreeSet: 基于红黑树，有序，O(log n)操作，不允许null，非线程安全");
        System.out.println("LinkedHashSet: 基于哈希表+链表，维护插入顺序，O(1)操作");
        System.out.println("EnumSet: 专用于枚举，基于位向量，极高效");
        System.out.println("CopyOnWriteArraySet: 线程安全，适合读多写少场景");
        System.out.println();
    }
    
    public static void main(String[] args) {
        hashSetDemo();
        treeSetDemo();
        linkedHashSetDemo();
        enumSetDemo();
        copyOnWriteArraySetDemo();
        setOperations();
        performanceComparison();
        setComparison();
    }
}