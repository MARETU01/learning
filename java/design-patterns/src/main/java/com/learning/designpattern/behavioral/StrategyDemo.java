package com.learning.designpattern.behavioral;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 策略模式演示
 * 
 * 策略模式定义了一系列算法，将每个算法封装起来，并使它们可以相互替换。
 * 策略模式让算法独立于使用它的客户端而变化。
 * 
 * 应用场景：
 * 1. 当一个对象有很多的行为，而这些行为在操作中使用if-else语句来选择时
 * 2. 当需要在几种算法中动态地选择一种时
 * 3. 当对象的行为有很多种，且这些行为只是略有不同时
 * 4. 当算法需要自由切换时
 */
public class StrategyDemo {
    
    private static final Logger log = LoggerFactory.getLogger(StrategyDemo.class);

    // 策略接口
    public interface PaymentStrategy {
        void pay(double amount);
    }

    // 具体策略1：信用卡支付
    public static class CreditCardPayment implements PaymentStrategy {
        private String cardNumber;
        private String name;
        private String cvv;
        private String date;

        public CreditCardPayment(String cardNumber, String name, String cvv, String date) {
            this.cardNumber = cardNumber;
            this.name = name;
            this.cvv = cvv;
            this.date = date;
        }

        @Override
        public void pay(double amount) {
            log.info("使用信用卡支付 {:.2f} 元", amount);
            log.info("信用卡信息：");
            log.info("  卡号: {}", maskCardNumber(cardNumber));
            log.info("  持卡人: {}", name);
            log.info("  有效期: {}", date);
            log.info("支付处理中...");
            log.info("信用卡支付成功！");
        }

        private String maskCardNumber(String cardNumber) {
            if (cardNumber.length() >= 4) {
                return "**** **** **** " + cardNumber.substring(cardNumber.length() - 4);
            }
            return cardNumber;
        }
    }

    // 具体策略2：支付宝支付
    public static class AlipayPayment implements PaymentStrategy {
        private String account;
        private String password;

        public AlipayPayment(String account, String password) {
            this.account = account;
            this.password = password;
        }

        @Override
        public void pay(double amount) {
            log.info("使用支付宝支付 {:.2f} 元", amount);
            log.info("支付宝账户：{}", account);
            log.info("支付处理中...");
            log.info("支付宝支付成功！");
        }
    }

    // 具体策略3：微信支付
    public static class WechatPayment implements PaymentStrategy {
        private String account;

        public WechatPayment(String account) {
            this.account = account;
        }

        @Override
        public void pay(double amount) {
            log.info("使用微信支付 {:.2f} 元", amount);
            log.info("微信账户：{}", account);
            log.info("支付处理中...");
            log.info("微信支付成功！");
        }
    }

    // 具体策略4：现金支付
    public static class CashPayment implements PaymentStrategy {
        @Override
        public void pay(double amount) {
            log.info("使用现金支付 {:.2f} 元", amount);
            log.info("收取现金...");
            log.info("现金支付成功！");
        }
    }

    // 上下文类
    public static class PaymentContext {
        private PaymentStrategy paymentStrategy;

        public PaymentContext(PaymentStrategy paymentStrategy) {
            this.paymentStrategy = paymentStrategy;
        }

        public void setPaymentStrategy(PaymentStrategy paymentStrategy) {
            this.paymentStrategy = paymentStrategy;
        }

        public void processPayment(double amount) {
            if (paymentStrategy == null) {
                log.info("请先设置支付策略！");
                return;
            }
            paymentStrategy.pay(amount);
        }
    }

    // 购物车示例
    public static class ShoppingCart {
        public static class Item {
            private String name;
            private double price;
            private int quantity;

            public Item(String name, double price, int quantity) {
                this.name = name;
                this.price = price;
                this.quantity = quantity;
            }

            public double getTotalPrice() {
                return price * quantity;
            }

            @Override
            public String toString() {
                return String.format("%s x %d = %.2f", name, quantity, getTotalPrice());
            }
        }

        private java.util.List<Item> items = new java.util.ArrayList<>();
        private PaymentStrategy paymentStrategy;

        public void addItem(Item item) {
            items.add(item);
        }

        public void removeItem(Item item) {
            items.remove(item);
        }

        public double calculateTotal() {
            return items.stream().mapToDouble(Item::getTotalPrice).sum();
        }

        public void setPaymentStrategy(PaymentStrategy paymentStrategy) {
            this.paymentStrategy = paymentStrategy;
        }

        public void checkout() {
            double total = calculateTotal();
            log.info("=== 购物车结算 ===");
            log.info("商品清单：");
            for (Item item : items) {
                log.info("  {}", item);
            }
            log.info("总计：{:.2f} 元", total);
            log.info("================");
            
            if (paymentStrategy != null) {
                paymentStrategy.pay(total);
            } else {
                log.info("请先选择支付方式！");
            }
        }
    }

    // 排序算法策略示例
    public static class SortingStrategy {
        public interface SortAlgorithm {
            void sort(int[] array);
        }

        public static class BubbleSort implements SortAlgorithm {
            @Override
            public void sort(int[] array) {
                log.info("使用冒泡排序...");
                int n = array.length;
                for (int i = 0; i < n - 1; i++) {
                    for (int j = 0; j < n - i - 1; j++) {
                        if (array[j] > array[j + 1]) {
                            int temp = array[j];
                            array[j] = array[j + 1];
                            array[j + 1] = temp;
                        }
                    }
                }
                log.info("冒泡排序完成");
            }
        }

        public static class QuickSort implements SortAlgorithm {
            @Override
            public void sort(int[] array) {
                log.info("使用快速排序...");
                quickSort(array, 0, array.length - 1);
                log.info("快速排序完成");
            }

            private void quickSort(int[] array, int low, int high) {
                if (low < high) {
                    int pi = partition(array, low, high);
                    quickSort(array, low, pi - 1);
                    quickSort(array, pi + 1, high);
                }
            }

            private int partition(int[] array, int low, int high) {
                int pivot = array[high];
                int i = low - 1;
                for (int j = low; j < high; j++) {
                    if (array[j] < pivot) {
                        i++;
                        int temp = array[i];
                        array[i] = array[j];
                        array[j] = temp;
                    }
                }
                int temp = array[i + 1];
                array[i + 1] = array[high];
                array[high] = temp;
                return i + 1;
            }
        }

        public static class MergeSort implements SortAlgorithm {
            @Override
            public void sort(int[] array) {
                log.info("使用归并排序...");
                mergeSort(array, 0, array.length - 1);
                log.info("归并排序完成");
            }

            private void mergeSort(int[] array, int left, int right) {
                if (left < right) {
                    int mid = left + (right - left) / 2;
                    mergeSort(array, left, mid);
                    mergeSort(array, mid + 1, right);
                    merge(array, left, mid, right);
                }
            }

            private void merge(int[] array, int left, int mid, int right) {
                int n1 = mid - left + 1;
                int n2 = right - mid;
                int[] L = new int[n1];
                int[] R = new int[n2];
                for (int i = 0; i < n1; ++i) L[i] = array[left + i];
                for (int j = 0; j < n2; ++j) R[j] = array[mid + 1 + j];
                int i = 0, j = 0, k = left;
                while (i < n1 && j < n2) {
                    if (L[i] <= R[j]) array[k++] = L[i++];
                    else array[k++] = R[j++];
                }
                while (i < n1) array[k++] = L[i++];
                while (j < n2) array[k++] = R[j++];
            }
        }

        public static class Sorter {
            private SortAlgorithm algorithm;

            public Sorter(SortAlgorithm algorithm) {
                this.algorithm = algorithm;
            }

            public void setAlgorithm(SortAlgorithm algorithm) {
                this.algorithm = algorithm;
            }

            public void performSort(int[] array) {
                log.info("排序前数组：{}", java.util.Arrays.toString(array));
                algorithm.sort(array);
                log.info("排序后数组：{}", java.util.Arrays.toString(array));
            }
        }
    }

    // 演示策略模式的使用
    public static void main(String[] args) {
        log.info("=== 策略模式演示 ===");
        
        // 1. 支付策略示例
        log.info("\n1. 支付策略示例：");
        
        // 创建支付策略
        PaymentStrategy creditCard = new CreditCardPayment("1234567890123456", "张三", "123", "12/25");
        PaymentStrategy alipay = new AlipayPayment("zhangsan@example.com", "password123");
        PaymentStrategy wechat = new WechatPayment("zhangsan_wx");
        PaymentStrategy cash = new CashPayment();
        
        // 创建上下文
        PaymentContext paymentContext = new PaymentContext(creditCard);
        
        // 使用信用卡支付
        log.info("--- 信用卡支付 ---");
        paymentContext.processPayment(1000.0);
        
        // 切换到支付宝支付
        log.info("\n--- 支付宝支付 ---");
        paymentContext.setPaymentStrategy(alipay);
        paymentContext.processPayment(500.0);
        
        // 切换到微信支付
        log.info("\n--- 微信支付 ---");
        paymentContext.setPaymentStrategy(wechat);
        paymentContext.processPayment(300.0);
        
        // 切换到现金支付
        log.info("\n--- 现金支付 ---");
        paymentContext.setPaymentStrategy(cash);
        paymentContext.processPayment(200.0);
        
        // 2. 购物车示例
        log.info("\n2. 购物车示例：");
        ShoppingCart cart = new ShoppingCart();
        
        cart.addItem(new ShoppingCart.Item("笔记本电脑", 5999.0, 1));
        cart.addItem(new ShoppingCart.Item("鼠标", 99.0, 2));
        cart.addItem(new ShoppingCart.Item("键盘", 199.0, 1));
        
        // 使用信用卡支付
        cart.setPaymentStrategy(creditCard);
        cart.checkout();
        
        // 3. 排序算法策略示例
        log.info("\n3. 排序算法策略示例：");
        int[] array = {64, 34, 25, 12, 22, 11, 90};
        
        SortingStrategy.Sorter sorter = new SortingStrategy.Sorter(new SortingStrategy.BubbleSort());
        log.info("\n--- 冒泡排序 ---");
        sorter.performSort(array.clone());
        
        sorter.setAlgorithm(new SortingStrategy.QuickSort());
        log.info("\n--- 快速排序 ---");
        sorter.performSort(array.clone());
        
        sorter.setAlgorithm(new SortingStrategy.MergeSort());
        log.info("\n--- 归并排序 ---");
        sorter.performSort(array.clone());
        
        // 策略模式优缺点总结
        log.info("\n=== 策略模式优缺点总结 ===");
        log.info("优点：");
        log.info("1. 算法可以自由切换");
        log.info("2. 避免使用多重条件语句");
        log.info("3. 扩展性良好，增加新策略只需实现策略接口");
        log.info("4. 策略之间可以相互替换，提高了灵活性");
        
        log.info("\n缺点：");
        log.info("1. 策略类数量会增多");
        log.info("2. 所有策略类都需要对外暴露");
        log.info("3. 客户端必须了解所有策略的区别");
        log.info("4. 可能造成对象数量过多");
        
        log.info("\n=== 实际应用场景 ===");
        log.info("1. 支付系统中的多种支付方式");
        log.info("2. 排序算法的选择");
        log.info("3. 压缩算法的选择");
        log.info("4. 验证策略的选择");
        log.info("5. 出行路线规划算法");
        log.info("6. 游戏中的人工智能策略");
        log.info("7. Spring框架中的资源加载策略");
        
        log.info("\n=== JDK中的策略模式 ===");
        log.info("1. java.util.Comparator中的compare()方法");
        log.info("2. java.util.concurrent.Executor中的线程池策略");
        log.info("3. Java集合框架中的排序策略");
        log.info("4. Java NIO中的Buffer分配策略");
        log.info("5. Java加密中的算法选择");
    }
}
