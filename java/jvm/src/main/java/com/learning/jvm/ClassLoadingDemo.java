package com.learning.jvm;

import java.io.*;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * JVM类加载机制演示
 */
public class ClassLoadingDemo {
    
    public static void classLoaderHierarchyDemo() {
        System.out.println("=== 类加载器层次结构演示 ===");
        
        // 获取当前类的类加载器
        ClassLoader currentClassLoader = ClassLoadingDemo.class.getClassLoader();
        
        System.out.println("当前类的类加载器层次:");
        ClassLoader loader = currentClassLoader;
        int level = 1;
        
        while (loader != null) {
            System.out.println("第" + level + "层: " + loader.getClass().getName() + " - " + loader);
            loader = loader.getParent();
            level++;
        }
        
        System.out.println("第" + level + "层: Bootstrap ClassLoader (null)");
        
        // 不同类型类的加载器
        System.out.println("\n不同类型类的加载器:");
        
        // 核心API类（Bootstrap ClassLoader）
        System.out.println("String类加载器: " + String.class.getClassLoader());
        
        // 扩展API类
        try {
            Class<?> zipFileClass = Class.forName("java.util.zip.ZipFile");
            System.out.println("ZipFile类加载器: " + zipFileClass.getClassLoader());
        } catch (ClassNotFoundException e) {
            System.out.println("ZipFile类未找到");
        }
        
        // 应用程序类（Application ClassLoader）
        System.out.println("当前类加载器: " + currentClassLoader);
        
        // 数组类的加载器（和元素类型相同）
        String[] stringArray = new String[0];
        System.out.println("String[]类加载器: " + stringArray.getClass().getClassLoader());
        
        int[] intArray = new int[0];
        System.out.println("int[]类加载器: " + intArray.getClass().getClassLoader());
    }
    
    public static void classLoadingProcessDemo() {
        System.out.println("=== 类加载过程演示 ===");
        
        System.out.println("类加载的三个阶段：加载(Loading) -> 链接(Linking) -> 初始化(Initialization)");
        System.out.println("链接包含：验证(Verification) -> 准备(Preparation) -> 解析(Resolution)");
        
        System.out.println("\n演示类初始化顺序:");
        
        // 触发ParentClass的加载和初始化
        System.out.println("访问子类静态字段...");
        System.out.println("ChildClass.childField = " + ChildClass.childField);
        
        System.out.println("\n创建子类实例...");
        ChildClass child = new ChildClass();
        
        System.out.println("\n调用子类方法...");
        child.childMethod();
    }
    
    // 演示类初始化的父类
    static class ParentClass {
        static {
            System.out.println("ParentClass 静态初始化块执行");
        }
        
        public static String parentField = initParentField();
        
        private static String initParentField() {
            System.out.println("ParentClass.parentField 初始化");
            return "parent";
        }
        
        {
            System.out.println("ParentClass 实例初始化块执行");
        }
        
        public ParentClass() {
            System.out.println("ParentClass 构造函数执行");
        }
        
        public void parentMethod() {
            System.out.println("ParentClass.parentMethod() 调用");
        }
    }
    
    // 演示类初始化的子类
    static class ChildClass extends ParentClass {
        static {
            System.out.println("ChildClass 静态初始化块执行");
        }
        
        public static String childField = initChildField();
        
        private static String initChildField() {
            System.out.println("ChildClass.childField 初始化");
            return "child";
        }
        
        {
            System.out.println("ChildClass 实例初始化块执行");
        }
        
        public ChildClass() {
            System.out.println("ChildClass 构造函数执行");
        }
        
        public void childMethod() {
            System.out.println("ChildClass.childMethod() 调用");
            super.parentMethod();
        }
    }
    
    public static void customClassLoaderDemo() {
        System.out.println("=== 自定义类加载器演示 ===");
        
        try {
            // 创建自定义类加载器
            CustomClassLoader customLoader = new CustomClassLoader();
            
            // 使用自定义类加载器加载类
            Class<?> customClass = customLoader.loadClass("com.learning.jvm.CustomLoadedClass");
            System.out.println("自定义加载的类: " + customClass.getName());
            System.out.println("类加载器: " + customClass.getClassLoader());
            
            // 创建实例并调用方法
            Object instance = customClass.getDeclaredConstructor().newInstance();
            Method method = customClass.getDeclaredMethod("sayHello");
            method.invoke(instance);
            
        } catch (Exception e) {
            System.out.println("自定义类加载器演示出错: " + e.getMessage());
            
            // 演示简单的自定义类加载逻辑
            System.out.println("演示自定义类加载器的工作原理:");
            CustomClassLoader loader = new CustomClassLoader();
            loader.demonstrateLoading();
        }
    }
    
    // 自定义类加载器
    static class CustomClassLoader extends ClassLoader {
        
        public CustomClassLoader() {
            super(ClassLoadingDemo.class.getClassLoader());
        }
        
        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException {
            System.out.println("CustomClassLoader.findClass() 被调用: " + name);
            
            // 简单演示：对于特定的类名，返回动态生成的字节码
            if (name.equals("com.learning.jvm.DynamicClass")) {
                byte[] classBytes = generateSimpleClass(name);
                return defineClass(name, classBytes, 0, classBytes.length);
            }
            
            throw new ClassNotFoundException(name);
        }
        
        public void demonstrateLoading() {
            System.out.println("自定义类加载器工作流程:");
            System.out.println("1. 检查类是否已经被加载 (findLoadedClass)");
            System.out.println("2. 委托父类加载器加载 (parent.loadClass)");
            System.out.println("3. 如果父类加载器无法加载，调用 findClass");
            System.out.println("4. findClass 中读取字节码并调用 defineClass");
        }
        
        // 生成简单类的字节码（实际项目中可能使用ASM或Javassist）
        private byte[] generateSimpleClass(String className) {
            // 这里只是示例，实际应该生成有效的字节码
            return new byte[0];
        }
    }
    
    public static void classLoadingFeaturesDemo() {
        System.out.println("=== 类加载特性演示 ===");
        
        // 1. 双亲委派模型
        System.out.println("1. 双亲委派模型:");
        System.out.println("   - 子类加载器首先委派给父类加载器");
        System.out.println("   - 父类加载器无法加载时，子类加载器才尝试加载");
        System.out.println("   - 保证核心类库的安全性");
        
        // 2. 类的唯一性
        System.out.println("\n2. 类的唯一性:");
        System.out.println("   - 同一个类由不同类加载器加载会被视为不同的类");
        try {
            ClassLoader loader1 = new URLClassLoader(new URL[0]);
            ClassLoader loader2 = new URLClassLoader(new URL[0]);
            
            System.out.println("   不同类加载器实例: " + (loader1 != loader2));
        } catch (Exception e) {
            System.out.println("   类加载器比较出错: " + e.getMessage());
        }
        
        // 3. 类的加载时机
        System.out.println("\n3. 类的加载时机（懒加载）:");
        System.out.println("   - 首次创建类实例时");
        System.out.println("   - 访问类的静态变量时");
        System.out.println("   - 调用类的静态方法时");
        System.out.println("   - 使用反射操作类时");
        System.out.println("   - 子类被加载时，父类也会被加载");
        
        // 4. 演示类的延迟加载
        System.out.println("\n4. 延迟加载演示:");
        System.out.println("LazyClass还未被加载...");
        
        // 这时LazyClass还没有被加载
        try {
            Thread.sleep(1000);
            System.out.println("现在访问LazyClass.VALUE...");
            
            // 这里会触发LazyClass的加载和初始化
            System.out.println("LazyClass.VALUE = " + LazyClass.VALUE);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    // 用于演示延迟加载的类
    static class LazyClass {
        static {
            System.out.println("LazyClass 正在被初始化...");
        }
        
        public static final String VALUE = "LazyValue";
    }
    
    public static void bytecodeDemo() {
        System.out.println("=== 字节码相关演示 ===");
        
        // 获取类文件信息
        Class<?> clazz = ClassLoadingDemo.class;
        System.out.println("类名: " + clazz.getName());
        System.out.println("简单类名: " + clazz.getSimpleName());
        System.out.println("包名: " + clazz.getPackage().getName());
        
        // 获取类文件位置
        String className = clazz.getName().replace('.', '/') + ".class";
        URL resource = clazz.getClassLoader().getResource(className);
        System.out.println("类文件位置: " + resource);
        
        // 字节码相关工具介绍
        System.out.println("\n字节码操作工具:");
        System.out.println("1. javap - JDK自带的字节码分析工具");
        System.out.println("   使用: javap -c -v ClassName");
        System.out.println("2. ASM - 强大的字节码操作框架");
        System.out.println("3. Javassist - 简单易用的字节码操作库");
        System.out.println("4. CGLIB - 代码生成库");
        
        // 常用字节码指令介绍
        System.out.println("\n常用字节码指令:");
        System.out.println("- 加载指令: aload, iload, lload, fload, dload");
        System.out.println("- 存储指令: astore, istore, lstore, fstore, dstore");
        System.out.println("- 运算指令: iadd, isub, imul, idiv");
        System.out.println("- 类型转换: i2l, i2f, i2d, l2i, f2i, d2i");
        System.out.println("- 方法调用: invokevirtual, invokespecial, invokestatic, invokeinterface");
        System.out.println("- 返回指令: ireturn, lreturn, freturn, dreturn, areturn, return");
    }
    
    public static void classLoadingBestPractices() {
        System.out.println("=== 类加载最佳实践 ===");
        
        System.out.println("1. 类加载器使用建议:");
        System.out.println("   - 遵循双亲委派模型，不要轻易破坏");
        System.out.println("   - 自定义类加载器时要考虑类的唯一性");
        System.out.println("   - 避免类加载器泄漏");
        
        System.out.println("\n2. 性能优化建议:");
        System.out.println("   - 合理组织类路径，减少查找时间");
        System.out.println("   - 使用类预加载减少首次访问延迟");
        System.out.println("   - 避免不必要的静态初始化");
        
        System.out.println("\n3. 安全性考虑:");
        System.out.println("   - 验证自定义类加载器的安全性");
        System.out.println("   - 注意类加载过程中的权限检查");
        System.out.println("   - 防止恶意代码通过类加载注入");
        
        System.out.println("\n4. 调试技巧:");
        System.out.println("   - 使用 -verbose:class 查看类加载过程");
        System.out.println("   - 使用 -XX:+TraceClassLoading 跟踪类加载");
        System.out.println("   - 分析类加载器层次关系");
        System.out.println("   - 检查类路径配置");
        
        System.out.println("\n5. 常见问题排查:");
        System.out.println("   - ClassNotFoundException: 类路径问题");
        System.out.println("   - NoClassDefFoundError: 类初始化失败");
        System.out.println("   - ClassCastException: 类加载器不同导致");
        System.out.println("   - LinkageError: 类链接阶段错误");
    }
    
    public static void main(String[] args) {
        System.out.println("JVM类加载机制学习演示");
        System.out.println("======================");
        
        classLoaderHierarchyDemo();
        System.out.println("\n" + "=".repeat(50) + "\n");
        
        classLoadingProcessDemo();
        System.out.println("\n" + "=".repeat(50) + "\n");
        
        customClassLoaderDemo();
        System.out.println("\n" + "=".repeat(50) + "\n");
        
        classLoadingFeaturesDemo();
        System.out.println("\n" + "=".repeat(50) + "\n");
        
        bytecodeDemo();
        System.out.println("\n" + "=".repeat(50) + "\n");
        
        classLoadingBestPractices();
    }
}