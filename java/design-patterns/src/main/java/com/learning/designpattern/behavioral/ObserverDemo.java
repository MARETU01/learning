package com.learning.designpattern.behavioral;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 观察者模式演示
 * 
 * 观察者模式定义了对象之间一对多的依赖关系，当一个对象状态发生改变时，
 * 所有依赖于它的对象都会得到通知并自动更新。
 * 
 * 应用场景：
 * 1. 当一个对象的改变需要同时改变其他对象的时候
 * 2. 当一个对象必须通知其他对象，而它又不能假定其他对象是谁的时候
 * 3. 当一个抽象模型有两个方面，其中一个方面依赖于另一方面的时候
 */
public class ObserverDemo {
    
    private static final Logger log = LoggerFactory.getLogger(ObserverDemo.class);

    // 观察者接口
    public interface Observer {
        void update(String message);
    }

    // 被观察者接口（主题）
    public interface Subject {
        void attach(Observer observer);
        void detach(Observer observer);
        void notifyObservers();
        void setState(String state);
        String getState();
    }

    // 具体被观察者
    public static class ConcreteSubject implements Subject {
        private List<Observer> observers = new ArrayList<>();
        private String state;

        @Override
        public void attach(Observer observer) {
            if (!observers.contains(observer)) {
                observers.add(observer);
                log.info("添加观察者: {}", observer.getClass().getSimpleName());
            }
        }

        @Override
        public void detach(Observer observer) {
            observers.remove(observer);
            log.info("移除观察者: {}", observer.getClass().getSimpleName());
        }

        @Override
        public void notifyObservers() {
            for (Observer observer : observers) {
                observer.update(state);
            }
        }

        @Override
        public void setState(String state) {
            this.state = state;
            log.info("主题状态更新为: {}", state);
            notifyObservers();
        }

        @Override
        public String getState() {
            return state;
        }
    }

    // 具体观察者1
    public static class ConcreteObserver1 implements Observer {
        @Override
        public void update(String message) {
            log.info("观察者1收到通知: {}", message);
            log.info("观察者1执行相应操作...");
        }
    }

    // 具体观察者2
    public static class ConcreteObserver2 implements Observer {
        @Override
        public void update(String message) {
            log.info("观察者2收到通知: {}", message);
            log.info("观察者2执行相应操作...");
        }
    }

    // 具体观察者3
    public static class ConcreteObserver3 implements Observer {
        @Override
        public void update(String message) {
            log.info("观察者3收到通知: {}", message);
            log.info("观察者3执行相应操作...");
        }
    }

    // 天气站示例（经典观察者模式应用）
    public static class WeatherStation {
        public static class WeatherData {
            private float temperature;
            private float humidity;
            private float pressure;

            public WeatherData(float temperature, float humidity, float pressure) {
                this.temperature = temperature;
                this.humidity = humidity;
                this.pressure = pressure;
            }

            public float getTemperature() {
                return temperature;
            }

            public float getHumidity() {
                return humidity;
            }

            public float getPressure() {
                return pressure;
            }

            @Override
            public String toString() {
                return String.format("温度: %.1f°C, 湿度: %.1f%%, 气压: %.1fhPa", 
                                   temperature, humidity, pressure);
            }
        }

        public interface WeatherObserver {
            void update(WeatherData weatherData);
        }

        public static class WeatherSubject {
            private List<WeatherObserver> observers = new ArrayList<>();
            private WeatherData weatherData;

            public void addObserver(WeatherObserver observer) {
                observers.add(observer);
            }

            public void removeObserver(WeatherObserver observer) {
                observers.remove(observer);
            }

            public void notifyObservers() {
                for (WeatherObserver observer : observers) {
                    observer.update(weatherData);
                }
            }

            public void setWeatherData(WeatherData weatherData) {
                this.weatherData = weatherData;
                log.info("天气数据更新: {}", weatherData);
                notifyObservers();
            }
        }

        public static class PhoneDisplay implements WeatherObserver {
            @Override
            public void update(WeatherData weatherData) {
                log.info("手机显示更新: {}", weatherData);
            }
        }

        public static class TVDisplay implements WeatherObserver {
            @Override
            public void update(WeatherData weatherData) {
                log.info("电视显示更新: {}", weatherData);
            }
        }

        public static class WebsiteDisplay implements WeatherObserver {
            @Override
            public void update(WeatherData weatherData) {
                log.info("网站显示更新: {}", weatherData);
            }
        }
    }

    // 股票市场示例
    public static class StockMarket {
        public static class Stock {
            private String symbol;
            private double price;

            public Stock(String symbol, double price) {
                this.symbol = symbol;
                this.price = price;
            }

            public String getSymbol() {
                return symbol;
            }

            public double getPrice() {
                return price;
            }

            public void setPrice(double price) {
                this.price = price;
            }

            @Override
            public String toString() {
                return String.format("%s: $%.2f", symbol, price);
            }
        }

        public interface StockObserver {
            void update(Stock stock);
        }

        public static class StockExchange {
            private List<StockObserver> observers = new ArrayList<>();
            private List<Stock> stocks = new ArrayList<>();

            public void addObserver(StockObserver observer) {
                observers.add(observer);
            }

            public void removeObserver(StockObserver observer) {
                observers.remove(observer);
            }

            public void addStock(Stock stock) {
                stocks.add(stock);
            }

            public void updateStockPrice(String symbol, double newPrice) {
                for (Stock stock : stocks) {
                    if (stock.getSymbol().equals(symbol)) {
                        stock.setPrice(newPrice);
                        log.info("股票价格更新: {}", stock);
                        notifyObservers(stock);
                        break;
                    }
                }
            }

            private void notifyObservers(Stock stock) {
                for (StockObserver observer : observers) {
                    observer.update(stock);
                }
            }
        }

        public static class Investor implements StockObserver {
            private String name;

            public Investor(String name) {
                this.name = name;
            }

            @Override
            public void update(Stock stock) {
                log.info("投资者 {} 收到股票更新: {}", name, stock);
            }
        }

        public static class Broker implements StockObserver {
            private String company;

            public Broker(String company) {
                this.company = company;
            }

            @Override
            public void update(Stock stock) {
                log.info("券商 {} 收到股票更新: {}", company, stock);
            }
        }
    }

    // 演示观察者模式的使用
    public static void main(String[] args) {
        log.info("=== 观察者模式演示 ===");
        
        // 1. 基本观察者模式演示
        log.info("\n1. 基本观察者模式：");
        Subject subject = new ConcreteSubject();
        
        Observer observer1 = new ConcreteObserver1();
        Observer observer2 = new ConcreteObserver2();
        Observer observer3 = new ConcreteObserver3();
        
        // 添加观察者
        subject.attach(observer1);
        subject.attach(observer2);
        subject.attach(observer3);
        
        // 主题状态改变，通知所有观察者
        subject.setState("状态1");
        
        // 移除一个观察者
        subject.detach(observer2);
        
        // 再次改变状态
        subject.setState("状态2");
        
        // 2. 天气站示例
        log.info("\n2. 天气站示例：");
        WeatherStation.WeatherSubject weatherSubject = new WeatherStation.WeatherSubject();
        
        WeatherStation.WeatherObserver phoneDisplay = new WeatherStation.PhoneDisplay();
        WeatherStation.WeatherObserver tvDisplay = new WeatherStation.TVDisplay();
        WeatherStation.WeatherObserver websiteDisplay = new WeatherStation.WebsiteDisplay();
        
        weatherSubject.addObserver(phoneDisplay);
        weatherSubject.addObserver(tvDisplay);
        weatherSubject.addObserver(websiteDisplay);
        
        WeatherStation.WeatherData weatherData1 = new WeatherStation.WeatherData(25.5f, 60.0f, 1013.2f);
        weatherSubject.setWeatherData(weatherData1);
        
        WeatherStation.WeatherData weatherData2 = new WeatherStation.WeatherData(26.8f, 58.5f, 1012.8f);
        weatherSubject.setWeatherData(weatherData2);
        
        // 3. 股票市场示例
        log.info("\n3. 股票市场示例：");
        StockMarket.StockExchange stockExchange = new StockMarket.StockExchange();
        
        StockMarket.StockObserver investor1 = new StockMarket.Investor("张三");
        StockMarket.StockObserver investor2 = new StockMarket.Investor("李四");
        StockMarket.StockObserver broker = new StockMarket.Broker("华泰证券");
        
        stockExchange.addObserver(investor1);
        stockExchange.addObserver(investor2);
        stockExchange.addObserver(broker);
        
        StockMarket.Stock stock1 = new StockMarket.Stock("AAPL", 150.00);
        StockMarket.Stock stock2 = new StockMarket.Stock("GOOGL", 2800.00);
        
        stockExchange.addStock(stock1);
        stockExchange.addStock(stock2);
        
        stockExchange.updateStockPrice("AAPL", 152.50);
        stockExchange.updateStockPrice("GOOGL", 2850.00);
        
        // 观察者模式优缺点总结
        log.info("\n=== 观察者模式优缺点总结 ===");
        log.info("优点：");
        log.info("1. 实现了对象之间的松耦合");
        log.info("2. 支持广播通信，可以同时通知多个观察者");
        log.info("3. 符合开闭原则，可以随时增加新的观察者");
        log.info("4. 抽象了主体与观察者之间的耦合关系");
        
        log.info("\n缺点：");
        log.info("1. 如果观察者数量很多，通知所有观察者可能很耗时");
        log.info("2. 如果观察者与主体之间存在循环依赖，可能导致系统崩溃");
        log.info("3. 观察者只知道主体发生了变化，但不知道如何变化的");
        log.info("4. 通知顺序不确定，可能导致意外的行为");
        
        log.info("\n=== 实际应用场景 ===");
        log.info("1. GUI事件处理（按钮点击、鼠标移动等）");
        log.info("2. MVC架构中的模型和视图");
        log.info("3. 消息队列系统中的发布/订阅模式");
        log.info("4. Java AWT和Swing中的事件监听器");
        log.info("5. Spring框架中的事件机制");
        log.info("6. Android中的广播接收器");
        log.info("7. 响应式编程中的数据流");
        
        log.info("\n=== JDK中的观察者模式 ===");
        log.info("1. java.util.Observable和java.util.Observer");
        log.info("2. Java AWT事件模型");
        log.info("3. Java Swing事件模型");
        log.info("4. JavaFX属性和绑定");
        log.info("5. Java并发包中的Future和Promise");
    }
}
