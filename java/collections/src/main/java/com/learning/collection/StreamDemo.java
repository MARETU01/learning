package com.learning.collection;

import java.util.*;
import java.util.stream.*;
import java.util.function.*;
import java.util.concurrent.*;

public class StreamDemo {
    
    public static void basicStreamDemo() {
        System.out.println("=== Stream基本操作演示 ===");
        
        List<String> names = Arrays.asList("Alice", "Bob", "Charlie", "David", "Eve");
        
        System.out.println("原始列表: " + names);
        
        // 创建Stream
        Stream<String> stream = names.stream();
        System.out.println("创建Stream: " + stream.getClass().getSimpleName());
        
        // 终端操作 - 遍历
        System.out.println("使用forEach遍历:");
        names.stream().forEach(name -> System.out.println("Hello, " + name));
        
        // 转换为数组
        String[] nameArray = names.stream().toArray(String[]::new);
        System.out.println("转换为数组: " + Arrays.toString(nameArray));
        
        // 收集为List
        List<String> collectedList = names.stream().collect(Collectors.toList());
        System.out.println("收集为List: " + collectedList);
        
        // 收集为Set
        Set<String> collectedSet = names.stream().collect(Collectors.toSet());
        System.out.println("收集为Set: " + collectedSet);
        
        // 连接字符串
        String joined = names.stream().collect(Collectors.joining(", "));
        System.out.println("连接字符串: " + joined);
        
        // 计数
        long count = names.stream().count();
        System.out.println("元素数量: " + count);
        
        // 获取第一个元素
        Optional<String> first = names.stream().findFirst();
        System.out.println("第一个元素: " + first.orElse("None"));
        
        // 查找任意元素
        Optional<String> any = names.stream().findAny();
        System.out.println("任意元素: " + any.orElse("None"));
        System.out.println();
    }
    
    public static void intermediateOperationsDemo() {
        System.out.println("=== 中间操作演示 ===");
        
        List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        System.out.println("原始数字: " + numbers);
        
        // filter - 过滤
        List<Integer> evenNumbers = numbers.stream()
            .filter(n -> n % 2 == 0)
            .collect(Collectors.toList());
        System.out.println("偶数: " + evenNumbers);
        
        // map - 转换
        List<Integer> squared = numbers.stream()
            .map(n -> n * n)
            .collect(Collectors.toList());
        System.out.println("平方: " + squared);
        
        // flatMap - 扁平化
        List<List<String>> nestedList = Arrays.asList(
            Arrays.asList("A", "B"),
            Arrays.asList("C", "D", "E"),
            Arrays.asList("F")
        );
        List<String> flattened = nestedList.stream()
            .flatMap(List::stream)
            .collect(Collectors.toList());
        System.out.println("扁平化: " + flattened);
        
        // distinct - 去重
        List<Integer> withDuplicates = Arrays.asList(1, 2, 2, 3, 3, 3, 4);
        List<Integer> distinct = withDuplicates.stream()
            .distinct()
            .collect(Collectors.toList());
        System.out.println("去重: " + distinct);
        
        // sorted - 排序
        List<Integer> randomNumbers = Arrays.asList(5, 2, 8, 1, 9, 3);
        List<Integer> sorted = randomNumbers.stream()
            .sorted()
            .collect(Collectors.toList());
        System.out.println("排序: " + sorted);
        
        // 自定义排序
        List<Integer> reverseSorted = randomNumbers.stream()
            .sorted(Comparator.reverseOrder())
            .collect(Collectors.toList());
        System.out.println("倒序排序: " + reverseSorted);
        
        // limit - 限制
        List<Integer> limited = numbers.stream()
            .limit(5)
            .collect(Collectors.toList());
        System.out.println("前5个: " + limited);
        
        // skip - 跳过
        List<Integer> skipped = numbers.stream()
            .skip(5)
            .collect(Collectors.toList());
        System.out.println("跳过前5个: " + skipped);
        
        // peek - 查看但不修改
        List<Integer> peeked = numbers.stream()
            .peek(n -> System.out.println("处理: " + n))
            .collect(Collectors.toList());
        System.out.println("peek操作后: " + peeked);
        System.out.println();
    }
    
    public static void terminalOperationsDemo() {
        System.out.println("=== 终端操作演示 ===");
        
        List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        System.out.println("原始数字: " + numbers);
        
        // forEach - 遍历
        System.out.println("forEach遍历:");
        numbers.stream().forEach(n -> System.out.print(n + " "));
        System.out.println();
        
        // toArray - 转换为数组
        Integer[] array = numbers.stream().toArray(Integer[]::new);
        System.out.println("转换为数组: " + Arrays.toString(array));
        
        // collect - 收集
        List<Integer> collected = numbers.stream()
            .filter(n -> n % 2 == 0)
            .collect(Collectors.toList());
        System.out.println("收集偶数: " + collected);
        
        // reduce - 归约
        Optional<Integer> sum = numbers.stream().reduce(Integer::sum);
        System.out.println("求和: " + sum.orElse(0));
        
        Optional<Integer> product = numbers.stream().reduce((a, b) -> a * b);
        System.out.println("乘积: " + product.orElse(1));
        
        // min - 最小值
        Optional<Integer> min = numbers.stream().min(Integer::compare);
        System.out.println("最小值: " + min.orElse(0));
        
        // max - 最大值
        Optional<Integer> max = numbers.stream().max(Integer::compare);
        System.out.println("最大值: " + max.orElse(0));
        
        // count - 计数
        long evenCount = numbers.stream().filter(n -> n % 2 == 0).count();
        System.out.println("偶数数量: " + evenCount);
        
        // anyMatch - 任意匹配
        boolean hasEven = numbers.stream().anyMatch(n -> n % 2 == 0);
        System.out.println("包含偶数? " + hasEven);
        
        // allMatch - 全部匹配
        boolean allPositive = numbers.stream().allMatch(n -> n > 0);
        System.out.println("全部为正数? " + allPositive);
        
        // noneMatch - 无匹配
        boolean noneNegative = numbers.stream().noneMatch(n -> n < 0);
        System.out.println("没有负数? " + noneNegative);
        
        // findFirst - 查找第一个
        Optional<Integer> firstEven = numbers.stream().filter(n -> n % 2 == 0).findFirst();
        System.out.println("第一个偶数: " + firstEven.orElse(0));
        
        // findAny - 查找任意
        Optional<Integer> anyEven = numbers.stream().filter(n -> n % 2 == 0).findAny();
        System.out.println("任意偶数: " + anyEven.orElse(0));
        System.out.println();
    }
    
    public static void collectorsDemo() {
        System.out.println("=== Collectors收集器演示 ===");
        
        List<String> names = Arrays.asList("Alice", "Bob", "Charlie", "David", "Eve", "Alice");
        System.out.println("原始名字: " + names);
        
        // toList - 收集为List
        List<String> nameList = names.stream().collect(Collectors.toList());
        System.out.println("收集为List: " + nameList);
        
        // toSet - 收集为Set
        Set<String> nameSet = names.stream().collect(Collectors.toSet());
        System.out.println("收集为Set: " + nameSet);
        
        // toCollection - 收集为指定集合
        TreeSet<String> nameTreeSet = names.stream()
            .collect(Collectors.toCollection(TreeSet::new));
        System.out.println("收集为TreeSet: " + nameTreeSet);
        
        // toMap - 收集为Map
        Map<String, Integer> nameLengthMap = names.stream()
            .distinct()
            .collect(Collectors.toMap(
                name -> name,
                name -> name.length()
            ));
        System.out.println("名字长度Map: " + nameLengthMap);
        
        // joining - 连接字符串
        String joined = names.stream().collect(Collectors.joining(", "));
        System.out.println("连接字符串: " + joined);
        
        // counting - 计数
        Long count = names.stream().collect(Collectors.counting());
        System.out.println("计数: " + count);
        
        // summingInt/Double/Long - 求和
        Integer totalLength = names.stream().collect(Collectors.summingInt(String::length));
        System.out.println("总长度: " + totalLength);
        
        // averagingInt/Double/Long - 平均值
        Double avgLength = names.stream().collect(Collectors.averagingInt(String::length));
        System.out.println("平均长度: " + avgLength);
        
        // summarizingInt/Double/Long - 统计信息
        IntSummaryStatistics stats = names.stream().collect(Collectors.summarizingInt(String::length));
        System.out.println("统计信息: " + stats);
        
        // groupingBy - 分组
        Map<Integer, List<String>> groupedByLength = names.stream()
            .collect(Collectors.groupingBy(String::length));
        System.out.println("按长度分组: " + groupedByLength);
        
        // partitioningBy - 分区
        Map<Boolean, List<String>> partitionedByLength = names.stream()
            .collect(Collectors.partitioningBy(name -> name.length() > 4));
        System.out.println("按长度>4分区: " + partitionedByLength);
        
        // mapping - 映射后收集
        Map<Integer, Set<String>> groupedByLengthAsSet = names.stream()
            .collect(Collectors.groupingBy(
                String::length,
                Collectors.mapping(name -> name.toUpperCase(), Collectors.toSet())
            ));
        System.out.println("按长度分组并转换为大写: " + groupedByLengthAsSet);
        System.out.println();
    }
    
    public static void optionalDemo() {
        System.out.println("=== Optional演示 ===");
        
        // 创建Optional
        Optional<String> optional1 = Optional.of("Hello");
        Optional<String> optional2 = Optional.empty();
        Optional<String> optional3 = Optional.ofNullable(null);
        
        System.out.println("Optional1: " + optional1);
        System.out.println("Optional2: " + optional2);
        System.out.println("Optional3: " + optional3);
        
        // 检查值是否存在
        System.out.println("Optional1有值? " + optional1.isPresent());
        System.out.println("Optional2有值? " + optional2.isPresent());
        
        // 获取值
        System.out.println("Optional1的值: " + optional1.get());
        // System.out.println("Optional2的值: " + optional2.get()); // 会抛异常
        
        // 安全获取值
        System.out.println("Optional1的值(orElse): " + optional1.orElse("Default"));
        System.out.println("Optional2的值(orElse): " + optional2.orElse("Default"));
        
        System.out.println("Optional1的值(orElseGet): " + optional1.orElseGet(() -> "Generated Default"));
        System.out.println("Optional2的值(orElseGet): " + optional2.orElseGet(() -> "Generated Default"));
        
        // 抛出异常
        try {
            optional2.orElseThrow(() -> new RuntimeException("No value present"));
        } catch (RuntimeException e) {
            System.out.println("捕获异常: " + e.getMessage());
        }
        
        // 条件操作
        optional1.ifPresent(value -> System.out.println("Optional1的值: " + value));
        optional2.ifPresent(value -> System.out.println("Optional2的值: " + value));
        
        // 转换
        Optional<Integer> lengthOptional = optional1.map(String::length);
        System.out.println("Optional1的长度: " + lengthOptional);
        
        // 过滤
        Optional<String> filteredOptional = optional1.filter(s -> s.length() > 3);
        System.out.println("过滤后的Optional: " + filteredOptional);
        
        // 链式操作
        String result = optional1
            .map(String::toUpperCase)
            .filter(s -> s.length() > 3)
            .orElse("DEFAULT");
        System.out.println("链式操作结果: " + result);
        System.out.println();
    }
    
    public static void parallelStreamDemo() {
        System.out.println("=== 并行流演示 ===");
        
        List<Integer> numbers = IntStream.range(1, 1000000).boxed().collect(Collectors.toList());
        
        // 顺序流
        long start = System.currentTimeMillis();
        long sequentialSum = numbers.stream()
            .mapToLong(Integer::longValue)
            .sum();
        long sequentialTime = System.currentTimeMillis() - start;
        System.out.println("顺序流求和: " + sequentialSum + ", 耗时: " + sequentialTime + "ms");
        
        // 并行流
        start = System.currentTimeMillis();
        long parallelSum = numbers.parallelStream()
            .mapToLong(Integer::longValue)
            .sum();
        long parallelTime = System.currentTimeMillis() - start;
        System.out.println("并行流求和: " + parallelSum + ", 耗时: " + parallelTime + "ms");
        
        // 并行流的注意事项
        System.out.println("并行流注意事项:");
        System.out.println("1. 适合CPU密集型操作");
        System.out.println("2. 数据量要足够大");
        System.out.println("3. 操作要无状态");
        System.out.println("4. 避免共享可变状态");
        System.out.println("5. 考虑线程安全问题");
        
        // 并行流使用线程池
        System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", "4");
        System.out.println("设置并行度为4");
        
        // 并行流的归约操作
        List<String> words = Arrays.asList("Hello", "World", "Java", "Stream", "Parallel");
        String concatenated = words.parallelStream()
            .reduce("", (a, b) -> a + b);
        System.out.println("并行连接字符串: " + concatenated);
        System.out.println();
    }
    
    public static void streamPrimitivesDemo() {
        System.out.println("=== 原始类型流演示 ===");
        
        // IntStream
        System.out.println("IntStream演示:");
        IntStream intStream = IntStream.of(1, 2, 3, 4, 5);
        System.out.println("IntStream求和: " + intStream.sum());
        
        // 生成范围
        System.out.println("IntStream范围:");
        IntStream.range(1, 5).forEach(n -> System.out.print(n + " "));
        System.out.println();
        
        IntStream.rangeClosed(1, 5).forEach(n -> System.out.print(n + " "));
        System.out.println();
        
        // 生成随机数
        System.out.println("IntStream随机数:");
        IntStream.generate(() -> (int) (Math.random() * 100))
            .limit(5)
            .forEach(n -> System.out.print(n + " "));
        System.out.println();
        
        // LongStream
        System.out.println("LongStream演示:");
        LongStream longStream = LongStream.of(1L, 2L, 3L, 4L, 5L);
        System.out.println("LongStream平均值: " + longStream.average().orElse(0));
        
        // DoubleStream
        System.out.println("DoubleStream演示:");
        DoubleStream doubleStream = DoubleStream.of(1.1, 2.2, 3.3, 4.4, 5.5);
        System.out.println("DoubleStream最大值: " + doubleStream.max().orElse(0));
        
        // 原始类型流的统计
        System.out.println("原始类型流统计:");
        IntSummaryStatistics intStats = IntStream.range(1, 101).summaryStatistics();
        System.out.println("1-100的统计: " + intStats);
        
        // 装箱和拆箱
        System.out.println("装箱和拆箱:");
        Stream<Integer> boxed = IntStream.range(1, 5).boxed();
        System.out.println("装箱后的Stream: " + boxed.collect(Collectors.toList()));
        
        IntStream unboxed = Stream.of(1, 2, 3, 4).mapToInt(Integer::intValue);
        System.out.println("拆箱后的IntStream求和: " + unboxed.sum());
        System.out.println();
    }
    
    public static void streamBestPractices() {
        System.out.println("=== Stream最佳实践 ===");
        System.out.println("1. 使用Stream API使代码更声明式和函数式");
        System.out.println("2. 链式操作时注意中间操作的顺序");
        System.out.println("3. 避免在流操作中修改外部状态");
        System.out.println("4. 谨慎使用并行流，确保适合并行处理");
        System.out.println("5. 使用Optional来处理可能为空的值");
        System.out.println("6. 选择合适的收集器来收集结果");
        System.out.println("7. 注意流的延迟执行特性");
        System.out.println("8. 考虑使用原始类型流来避免自动装箱");
        System.out.println("9. 在复杂数据处理中使用方法引用");
        System.out.println("10. 注意流的性能特征和内存使用");
        System.out.println();
    }
    
    public static void main(String[] args) {
        basicStreamDemo();
        intermediateOperationsDemo();
        terminalOperationsDemo();
        collectorsDemo();
        optionalDemo();
        parallelStreamDemo();
        streamPrimitivesDemo();
        streamBestPractices();
    }
}
