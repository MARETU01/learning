package main

import (
	"fmt"
)

// defer.go
//
// 这个文件用一组小例子演示 Go 语言里 defer 的常见用法与注意点。
//
// 核心规则：
// 1) defer 会把“延迟调用”压栈，等当前函数返回前再执行
// 2) 执行顺序是 LIFO（后进先出）
// 3) defer 的“参数”在注册 defer 的那一刻就求值（函数体真正执行在 return 前）
// 4) defer 常用于资源清理、解锁、回收等，让代码更安全、更易读

func deferSection(title string) {
	fmt.Println("\n== " + title + " ==")
}

// exampleOrder 演示 defer 的 LIFO 执行顺序。
func exampleOrder() {
	deferSection("1) 执行顺序：LIFO（后进先出）")

	fmt.Println("enter exampleOrder")
	defer fmt.Println("defer #1")
	defer fmt.Println("defer #2")
	defer fmt.Println("defer #3")
	fmt.Println("leave exampleOrder")

	// 预期输出：
	// enter
	// leave
	// defer #3
	// defer #2
	// defer #1
}

// exampleArgEval 演示 defer 参数求值时机：注册 defer 时就已经把参数值算好了。
func exampleArgEval() {
	deferSection("2) 参数求值：defer 语句执行时就求值")

	i := 1
	defer fmt.Printf("defer print i=%d\n", i) // 这里 i=1 会被捕获为参数值

	i = 2
	fmt.Printf("now i=%d\n", i)

	// 注意：如果你希望 defer 时读取“最新的 i”，通常用闭包：
	j := 10
	defer func() {
		fmt.Printf("defer closure j=%d\n", j) // 这里读取的是执行时的 j
	}()
	j = 20

	// 预期：
	// now i=2
	// defer closure j=20
	// defer print i=1
	// （closure defer 注册在后面，所以先执行）
}

// trace 用于演示“函数返回值 + defer 修改”的场景。
func trace(name string) func() {
	fmt.Println("enter", name)
	return func() {
		fmt.Println("exit ", name)
	}
}

// exampleNamedReturn 演示 defer 可以读取/修改“具名返回值”。
func exampleNamedReturn() {
	deferSection("3) 具名返回值：defer 可读取/修改返回值")

	fmt.Println("result =", addWithDefer(10, 3))
	// addWithDefer 的返回值会被 defer 在 return 之前修改。
}

func addWithDefer(a, b int) (sum int) {
	defer trace("addWithDefer")()

	sum = a + b
	defer func() {
		// 这里修改的是具名返回值 sum
		sum++
	}()
	return // 等价于 return sum，但会先跑 defer
}

// exampleCleanup 演示 defer 常见的“清理/收尾”写法：即使提前 return 也能执行。
func exampleCleanup() {
	deferSection("4) 清理收尾：即使提前 return 也会执行")

	fmt.Println("call doWork(true)")
	_ = doWork(true)

	fmt.Println("call doWork(false)")
	_ = doWork(false)
}

func doWork(ok bool) (err error) {
	fmt.Println("acquire resource")
	defer fmt.Println("release resource")

	if !ok {
		fmt.Println("doWork failed, return early")
		return fmt.Errorf("not ok")
	}

	fmt.Println("doWork success")
	return nil
}

// exampleDeferInLoop 演示 defer 放在循环里会“累积到函数返回才执行”。
// 如果循环次数很多，可能导致资源长时间不释放。
func exampleDeferInLoop() {
	deferSection("5) 循环里的 defer：会累积到函数退出才执行")

	fmt.Println("defer inside loop:")
	for i := 1; i <= 3; i++ {
		defer fmt.Println("defer in loop i=", i)
		fmt.Println("loop i=", i)
	}

	// 推荐：用一层小函数把每次迭代的 defer 限制在迭代作用域内。
	fmt.Println("\ndefer with per-iteration scope:")
	for i := 1; i <= 3; i++ {
		func(n int) {
			defer fmt.Println("defer per-iteration n=", n)
			fmt.Println("loop n=", n)
		}(i)
	}

	// 说明：
	// - 第一段的 3 个 defer 会在 exampleDeferInLoop 返回时一次性按 LIFO 执行。
	// - 第二段每次迭代都会执行完内部函数，defer 会在该内部函数返回时立即执行。
}

// examplePanicRecover 演示 defer 与 panic/recover 的配合。
// recover 只能在 defer 的函数里生效。
func examplePanicRecover() {
	deferSection("6) panic/recover：在 defer 中捕获异常")

	safeCall(func() {
		fmt.Println("about to panic")
		panic("boom")
	})

	safeCall(func() {
		fmt.Println("no panic")
	})
}

func safeCall(fn func()) {
	defer func() {
		if r := recover(); r != nil {
			fmt.Println("recovered:", r)
		}
	}()

	fn()
	fmt.Println("safeCall returns normally")
}

func main() {
	exampleOrder()
	exampleArgEval()
	exampleNamedReturn()
	exampleCleanup()
	exampleDeferInLoop()
	examplePanicRecover()
}
