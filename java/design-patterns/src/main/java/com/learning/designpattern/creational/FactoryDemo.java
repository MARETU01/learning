package com.learning.designpattern.creational;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 工厂模式演示
 * 
 * 工厂模式提供了一种创建对象的最佳方式。在工厂模式中，我们在创建对象时不会对客户端暴露创建逻辑，
 * 并且是通过使用一个共同的接口来指向新创建的对象。
 * 
 * 应用场景：
 * 1. 当一个类不知道它所必须创建的对象的类的时候
 * 2. 当一个类希望由其子类来指定它所创建的对象的时候
 * 3. 当类将创建对象的职责委托给多个帮助子类中的某一个，并且你希望将哪一个帮助子类是代理者这一信息局部化的时候
 */
public class FactoryDemo {
    
    private static final Logger log = LoggerFactory.getLogger(FactoryDemo.class);

    // 产品接口
    public interface Shape {
        void draw();
    }

    // 具体产品类
    public static class Circle implements Shape {
        @Override
        public void draw() {
            log.info("绘制圆形");
        }
    }

    public static class Rectangle implements Shape {
        @Override
        public void draw() {
            log.info("绘制矩形");
        }
    }

    public static class Triangle implements Shape {
        @Override
        public void draw() {
            log.info("绘制三角形");
        }
    }

    // 1. 简单工厂模式
    public static class SimpleShapeFactory {
        public Shape createShape(String shapeType) {
            if (shapeType == null) {
                return null;
            }
            
            switch (shapeType.toLowerCase()) {
                case "circle":
                    return new Circle();
                case "rectangle":
                    return new Rectangle();
                case "triangle":
                    return new Triangle();
                default:
                    throw new IllegalArgumentException("不支持的形状类型: " + shapeType);
            }
        }
    }

    // 2. 工厂方法模式
    public abstract static class AbstractShapeFactory {
        public abstract Shape createShape();
        
        public void drawShape() {
            Shape shape = createShape();
            shape.draw();
        }
    }

    public static class CircleFactory extends AbstractShapeFactory {
        @Override
        public Shape createShape() {
            return new Circle();
        }
    }

    public static class RectangleFactory extends AbstractShapeFactory {
        @Override
        public Shape createShape() {
            return new Rectangle();
        }
    }

    public static class TriangleFactory extends AbstractShapeFactory {
        @Override
        public Shape createShape() {
            return new Triangle();
        }
    }

    // 3. 抽象工厂模式
    public interface Color {
        void fill();
    }

    public static class Red implements Color {
        @Override
        public void fill() {
            log.info("填充红色");
        }
    }

    public static class Green implements Color {
        @Override
        public void fill() {
            log.info("填充绿色");
        }
    }

    public static class Blue implements Color {
        @Override
        public void fill() {
            log.info("填充蓝色");
        }
    }

    // 抽象工厂接口
    public interface AbstractFactory {
        Shape createShape(String shapeType);
        Color createColor(String colorType);
    }

    // 具体工厂
    public static class ShapeAndColorFactory implements AbstractFactory {
        @Override
        public Shape createShape(String shapeType) {
            if (shapeType == null) {
                return null;
            }
            
            switch (shapeType.toLowerCase()) {
                case "circle":
                    return new Circle();
                case "rectangle":
                    return new Rectangle();
                case "triangle":
                    return new Triangle();
                default:
                    throw new IllegalArgumentException("不支持的形状类型: " + shapeType);
            }
        }

        @Override
        public Color createColor(String colorType) {
            if (colorType == null) {
                return null;
            }
            
            switch (colorType.toLowerCase()) {
                case "red":
                    return new Red();
                case "green":
                    return new Green();
                case "blue":
                    return new Blue();
                default:
                    throw new IllegalArgumentException("不支持的颜色类型: " + colorType);
            }
        }
    }

    // 工厂生产者
    public static class FactoryProducer {
        public static AbstractFactory getFactory(String factoryType) {
            if (factoryType == null) {
                return null;
            }
            
            if (factoryType.equalsIgnoreCase("SHAPE_AND_COLOR")) {
                return new ShapeAndColorFactory();
            }
            
            return null;
        }
    }

    // 演示工厂模式的使用
    public static void main(String[] args) {
        log.info("=== 工厂模式演示 ===");
        
        // 1. 简单工厂模式演示
        log.info("\n1. 简单工厂模式：");
        SimpleShapeFactory simpleFactory = new SimpleShapeFactory();
        
        Shape circle = simpleFactory.createShape("circle");
        circle.draw();
        
        Shape rectangle = simpleFactory.createShape("rectangle");
        rectangle.draw();
        
        Shape triangle = simpleFactory.createShape("triangle");
        triangle.draw();
        
        // 2. 工厂方法模式演示
        log.info("\n2. 工厂方法模式：");
        AbstractShapeFactory circleFactory = new CircleFactory();
        circleFactory.drawShape();
        
        AbstractShapeFactory rectangleFactory = new RectangleFactory();
        rectangleFactory.drawShape();
        
        AbstractShapeFactory triangleFactory = new TriangleFactory();
        triangleFactory.drawShape();
        
        // 3. 抽象工厂模式演示
        log.info("\n3. 抽象工厂模式：");
        AbstractFactory abstractFactory = FactoryProducer.getFactory("SHAPE_AND_COLOR");
        
        if (abstractFactory != null) {
            // 创建形状
            Shape shape1 = abstractFactory.createShape("circle");
            shape1.draw();
            
            Shape shape2 = abstractFactory.createShape("rectangle");
            shape2.draw();
            
            // 创建颜色
            Color color1 = abstractFactory.createColor("red");
            color1.fill();
            
            Color color2 = abstractFactory.createColor("green");
            color2.fill();
            
            Color color3 = abstractFactory.createColor("blue");
            color3.fill();
        }
        
        // 工厂模式优缺点总结
        log.info("\n=== 工厂模式优缺点总结 ===");
        log.info("优点：");
        log.info("1. 解耦对象的创建和使用");
        log.info("2. 易于扩展，新增产品类时无需修改现有代码");
        log.info("3. 通过继承实现工厂方法，具有良好的扩展性");
        log.info("4. 屏蔽产品类的具体实现，调用者只关心产品的接口");
        
        log.info("\n缺点：");
        log.info("1. 增加了系统中类的数量");
        log.info("2. 增加了系统的复杂度和理解难度");
        log.info("3. 简单工厂模式违反开闭原则");
        log.info("4. 抽象工厂模式扩展产品族困难");
        
        log.info("\n=== 各种工厂模式比较 ===");
        log.info("简单工厂模式：");
        log.info("  - 优点：结构简单，调用方便");
        log.info("  - 缺点：违反开闭原则，扩展困难");
        log.info("  - 适用：产品种类较少且固定的场景");
        
        log.info("\n工厂方法模式：");
        log.info("  - 优点：符合开闭原则，扩展性好");
        log.info("  - 缺点：类数量增加，系统复杂度提高");
        log.info("  - 适用：需要灵活扩展产品类的场景");
        
        log.info("\n抽象工厂模式：");
        log.info("  - 优点：可以创建产品族，保证产品一致性");
        log.info("  - 缺点：扩展产品族困难，增加了抽象性");
        log.info("  - 适用：需要创建相关或依赖对象的家族");
        
        log.info("\n=== 实际应用场景 ===");
        log.info("1. JDK中的Calendar.getInstance()");
        log.info("2. Spring中的BeanFactory");
        log.info("3. Log4j中的LogManager");
        log.info("4. 数据库连接池的Connection获取");
        log.info("5. Java加密技术中的KeyGenerator");
    }
}
