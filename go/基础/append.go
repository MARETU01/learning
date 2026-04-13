package main

import "fmt"

// 这个文件演示 Go 语言中 append 的常见用法（slice 追加元素）。
//
// 你可以直接运行：
//   go run learning/append.go
//
// 关键点速记：
// - append 会返回一个“新切片”
// - 新切片可能复用原来的底层数组，也可能因为扩容分配新数组
// - 对一个切片的 append 结果，一定要接住返回值（s = append(s, ...)）

func main() {
	fmt.Println("Go append 示例:\n")

	exampleAppendBasics()
	exampleAppendSliceToSlice()
	exampleAppendCapacityGrowth()
	exampleAppendAndSharedBackingArrayPitfall()
	// bonus：带容量预估的写法（减少扩容次数）
	exampleAppendWithPreAlloc()
}

// 演示：向切片末尾追加一个或多个元素
func exampleAppendBasics() {
	fmt.Println("1) append 基础：追加元素")

	// nil 切片也可以 append（append 会自动分配底层数组）
	var s []int
	fmt.Printf("before: s=%v len=%d cap=%d nil=%v\n", s, len(s), cap(s), s == nil)

	s = append(s, 1)
	s = append(s, 2, 3)
	fmt.Printf("after : s=%v len=%d cap=%d nil=%v\n", s, len(s), cap(s), s == nil)

	fmt.Println()
}

// 演示：把一个切片追加到另一个切片（使用 ... 打散）
func exampleAppendSliceToSlice() {
	fmt.Println("2) append 切片：append(dst, src...)")

	a := []int{1, 2}
	b := []int{3, 4, 5}

	// 追加 b 的所有元素到 a：必须写 b...
	a = append(a, b...)
	fmt.Println("a after append b... =>", a)

	fmt.Println()
}

// 演示：观察 append 扩容时 len/cap 的变化
func exampleAppendCapacityGrowth() {
	fmt.Println("3) append 扩容：观察 len/cap")

	// 从一个小容量开始，便于观察 cap 的变化
	s := make([]int, 0, 1)
	for i := 0; i < 8; i++ {
		beforeCap := cap(s)
		s = append(s, i)
		afterCap := cap(s)
		if afterCap != beforeCap {
			fmt.Printf("append %d -> len=%d cap=%d (grew from %d)\n", i, len(s), cap(s), beforeCap)
		} else {
			fmt.Printf("append %d -> len=%d cap=%d\n", i, len(s), cap(s))
		}
	}

	fmt.Println()
}

// 演示：共享底层数组的“坑”
//
// 当你从一个切片 reslice（切出子切片）后，再对它 append：
// - 如果子切片的 cap 还够，append 可能复用同一个底层数组
// - 这会导致通过子切片写入的数据，反过来影响原切片
func exampleAppendAndSharedBackingArrayPitfall() {
	fmt.Println("4) 共享底层数组的坑：reslice 后 append 影响原切片")

	base := []int{1, 2, 3, 4}
	fmt.Printf("base=%v len=%d cap=%d\n", base, len(base), cap(base))

	// sub 指向 base 的同一个底层数组（从 index=0 截到 len=2，但 cap 仍然可能很大）
	sub := base[:2]
	fmt.Printf("sub = base[:2] => %v len=%d cap=%d\n", sub, len(sub), cap(sub))

	// 由于 sub 的 cap 可能还够，append 会直接写到 base 的后面位置
	sub = append(sub, 99)
	fmt.Printf("after sub append 99\n")
	fmt.Printf("sub = %v\n", sub)
	fmt.Printf("base= %v  (base 可能被修改了)\n", base)

	// 解决：限制子切片的容量（full slice expression），让 append 必然触发扩容
	base2 := []int{1, 2, 3, 4}
	sub2 := base2[:2:2] // len=2, cap=2
	sub2 = append(sub2, 99)
	fmt.Printf("\nuse full slice expr:\n")
	fmt.Printf("sub2 = %v\n", sub2)
	fmt.Printf("base2= %v  (base2 不会被修改)\n", base2)

	fmt.Println()
}

// 演示：提前预分配容量，减少扩容
func exampleAppendWithPreAlloc() {
	fmt.Println("5) 预分配容量：make([]T, 0, n) + append")

	// 假设我们已知要追加 5 个元素，预分配 cap=5 可以减少扩容
	s := make([]int, 0, 5)
	for i := 1; i <= 5; i++ {
		s = append(s, i)
		fmt.Printf("append %d -> len=%d cap=%d\n", i, len(s), cap(s))
	}
	fmt.Println("result:", s)

	fmt.Println()
}
