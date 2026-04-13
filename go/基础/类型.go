package main

import "fmt"

// 本文件演示 Go 的基本数据类型及其零值、类型输出

type Person struct {
	Name string
	Age  int
}

func main() {
	// 布尔
	var b bool

	// 整数（不同位宽）
	var i int          // 取值范围：32位 -2147483648~2147483647；64位 -9223372036854775808~9223372036854775807（随平台）
	var i8 int8 = -8   // -128~127
	var i16 int16 = 16 // -32768~32767
	var i32 int32 = 32 // -2147483648~2147483647
	var i64 int64 = 64 // -9223372036854775808~9223372036854775807

	// 无符号整数
	var u uint             // 取值范围：32位 0~4294967295；64位 0~18446744073709551615（随平台）
	var u8 uint8 = 255     // 0~255
	var u16 uint16 = 65535 // 0~65535

	// 别名类型
	var by byte = 'a' // byte == uint8，范围 0~255
	var r rune = '世'  // rune == int32，范围 -2147483648~2147483647

	// 浮点与复数
	var f32 float32 = 3.14
	var f64 float64 = 2.718281828
	var c64 complex64 = complex(1, 2)
	var c128 complex128 = complex(2, -3)

	// 字符串
	var s string = "Hello, 世界"

	// 数组、切片、映射
	arr := [3]int{1, 2, 3}
	sl := []string{"a", "b", "c"}
	m := map[string]int{"x": 1, "y": 2}

	// 结构体
	p := Person{Name: "Alice", Age: 30}

	// 指针
	var julia int = 7
	ptr := &julia

	// 通道
	ch := make(chan int, 1)
	ch <- 42

	// 函数值
	fn := func(x int) int { return x * 2 }

	// 空接口
	var iface interface{} = "something"

	// 打印值和类型（使用 %T 查看类型）
	fmt.Printf("bool: value=%v type=%T\n", b, b)
	fmt.Printf("int: value=%v type=%T\n", i, i)
	fmt.Printf("int8: %v %T\n", i8, i8)
	fmt.Printf("int16: %v %T\n", i16, i16)
	fmt.Printf("int32: %v %T\n", i32, i32)
	fmt.Printf("int64: %v %T\n", i64, i64)

	fmt.Printf("uint: %v %T\n", u, u)
	fmt.Printf("uint8(byte): %v %T\n", u8, u8)
	fmt.Printf("uint16: %v %T\n", u16, u16)

	fmt.Printf("byte: %v %T\n", by, by)
	fmt.Printf("rune: %v %T\n", r, r)

	fmt.Printf("float32: %v %T\n", f32, f32)
	fmt.Printf("float64: %v %T\n", f64, f64)
	fmt.Printf("complex64: %v %T\n", c64, c64)
	fmt.Printf("complex128: %v %T\n", c128, c128)

	fmt.Printf("string: %q %T\n", s, s)

	fmt.Printf("array: %v %T\n", arr, arr)
	fmt.Printf("slice: %v %T\n", sl, sl)
	fmt.Printf("map: %v %T\n", m, m)

	fmt.Printf("struct: %v %T\n", p, p)
	fmt.Printf("pointer: %v points to %v %T\n", ptr, *ptr, ptr)

	valFromChan := <-ch
	fmt.Printf("chan (received): %v %T\n", valFromChan, valFromChan)

	fmt.Printf("func value: %v %T -> fn(3)=%v\n", fn, fn, fn(3))
	fmt.Printf("interface empty: %v %T\n", iface, iface)

	// 常量类型示例
	const c = 100
	fmt.Printf("constant c: %v %T (untyped constant)\n", c, c)
}
