package main

import (
	"bytes"
	"errors"
	"fmt"
	"strings"
)

func main() {
	// 这个文件是 fmt 包的常用用法速览。
	// 你可以直接运行：go run learning/fmt使用.go

	section("1) Print / Println / Printf 基础输出")
	printBasics()

	section("2) 常见格式化占位符")
	formatVerbs()

	section("3) Sprintf / Fprintf / Fprint：格式化到字符串或 io.Writer")
	formatToStringOrWriter()

	section("4) Errorf：构造错误与 %w 包装")
	errWrapping()

	section("5) Scan / Scanf：解析输入（用 strings.NewReader 模拟输入，避免阻塞）")
	scanning()
}

func section(title string) {
	fmt.Println("\n==================================================")
	fmt.Println(title)
	fmt.Println("==================================================")
}

func printBasics() {
	// Print：不自动换行
	fmt.Print("fmt.Print: ")
	fmt.Print("hello", " ", "world")
	fmt.Print("\n")

	// Println：自动在最后加空格并换行
	fmt.Println("fmt.Println:", "hello", "world")

	// Printf：按格式输出（不会自动换行，通常自己加 \n）
	name := "Alice"
	age := 18
	fmt.Printf("fmt.Printf: name=%s age=%d\n", name, age)
}

func formatVerbs() {
	n := 42
	pi := 3.1415926
	s := "Go\n"
	// 让 bool 依赖运行时值，避免静态分析认为恒真/恒假
	b := len(strings.TrimSpace(s))%2 == 0

	// %v：默认格式
	fmt.Printf("%%v: n=%v pi=%v s=%v b=%v\n", n, pi, s, b)
	// %T：类型
	fmt.Printf("%%T: n=%T pi=%T s=%T b=%T\n", n, pi, s, b)

	// 整数
	fmt.Printf("%%d (十进制) : %d\n", n)
	fmt.Printf("%%b (二进制) : %b\n", n)
	fmt.Printf("%%x (十六进制): %x\n", n)
	fmt.Printf("%%#x(带0x)   : %#x\n", n)

	// 浮点数
	fmt.Printf("%%f (默认小数): %f\n", pi)
	fmt.Printf("%%.2f(两位小数): %.2f\n", pi)
	fmt.Printf("%%e (科学计数): %e\n", pi)

	// 字符串
	fmt.Printf("%%s (原样)     : %s", s) // s 本身包含换行
	fmt.Printf("%%q (带引号转义): %q\n", s)

	// 布尔
	fmt.Printf("%%t (bool)     : %t\n", b)

	// 宽度 / 对齐
	fmt.Printf("宽度对齐：|%8s| |%-8s|\n", "Go", "Go")
	fmt.Printf("数字补0：|%08d|\n", n)

	// 结构体 / 复合类型：%#v 会输出更“像 Go 代码”的形式
	type Person struct {
		Name string
		Age  int
	}
	p := Person{Name: "Bob", Age: 20}
	fmt.Printf("%%v   : %v\n", p)
	fmt.Printf("%%+v  : %+v\n", p)
	fmt.Printf("%%#v  : %#v\n", p)
}

func formatToStringOrWriter() {
	orderID := 10086
	amount := 12.345

	// Sprintf：返回格式化后的字符串
	msg := fmt.Sprintf("order=%d amount=%.2f", orderID, amount)
	fmt.Println("Sprintf ->", msg)

	// Fprintf：把格式化内容写入一个 io.Writer
	// 这里用 bytes.Buffer 当“内存文件”，避免直接写到 stdout
	var buf bytes.Buffer
	_, _ = fmt.Fprintf(&buf, "Fprintf -> order=%d amount=%.2f", orderID, amount)
	fmt.Println("buffer string:", buf.String())

	// Fprint / Fprintln：不需要格式化占位符时直接写入 writer
	var sb strings.Builder
	_, _ = fmt.Fprint(&sb, "Fprint -> ")
	_, _ = fmt.Fprintln(&sb, "hello", "writer")
	_, _ = fmt.Fprintf(&sb, "Fprintf -> hex=%#x\n", 255)
	fmt.Print(sb.String())
}

func errWrapping() {
	// Errorf：构造带格式的 error
	err := fmt.Errorf("request failed: status=%d", 500)
	fmt.Println("Errorf ->", err)

	// %w：包装一个已有的 error，方便 errors.Is / errors.As 判断
	base := errors.New("database: connection refused")
	wrapped := fmt.Errorf("save user: %w", base)
	fmt.Println("wrapped ->", wrapped)
	fmt.Println("errors.Is(wrapped, base) =>", errors.Is(wrapped, base))
}

func scanning() {
	// 推荐用 Fscan / Fscanf 配合任意 io.Reader（文件、网络、字符串...）
	// 这样不会像 fmt.Scan 那样等待真实 stdin 输入而阻塞程序。

	// Fscan：按空白分隔读取
	input := strings.NewReader("Tom 25 88.5 true")
	var name string
	var age int
	var score float64
	var ok bool

	n, err := fmt.Fscan(input, &name, &age, &score, &ok)
	fmt.Printf("Fscan read count=%d err=%v\n", n, err)
	fmt.Printf("values: name=%s age=%d score=%.1f ok=%t\n", name, age, score, ok)

	// Fscanf：按格式读取
	input2 := strings.NewReader("id=7,level=12\n")
	var id, level int
	n, err = fmt.Fscanf(input2, "id=%d,level=%d", &id, &level)
	fmt.Printf("Fscanf read count=%d err=%v\n", n, err)
	fmt.Printf("values: id=%d level=%d\n", id, level)

	// 你也可以在需要时使用 fmt.Sscan / fmt.Sscanf 直接从字符串解析：
	var x, y int
	n, err = fmt.Sscanf("10 20", "%d %d", &x, &y)
	fmt.Printf("Sscanf read count=%d err=%v\n", n, err)
	fmt.Printf("values: x=%d y=%d\n", x, y)
}
