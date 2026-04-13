package main

import "fmt"

// 演示 if 条件
func exampleIf() {
	x := 5
	if x%2 == 0 {
		fmt.Println("exampleIf: 5 is even")
	} else {
		fmt.Println("exampleIf: 5 is odd")
	}
}

// 演示带短变量声明的 if
func exampleIfWithShortStmt() {
	if y := 10; y > 5 {
		fmt.Println("exampleIfWithShortStmt: y>5", y)
	} else {
		fmt.Println("exampleIfWithShortStmt: y<=5", y)
	}
}

// 演示嵌套 if
func exampleNestedIf() {
	a, b := 3, 7
	if a > 0 {
		if b > 5 {
			fmt.Println("exampleNestedIf: a>0 and b>5")
		} else {
			fmt.Println("exampleNestedIf: a>0 but b<=5")
		}
	} else {
		fmt.Println("exampleNestedIf: a<=0")
	}
}

// 演示 switch：常规 case、组合 case、fallthrough
func exampleSwitch() {
	num := 2
	switch num {
	case 1:
		fmt.Println("exampleSwitch: one")
	case 2:
		fmt.Println("exampleSwitch: two")
		// fallthrough 会让执行继续到下一个 case（不检查条件）
		fallthrough
	case 3:
		fmt.Println("exampleSwitch: three (or fell through)")
	default:
		fmt.Println("exampleSwitch: other")
	}
}

// 演示 switch 作为表达式（没有变量，等价于 switch true）
func exampleSwitchExpr() {
	n := 15
	switch {
	case n%3 == 0:
		fmt.Println("exampleSwitchExpr: divisible by 3")
	case n%5 == 0:
		fmt.Println("exampleSwitchExpr: divisible by 5")
	default:
		fmt.Println("exampleSwitchExpr: not divisible by 3 or 5")
	}
}

// 演示 type switch（类型选择）
func exampleTypeSwitch() {
	var i interface{} = "hello"
	switch v := i.(type) {
	case int:
		fmt.Printf("exampleTypeSwitch: int %d\n", v)
	case string:
		fmt.Printf("exampleTypeSwitch: string %q\n", v)
	default:
		fmt.Println("exampleTypeSwitch: unknown type")
	}
}

func main() {
	fmt.Println("Go 条件语句示例输出:")
	exampleIf()
	exampleIfWithShortStmt()
	exampleNestedIf()
	exampleSwitch()
	exampleSwitchExpr()
	exampleTypeSwitch()
}
