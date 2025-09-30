# 设计模式学习模块

## 模块概述

本模块是Java学习项目中的设计模式学习模块，提供了经典设计模式的实现示例和详细说明。通过本模块，您可以深入理解各种设计模式的核心思想、应用场景和实现方式。

## 已实现的设计模式

### 创建型模式 (Creational Patterns)

#### 1. 单例模式 (Singleton Pattern)
- **文件位置**: `src/main/java/com/learning/designpattern/creational/SingletonDemo.java`
- **实现方式**:
  - 饿汉式单例模式
  - 懒汉式单例模式（线程不安全）
  - 懒汉式单例模式（线程安全，同步方法）
  - 双重检查锁定单例模式
  - 静态内部类单例模式
  - 枚举单例模式
  - 登记式单例模式
- **核心思想**: 确保一个类只有一个实例，并提供一个全局访问点
- **应用场景**: 配置管理器、线程池、缓存、日志对象等

#### 2. 工厂模式 (Factory Pattern)
- **文件位置**: `src/main/java/com/learning/designpattern/creational/FactoryDemo.java`
- **实现方式**:
  - 简单工厂模式
  - 工厂方法模式
  - 抽象工厂模式
- **核心思想**: 定义创建对象的接口，让子类决定实例化哪个类
- **应用场景**: 数据库连接、日志记录、支付方式等

### 行为型模式 (Behavioral Patterns)

#### 1. 观察者模式 (Observer Pattern)
- **文件位置**: `src/main/java/com/learning/designpattern/behavioral/ObserverDemo.java`
- **实现方式**:
  - 基础观察者模式
  - 天气站示例
  - 股票市场示例
- **核心思想**: 定义对象间的一对多依赖关系
- **应用场景**: 事件处理、MVC架构、消息队列等

#### 2. 策略模式 (Strategy Pattern)
- **文件位置**: `src/main/java/com/learning/designpattern/behavioral/StrategyDemo.java`
- **实现方式**:
  - 支付策略示例
  - 购物车示例
  - 排序算法策略示例
- **核心思想**: 定义算法族，并使它们可以相互替换
- **应用场景**: 支付方式、排序算法、压缩算法等

## 项目结构

```
design-patterns/
├── pom.xml                              # Maven配置文件
├── README.md                            # 模块说明文档
├── src/
│   ├── main/
│   │   └── java/
│   │       └── com/
│   │           └── learning/
│   │               └── designpattern/
│   │                   ├── DesignPatternsDemo.java        # 主演示类
│   │                   ├── creational/                    # 创建型模式
│   │                   │   ├── SingletonDemo.java         # 单例模式
│   │                   │   └── FactoryDemo.java           # 工厂模式
│   │                   └── behavioral/                    # 行为型模式
│   │                       ├── ObserverDemo.java         # 观察者模式
│   │                       └── StrategyDemo.java          # 策略模式
│   └── test/
│       └── java/
│           └── com/
│               └── learning/
│                   └── designpattern/
│                       └── DesignPatternsTest.java       # 测试类
```

## 如何运行

### 1. 运行单个设计模式演示

```bash
# 运行单例模式演示
cd design-patterns
mvn compile exec:java -Dexec.mainClass="com.learning.designpattern.creational.SingletonDemo"

# 运行工厂模式演示
mvn compile exec:java -Dexec.mainClass="com.learning.designpattern.creational.FactoryDemo"

# 运行观察者模式演示
mvn compile exec:java -Dexec.mainClass="com.learning.designpattern.behavioral.ObserverDemo"

# 运行策略模式演示
mvn compile exec:java -Dexec.mainClass="com.learning.designpattern.behavioral.StrategyDemo"
```

### 2. 运行所有设计模式演示

```bash
# 运行主演示类（包含所有设计模式）
mvn compile exec:java -Dexec.mainClass="com.learning.designpattern.DesignPatternsDemo"
```

### 3. 运行测试

```bash
# 运行所有测试
mvn test

# 运行特定测试类
mvn test -Dtest=DesignPatternsTest
```

## 学习建议

### 1. 学习顺序
1. **单例模式** - 最简单的设计模式，适合入门
2. **工厂模式** - 理解对象创建的抽象
3. **观察者模式** - 理解对象间的依赖关系
4. **策略模式** - 理解算法的封装和切换

### 2. 深入理解
- 阅读每个模式的核心思想和应用场景
- 运行代码示例，观察输出结果
- 尝试修改代码，理解每个部分的作用
- 思考在实际项目中如何应用这些模式

### 3. 实践建议
- 在实际项目中寻找应用场景
- 阅读开源框架中设计模式的应用
- 参与代码审查，识别设计模式的使用
- 尝试重构现有代码，应用设计模式

## 扩展计划

本模块计划继续实现以下设计模式：

### 创建型模式
- [ ] 建造者模式 (Builder Pattern)
- [ ] 原型模式 (Prototype Pattern)

### 行为型模式
- [ ] 命令模式 (Command Pattern)
- [ ] 责任链模式 (Chain of Responsibility Pattern)
- [ ] 迭代器模式 (Iterator Pattern)
- [ ] 中介者模式 (Mediator Pattern)
- [ ] 备忘录模式 (Memento Pattern)
- [ ] 解释器模式 (Interpreter Pattern)
- [ ] 状态模式 (State Pattern)
- [ ] 模板方法模式 (Template Method Pattern)
- [ ] 访问者模式 (Visitor Pattern)

### 结构型模式
- [ ] 适配器模式 (Adapter Pattern)
- [ ] 装饰器模式 (Decorator Pattern)
- [ ] 代理模式 (Proxy Pattern)
- [ ] 外观模式 (Facade Pattern)
- [ ] 桥接模式 (Bridge Pattern)
- [ ] 组合模式 (Composite Pattern)
- [ ] 享元模式 (Flyweight Pattern)

## 参考资源

### 书籍
- 《设计模式：可复用面向对象软件的基础》- GoF
- 《Head First设计模式》- Eric Freeman等
- 《Java设计模式》- 刘伟

### 在线资源
- [Refactoring.Guru](https://refactoring.guru/design-patterns)
- [Java设计模式教程](https://www.tutorialspoint.com/design_pattern/design_pattern_overview.htm)
- [Spring框架中的设计模式](https://springframework.guru/design-patterns/)

### 工具
- [PlantUML](https://plantuml.com/) - UML图表绘制工具
- [Draw.io](https://draw.io/) - 在线图表绘制工具
- [Visual Paradigm](https://www.visual-paradigm.com/) - UML建模工具

## 贡献指南

如果您想为本模块贡献代码或文档，请遵循以下步骤：

1. Fork 本项目
2. 创建新的分支 (`git checkout -b feature/new-pattern`)
3. 提交您的更改 (`git commit -am 'Add new pattern'`)
4. 推送到分支 (`git push origin feature/new-pattern`)
5. 创建Pull Request

## 许可证

本模块遵循与主项目相同的许可证。

## 联系方式

如有问题或建议，请通过以下方式联系：
- 创建Issue
- 发送邮件
- 参与讨论

---

**注意**: 本模块主要用于学习目的，在实际项目中使用设计模式时，请根据具体需求选择合适的模式，避免过度设计。
