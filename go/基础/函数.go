package main

import (
	"errors"
	"fmt"
)

// add: 基本函数，返回两个整数的和
func add(a, b int) int {
	return a + b
}

// swap: 多返回值函数，返回交换后的两个字符串
func swap(a, b string) (string, string) {
	return b, a
}

// split: 命名返回值示例
func split(sum int) (x, y int) {
	x = sum * 4 / 9
	y = sum - x
	return // 使用命名返回值可以直接返回
}

// variadicSum: 可变参数函数
func variadicSum(nums ...int) int {
	total := 0
	for _, n := range nums {
		total += n
	}
	return total
}

// divide: 返回错误的函数示例
func divide(a, b int) (int, error) {
	if b == 0 {
		return 0, errors.New("除以零错误")
	}
	return a / b, nil
}

// pointerSwap: 使用指针修改调用者的值
func pointerSwap(a, b *int) {
	*a, *b = *b, *a
}

// recursion: 递归函数（斐波那契，简单示例）
func fib(n int) int {
	if n <= 1 {
		return n
	}
	return fib(n-1) + fib(n-2)
}

// 方法示例: 定义一个结构体并为其添加方法
type Rectangle struct {
	Width, Height float64
}

// Area: 值接收者方法
func (r Rectangle) Area() float64 {
	return r.Width * r.Height
}

// Scale: 指针接收者方法，用于修改原对象
func (r *Rectangle) Scale(factor float64) {
	r.Width *= factor
	r.Height *= factor
}

// closure: 闭包，返回一个计数器函数
func counter() func() int {
	count := 0
	return func() int {
		count++
		return count
	}
}

// deferExample: 展示 defer 的执行时机
func deferExample() {
	fmt.Println("deferExample start")
	defer fmt.Println("defer: first deferred")
	defer fmt.Println("defer: second deferred")
	fmt.Println("deferExample end")
}

func main() {
	fmt.Println("=== Go 函数示例 ===")

	// 基本函数
	fmt.Printf("add(3,4) = %d\n", add(3, 4))

	// 多返回值
	a, b := swap("hello", "world")
	fmt.Printf("swap: %s %s\n", a, b)

	// 命名返回值
	x, y := split(17)
	fmt.Printf("split(17) = %d, %d\n", x, y)

	// 可变参数
	fmt.Printf("variadicSum(1,2,3,4) = %d\n", variadicSum(1, 2, 3, 4))
	fmt.Printf("variadicSum(1,2,3,4) = %d  也可以传入切片\n", variadicSum([]int{1, 2, 3, 4}...)) // 也可以传切片

	// 错误返回
	if res, err := divide(10, 0); err != nil {
		fmt.Printf("divide error: %v\n", err)
	} else {
		fmt.Printf("divide: %d\n", res)
	}

	// 指针交换
	x1, x2 := 100, 200
	pointerSwap(&x1, &x2)
	fmt.Printf("pointerSwap -> x1=%d x2=%d\n", x1, x2)

	// 递归
	fmt.Printf("fib(7) = %d\n", fib(7))

	// 方法
	r := Rectangle{Width: 3, Height: 4}
	fmt.Printf("Area before scale: %.2f\n", r.Area())
	r.Scale(2)
	fmt.Printf("Area after scale: %.2f\n", r.Area())

	// 闭包
	cnt := counter()
	fmt.Printf("counter: %d %d %d\n", cnt(), cnt(), cnt())

	// 匿名函数
	func(msg string) {
		fmt.Println("匿名函数收到:", msg)
	}("hi")

	// defer 展示
	deferExample()

	fmt.Println("=== 示例结束 ===")
}
