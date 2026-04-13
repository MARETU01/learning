package main

import "fmt"

// === Go 数组(Array) ===
// 数组是固定长度、同类型元素的序列
// 长度是类型的一部分：[3]int 和 [4]int 是不同类型
// 数组是值类型：赋值/传参会复制整个数组

// 数组的声明与初始化
func exampleDeclareAndInit() {
	fmt.Println("1) 数组声明与初始化:")

	// 声明但不初始化：元素为零值
	var a1 [3]int
	fmt.Printf("a1 (零值): %#v\n", a1)

	// 声明并初始化（完整写法）
	a2 := [3]int{1, 2, 3}
	fmt.Printf("a2 (完整初始化): %#v\n", a2)

	// 部分初始化（未指定的为零值）
	a3 := [5]int{1, 2}
	fmt.Printf("a3 (部分初始化): %#v\n", a3)

	// 让编译器推断长度
	a4 := [...]int{1, 2, 3, 4, 5}
	fmt.Printf("a4 (自动推断长度): %#v, len=%d\n", a4, len(a4))

	// 指定索引初始化
	a5 := [5]int{1: 10, 3: 30}
	fmt.Printf("a5 (指定索引): %#v\n", a5)

	fmt.Println()
}

// 数组是值类型
func exampleValueType() {
	fmt.Println("2) 数组是值类型:")

	a := [3]int{1, 2, 3}
	b := a    // 复制整个数组
	b[0] = 99 // 修改 b 不影响 a

	fmt.Printf("a=%#v\n", a)
	fmt.Printf("b=%#v (修改 b 不影响 a)\n", b)

	// 传参也是复制
	modifyArray(a)
	fmt.Printf("a after modifyArray: %#v (传参也是复制，原数组不变)\n", a)

	// 传指针可以修改原数组
	modifyArrayPtr(&a)
	fmt.Printf("a after modifyArrayPtr: %#v (传指针可修改原数组)\n", a)

	fmt.Println()
}

func modifyArray(arr [3]int) {
	arr[0] = 100
}

func modifyArrayPtr(arr *[3]int) {
	arr[0] = 100
}

// 数组遍历
func exampleIteration() {
	fmt.Println("3) 数组遍历:")

	arr := [5]int{10, 20, 30, 40, 50}

	// 方式1：经典 for 循环
	fmt.Println("方式1 - 经典 for:")
	for i := 0; i < len(arr); i++ {
		fmt.Printf("  arr[%d]=%d\n", i, arr[i])
	}

	// 方式2：for-range（推荐）
	fmt.Println("方式2 - for-range:")
	for i, v := range arr {
		fmt.Printf("  arr[%d]=%d\n", i, v)
	}

	// 只需要值时
	fmt.Println("只取值:")
	for _, v := range arr {
		fmt.Printf("  v=%d\n", v)
	}

	// 只需要索引时
	fmt.Println("只取索引:")
	for i := range arr {
		fmt.Printf("  i=%d\n", i)
	}

	fmt.Println()
}

// 多维数组
func exampleMultiDimensional() {
	fmt.Println("4) 多维数组:")

	// 声明二维数组
	var m1 [2][3]int
	fmt.Printf("m1 (零值):\n")
	for _, row := range m1 {
		fmt.Printf("  %#v\n", row)
	}

	// 初始化二维数组
	m2 := [2][3]int{
		{1, 2, 3},
		{4, 5, 6},
	}
	fmt.Printf("m2:\n")
	for _, row := range m2 {
		fmt.Printf("  %#v\n", row)
	}

	// 遍历二维数组
	fmt.Println("遍历 m2:")
	for i, row := range m2 {
		for j, v := range row {
			fmt.Printf("  m2[%d][%d]=%d\n", i, j, v)
		}
	}

	fmt.Println()
}

// 数组长度是类型的一部分
func exampleLengthInType() {
	fmt.Println("5) 长度是类型的一部分:")

	a := [3]int{1, 2, 3}
	b := [4]int{1, 2, 3, 4}

	fmt.Printf("a 类型: %T\n", a) // [3]int
	fmt.Printf("b 类型: %T\n", b) // [4]int

	// a = b // 编译错误：不能将 [4]int 赋值给 [3]int

	fmt.Println("[3]int 和 [4]int 是不同类型，不能相互赋值")
	fmt.Println()
}

// 数组的比较
func exampleComparison() {
	fmt.Println("6) 数组比较:")

	a := [3]int{1, 2, 3}
	b := [3]int{1, 2, 3}
	c := [3]int{1, 2, 4}

	fmt.Printf("a=%#v\n", a)
	fmt.Printf("b=%#v\n", b)
	fmt.Printf("c=%#v\n", c)
	fmt.Printf("a == b: %v\n", a == b) // true
	fmt.Printf("a == c: %v\n", a == c) // false

	// 注意：元素类型必须可比较（func/map/slice 不能作为元素类型）
	fmt.Println()
}

// 数组与 slice 的区别
func exampleArrayVsSlice() {
	fmt.Println("7) 数组 vs slice:")

	// 数组：固定长度，值类型
	arr := [3]int{1, 2, 3}
	fmt.Printf("数组: type=%T, len=%d\n", arr, len(arr))

	// slice：动态长度，引用类型
	slice := []int{1, 2, 3}
	fmt.Printf("切片: type=%T, len=%d, cap=%d\n", slice, len(slice), cap(slice))

	// 数组传参复制，slice 传参共享底层数组
	fmt.Println("\n传参行为:")
	testArrayPass(arr)
	testSlicePass(slice)
	fmt.Printf("  传参后: arr=%#v, slice=%#v\n", arr, slice)

	// 数组转 slice
	sliceFromArray := arr[:] // 从数组创建 slice
	fmt.Printf("\n数组转 slice: arr[:] -> %#v\n", sliceFromArray)

	fmt.Println()
}

func testArrayPass(a [3]int) {
	a[0] = 99
	fmt.Printf("  函数内修改数组: a=%#v (不影响原数组)\n", a)
}

func testSlicePass(s []int) {
	s[0] = 99
	fmt.Printf("  函数内修改切片: s=%#v (会影响原切片)\n", s)
}

// 数组作为 map 的 key
func exampleArrayAsMapKey() {
	fmt.Println("8) 数组作为 map 的 key:")

	// slice 不能作为 key，但数组可以
	m := make(map[[2]string]string)
	m[[2]string{"user", "name"}] = "Alice"
	m[[2]string{"user", "age"}] = "18"

	fmt.Printf("map: %#v\n", m)

	// 查找
	key := [2]string{"user", "name"}
	if v, ok := m[key]; ok {
		fmt.Printf("m[\"user\",\"name\"] = %s\n", v)
	}

	fmt.Println()
}

// 数组指针与指针数组
func exampleArrayPointer() {
	fmt.Println("9) 数组指针 vs 指针数组:")

	// 指针数组：元素是指针
	a := [3]*int{}
	x, y, z := 1, 2, 3
	a[0], a[1], a[2] = &x, &y, &z
	fmt.Printf("指针数组: %#v\n", a)
	fmt.Printf("  *a[0]=%d, *a[1]=%d, *a[2]=%d\n", *a[0], *a[1], *a[2])

	// 数组指针：指向数组的指针
	arr := [3]int{1, 2, 3}
	p := &arr
	fmt.Printf("数组指针: type=%T, value=%#v\n", p, p)
	fmt.Printf("  (*p)[0]=%d, p[0]=%d (可以直接 p[i] 访问)\n", (*p)[0], p[0])

	fmt.Println()
}

// 常见操作
func exampleCommonOperations() {
	fmt.Println("10) 常用操作:")

	arr := [5]int{1, 2, 3, 4, 5}

	// 获取长度
	fmt.Printf("len(arr) = %d\n", len(arr))

	// 数组不能追加/删除（固定长度）
	// 如需动态操作，转换为 slice

	// 数组拷贝
	var copyArr [5]int
	copy(copyArr[:], arr[:]) // 使用 copy 函数（需要转为 slice）
	fmt.Printf("copy 后: %#v\n", copyArr)

	fmt.Println()
}

func main() {
	fmt.Println("=== Go 数组(Array) 示例 ===\n")

	exampleDeclareAndInit()
	exampleValueType()
	exampleIteration()
	exampleMultiDimensional()
	exampleLengthInType()
	exampleComparison()
	exampleArrayVsSlice()
	exampleArrayAsMapKey()
	exampleArrayPointer()
	exampleCommonOperations()

	fmt.Println("=== 示例结束 ===")
}
