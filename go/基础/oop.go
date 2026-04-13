package main

import "fmt"

// === Go OOP 特性示例 ===
// Go 没有 class/extends，但通过：
// 1) struct + method（把“数据+行为”绑定在一起）
// 2) 组合/嵌入（替代继承，复用字段/方法）
// 3) interface + 隐式实现（多态）
// 4) 包级可见性（大小写导出规则，用于封装）
// 来实现大多数 OOP 需求。

// Rectangle: struct 承载状态（字段）
type Rectangle struct {
	Width, Height float64
}

// Area: 值接收者方法（不修改对象）
func (r Rectangle) Area() float64 {
	return r.Width * r.Height
}

// Scale: 指针接收者方法（会修改对象）
func (r *Rectangle) Scale(factor float64) {
	r.Width *= factor
	r.Height *= factor
}

// Animal: 接口用于多态（只关心"能做什么"，不关心"是什么类型"）
type Animal interface {
	Speak() string
}

func printSound(a Animal) {
	fmt.Printf("animal says: %s\n", a.Speak())
}

// Dog: 实现 Animal 的具体类型
type Dog struct {
	Name string
}

func (d Dog) Speak() string {
	return "Woof! I'm " + d.Name
}

// Cat: 另一个实现 Animal 的具体类型
type Cat struct {
	Name string
}

func (c Cat) Speak() string {
	return "Meow! I'm " + c.Name
}

// Named: 组合/嵌入示例，用一个基础结构体复用字段/方法
// 注意：不需要 extends；把一个类型嵌入进来即可。
type Named struct {
	Name string
}

func (n Named) Describe() string {
	return "name=" + n.Name
}

// NamedRectangle: 嵌入 Named，让它“拥有” Name 字段和 Describe 方法
// 同时再嵌入 Rectangle，让它“拥有” Area/Scale 等方法。
type NamedRectangle struct {
	Named
	Rectangle
}

// 封装示例：包内私有字段（小写）+ 方法控制访问
// （在 package main 内仍可访问；放到其它包时会体现 “封装” 效果）
type account struct {
	owner   string // 不导出字段：包外不可见
	balance int
}

func newAccount(owner string, balance int) *account {
	return &account{owner: owner, balance: balance}
}

func (a *account) Deposit(amount int) {
	if amount <= 0 {
		return
	}
	a.balance += amount
}

func (a *account) Balance() int { return a.balance }

func main() {
	fmt.Println("=== Go OOP 示例 ===")

	// 1) struct + method
	r := Rectangle{Width: 3, Height: 4}
	fmt.Printf("Rectangle area: %.2f\n", r.Area())
	r.Scale(2)
	fmt.Printf("Rectangle area after scale: %.2f\n", r.Area())

	// 2) interface 多态：同一个函数接收不同具体类型
	fmt.Println("-- polymorphism via interface --")
	printSound(Dog{Name: "Buddy"})
	printSound(Cat{Name: "Kitty"})

	// 3) 组合/嵌入：复用字段/方法（替代继承）
	fmt.Println("-- composition / embedding --")
	nr := NamedRectangle{
		Named:     Named{Name: "my-rect"},
		Rectangle: Rectangle{Width: 2, Height: 10},
	}
	fmt.Println(nr.Describe())
	fmt.Printf("NamedRectangle area: %.2f\n", nr.Area())

	// 4) 封装：通过字段/方法的导出规则控制访问
	fmt.Println("-- encapsulation --")
	acc := newAccount("alice", 100)
	acc.Deposit(50)
	fmt.Printf("account balance: %d\n", acc.Balance())

	fmt.Println("=== 示例结束 ===")
}
