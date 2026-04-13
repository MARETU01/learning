package main

import (
	"encoding/json"
	"fmt"
)

// === Go 结构体(Struct) ===
// 结构体是字段的集合，用于组合不同类型的数据
// Go 使用组合而非继承来复用代码

// 基本结构体定义
type Person struct {
	Name string
	Age  int
	City string
}

// 带标签的结构体（常用于 JSON/DB 映射）
type User struct {
	ID       int    `json:"id" db:"user_id"`
	Username string `json:"username"`
	Password string `json:"-"`               // json:"-" 表示 JSON 序列化时忽略
	Email    string `json:"email,omitempty"` // omitempty: 零值时不输出
}

// 匿名字段（嵌入）
type Employee struct {
	Person     // 匿名字段（嵌入），Employee "拥有" Person 的字段
	Department string
	Salary     float64
}

// 嵌套结构体
type Address struct {
	Street  string
	City    string
	Country string
}

type Contact struct {
	Name    string
	Address Address // 嵌套结构体
	Phone   string
}

// 结构体指针
type Node struct {
	Value int
	Next  *Node // 指向自身类型的指针（链表）
}

// 结构体定义与初始化
func exampleDeclareAndInit() {
	fmt.Println("1) 结构体定义与初始化:")

	// 方式1：零值初始化
	var p1 Person
	fmt.Printf("零值: %#v\n", p1)

	// 方式2：字段名初始化（推荐，可读性好）
	p2 := Person{
		Name: "Alice",
		Age:  25,
		City: "Beijing",
	}
	fmt.Printf("字段名初始化: %#v\n", p2)

	// 方式3：顺序初始化（必须按定义顺序，不推荐）
	p3 := Person{"Bob", 30, "Shanghai"}
	fmt.Printf("顺序初始化: %#v\n", p3)

	// 方式4：部分字段初始化（未指定的为零值）
	p4 := Person{Name: "Charlie"}
	fmt.Printf("部分初始化: %#v\n", p4)

	// 方式5：new 函数（返回指针）
	p5 := new(Person)
	p5.Name = "David"
	fmt.Printf("new 初始化: %#v\n", p5)

	// 方式6：取地址初始化
	p6 := &Person{Name: "Eve", Age: 28}
	fmt.Printf("& 初始化: %#v\n", p6)

	fmt.Println()
}

// 字段访问与修改
func exampleFieldAccess() {
	fmt.Println("2) 字段访问与修改:")

	p := Person{Name: "Alice", Age: 25, City: "Beijing"}
	fmt.Printf("原始: %#v\n", p)

	// 访问字段
	fmt.Printf("Name: %s, Age: %d\n", p.Name, p.Age)

	// 修改字段
	p.Age = 26
	p.City = "Shanghai"
	fmt.Printf("修改后: %#v\n", p)

	// 指针访问（两种方式等价）
	pp := &p
	fmt.Printf("(*pp).Name: %s\n", (*pp).Name)
	fmt.Printf("pp.Name: %s (语法糖，自动解引用)\n", pp.Name)

	fmt.Println()
}

// 匿名字段与嵌入
func exampleAnonymousField() {
	fmt.Println("3) 匿名字段与嵌入:")

	emp := Employee{
		Person:     Person{Name: "Alice", Age: 30, City: "Beijing"},
		Department: "Engineering",
		Salary:     15000,
	}

	// 访问嵌入字段
	fmt.Printf("emp.Name: %s (直接访问嵌入字段)\n", emp.Name)
	fmt.Printf("emp.Person.Name: %s (完整路径)\n", emp.Person.Name)
	fmt.Printf("emp.Department: %s\n", emp.Department)

	// 修改嵌入字段
	emp.Age = 31
	emp.Person.City = "Shanghai"
	fmt.Printf("修改后: %#v\n", emp)

	fmt.Println()
}

// 嵌套结构体
func exampleNestedStruct() {
	fmt.Println("4) 嵌套结构体:")

	contact := Contact{
		Name: "Alice",
		Address: Address{
			Street:  "Main St",
			City:    "Beijing",
			Country: "China",
		},
		Phone: "12345678",
	}

	fmt.Printf("contact: %#v\n", contact)
	fmt.Printf("Street: %s\n", contact.Address.Street)
	fmt.Printf("City: %s\n", contact.Address.City)

	fmt.Println()
}

// 结构体比较
func exampleComparison() {
	fmt.Println("5) 结构体比较:")

	p1 := Person{Name: "Alice", Age: 25, City: "Beijing"}
	p2 := Person{Name: "Alice", Age: 25, City: "Beijing"}
	p3 := Person{Name: "Bob", Age: 30, City: "Shanghai"}

	// 结构体可以直接比较（字段类型必须可比较）
	fmt.Printf("p1 == p2: %v\n", p1 == p2)
	fmt.Printf("p1 == p3: %v\n", p1 == p3)

	// 包含不可比较字段（slice/map/func）的结构体不能比较
	// type Bad struct { Data []int }
	// var b1, b2 Bad
	// fmt.Println(b1 == b2) // 编译错误

	fmt.Println()
}

// 结构体作为函数参数
func exampleFuncParameter() {
	fmt.Println("6) 结构体作为函数参数:")

	p := Person{Name: "Alice", Age: 25}

	// 值传递：复制整个结构体
	modifyByValue(p)
	fmt.Printf("值传递后: %#v (原值不变)\n", p)

	// 指针传递：传递地址
	modifyByPointer(&p)
	fmt.Printf("指针传递后: %#v (原值改变)\n", p)

	fmt.Println()
}

func modifyByValue(p Person) {
	p.Age = 100
}

func modifyByPointer(p *Person) {
	p.Age = 100
}

// 结构体方法
func exampleMethods() {
	fmt.Println("7) 结构体方法:")

	r := Rectangle{Width: 10, Height: 5}

	// 值接收者方法
	fmt.Printf("Area(): %.2f\n", r.Area())

	// 指针接收者方法
	r.Scale(2)
	fmt.Printf("Scale(2) 后: Width=%.1f, Height=%.1f\n", r.Width, r.Height)

	// 方法集规则：
	// - 值类型 T：只能调用值接收者方法
	// - 指针类型 *T：可以调用值接收者和指针接收者方法
	// - Go 会自动取地址/解引用

	fmt.Println()
}

type Rectangle struct {
	Width, Height float64
}

// 值接收者：不修改结构体
func (r Rectangle) Area() float64 {
	return r.Width * r.Height
}

// 指针接收者：可以修改结构体
func (r *Rectangle) Scale(factor float64) {
	r.Width *= factor
	r.Height *= factor
}

// 结构体标签
func exampleTags() {
	fmt.Println("8) 结构体标签:")

	user := User{
		ID:       1,
		Username: "alice",
		Password: "secret",
		Email:    "alice@example.com",
	}

	// JSON 序列化（使用标签）
	data, _ := json.Marshal(user)
	fmt.Printf("JSON: %s\n", string(data))

	// 零值字段与 omitempty
	user2 := User{ID: 2, Username: "bob"}
	data2, _ := json.Marshal(user2)
	fmt.Printf("omitempty: %s (Email 零值不输出)\n", string(data2))

	// 读取标签（反射）
	// typ := reflect.TypeOf(user)
	// field, _ := typ.FieldByName("ID")
	// fmt.Printf("ID tag: %s\n", field.Tag.Get("json"))

	fmt.Println()
}

// 结构体与 map 互转
func exampleStructMap() {
	fmt.Println("9) 结构体与 map 互转:")

	// 结构体转 map（通过 JSON）
	p := Person{Name: "Alice", Age: 25, City: "Beijing"}
	data, _ := json.Marshal(p)
	var m map[string]interface{}
	json.Unmarshal(data, &m)
	fmt.Printf("struct -> map: %#v\n", m)

	// map 转结构体
	m2 := map[string]interface{}{
		"Name": "Bob",
		"Age":  30,
		"City": "Shanghai",
	}
	data2, _ := json.Marshal(m2)
	var p2 Person
	json.Unmarshal(data2, &p2)
	fmt.Printf("map -> struct: %#v\n", p2)

	fmt.Println()
}

// 空结构体
func exampleEmptyStruct() {
	fmt.Println("10) 空结构体:")

	// 空结构体不占用内存
	var es struct{}
	fmt.Printf("空结构体大小: %d 字节\n", int(0)) // 实际是 0

	// 常见用途1：实现集合（set）
	set := make(map[string]struct{})
	set["apple"] = struct{}{}
	set["banana"] = struct{}{}
	if _, exists := set["apple"]; exists {
		fmt.Println("apple 在集合中")
	}

	// 常见用途2：通道信号
	done := make(chan struct{})
	go func() {
		// 做一些工作
		close(done) // 发送完成信号
	}()
	<-done // 等待完成
	fmt.Println("工作完成")

	// 常见用途3：方法接收者（不需要数据）
	var timer Timer
	timer.Start()
	timer.Stop()

	fmt.Println()
}

type Timer struct{}

func (t Timer) Start() { fmt.Println("Timer started") }
func (t Timer) Stop()  { fmt.Println("Timer stopped") }

// 结构体深拷贝
func exampleDeepCopy() {
	fmt.Println("11) 结构体深拷贝:")

	// 浅拷贝：赋值即是
	p1 := Person{Name: "Alice", Age: 25}
	p2 := p1 // 值拷贝
	p2.Name = "Bob"
	fmt.Printf("p1: %#v (修改 p2 不影响 p1)\n", p1)

	// 含指针的浅拷贝问题
	type Data struct {
		Values []int
	}
	d1 := Data{Values: []int{1, 2, 3}}
	d2 := d1
	d2.Values[0] = 99
	fmt.Printf("d1.Values: %#v (浅拷贝，修改 d2 影响 d1)\n", d1.Values)

	// 深拷贝（手动复制）
	d3 := Data{Values: make([]int, len(d1.Values))}
	copy(d3.Values, d1.Values)
	d3.Values[0] = 100
	fmt.Printf("d1.Values: %#v (深拷贝，修改 d3 不影响 d1)\n", d1.Values)

	// 深拷贝（JSON 序列化）
	data, _ := json.Marshal(d1)
	var d4 Data
	json.Unmarshal(data, &d4)
	d4.Values[0] = 200
	fmt.Printf("d1.Values: %#v (JSON 深拷贝)\n", d1.Values)

	fmt.Println()
}

// 构造函数模式
func exampleConstructor() {
	fmt.Println("12) 构造函数模式:")

	// Go 没有构造函数，通常用 NewXxx 函数
	p := NewPerson("Alice", 25, "Beijing")
	fmt.Printf("NewPerson: %#v\n", p)

	// 带默认值的构造函数
	emp := NewEmployee("Bob", 30, "Engineering")
	fmt.Printf("NewEmployee: %#v\n", emp)

	// 可选参数模式（函数选项模式）
	opt := NewOption(
		WithName("Charlie"),
		WithAge(35),
	)
	fmt.Printf("Option: %#v\n", opt)

	fmt.Println()
}

// 标准构造函数
func NewPerson(name string, age int, city string) *Person {
	return &Person{
		Name: name,
		Age:  age,
		City: city,
	}
}

// 带默认值的构造函数
func NewEmployee(name string, age int, dept string) *Employee {
	return &Employee{
		Person: Person{
			Name: name,
			Age:  age,
			City: "Default City",
		},
		Department: dept,
		Salary:     10000, // 默认薪资
	}
}

// 函数选项模式
type Option struct {
	Name string
	Age  int
}

type OptionFunc func(*Option)

func WithName(name string) OptionFunc {
	return func(o *Option) { o.Name = name }
}

func WithAge(age int) OptionFunc {
	return func(o *Option) { o.Age = age }
}

func NewOption(opts ...OptionFunc) *Option {
	o := &Option{
		Name: "default",
		Age:  0,
	}
	for _, opt := range opts {
		opt(o)
	}
	return o
}

func main() {
	fmt.Println("=== Go 结构体(Struct) 示例 ===\n")

	exampleDeclareAndInit()
	exampleFieldAccess()
	exampleAnonymousField()
	exampleNestedStruct()
	exampleComparison()
	exampleFuncParameter()
	exampleMethods()
	exampleTags()
	exampleStructMap()
	exampleEmptyStruct()
	exampleDeepCopy()
	exampleConstructor()

	fmt.Println("=== 示例结束 ===")
}
