package com.learning.collection;

import java.util.*;
import java.util.stream.Collectors;
import java.util.function.Function;
import java.util.function.Predicate;

public class CollectionUtilsDemo {
    
    public static void collectionsClassDemo() {
        System.out.println("=== Collections工具类演示 ===");
        
        List<Integer> numbers = new ArrayList<>(Arrays.asList(5, 2, 8, 1, 9, 3));
        System.out.println("原始列表: " + numbers);
        
        // 排序
        Collections.sort(numbers);
        System.out.println("排序后: " + numbers);
        
        // 反转
        Collections.reverse(numbers);
        System.out.println("反转后: " + numbers);
        
        // 打乱
        Collections.shuffle(numbers);
        System.out.println("打乱后: " + numbers);
        
        // 旋转
        Collections.rotate(numbers, 2);
        System.out.println("右旋转2位: " + numbers);
        
        // 二分查找（需要有序列表）
        Collections.sort(numbers);
        int index = Collections.binarySearch(numbers, 5);
        System.out.println("排序后: " + numbers);
        System.out.println("5的位置: " + index);
        
        // 最大最小值
        System.out.println("最大值: " + Collections.max(numbers));
        System.out.println("最小值: " + Collections.min(numbers));
        
        // 填充
        List<String> strings = new ArrayList<>(Arrays.asList("a", "b", "c", "d"));
        Collections.fill(strings, "x");
        System.out.println("填充后: " + strings);
        
        // 替换
        strings = new ArrayList<>(Arrays.asList("a", "b", "a", "c", "a"));
        Collections.replaceAll(strings, "a", "z");
        System.out.println("替换a为z: " + strings);
        
        // 复制
        List<String> source = Arrays.asList("1", "2", "3", "4");
        List<String> dest = new ArrayList<>(Arrays.asList("a", "b", "c", "d"));
        Collections.copy(dest, source);
        System.out.println("复制后: " + dest);
        
        // 交换
        Collections.swap(numbers, 0, numbers.size() - 1);
        System.out.println("交换首尾元素: " + numbers);
        System.out.println();
    }
    
    public static void unmodifiableCollectionsDemo() {
        System.out.println("=== 不可修改集合演示 ===");
        
        List<String> originalList = new ArrayList<>(Arrays.asList("A", "B", "C"));
        List<String> unmodifiableList = Collections.unmodifiableList(originalList);
        
        System.out.println("原始列表: " + originalList);
        System.out.println("不可修改列表: " + unmodifiableList);
        
        // 修改原始列表，不可修改列表也会变化
        originalList.add("D");
        System.out.println("修改原始列表后，不可修改列表: " + unmodifiableList);
        
        try {
            unmodifiableList.add("E"); // 会抛出异常
        } catch (UnsupportedOperationException e) {
            System.out.println("尝试修改不可修改列表抛出异常: " + e.getClass().getSimpleName());
        }
        
        // 其他不可修改集合
        Set<String> unmodifiableSet = Collections.unmodifiableSet(
            new HashSet<>(Arrays.asList("X", "Y", "Z")));
        Map<String, Integer> unmodifiableMap = Collections.unmodifiableMap(
            new HashMap<String, Integer>() {{ put("one", 1); put("two", 2); }});
        
        System.out.println("不可修改Set: " + unmodifiableSet);
        System.out.println("不可修改Map: " + unmodifiableMap);
        System.out.println();
    }
    
    public static void synchronizedCollectionsDemo() {
        System.out.println("=== 同步集合演示 ===");
        
        // 创建同步集合
        List<String> syncList = Collections.synchronizedList(new ArrayList<>());
        Set<String> syncSet = Collections.synchronizedSet(new HashSet<>());
        Map<String, Integer> syncMap = Collections.synchronizedMap(new HashMap<>());
        
        syncList.addAll(Arrays.asList("A", "B", "C"));
        syncSet.addAll(Arrays.asList("X", "Y", "Z"));
        syncMap.put("one", 1);
        syncMap.put("two", 2);
        
        System.out.println("同步列表: " + syncList);
        System.out.println("同步Set: " + syncSet);
        System.out.println("同步Map: " + syncMap);
        
        // 注意：遍历同步集合时仍需要手动同步
        System.out.println("遍历同步集合时需要手动同步:");
        synchronized (syncList) {
            Iterator<String> it = syncList.iterator();
            while (it.hasNext()) {
                System.out.print(it.next() + " ");
            }
        }
        System.out.println("\n");
    }
    
    public static void checkedCollectionsDemo() {
        System.out.println("=== 类型检查集合演示 ===");
        
        // 创建类型检查集合
        List<String> checkedList = Collections.checkedList(new ArrayList<>(), String.class);
        Set<Integer> checkedSet = Collections.checkedSet(new HashSet<>(), Integer.class);
        Map<String, Integer> checkedMap = Collections.checkedMap(new HashMap<>(), String.class, Integer.class);
        
        checkedList.add("Valid String");
        checkedSet.add(123);
        checkedMap.put("key", 456);
        
        System.out.println("类型检查列表: " + checkedList);
        System.out.println("类型检查Set: " + checkedSet);
        System.out.println("类型检查Map: " + checkedMap);
        
        // 在运行时会检查类型
        try {
            @SuppressWarnings({"unchecked", "rawtypes"})
            List rawList = checkedList;
            rawList.add(123); // 会抛出ClassCastException
        } catch (ClassCastException e) {
            System.out.println("类型检查异常: " + e.getMessage());
        }
        System.out.println();
    }
    
    public static void singletonCollectionsDemo() {
        System.out.println("=== 单元素集合演示 ===");
        
        // 创建只包含一个元素的不可变集合
        Set<String> singleton = Collections.singleton("OnlyElement");
        List<String> singletonList = Collections.singletonList("OnlyElement");
        Map<String, String> singletonMap = Collections.singletonMap("key", "value");
        
        System.out.println("单元素Set: " + singleton);
        System.out.println("单元素List: " + singletonList);
        System.out.println("单元素Map: " + singletonMap);
        
        // 这些集合是不可修改的
        try {
            singleton.add("Another");
        } catch (UnsupportedOperationException e) {
            System.out.println("单元素集合不可修改: " + e.getClass().getSimpleName());
        }
        System.out.println();
    }
    
    public static void emptyCollectionsDemo() {
        System.out.println("=== 空集合演示 ===");
        
        // 创建空的不可变集合
        List<String> emptyList = Collections.emptyList();
        Set<String> emptySet = Collections.emptySet();
        Map<String, String> emptyMap = Collections.emptyMap();
        
        System.out.println("空列表: " + emptyList + ", 大小: " + emptyList.size());
        System.out.println("空Set: " + emptySet + ", 大小: " + emptySet.size());
        System.out.println("空Map: " + emptyMap + ", 大小: " + emptyMap.size());
        
        // 这些是单例对象，效率很高
        System.out.println("空列表引用相同? " + (emptyList.equals(Collections.emptyList())));
        
        // 用于默认返回值
        System.out.println("常用于方法的默认返回值，避免返回null");
        System.out.println();
    }
    
    public static void frequencyAndDisjointDemo() {
        System.out.println("=== 频率统计和不相交判断演示 ===");
        
        List<String> list = Arrays.asList("apple", "banana", "apple", "cherry", "apple", "banana");
        System.out.println("列表: " + list);
        
        // 统计元素出现频率
        int appleCount = Collections.frequency(list, "apple");
        int bananaCount = Collections.frequency(list, "banana");
        System.out.println("apple出现次数: " + appleCount);
        System.out.println("banana出现次数: " + bananaCount);
        
        // 判断两个集合是否不相交
        Set<String> set1 = new HashSet<>(Arrays.asList("a", "b", "c"));
        Set<String> set2 = new HashSet<>(Arrays.asList("d", "e", "f"));
        Set<String> set3 = new HashSet<>(Arrays.asList("c", "d", "e"));
        
        System.out.println("set1: " + set1);
        System.out.println("set2: " + set2);
        System.out.println("set3: " + set3);
        System.out.println("set1和set2不相交? " + Collections.disjoint(set1, set2));
        System.out.println("set1和set3不相交? " + Collections.disjoint(set1, set3));
        System.out.println();
    }
    
    public static void customUtilMethodsDemo() {
        System.out.println("=== 自定义工具方法演示 ===");
        
        List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        System.out.println("原始数字列表: " + numbers);
        
        // 过滤偶数
        List<Integer> evenNumbers = filter(numbers, n -> n % 2 == 0);
        System.out.println("偶数: " + evenNumbers);
        
        // 转换为字符串
        List<String> stringNumbers = map(numbers, Object::toString);
        System.out.println("转换为字符串: " + stringNumbers);
        
        // 求和
        int sum = reduce(numbers, 0, Integer::sum);
        System.out.println("求和: " + sum);
        
        // 分组
        Map<Boolean, List<Integer>> grouped = groupBy(numbers, n -> n % 2 == 0);
        System.out.println("按奇偶分组: " + grouped);
        
        // 去重
        List<String> duplicates = Arrays.asList("a", "b", "a", "c", "b", "d");
        List<String> distinct = removeDuplicates(duplicates);
        System.out.println("原列表: " + duplicates);
        System.out.println("去重后: " + distinct);
        
        // 安全获取元素
        System.out.println("安全获取索引5: " + safeGet(numbers, 5));
        System.out.println("安全获取索引15: " + safeGet(numbers, 15));
        System.out.println();
    }
    
    // 自定义工具方法
    public static <T> List<T> filter(List<T> list, Predicate<T> predicate) {
        return list.stream().filter(predicate).collect(Collectors.toList());
    }
    
    public static <T, R> List<R> map(List<T> list, Function<T, R> mapper) {
        return list.stream().map(mapper).collect(Collectors.toList());
    }
    
    public static <T> T reduce(List<T> list, T identity, java.util.function.BinaryOperator<T> accumulator) {
        return list.stream().reduce(identity, accumulator);
    }
    
    public static <T, K> Map<K, List<T>> groupBy(List<T> list, Function<T, K> classifier) {
        return list.stream().collect(Collectors.groupingBy(classifier));
    }
    
    public static <T> List<T> removeDuplicates(List<T> list) {
        return new ArrayList<>(new LinkedHashSet<>(list));
    }
    
    public static <T> Optional<T> safeGet(List<T> list, int index) {
        if (index >= 0 && index < list.size()) {
            return Optional.of(list.get(index));
        }
        return Optional.empty();
    }
    
    public static void arraysUtilityDemo() {
        System.out.println("=== Arrays工具类演示 ===");
        
        int[] array = {5, 2, 8, 1, 9, 3};
        System.out.println("原数组: " + Arrays.toString(array));
        
        // 排序
        int[] sortedArray = array.clone();
        Arrays.sort(sortedArray);
        System.out.println("排序后: " + Arrays.toString(sortedArray));
        
        // 二分查找
        int index = Arrays.binarySearch(sortedArray, 8);
        System.out.println("8的位置: " + index);
        
        // 填充
        int[] fillArray = new int[5];
        Arrays.fill(fillArray, 42);
        System.out.println("填充数组: " + Arrays.toString(fillArray));
        
        // 比较
        int[] array1 = {1, 2, 3};
        int[] array2 = {1, 2, 3};
        int[] array3 = {1, 2, 4};
        System.out.println("array1 equals array2: " + Arrays.equals(array1, array2));
        System.out.println("array1 equals array3: " + Arrays.equals(array1, array3));
        
        // 转换为List
        List<Integer> list = Arrays.asList(1, 2, 3, 4, 5);
        System.out.println("Arrays.asList: " + list);
        
        // 复制
        int[] copiedArray = Arrays.copyOf(array, 10);
        System.out.println("复制并扩展: " + Arrays.toString(copiedArray));
        
        int[] rangedCopy = Arrays.copyOfRange(array, 2, 5);
        System.out.println("范围复制[2-5): " + Arrays.toString(rangedCopy));
        
        // 多维数组
        int[][] matrix = {{1, 2}, {3, 4}};
        System.out.println("深度toString: " + Arrays.deepToString(matrix));
        
        int[][] matrix2 = {{1, 2}, {3, 4}};
        System.out.println("深度equals: " + Arrays.deepEquals(matrix, matrix2));
        System.out.println();
    }
    
    public static void bestPracticesDemo() {
        System.out.println("=== 集合工具最佳实践 ===");
        System.out.println("1. 优先使用Collections和Arrays提供的工具方法");
        System.out.println("2. 需要不可修改集合时使用unmodifiable包装");
        System.out.println("3. 多线程环境考虑synchronized包装或concurrent集合");
        System.out.println("4. 使用空集合而不是null作为默认返回值");
        System.out.println("5. 需要类型安全时使用checked包装");
        System.out.println("6. 合理使用Stream API进行函数式编程");
        System.out.println("7. 自定义工具方法要考虑null安全和边界情况");
        System.out.println("8. 大数据量操作时注意性能和内存使用");
        System.out.println();
    }
    
    public static void main(String[] args) {
        collectionsClassDemo();
        unmodifiableCollectionsDemo();
        synchronizedCollectionsDemo();
        checkedCollectionsDemo();
        singletonCollectionsDemo();
        emptyCollectionsDemo();
        frequencyAndDisjointDemo();
        customUtilMethodsDemo();
        arraysUtilityDemo();
        bestPracticesDemo();
    }
}