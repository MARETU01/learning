package main

import "fmt"

// === Go switch 语句 ===
// switch 是一种多分支选择结构，比多个 if-else 更清晰
// Go 的 switch 默认每个 case 后自带 break，不会穿透
// 需要"穿透"时使用 fallthrough 关键字

// 基本 switch
func exampleBasicSwitch() {
	fmt.Println("1) 基本 switch:")

	day := 3

	switch day {
	case 1:
		fmt.Println("星期一")
	case 2:
		fmt.Println("星期二")
	case 3:
		fmt.Println("星期三")
	case 4:
		fmt.Println("星期四")
	case 5:
		fmt.Println("星期五")
	case 6, 7: // 多个条件
		fmt.Println("周末")
	default:
		fmt.Println("无效的日期")
	}

	fmt.Println()
}

// 无表达式的 switch（替代 if-else if 链）
func exampleNoExpression() {
	fmt.Println("2) 无表达式的 switch:")

	score := 85

	switch {
	case score >= 90:
		fmt.Println("优秀")
	case score >= 80:
		fmt.Println("良好")
	case score >= 70:
		fmt.Println("中等")
	case score >= 60:
		fmt.Println("及格")
	default:
		fmt.Println("不及格")
	}

	fmt.Println()
}

// fallthrough 穿透
func exampleFallthrough() {
	fmt.Println("3) fallthrough 穿透:")

	num := 2

	switch num {
	case 1:
		fmt.Println("case 1")
	case 2:
		fmt.Println("case 2")
		fallthrough // 继续执行下一个 case
	case 3:
		fmt.Println("case 3 (由 fallthrough 触发)")
		fallthrough
	case 4:
		fmt.Println("case 4 (由 fallthrough 触发)")
	default:
		fmt.Println("default")
	}

	fmt.Println()
}

// 带初始化语句的 switch
func exampleInitStatement() {
	fmt.Println("4) 带初始化语句的 switch:")

	switch n := 15; {
	case n%2 == 0:
		fmt.Printf("%d 是偶数\n", n)
	case n%2 == 1:
		fmt.Printf("%d 是奇数\n", n)
	}

	// n 的作用域在 switch 内
	// fmt.Println(n) // 编译错误：undefined: n

	fmt.Println()
}

// 类型 switch（type switch）
func exampleTypeSwitch() {
	fmt.Println("5) 类型 switch:")

	var x interface{} = "hello"

	switch v := x.(type) {
	case nil:
		fmt.Println("x 是 nil")
	case int:
		fmt.Printf("x 是 int: %d\n", v)
	case string:
		fmt.Printf("x 是 string: %s\n", v)
	case bool:
		fmt.Printf("x 是 bool: %v\n", v)
	default:
		fmt.Printf("x 是未知类型: %T\n", v)
	}

	fmt.Println()
}

// case 中使用表达式
func exampleExpressionCase() {
	fmt.Println("6) case 中使用表达式:")

	a, b := 10, 20

	switch {
	case a > b:
		fmt.Printf("%d > %d\n", a, b)
	case a < b:
		fmt.Printf("%d < %d\n", a, b)
	case a == b:
		fmt.Printf("%d == %d\n", a, b)
	}

	fmt.Println()
}

// 多值匹配
func exampleMultiValue() {
	fmt.Println("7) 多值匹配:")

	char := 'a'

	switch char {
	case 'a', 'e', 'i', 'o', 'u':
		fmt.Printf("%c 是元音字母\n", char)
	case 'A', 'E', 'I', 'O', 'U':
		fmt.Printf("%c 是大写元音字母\n", char)
	default:
		fmt.Printf("%c 是辅音字母或其他\n", char)
	}

	fmt.Println()
}

// switch 判断接口类型
func exampleSwitchInterface() {
	fmt.Println("8) switch 判断接口类型:")

	var values []interface{} = []interface{}{
		42,
		"hello",
		3.14,
		true,
		[]int{1, 2, 3},
	}

	for _, v := range values {
		switch val := v.(type) {
		case int:
			fmt.Printf("int: %d\n", val)
		case string:
			fmt.Printf("string: %s\n", val)
		case float64:
			fmt.Printf("float64: %f\n", val)
		case bool:
			fmt.Printf("bool: %v\n", val)
		default:
			fmt.Printf("other: %T\n", val)
		}
	}

	fmt.Println()
}

// break 和标签
func exampleBreakWithLabel() {
	fmt.Println("9) break 与标签:")

	// 使用标签跳出外层循环
outer:
	for i := 0; i < 3; i++ {
		for j := 0; j < 3; j++ {
			switch {
			case i == 1 && j == 1:
				fmt.Printf("跳出外层循环 (%d, %d)\n", i, j)
				break outer // 跳出到标签位置
			default:
				fmt.Printf("(%d, %d)\n", i, j)
			}
		}
	}

	fmt.Println()
}

// switch vs if-else 对比
func exampleSwitchVsIf() {
	fmt.Println("10) switch vs if-else:")

	status := 200

	// switch 更清晰
	switch status {
	case 200:
		fmt.Println("OK")
	case 404:
		fmt.Println("Not Found")
	case 500:
		fmt.Println("Internal Server Error")
	default:
		fmt.Println("Unknown Status")
	}

	// 等价的 if-else
	if status == 200 {
		fmt.Println("OK")
	} else if status == 404 {
		fmt.Println("Not Found")
	} else if status == 500 {
		fmt.Println("Internal Server Error")
	} else {
		fmt.Println("Unknown Status")
	}

	fmt.Println()
}

func main() {
	fmt.Println("=== Go switch 语句示例 ===\n")

	exampleBasicSwitch()
	exampleNoExpression()
	exampleFallthrough()
	exampleInitStatement()
	exampleTypeSwitch()
	exampleExpressionCase()
	exampleMultiValue()
	exampleSwitchInterface()
	exampleBreakWithLabel()
	exampleSwitchVsIf()

	fmt.Println("=== 示例结束 ===")
}
