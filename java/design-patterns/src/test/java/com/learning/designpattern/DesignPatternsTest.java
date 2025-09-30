package com.learning.designpattern;

import com.learning.designpattern.creational.SingletonDemo;
import com.learning.designpattern.creational.FactoryDemo;
import com.learning.designpattern.behavioral.ObserverDemo;
import com.learning.designpattern.behavioral.StrategyDemo;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 设计模式测试类
 * 
 * 这个类包含了对各种设计模式实现的单元测试，
 * 确保设计模式的正确实现和功能。
 */
public class DesignPatternsTest {
    
    private static final Logger log = LoggerFactory.getLogger(DesignPatternsTest.class);

    // ========== 单例模式测试 ==========
    
    @Test
    void testEagerSingleton() {
        log.info("=== 测试饿汉式单例模式 ===");
        
        SingletonDemo.EagerSingleton instance1 = SingletonDemo.EagerSingleton.getInstance();
        SingletonDemo.EagerSingleton instance2 = SingletonDemo.EagerSingleton.getInstance();
        
        // 验证是否为同一个实例
        assertSame(instance1, instance2, "饿汉式单例应该返回同一个实例");
        
        // 验证实例不为null
        assertNotNull(instance1, "实例不应该为null");
        
        log.info("饿汉式单例模式测试通过");
    }
    
    @Test
    void testLazySingleton() {
        log.info("=== 测试懒汉式单例模式 ===");
        
        SingletonDemo.LazySingleton instance1 = SingletonDemo.LazySingleton.getInstance();
        SingletonDemo.LazySingleton instance2 = SingletonDemo.LazySingleton.getInstance();
        
        // 验证是否为同一个实例
        assertSame(instance1, instance2, "懒汉式单例应该返回同一个实例");
        
        // 验证实例不为null
        assertNotNull(instance1, "实例不应该为null");
        
        log.info("懒汉式单例模式测试通过");
    }
    
    @Test
    void testThreadSafeLazySingleton() {
        log.info("=== 测试线程安全懒汉式单例模式 ===");
        
        SingletonDemo.ThreadSafeLazySingleton instance1 = SingletonDemo.ThreadSafeLazySingleton.getInstance();
        SingletonDemo.ThreadSafeLazySingleton instance2 = SingletonDemo.ThreadSafeLazySingleton.getInstance();
        
        // 验证是否为同一个实例
        assertSame(instance1, instance2, "线程安全懒汉式单例应该返回同一个实例");
        
        // 验证实例不为null
        assertNotNull(instance1, "实例不应该为null");
        
        log.info("线程安全懒汉式单例模式测试通过");
    }
    
    @Test
    void testDoubleCheckedLockingSingleton() {
        log.info("=== 测试双重检查锁定单例模式 ===");
        
        SingletonDemo.DoubleCheckedLockingSingleton instance1 = SingletonDemo.DoubleCheckedLockingSingleton.getInstance();
        SingletonDemo.DoubleCheckedLockingSingleton instance2 = SingletonDemo.DoubleCheckedLockingSingleton.getInstance();
        
        // 验证是否为同一个实例
        assertSame(instance1, instance2, "双重检查锁定单例应该返回同一个实例");
        
        // 验证实例不为null
        assertNotNull(instance1, "实例不应该为null");
        
        log.info("双重检查锁定单例模式测试通过");
    }
    
    @Test
    void testStaticInnerClassSingleton() {
        log.info("=== 测试静态内部类单例模式 ===");
        
        SingletonDemo.StaticInnerClassSingleton instance1 = SingletonDemo.StaticInnerClassSingleton.getInstance();
        SingletonDemo.StaticInnerClassSingleton instance2 = SingletonDemo.StaticInnerClassSingleton.getInstance();
        
        // 验证是否为同一个实例
        assertSame(instance1, instance2, "静态内部类单例应该返回同一个实例");
        
        // 验证实例不为null
        assertNotNull(instance1, "实例不应该为null");
        
        log.info("静态内部类单例模式测试通过");
    }
    
    @Test
    void testEnumSingleton() {
        log.info("=== 测试枚举单例模式 ===");
        
        SingletonDemo.EnumSingleton instance1 = SingletonDemo.EnumSingleton.INSTANCE;
        SingletonDemo.EnumSingleton instance2 = SingletonDemo.EnumSingleton.INSTANCE;
        
        // 验证是否为同一个实例
        assertSame(instance1, instance2, "枚举单例应该返回同一个实例");
        
        // 验证实例不为null
        assertNotNull(instance1, "实例不应该为null");
        
        log.info("枚举单例模式测试通过");
    }
    
    @Test
    void testRegisteredSingleton() {
        log.info("=== 测试登记式单例模式 ===");
        
        SingletonDemo.RegisteredSingleton instance1 = SingletonDemo.RegisteredSingleton.getInstance("test");
        SingletonDemo.RegisteredSingleton instance2 = SingletonDemo.RegisteredSingleton.getInstance("test");
        SingletonDemo.RegisteredSingleton instance3 = SingletonDemo.RegisteredSingleton.getInstance("different");
        
        // 验证相同名称返回同一个实例
        assertSame(instance1, instance2, "相同名称的登记式单例应该返回同一个实例");
        
        // 验证不同名称返回不同实例
        assertNotSame(instance1, instance3, "不同名称的登记式单例应该返回不同实例");
        
        // 验证实例不为null
        assertNotNull(instance1, "实例不应该为null");
        assertNotNull(instance3, "实例不应该为null");
        
        log.info("登记式单例模式测试通过");
    }
    
    // ========== 工厂模式测试 ==========
    
    @Test
    void testSimpleFactory() {
        log.info("=== 测试简单工厂模式 ===");
        
        FactoryDemo.SimpleShapeFactory factory = new FactoryDemo.SimpleShapeFactory();
        
        // 测试创建圆形
        FactoryDemo.Shape circle = factory.createShape("circle");
        assertNotNull(circle, "圆形不应该为null");
        circle.draw();
        
        // 测试创建矩形
        FactoryDemo.Shape rectangle = factory.createShape("rectangle");
        assertNotNull(rectangle, "矩形不应该为null");
        rectangle.draw();
        
        // 测试创建三角形
        FactoryDemo.Shape triangle = factory.createShape("triangle");
        assertNotNull(triangle, "三角形不应该为null");
        triangle.draw();
        
        // 测试无效输入
        assertThrows(IllegalArgumentException.class, () -> {
            factory.createShape("invalid");
        }, "应该抛出IllegalArgumentException异常");
        
        log.info("简单工厂模式测试通过");
    }
    
    @Test
    void testFactoryMethod() {
        log.info("=== 测试工厂方法模式 ===");
        
        FactoryDemo.AbstractShapeFactory circleFactory = new FactoryDemo.CircleFactory();
        FactoryDemo.AbstractShapeFactory rectangleFactory = new FactoryDemo.RectangleFactory();
        FactoryDemo.AbstractShapeFactory triangleFactory = new FactoryDemo.TriangleFactory();
        
        // 测试圆形工厂
        FactoryDemo.Shape circle = circleFactory.createShape();
        assertNotNull(circle, "圆形不应该为null");
        circle.draw();
        
        // 测试矩形工厂
        FactoryDemo.Shape rectangle = rectangleFactory.createShape();
        assertNotNull(rectangle, "矩形不应该为null");
        rectangle.draw();
        
        // 测试三角形工厂
        FactoryDemo.Shape triangle = triangleFactory.createShape();
        assertNotNull(triangle, "三角形不应该为null");
        triangle.draw();
        
        log.info("工厂方法模式测试通过");
    }
    
    @Test
    void testAbstractFactory() {
        log.info("=== 测试抽象工厂模式 ===");
        
        FactoryDemo.AbstractFactory factory = FactoryDemo.FactoryProducer.getFactory("SHAPE_AND_COLOR");
        assertNotNull(factory, "工厂不应该为null");
        
        // 测试创建形状
        FactoryDemo.Shape circle = factory.createShape("circle");
        assertNotNull(circle, "圆形不应该为null");
        circle.draw();
        
        // 测试创建颜色
        FactoryDemo.Color red = factory.createColor("red");
        assertNotNull(red, "红色不应该为null");
        red.fill();
        
        // 测试无效输入
        assertThrows(IllegalArgumentException.class, () -> {
            factory.createShape("invalid");
        }, "应该抛出IllegalArgumentException异常");
        
        assertThrows(IllegalArgumentException.class, () -> {
            factory.createColor("invalid");
        }, "应该抛出IllegalArgumentException异常");
        
        log.info("抽象工厂模式测试通过");
    }
    
    // ========== 观察者模式测试 ==========
    
    @Test
    void testObserverPattern() {
        log.info("=== 测试观察者模式 ===");
        
        ObserverDemo.Subject subject = new ObserverDemo.ConcreteSubject();
        ObserverDemo.Observer observer1 = new ObserverDemo.ConcreteObserver1();
        ObserverDemo.Observer observer2 = new ObserverDemo.ConcreteObserver2();
        
        // 测试添加观察者
        subject.attach(observer1);
        subject.attach(observer2);
        
        // 测试通知观察者
        subject.setState("测试状态");
        
        // 测试移除观察者
        subject.detach(observer2);
        subject.setState("新状态");
        
        log.info("观察者模式测试通过");
    }
    
    @Test
    void testWeatherStation() {
        log.info("=== 测试天气站观察者模式 ===");
        
        ObserverDemo.WeatherStation.WeatherSubject weatherSubject = new ObserverDemo.WeatherStation.WeatherSubject();
        ObserverDemo.WeatherStation.WeatherObserver phoneDisplay = new ObserverDemo.WeatherStation.PhoneDisplay();
        ObserverDemo.WeatherStation.WeatherObserver tvDisplay = new ObserverDemo.WeatherStation.TVDisplay();
        
        // 添加观察者
        weatherSubject.addObserver(phoneDisplay);
        weatherSubject.addObserver(tvDisplay);
        
        // 测试天气数据更新
        ObserverDemo.WeatherStation.WeatherData weatherData = 
            new ObserverDemo.WeatherStation.WeatherData(25.0f, 60.0f, 1013.0f);
        weatherSubject.setWeatherData(weatherData);
        
        log.info("天气站观察者模式测试通过");
    }
    
    @Test
    void testStockMarket() {
        log.info("=== 测试股票市场观察者模式 ===");
        
        ObserverDemo.StockMarket.StockExchange stockExchange = new ObserverDemo.StockMarket.StockExchange();
        ObserverDemo.StockMarket.StockObserver investor = new ObserverDemo.StockMarket.Investor("测试投资者");
        
        // 添加观察者
        stockExchange.addObserver(investor);
        
        // 添加股票
        ObserverDemo.StockMarket.Stock stock = new ObserverDemo.StockMarket.Stock("TEST", 100.0);
        stockExchange.addStock(stock);
        
        // 测试股票价格更新
        stockExchange.updateStockPrice("TEST", 105.0);
        
        log.info("股票市场观察者模式测试通过");
    }
    
    // ========== 策略模式测试 ==========
    
    @Test
    void testPaymentStrategy() {
        log.info("=== 测试支付策略模式 ===");
        
        StrategyDemo.PaymentStrategy creditCard = new StrategyDemo.CreditCardPayment("1234567890123456", "测试", "123", "12/25");
        StrategyDemo.PaymentStrategy alipay = new StrategyDemo.AlipayPayment("test@example.com", "password");
        StrategyDemo.PaymentStrategy wechat = new StrategyDemo.WechatPayment("test_wx");
        
        StrategyDemo.PaymentContext context = new StrategyDemo.PaymentContext(creditCard);
        
        // 测试信用卡支付
        context.processPayment(100.0);
        
        // 测试切换到支付宝
        context.setPaymentStrategy(alipay);
        context.processPayment(200.0);
        
        // 测试切换到微信
        context.setPaymentStrategy(wechat);
        context.processPayment(300.0);
        
        log.info("支付策略模式测试通过");
    }
    
    @Test
    void testShoppingCart() {
        log.info("=== 测试购物车策略模式 ===");
        
        StrategyDemo.ShoppingCart cart = new StrategyDemo.ShoppingCart();
        StrategyDemo.PaymentStrategy creditCard = new StrategyDemo.CreditCardPayment("1234567890123456", "测试", "123", "12/25");
        
        // 添加商品
        cart.addItem(new StrategyDemo.ShoppingCart.Item("测试商品", 10.0, 2));
        
        // 设置支付策略
        cart.setPaymentStrategy(creditCard);
        
        // 测试结算
        cart.checkout();
        
        log.info("购物车策略模式测试通过");
    }
    
    @Test
    void testSortingStrategy() {
        log.info("=== 测试排序策略模式 ===");
        
        int[] originalArray = {5, 2, 8, 1, 9};
        
        // 测试冒泡排序
        StrategyDemo.SortingStrategy.SortAlgorithm bubbleSort = new StrategyDemo.SortingStrategy.BubbleSort();
        StrategyDemo.SortingStrategy.Sorter bubbleSorter = new StrategyDemo.SortingStrategy.Sorter(bubbleSort);
        
        int[] array1 = originalArray.clone();
        bubbleSorter.performSort(array1);
        
        // 验证数组已排序
        assertTrue(isSorted(array1), "数组应该已排序");
        
        // 测试快速排序
        StrategyDemo.SortingStrategy.SortAlgorithm quickSort = new StrategyDemo.SortingStrategy.QuickSort();
        StrategyDemo.SortingStrategy.Sorter quickSorter = new StrategyDemo.SortingStrategy.Sorter(quickSort);
        
        int[] array2 = originalArray.clone();
        quickSorter.performSort(array2);
        
        // 验证数组已排序
        assertTrue(isSorted(array2), "数组应该已排序");
        
        // 测试归并排序
        StrategyDemo.SortingStrategy.SortAlgorithm mergeSort = new StrategyDemo.SortingStrategy.MergeSort();
        StrategyDemo.SortingStrategy.Sorter mergeSorter = new StrategyDemo.SortingStrategy.Sorter(mergeSort);
        
        int[] array3 = originalArray.clone();
        mergeSorter.performSort(array3);
        
        // 验证数组已排序
        assertTrue(isSorted(array3), "数组应该已排序");
        
        log.info("排序策略模式测试通过");
    }
    
    // ========== 辅助方法 ==========
    
    /**
     * 检查数组是否已排序
     */
    private boolean isSorted(int[] array) {
        for (int i = 0; i < array.length - 1; i++) {
            if (array[i] > array[i + 1]) {
                return false;
            }
        }
        return true;
    }
    
    // ========== 综合测试 ==========
    
    @Test
    void testAllPatternsIntegration() {
        log.info("=== 测试所有设计模式的集成 ===");
        
        // 1. 使用单例模式创建工厂
        // 这里假设有一个工厂管理器是单例的
        
        // 2. 使用工厂模式创建对象
        FactoryDemo.SimpleShapeFactory shapeFactory = new FactoryDemo.SimpleShapeFactory();
        FactoryDemo.Shape shape = shapeFactory.createShape("circle");
        assertNotNull(shape, "形状不应该为null");
        
        // 3. 使用观察者模式监听形状变化
        ObserverDemo.Subject subject = new ObserverDemo.ConcreteSubject();
        ObserverDemo.Observer observer = new ObserverDemo.ConcreteObserver1();
        subject.attach(observer);
        subject.setState("形状已创建");
        
        // 4. 使用策略模式处理形状
        StrategyDemo.PaymentStrategy paymentStrategy = new StrategyDemo.CashPayment();
        StrategyDemo.PaymentContext paymentContext = new StrategyDemo.PaymentContext(paymentStrategy);
        paymentContext.processPayment(50.0);
        
        log.info("所有设计模式集成测试通过");
    }
    
    @Test
    void testDesignPatternsDemo() {
        log.info("=== 测试设计模式主演示类 ===");
        
        // 测试主演示类能够正常运行
        assertDoesNotThrow(() -> {
            DesignPatternsDemo.main(new String[0]);
        }, "设计模式主演示类应该能够正常运行");
        
        log.info("设计模式主演示类测试通过");
    }
}
