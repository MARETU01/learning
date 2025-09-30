package com.learning.collection;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class IteratorDemo {
    
    public static void basicIteratorDemo() {
        System.out.println("=== 基本Iterator演示 ===");
        List<String> list = new ArrayList<>(Arrays.asList("Apple", "Banana", "Cherry", "Date"));
        
        System.out.println("原始列表: " + list);
        
        // 使用Iterator遍历
        System.out.println("使用Iterator遍历:");
        Iterator<String> iterator = list.iterator();
        while (iterator.hasNext()) {
            String item = iterator.next();
            System.out.print(item + " ");
        }
        System.out.println();
        
        // 使用Iterator安全删除
        System.out.println("删除包含'a'的元素:");
        iterator = list.iterator();
        while (iterator.hasNext()) {
            String item = iterator.next();
            if (item.toLowerCase().contains("a")) {
                iterator.remove(); // 安全删除
                System.out.println("删除了: " + item);
            }
        }
        System.out.println("删除后的列表: " + list);
        System.out.println();
    }
    
    public static void listIteratorDemo() {
        System.out.println("=== ListIterator演示 (支持双向遍历和修改) ===");
        List<Integer> list = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5));
        
        System.out.println("原始列表: " + list);
        
        // 正向遍历
        System.out.println("正向遍历:");
        ListIterator<Integer> listIterator = list.listIterator();
        while (listIterator.hasNext()) {
            int index = listIterator.nextIndex();
            int value = listIterator.next();
            System.out.println("索引 " + index + ": " + value);
        }
        
        // 反向遍历
        System.out.println("反向遍历:");
        while (listIterator.hasPrevious()) {
            int index = listIterator.previousIndex();
            int value = listIterator.previous();
            System.out.println("索引 " + index + ": " + value);
        }
        
        // 从指定位置开始的ListIterator
        System.out.println("从索引2开始的ListIterator:");
        ListIterator<Integer> midIterator = list.listIterator(2);
        while (midIterator.hasNext()) {
            System.out.print(midIterator.next() + " ");
        }
        System.out.println();
        
        // 使用ListIterator修改列表
        System.out.println("使用ListIterator修改列表:");
        listIterator = list.listIterator();
        while (listIterator.hasNext()) {
            int value = listIterator.next();
            if (value % 2 == 0) {
                listIterator.set(value * 10); // 偶数乘以10
            } else {
                listIterator.add(value + 100); // 奇数后插入+100的值
            }
        }
        System.out.println("修改后的列表: " + list);
        System.out.println();
    }
    
    public static void enhancedForLoopDemo() {
        System.out.println("=== 增强for循环演示 (语法糖) ===");
        List<String> fruits = Arrays.asList("Apple", "Banana", "Cherry");
        
        // 增强for循环（实际上使用Iterator）
        System.out.println("增强for循环遍历:");
        for (String fruit : fruits) {
            System.out.print(fruit + " ");
        }
        System.out.println();
        
        // 对比传统for循环
        System.out.println("传统for循环遍历:");
        for (int i = 0; i < fruits.size(); i++) {
            System.out.print(fruits.get(i) + " ");
        }
        System.out.println();
        
        // 对比Iterator
        System.out.println("Iterator遍历:");
        Iterator<String> it = fruits.iterator();
        while (it.hasNext()) {
            System.out.print(it.next() + " ");
        }
        System.out.println();
        
        // 注意：增强for循环无法修改集合
        System.out.println("增强for循环的限制：无法在遍历时修改集合");
        System.out.println();
    }
    
    public static void concurrentModificationDemo() {
        System.out.println("=== ConcurrentModificationException演示 ===");
        List<String> list = new ArrayList<>(Arrays.asList("A", "B", "C", "D", "E"));
        
        System.out.println("原始列表: " + list);
        
        // 错误的做法 - 会抛出ConcurrentModificationException
        System.out.println("错误做法（会抛异常）:");
        try {
            for (String item : list) {
                if (item.equals("C")) {
                    list.remove(item); // 这会导致ConcurrentModificationException
                }
            }
        } catch (ConcurrentModificationException e) {
            System.out.println("捕获到异常: " + e.getClass().getSimpleName());
        }
        
        // 正确的做法1 - 使用Iterator
        System.out.println("正确做法1 - 使用Iterator:");
        list = new ArrayList<>(Arrays.asList("A", "B", "C", "D", "E"));
        Iterator<String> iterator = list.iterator();
        while (iterator.hasNext()) {
            String item = iterator.next();
            if (item.equals("C")) {
                iterator.remove(); // 使用Iterator的remove方法
            }
        }
        System.out.println("删除C后: " + list);
        
        // 正确的做法2 - 收集要删除的元素
        System.out.println("正确做法2 - 收集要删除的元素:");
        list = new ArrayList<>(Arrays.asList("A", "B", "C", "D", "E"));
        List<String> toRemove = new ArrayList<>();
        for (String item : list) {
            if (item.equals("B") || item.equals("D")) {
                toRemove.add(item);
            }
        }
        list.removeAll(toRemove);
        System.out.println("删除B和D后: " + list);
        
        // 正确的做法3 - 使用removeIf (Java 8+)
        System.out.println("正确做法3 - 使用removeIf:");
        list = new ArrayList<>(Arrays.asList("A", "B", "C", "D", "E"));
        list.removeIf(item -> item.equals("A") || item.equals("E"));
        System.out.println("删除A和E后: " + list);
        System.out.println();
    }
    
    public static void failSafeIteratorDemo() {
        System.out.println("=== Fail-Safe Iterator演示 ===");
        
        // CopyOnWriteArrayList提供fail-safe iterator
        CopyOnWriteArrayList<String> cowList = new CopyOnWriteArrayList<>(
            Arrays.asList("Item1", "Item2", "Item3", "Item4")
        );
        
        System.out.println("CopyOnWriteArrayList: " + cowList);
        
        // 在迭代过程中修改集合不会抛出异常
        System.out.println("在迭代过程中修改集合:");
        Iterator<String> cowIterator = cowList.iterator();
        while (cowIterator.hasNext()) {
            String item = cowIterator.next();
            System.out.println("当前元素: " + item);
            
            if (item.equals("Item2")) {
                cowList.add("NewItem"); // 在迭代过程中添加元素
                System.out.println("添加了新元素");
            }
        }
        
        System.out.println("修改后的列表: " + cowList);
        
        // 但是Iterator看到的是快照
        System.out.println("新的Iterator会看到更新后的集合:");
        for (String item : cowList) {
            System.out.print(item + " ");
        }
        System.out.println("\n");
    }
    
    public static void customIteratorDemo() {
        System.out.println("=== 自定义Iterable类演示 ===");
        NumberRange range = new NumberRange(1, 5);
        
        System.out.println("遍历NumberRange(1-5):");
        for (int number : range) {
            System.out.print(number + " ");
        }
        System.out.println();
        
        // 使用Iterator
        System.out.println("使用Iterator遍历:");
        Iterator<Integer> iterator = range.iterator();
        while (iterator.hasNext()) {
            System.out.print(iterator.next() + " ");
        }
        System.out.println("\n");
    }
    
    // 自定义可迭代类
    static class NumberRange implements Iterable<Integer> {
        private int start;
        private int end;
        
        public NumberRange(int start, int end) {
            this.start = start;
            this.end = end;
        }
        
        @Override
        public Iterator<Integer> iterator() {
            return new NumberIterator();
        }
        
        private class NumberIterator implements Iterator<Integer> {
            private int current = start;
            
            @Override
            public boolean hasNext() {
                return current <= end;
            }
            
            @Override
            public Integer next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                return current++;
            }
        }
    }
    
    public static void iteratorPerformanceDemo() {
        System.out.println("=== Iterator性能对比 ===");
        int size = 100000;
        
        // ArrayList
        List<Integer> arrayList = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            arrayList.add(i);
        }
        
        // Iterator遍历
        long start = System.currentTimeMillis();
        Iterator<Integer> iterator = arrayList.iterator();
        while (iterator.hasNext()) {
            iterator.next();
        }
        long end = System.currentTimeMillis();
        System.out.println("ArrayList Iterator遍历耗时: " + (end - start) + "ms");
        
        // 增强for循环遍历
        start = System.currentTimeMillis();
        for (Integer item : arrayList) {
            // 空操作
        }
        end = System.currentTimeMillis();
        System.out.println("ArrayList 增强for循环遍历耗时: " + (end - start) + "ms");
        
        // 传统for循环遍历
        start = System.currentTimeMillis();
        for (int i = 0; i < arrayList.size(); i++) {
            arrayList.get(i);
        }
        end = System.currentTimeMillis();
        System.out.println("ArrayList 传统for循环遍历耗时: " + (end - start) + "ms");
        
        // LinkedList比较
        LinkedList<Integer> linkedList = new LinkedList<>(arrayList);
        
        // Iterator遍历LinkedList
        start = System.currentTimeMillis();
        iterator = linkedList.iterator();
        while (iterator.hasNext()) {
            iterator.next();
        }
        end = System.currentTimeMillis();
        System.out.println("LinkedList Iterator遍历耗时: " + (end - start) + "ms");
        
        // 传统for循环遍历LinkedList（很慢）
        start = System.currentTimeMillis();
        for (int i = 0; i < Math.min(linkedList.size(), 10000); i++) { // 只测试前10000个
            linkedList.get(i);
        }
        end = System.currentTimeMillis();
        System.out.println("LinkedList 传统for循环遍历前10000个耗时: " + (end - start) + "ms");
        System.out.println();
    }
    
    public static void iteratorBestPractices() {
        System.out.println("=== Iterator最佳实践 ===");
        
        System.out.println("1. 优先使用增强for循环，代码更简洁");
        System.out.println("2. 需要删除元素时使用Iterator");
        System.out.println("3. 需要获取索引或双向遍历时使用ListIterator");
        System.out.println("4. LinkedList等链表结构避免使用索引访问");
        System.out.println("5. 并发环境下考虑使用fail-safe的集合");
        System.out.println("6. 自定义集合类实现Iterable接口");
        System.out.println("7. 不要在遍历时直接修改集合（除了Iterator.remove()）");
        System.out.println();
    }
    
    public static void main(String[] args) {
        basicIteratorDemo();
        listIteratorDemo();
        enhancedForLoopDemo();
        concurrentModificationDemo();
        failSafeIteratorDemo();
        customIteratorDemo();
        iteratorPerformanceDemo();
        iteratorBestPractices();
    }
}