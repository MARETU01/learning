package main

import (
	"fmt"
	"math"
)

// === Go 接口(Interface) ===
// 接口是一组方法签名的集合
// 接口是隐式实现的：类型只需实现接口的所有方法即可
// 接口是 Go 实现多态的核心机制

// 定义接口
type Speaker interface {
	Speak() string
}

type Mover interface {
	Move() string
}

// 接口组合
type Animal interface {
	Speaker
	Mover
}

// 实现 Speaker 接口
type Dog struct {
	Name string
}

func (d Dog) Speak() string {
	return d.Name + " says: Woof!"
}

func (d Dog) Move() string {
	return d.Name + " is running"
}

type Cat struct {
	Name string
}

func (c Cat) Speak() string {
	return c.Name + " says: Meow!"
}

func (c Cat) Move() string {
	return c.Name + " is jumping"
}

// 基本接口使用
func exampleBasicInterface() {
	fmt.Println("1) 基本接口使用:")

	var s Speaker // 接口变量

	s = Dog{Name: "Buddy"}
	fmt.Println(s.Speak())

	s = Cat{Name: "Whiskers"}
	fmt.Println(s.Speak())

	fmt.Println()
}

// 接口多态
func examplePolymorphism() {
	fmt.Println("2) 接口多态:")

	animals := []Animal{
		Dog{Name: "Buddy"},
		Cat{Name: "Whiskers"},
	}

	for _, a := range animals {
		fmt.Println(a.Speak())
		fmt.Println(a.Move())
	}

	fmt.Println()
}

// 值接收者 vs 指针接收者
type Counter struct {
	value int
}

// 值接收者
func (c Counter) Value() int {
	return c.value
}

// 指针接收者（修改状态）
func (c *Counter) Increment() {
	c.value++
}

type ValueGetter interface {
	Value() int
}

type Incrementer interface {
	Increment()
}

func exampleValuePointerReceiver() {
	fmt.Println("3) 值接收者 vs 指针接收者:")

	c := Counter{value: 10}

	// 值接收者的方法，值和指针都可以调用
	var vg1 ValueGetter = c
	var vg2 ValueGetter = &c
	fmt.Printf("vg1.Value() = %d\n", vg1.Value())
	fmt.Printf("vg2.Value() = %d\n", vg2.Value())

	// 指针接收者的方法，只有指针能赋给接口
	var inc Incrementer = &c // 正确
	inc.Increment()
	fmt.Printf("After increment: %d\n", c.value)

	// var inc2 Incrementer = c // 编译错误：Counter 未实现 Incrementer

	fmt.Println()
}

// 空接口 interface{}
func exampleEmptyInterface() {
	fmt.Println("4) 空接口 interface{}:")

	// 空接口可以存储任何类型的值
	var any interface{}

	any = 42
	fmt.Printf("any = %v, type = %T\n", any, any)

	any = "hello"
	fmt.Printf("any = %v, type = %T\n", any, any)

	any = []int{1, 2, 3}
	fmt.Printf("any = %v, type = %T\n", any, any)

	// 常用场景：通用容器
	items := []interface{}{1, "two", 3.0, true}
	fmt.Printf("items = %v\n", items)

	fmt.Println()
}

// 类型断言
func exampleTypeAssertion() {
	fmt.Println("5) 类型断言:")

	var i interface{} = "hello"

	// 直接断言（不安全，可能 panic）
	s := i.(string)
	fmt.Printf("s = %s\n", s)

	// n := i.(int) // panic: interface conversion

	// 安全的类型断言（comma, ok 模式）
	if s, ok := i.(string); ok {
		fmt.Printf("i 是 string: %s\n", s)
	}

	if n, ok := i.(int); ok {
		fmt.Printf("i 是 int: %d\n", n)
	} else {
		fmt.Println("i 不是 int")
	}

	fmt.Println()
}

// 类型 switch
func exampleTypeSwitch() {
	fmt.Println("6) 类型 switch:")

	var checkType func(v interface{})
	checkType = func(v interface{}) {
		switch val := v.(type) {
		case nil:
			fmt.Println("nil")
		case int:
			fmt.Printf("int: %d\n", val)
		case string:
			fmt.Printf("string: %s\n", val)
		case bool:
			fmt.Printf("bool: %v\n", val)
		case []int:
			fmt.Printf("[]int: %v\n", val)
		default:
			fmt.Printf("other: %T\n", val)
		}
	}

	checkType(nil)
	checkType(42)
	checkType("hello")
	checkType(true)
	checkType([]int{1, 2, 3})
	checkType(3.14)

	fmt.Println()
}

// 接口嵌套
type Reader interface {
	Read() string
}

type Writer interface {
	Write(data string)
}

type ReadWriter interface {
	Reader
	Writer
}

type File struct {
	content string
}

func (f *File) Read() string {
	return f.content
}

func (f *File) Write(data string) {
	f.content = data
}

func exampleInterfaceEmbedding() {
	fmt.Println("7) 接口嵌套:")

	var rw ReadWriter = &File{}

	rw.Write("Hello, World!")
	fmt.Printf("Read: %s\n", rw.Read())

	// 可以转换为子接口
	var r Reader = rw
	fmt.Printf("As Reader: %s\n", r.Read())

	fmt.Println()
}

// 接口零值是 nil
func exampleNilInterface() {
	fmt.Println("8) 接口零值是 nil:")

	var s Speaker
	fmt.Printf("s == nil: %v\n", s == nil) // true

	// 但要注意：接口包含 (type, value) 两个部分
	// 即使 value 是 nil，如果 type 不为 nil，整个接口 != nil
	var c *Counter = nil
	var vg ValueGetter = c
	fmt.Printf("c == nil: %v\n", c == nil)      // true
	fmt.Printf("vg == nil: %v\n", vg == nil)    // false! 接口有类型信息
	fmt.Printf("vg.Value() = %d\n", vg.Value()) // 可以调用（但若方法解引用会 panic）

	fmt.Println()
}

// 常用标准库接口
type Shape interface {
	Area() float64
}

type Circle struct {
	Radius float64
}

func (c Circle) Area() float64 {
	return math.Pi * c.Radius * c.Radius
}

type Rectangle struct {
	Width, Height float64
}

func (r Rectangle) Area() float64 {
	return r.Width * r.Height
}

func exampleStdInterface() {
	fmt.Println("9) 自定义接口示例:")

	shapes := []Shape{
		Circle{Radius: 5},
		Rectangle{Width: 3, Height: 4},
	}

	for _, s := range shapes {
		fmt.Printf("Area: %.2f, Type: %T\n", s.Area(), s)
	}

	fmt.Println()
}

// 接口设计原则
func exampleInterfaceDesign() {
	fmt.Println("10) 接口设计原则:")

	// 原则1：接口应该小而精（接口隔离）
	// 好的设计：多个小接口
	// 不好的设计：一个大接口包含很多方法

	// 原则2：接受接口，返回具体类型
	// func Process(r Reader) *Result { ... }

	// 原则3：在需要时才定义接口，不要过度设计
	fmt.Println("原则1: 接口应该小而精（接口隔离原则）")
	fmt.Println("原则2: 函数接受接口，返回具体类型")
	fmt.Println("原则3: 按需定义接口，避免过度设计")
	fmt.Println("原则4: Go 的接口是隐式实现的，更灵活")

	fmt.Println()
}

func main() {
	fmt.Println("=== Go 接口(Interface) 示例 ===\n")

	exampleBasicInterface()
	examplePolymorphism()
	exampleValuePointerReceiver()
	exampleEmptyInterface()
	exampleTypeAssertion()
	exampleTypeSwitch()
	exampleInterfaceEmbedding()
	exampleNilInterface()
	exampleStdInterface()
	exampleInterfaceDesign()

	fmt.Println("=== 示例结束 ===")
}
