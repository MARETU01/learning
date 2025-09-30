package com.learning.designpattern.creational;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 单例模式演示
 * 
 * 单例模式确保一个类只有一个实例，并提供一个全局访问点来访问该实例。
 * 
 * 应用场景：
 * 1. 需要频繁实例化然后销毁的对象
 * 2. 创建对象时耗时过多或者耗资源过多，但又经常用到的对象
 * 3. 工具类对象
 * 4. 频繁访问数据库或文件的对象
 */
public class SingletonDemo {
    
    private static final Logger log = LoggerFactory.getLogger(SingletonDemo.class);

    // 1. 饿汉式单例模式
    public static class EagerSingleton {
        // 在类加载时就创建实例
        private static final EagerSingleton instance = new EagerSingleton();
        
        // 私有构造函数，防止外部实例化
        private EagerSingleton() {
            log.info("EagerSingleton 实例被创建");
        }
        
        // 提供全局访问点
        public static EagerSingleton getInstance() {
            return instance;
        }
        
        public void showMessage() {
            log.info("这是饿汉式单例模式");
        }
    }

    // 2. 懒汉式单例模式（线程不安全）
    public static class LazySingleton {
        private static LazySingleton instance;
        
        private LazySingleton() {
            log.info("LazySingleton 实例被创建");
        }
        
        public static LazySingleton getInstance() {
            if (instance == null) {
                instance = new LazySingleton();
            }
            return instance;
        }
        
        public void showMessage() {
            log.info("这是懒汉式单例模式（线程不安全）");
        }
    }

    // 3. 懒汉式单例模式（线程安全，同步方法）
    public static class ThreadSafeLazySingleton {
        private static ThreadSafeLazySingleton instance;
        
        private ThreadSafeLazySingleton() {
            log.info("ThreadSafeLazySingleton 实例被创建");
        }
        
        // 使用synchronized关键字保证线程安全
        public static synchronized ThreadSafeLazySingleton getInstance() {
            if (instance == null) {
                instance = new ThreadSafeLazySingleton();
            }
            return instance;
        }
        
        public void showMessage() {
            log.info("这是懒汉式单例模式（线程安全，同步方法）");
        }
    }

    // 4. 双重检查锁定单例模式
    public static class DoubleCheckedLockingSingleton {
        // 使用volatile关键字防止指令重排序
        private static volatile DoubleCheckedLockingSingleton instance;
        
        private DoubleCheckedLockingSingleton() {
            log.info("DoubleCheckedLockingSingleton 实例被创建");
        }
        
        public static DoubleCheckedLockingSingleton getInstance() {
            if (instance == null) {
                synchronized (DoubleCheckedLockingSingleton.class) {
                    if (instance == null) {
                        instance = new DoubleCheckedLockingSingleton();
                    }
                }
            }
            return instance;
        }
        
        public void showMessage() {
            log.info("这是双重检查锁定单例模式");
        }
    }

    // 5. 静态内部类单例模式
    public static class StaticInnerClassSingleton {
        // 私有构造函数
        private StaticInnerClassSingleton() {
            log.info("StaticInnerClassSingleton 实例被创建");
        }
        
        // 静态内部类，在需要时才会加载
        private static class SingletonHolder {
            private static final StaticInnerClassSingleton INSTANCE = new StaticInnerClassSingleton();
        }
        
        public static StaticInnerClassSingleton getInstance() {
            return SingletonHolder.INSTANCE;
        }
        
        public void showMessage() {
            log.info("这是静态内部类单例模式");
        }
    }

    // 6. 枚举单例模式
    public enum EnumSingleton {
        INSTANCE;
        
        // 枚举的构造函数是私有的
        EnumSingleton() {
            log.info("EnumSingleton 实例被创建");
        }
        
        public void showMessage() {
            log.info("这是枚举单例模式");
        }
    }

    // 7. 登记式单例模式（Spring框架中使用）
    public static class RegisteredSingleton {
        // 使用Map来存储单例实例
        private static final java.util.Map<String, RegisteredSingleton> map = new java.util.HashMap<>();
        
        private RegisteredSingleton() {
            log.info("RegisteredSingleton 实例被创建");
        }
        
        public static synchronized RegisteredSingleton getInstance(String name) {
            if (name == null) {
                name = "default";
            }
            
            if (!map.containsKey(name)) {
                map.put(name, new RegisteredSingleton());
            }
            
            return map.get(name);
        }
        
        public void showMessage() {
            log.info("这是登记式单例模式");
        }
    }

    // 演示各种单例模式的使用
    public static void main(String[] args) {
        log.info("=== 单例模式演示 ===");
        
        // 1. 饿汉式单例
        log.info("\n1. 饿汉式单例模式：");
        EagerSingleton eager1 = EagerSingleton.getInstance();
        EagerSingleton eager2 = EagerSingleton.getInstance();
        log.info("eager1 == eager2: {}", (eager1 == eager2));
        eager1.showMessage();
        
        // 2. 懒汉式单例（线程不安全）
        log.info("\n2. 懒汉式单例模式（线程不安全）：");
        LazySingleton lazy1 = LazySingleton.getInstance();
        LazySingleton lazy2 = LazySingleton.getInstance();
        log.info("lazy1 == lazy2: {}", (lazy1 == lazy2));
        lazy1.showMessage();
        
        // 3. 懒汉式单例（线程安全）
        log.info("\n3. 懒汉式单例模式（线程安全）：");
        ThreadSafeLazySingleton threadSafe1 = ThreadSafeLazySingleton.getInstance();
        ThreadSafeLazySingleton threadSafe2 = ThreadSafeLazySingleton.getInstance();
        log.info("threadSafe1 == threadSafe2: {}", (threadSafe1 == threadSafe2));
        threadSafe1.showMessage();
        
        // 4. 双重检查锁定
        log.info("\n4. 双重检查锁定单例模式：");
        DoubleCheckedLockingSingleton dcl1 = DoubleCheckedLockingSingleton.getInstance();
        DoubleCheckedLockingSingleton dcl2 = DoubleCheckedLockingSingleton.getInstance();
        log.info("dcl1 == dcl2: {}", (dcl1 == dcl2));
        dcl1.showMessage();
        
        // 5. 静态内部类
        log.info("\n5. 静态内部类单例模式：");
        StaticInnerClassSingleton inner1 = StaticInnerClassSingleton.getInstance();
        StaticInnerClassSingleton inner2 = StaticInnerClassSingleton.getInstance();
        log.info("inner1 == inner2: {}", (inner1 == inner2));
        inner1.showMessage();
        
        // 6. 枚举单例
        log.info("\n6. 枚举单例模式：");
        EnumSingleton enum1 = EnumSingleton.INSTANCE;
        EnumSingleton enum2 = EnumSingleton.INSTANCE;
        log.info("enum1 == enum2: {}", (enum1 == enum2));
        enum1.showMessage();
        
        // 7. 登记式单例
        log.info("\n7. 登记式单例模式：");
        RegisteredSingleton registered1 = RegisteredSingleton.getInstance("singleton1");
        RegisteredSingleton registered2 = RegisteredSingleton.getInstance("singleton1");
        RegisteredSingleton registered3 = RegisteredSingleton.getInstance("singleton2");
        log.info("registered1 == registered2: {}", (registered1 == registered2));
        log.info("registered1 == registered3: {}", (registered1 == registered3));
        registered1.showMessage();
        
        // 单例模式优缺点总结
        log.info("\n=== 单例模式优缺点总结 ===");
        log.info("优点：");
        log.info("1. 在内存中只有一个实例，减少了内存开销");
        log.info("2. 避免对资源的多重占用");
        log.info("3. 设置全局访问点，严格控制访问");
        
        log.info("\n缺点：");
        log.info("1. 没有接口，扩展困难");
        log.info("2. 不利于测试");
        log.info("3. 与单一职责原则冲突");
        
        log.info("\n=== 各种实现方式比较 ===");
        log.info("饿汉式：简单，但可能造成资源浪费");
        log.info("懒汉式（线程不安全）：多线程环境下有问题");
        log.info("懒汉式（同步方法）：线程安全，但性能较差");
        log.info("双重检查锁定：线程安全，性能较好，实现复杂");
        log.info("静态内部类：线程安全，性能好，推荐使用");
        log.info("枚举：线程安全，防止反序列化，最佳实践");
        log.info("登记式：可以管理多个单例，适合复杂场景");
    }
}
