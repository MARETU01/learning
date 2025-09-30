package com.learning.collection;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class ListDemo {
    
    public static void arrayListDemo() {
        System.out.println("=== ArrayList 演示 (基于动态数组) ===");
        List<String> arrayList = new ArrayList<>();
        
        // 基本操作
        arrayList.add("Apple");
        arrayList.add("Banana");
        arrayList.add("Cherry");
        arrayList.add(1, "Orange"); // 在指定位置插入
        
        System.out.println("ArrayList: " + arrayList);
        System.out.println("第一个元素: " + arrayList.get(0));
        System.out.println("大小: " + arrayList.size());
        
        // 查找操作
        System.out.println("包含Banana? " + arrayList.contains("Banana"));
        System.out.println("Orange的索引: " + arrayList.indexOf("Orange"));
        
        // 修改操作
        arrayList.set(0, "Pineapple"); // 替换元素
        System.out.println("替换后: " + arrayList);
        
        // 删除操作
        arrayList.remove("Banana");
        arrayList.remove(1); // 按索引删除
        System.out.println("删除后: " + arrayList);
        
        // 批量操作
        List<String> moreItems = Arrays.asList("Mango", "Grape");
        arrayList.addAll(moreItems);
        System.out.println("批量添加后: " + arrayList);
        
        // 转换为数组
        String[] array = arrayList.toArray(new String[0]);
        System.out.println("转换为数组: " + Arrays.toString(array));
        
        // 清空
        arrayList.clear();
        System.out.println("清空后大小: " + arrayList.size());
        System.out.println();
    }
    
    public static void linkedListDemo() {
        System.out.println("=== LinkedList 演示 (基于双向链表) ===");
        LinkedList<Integer> linkedList = new LinkedList<>();
        
        // 基本操作
        linkedList.add(10);
        linkedList.add(20);
        linkedList.add(30);
        System.out.println("初始LinkedList: " + linkedList);
        
        // 头部操作
        linkedList.addFirst(5);
        linkedList.addFirst(1);
        System.out.println("添加到头部: " + linkedList);
        
        // 尾部操作
        linkedList.addLast(40);
        linkedList.addLast(50);
        System.out.println("添加到尾部: " + linkedList);
        
        // 访问头尾元素
        System.out.println("头部元素: " + linkedList.getFirst());
        System.out.println("尾部元素: " + linkedList.getLast());
        System.out.println("偷看头部(不删除): " + linkedList.peekFirst());
        System.out.println("偷看尾部(不删除): " + linkedList.peekLast());
        
        // 删除头尾元素
        System.out.println("删除头部元素: " + linkedList.removeFirst());
        System.out.println("删除尾部元素: " + linkedList.removeLast());
        System.out.println("删除后: " + linkedList);
        
        // 作为栈使用 (LIFO)
        System.out.println("\n=== 作为栈使用 ===");
        LinkedList<String> stack = new LinkedList<>();
        stack.push("Bottom");
        stack.push("Middle");
        stack.push("Top");
        System.out.println("栈: " + stack);
        System.out.println("弹出: " + stack.pop());
        System.out.println("栈顶: " + stack.peek());
        
        // 作为队列使用 (FIFO)
        System.out.println("\n=== 作为队列使用 ===");
        LinkedList<String> queue = new LinkedList<>();
        queue.offer("First");
        queue.offer("Second");
        queue.offer("Third");
        System.out.println("队列: " + queue);
        System.out.println("出队: " + queue.poll());
        System.out.println("队首: " + queue.peek());
        System.out.println();
    }
    
    public static void vectorDemo() {
        System.out.println("=== Vector 演示 (线程安全的动态数组) ===");
        Vector<String> vector = new Vector<>();
        
        vector.add("Element1");
        vector.add("Element2");
        vector.add("Element3");
        
        System.out.println("Vector: " + vector);
        System.out.println("容量: " + vector.capacity());
        System.out.println("大小: " + vector.size());
        
        // Vector特有的方法
        vector.insertElementAt("Inserted", 1);
        System.out.println("插入后: " + vector);
        
        vector.removeElementAt(2);
        System.out.println("删除索引2后: " + vector);
        System.out.println();
    }
    
    public static void copyOnWriteArrayListDemo() {
        System.out.println("=== CopyOnWriteArrayList 演示 (写时复制，线程安全) ===");
        CopyOnWriteArrayList<String> cowList = new CopyOnWriteArrayList<>();
        
        cowList.add("Thread-Safe1");
        cowList.add("Thread-Safe2");
        cowList.add("Thread-Safe3");
        
        System.out.println("CopyOnWriteArrayList: " + cowList);
        
        // 适合读多写少的场景
        System.out.println("读取操作不需要锁定");
        for (String item : cowList) {
            System.out.println("读取: " + item);
        }
        System.out.println();
    }
    
    public static void performanceComparison() {
        System.out.println("=== 性能对比演示 ===");
        int size = 100000;
        
        // ArrayList添加性能
        long start = System.currentTimeMillis();
        List<Integer> arrayList = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            arrayList.add(i);
        }
        long end = System.currentTimeMillis();
        System.out.println("ArrayList添加 " + size + " 个元素耗时: " + (end - start) + "ms");
        
        // LinkedList添加性能
        start = System.currentTimeMillis();
        List<Integer> linkedList = new LinkedList<>();
        for (int i = 0; i < size; i++) {
            linkedList.add(i);
        }
        end = System.currentTimeMillis();
        System.out.println("LinkedList添加 " + size + " 个元素耗时: " + (end - start) + "ms");
        
        // 随机访问性能对比
        start = System.currentTimeMillis();
        for (int i = 0; i < 1000; i++) {
            arrayList.get((int) (Math.random() * size));
        }
        end = System.currentTimeMillis();
        System.out.println("ArrayList随机访问1000次耗时: " + (end - start) + "ms");
        
        start = System.currentTimeMillis();
        for (int i = 0; i < 1000; i++) {
            linkedList.get((int) (Math.random() * size));
        }
        end = System.currentTimeMillis();
        System.out.println("LinkedList随机访问1000次耗时: " + (end - start) + "ms");
        System.out.println();
    }
    
    public static void listFeatures() {
        System.out.println("=== List接口特性演示 ===");
        List<String> list = new ArrayList<>(Arrays.asList("A", "B", "C", "B", "D"));
        
        System.out.println("原始列表: " + list);
        
        // 子列表
        List<String> subList = list.subList(1, 4);
        System.out.println("子列表(1-4): " + subList);
        
        // 列表迭代器
        ListIterator<String> iterator = list.listIterator();
        System.out.println("正向遍历:");
        while (iterator.hasNext()) {
            System.out.print(iterator.next() + " ");
        }
        System.out.println();
        
        System.out.println("反向遍历:");
        while (iterator.hasPrevious()) {
            System.out.print(iterator.previous() + " ");
        }
        System.out.println();
        
        // 排序
        Collections.sort(list);
        System.out.println("排序后: " + list);
        
        // 二分查找
        int index = Collections.binarySearch(list, "C");
        System.out.println("C的位置: " + index);
        
        // 反转
        Collections.reverse(list);
        System.out.println("反转后: " + list);
        
        // 打乱
        Collections.shuffle(list);
        System.out.println("打乱后: " + list);
        System.out.println();
    }
    
    public static void main(String[] args) {
        arrayListDemo();
        linkedListDemo();
        vectorDemo();
        copyOnWriteArrayListDemo();
        performanceComparison();
        listFeatures();
    }
}