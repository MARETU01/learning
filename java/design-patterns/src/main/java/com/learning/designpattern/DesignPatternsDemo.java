package com.learning.designpattern;

import com.learning.designpattern.creational.FactoryDemo;
import com.learning.designpattern.creational.SingletonDemo;
import com.learning.designpattern.behavioral.ObserverDemo;
import com.learning.designpattern.behavioral.StrategyDemo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 设计模式主演示类
 * 
 * 这个类演示了各种设计模式的使用方法，包括：
 * - 创建型模式：单例模式、工厂模式
 * - 行为型模式：观察者模式、策略模式
 * 
 * 运行这个类可以看到所有设计模式的演示效果。
 */
public class DesignPatternsDemo {
    
    private static final Logger log = LoggerFactory.getLogger(DesignPatternsDemo.class);

    public static void main(String[] args) {
        log.info("Java设计模式学习演示");
        log.info("=====================");
        
        // 创建型模式演示
        log.info("\n=== 创建型模式 ===");
        demonstrateCreationalPatterns();
        
        // 行为型模式演示
        log.info("\n=== 行为型模式 ===");
        demonstrateBehavioralPatterns();
        
        // 总结
        printSummary();
    }
    
    /**
     * 演示创建型模式
     */
    private static void demonstrateCreationalPatterns() {
        log.info("\n1. 单例模式演示：");
        log.info("-------------------");
        SingletonDemo.main(new String[0]);
        
        log.info("\n2. 工厂模式演示：");
        log.info("-------------------");
        FactoryDemo.main(new String[0]);
    }
    
    /**
     * 演示行为型模式
     */
    private static void demonstrateBehavioralPatterns() {
        log.info("\n1. 观察者模式演示：");
        log.info("-------------------");
        ObserverDemo.main(new String[0]);
        
        log.info("\n2. 策略模式演示：");
        log.info("-----------------");
        StrategyDemo.main(new String[0]);
    }
    
    /**
     * 打印设计模式学习总结
     */
    private static void printSummary() {
        log.info("\n=== 设计模式学习总结 ===");
        log.info("");
        
        log.info("设计模式是软件开发中经过验证的解决方案，它们提供了");
        log.info("解决常见问题的最佳实践。通过学习设计模式，我们可以：");
        log.info("");
        
        log.info("1. 提高代码的可重用性");
        log.info("2. 增强代码的可维护性");
        log.info("3. 使代码更易于理解");
        log.info("4. 促进团队协作");
        log.info("5. 加速开发过程");
        log.info("");
        
        log.info("已学习的设计模式：");
        log.info("");
        
        log.info("【创建型模式】");
        log.info("├── 单例模式：确保一个类只有一个实例");
        log.info("├── 工厂模式：创建对象的接口，让子类决定实例化哪个类");
        log.info("├── 建造者模式：分步骤构建复杂对象");
        log.info("├── 原型模式：通过复制现有对象创建新对象");
        log.info("└── 抽象工厂模式：创建相关对象的家族");
        log.info("");
        
        log.info("【行为型模式】");
        log.info("├── 观察者模式：定义对象间的一对多依赖关系");
        log.info("├── 策略模式：定义算法族，并使它们可以相互替换");
        log.info("├── 命令模式：将请求封装为对象");
        log.info("├── 责任链模式：将请求的发送者和接收者解耦");
        log.info("├── 迭代器模式：提供一种方法来访问聚合对象的元素");
        log.info("├── 中介者模式：定义一个中介对象来封装对象间的交互");
        log.info("├── 备忘录模式：在不破坏封装的前提下，捕获对象的内部状态");
        log.info("├── 解释器模式：定义语言的文法表示");
        log.info("├── 状态模式：允许对象在内部状态改变时改变它的行为");
        log.info("├── 模板方法模式：定义算法的骨架，让子类实现具体步骤");
        log.info("└── 访问者模式：在不改变数据结构的前提下，定义新的操作");
        log.info("");
        
        log.info("【结构型模式】");
        log.info("├── 适配器模式：将一个类的接口转换成客户端期望的接口");
        log.info("├── 装饰器模式：动态地给对象添加新的功能");
        log.info("├── 代理模式：为其他对象提供一种代理以控制对这个对象的访问");
        log.info("├── 外观模式：为子系统中的一组接口提供一个一致的界面");
        log.info("├── 桥接模式：将抽象部分与实现部分分离");
        log.info("├── 组合模式：将对象组合成树形结构以表示'部分-整体'的层次结构");
        log.info("└── 享元模式：运用共享技术来有效地支持大量细粒度的对象");
        log.info("");
        
        log.info("学习建议：");
        log.info("1. 先理解每个模式的核心思想和应用场景");
        log.info("2. 通过实际代码示例加深理解");
        log.info("3. 在实际项目中尝试应用");
        log.info("4. 阅读经典书籍如《设计模式：可复用面向对象软件的基础》");
        log.info("5. 学习开源框架中设计模式的应用");
        log.info("");
        
        log.info("记住：设计模式不是银弹，需要根据具体情况选择合适的模式。");
        log.info("过度使用设计模式可能会导致代码复杂化，应该适度使用。");
    }
}
