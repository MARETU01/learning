package main

import (
	"fmt"
	"time"
)

type DemoPerson struct {
	Name string
	Age  int
}

func FormatPerson(p DemoPerson) string {
	return fmt.Sprintf("%s is %d years old.", p.Name, p.Age) // %s=字符串，%d=十进制整数(int 等)
}

// exampleBasicVerbs 演示 fmt.Printf 常用“格式化动词/占位符”。
//
// 速记：格式一般是：%[flags][width][.precision]verb
// - flags: '-' 左对齐；'0' 用 0 填充；（还有 '+' ' ' '#' 等，这里先不展开）
// - width: 最小显示宽度（不足会填充；超过不会截断）
// - precision: 精度/小数位（对浮点/字符串等含义不同）
// - verb: 动词，决定“怎么格式化这个值”（%d %f %s ...）
func exampleBasicVerbs() {
	fmt.Println("1) 常用占位符/动词:")

	i := len("hello") + 37 // int
	f := 12.3456           // float64
	s := "Hello, 世界"       // string（UTF-8）
	r := '世'               // rune（int32，表示一个 Unicode 码点）
	// 用变量（且不是明显可常量折叠的表达式）来示例 %t
	b := time.Now().UnixNano()%2 == 0 // bool
	p := DemoPerson{Name: "Alice", Age: 30}

	fmt.Printf("bool: %%t -> %t\n", b)   // %t: 布尔值(true/false)
	fmt.Printf("int: %%d -> %d\n", i)    // %d: 十进制整数（常用于 int/int32/int64 等）
	fmt.Printf("float: %%f -> %f\n", f)  // %f: 浮点数（默认 6 位小数）
	fmt.Printf("string: %%s -> %s\n", s) // %s: 字符串/[]byte（按字节解释为字符串）
	fmt.Printf("rune: %%c -> %c\n", r)   // %c: 单个字符（按 Unicode 码点输出）
	fmt.Printf("%%占位符\n\n")              // %%: 输出字面量的百分号 %

	// 宽度与对齐：%-10s 表示最小宽度 10，左对齐，不足用空格补齐；%10s 表示右对齐。
	fmt.Printf("%%-10s | %-10s\n", "Name", "Age") // %-10s: 字符串左对齐，最小宽度 10
	fmt.Printf("%-10s | %-10d\n", p.Name, p.Age)  // %-10d: 整数左对齐，最小宽度 10
	fmt.Printf("%%10s | %10s\n", "Name", "Age")   // %10s: 字符串右对齐，最小宽度 10
	fmt.Printf("%10s | %10d\n\n", p.Name, p.Age)  // %10d: 整数右对齐，最小宽度 10

	fmt.Printf("%%010d | %010d\n", p.Age, p.Age) // %010d: 宽度 10，不足左侧用 0 填充
	fmt.Printf("%%.2f | %.2f\n\n", f, f)         // %.2f: 浮点数保留 2 位小数（四舍五入）

	// 进制相关：b=二进制，o=八进制，x/X=十六进制（x 小写，X 大写）。
	fmt.Printf("%%b | %b\n", i)   // %b: 二进制
	fmt.Printf("%%o | %o\n", i)   // %o: 八进制
	fmt.Printf("%%x | %x\n", i)   // %x: 十六进制(小写)
	fmt.Printf("%%X | %X\n\n", i) // %X: 十六进制(大写)

	fmt.Printf("%%U | %U\n", r)   // %U: Unicode 格式，如 U+4E16
	fmt.Printf("%%q | %q\n\n", s) // %q: 带引号的安全表示（必要时会转义）；字符串/字符都常用

	fmt.Printf("%%p | %p\n", &p) // %p: 指针地址（十六进制，带 0x 前缀）
	fmt.Printf("%%T | %T\n", p)  // %T: Go 语法形式的类型（如 main.DemoPerson）
}

func main() {
	exampleBasicVerbs()
}
