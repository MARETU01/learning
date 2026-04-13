package main

import "fmt"

// 本文件演示 Go 语言指针的常见用法：取址、解引用、函数传参、结构体指针接收者、new、以及一些易踩坑

type User struct {
	Name string
	Age  int
}

// 值接收者：不会修改调用者
func (u User) BirthdayByValue() {
	u.Age++
}

// 指针接收者：会修改调用者
func (u *User) BirthdayByPointer() {
	u.Age++
}

func exampleBasicPointer() {
	fmt.Println("1) 基本用法: 取址(&)、解引用(*):")

	var x int = 10
	p := &x
	fmt.Printf("x=%d address=%p\n", x, p)

	*p = 20
	fmt.Printf("after *p=20 -> x=%d *p=%d\n", x, *p)

	// 指针的零值是 nil
	var nilPtr *int
	fmt.Printf("nil pointer: %v\n", nilPtr)
	fmt.Println()
}

func incByValue(x int) {
	x++
}

func incByPointer(x *int) {
	*x++
}

func exampleFunctionArgs() {
	fmt.Println("2) 函数参数: 值传递 vs 指针传递:")

	x := 1
	incByValue(x)
	fmt.Printf("after incByValue(x) -> x=%d\n", x)

	incByPointer(&x)
	fmt.Printf("after incByPointer(&x) -> x=%d\n", x)
	fmt.Println()
}

func exampleStructPointer() {
	fmt.Println("3) 结构体与指针接收者方法:")

	u := User{Name: "Alice", Age: 20}
	u.BirthdayByValue()
	fmt.Printf("after BirthdayByValue -> %+v\n", u)

	u.BirthdayByPointer()
	fmt.Printf("after BirthdayByPointer -> %+v\n", u)

	// 结构体指针可以直接访问字段，Go 会自动解引用
	up := &u
	up.Age += 10
	fmt.Printf("after up.Age+=10 -> %+v (up=%p)\n", u, up)
	fmt.Println()
}

func exampleNew() {
	fmt.Println("4) new: 分配零值并返回指针:")

	p := new(int) // *int, 指向一个值为 0 的 int
	fmt.Printf("p=%p *p=%d\n", p, *p)
	*p = 99
	fmt.Printf("after *p=99 -> *p=%d\n", *p)
	fmt.Println()
}

func exampleSliceAndMap() {
	fmt.Println("5) 指针与 slice/map(引用语义):")

	// slice 本身是一个 descriptor，传参时拷贝 descriptor，但底层数组共享
	s := []int{1, 2, 3}
	modifySlice(s)
	fmt.Printf("after modifySlice -> %v\n", s)

	// map 是引用类型，传参后修改会影响调用者
	m := map[string]int{"a": 1}
	modifyMap(m)
	fmt.Printf("after modifyMap -> %v\n", m)

	// 如果要让函数“重新绑定”一个 slice（例如 append 后换了底层数组），通常传 *[]T
	arr := []int{1, 2}
	appendToSlice(&arr, 3, 4)
	fmt.Printf("after appendToSlice -> %v\n", arr)

	// map 也可以用指针，但一般没必要；仅在需要把 map 本身置为 nil/重建时才考虑
	fmt.Println()
}

func modifySlice(s []int) {
	if len(s) > 0 {
		s[0] = 100
	}
}

func modifyMap(m map[string]int) {
	m["b"] = 2
}

func appendToSlice(s *[]int, nums ...int) {
	*s = append(*s, nums...)
}

func exampleCommonPitfalls() {
	fmt.Println("6) 常见陷阱: for-range 变量取址:")

	// 下面这种写法很常见，但会踩坑：&v 总是同一个地址（v 是循环变量）
	vals := []int{10, 20, 30}
	wrong := make([]*int, 0, len(vals))
	for _, v := range vals {
		wrong = append(wrong, &v)
	}
	fmt.Println("wrong pointers deref:")
	for _, p := range wrong {
		fmt.Printf("%d ", *p)
	}
	fmt.Println()

	// 正确做法 1：用索引取地址（取的是切片元素的地址）
	correct1 := make([]*int, 0, len(vals))
	for i := range vals {
		correct1 = append(correct1, &vals[i])
	}
	fmt.Println("correct pointers deref (by index):")
	for _, p := range correct1 {
		fmt.Printf("%d ", *p)
	}
	fmt.Println()

	// 正确做法 2：在循环体内创建新变量
	correct2 := make([]*int, 0, len(vals))
	for _, v := range vals {
		vv := v
		correct2 = append(correct2, &vv)
	}
	fmt.Println("correct pointers deref (copy v):")
	for _, p := range correct2 {
		fmt.Printf("%d ", *p)
	}
	fmt.Println("\n")
}

func main() {
	fmt.Println("Go 指针示例输出:\n")

	exampleBasicPointer()
	exampleFunctionArgs()
	exampleStructPointer()
	exampleNew()
	exampleSliceAndMap()
	exampleCommonPitfalls()
}
