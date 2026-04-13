package main

import "fmt"

// === Go 切片(Slice) ===
// 切片是对底层数组的引用视图，包含三个信息：指针、长度(len)、容量(cap)
// 切片是引用类型：赋值/传参会共享底层数组
// 切片长度可变，容量可以扩展

// 切片的创建与初始化
func exampleCreateAndInit() {
	fmt.Println("1) 切片创建与初始化:")

	// 声明但不初始化：零值是 nil
	var s1 []int
	fmt.Printf("s1 == nil ? %v, len=%d, cap=%d\n", s1 == nil, len(s1), cap(s1))

	// make 创建：make([]T, len) 或 make([]T, len, cap)
	s2 := make([]int, 3)     // len=3, cap=3
	s3 := make([]int, 3, 10) // len=3, cap=10
	fmt.Printf("s2: len=%d, cap=%d\n", len(s2), cap(s2))
	fmt.Printf("s3: len=%d, cap=%d\n", len(s3), cap(s3))

	// 字面量创建
	s4 := []int{1, 2, 3}
	fmt.Printf("s4: %#v, len=%d, cap=%d\n", s4, len(s4), cap(s4))

	// 从数组创建切片
	arr := [5]int{1, 2, 3, 4, 5}
	s5 := arr[1:4] // arr[low:high]，包含 low，不包含 high
	fmt.Printf("s5=arr[1:4]: %#v, len=%d, cap=%d\n", s5, len(s5), cap(s5))

	fmt.Println()
}

// len 与 cap 的关系
func exampleLenAndCap() {
	fmt.Println("2) len 与 cap:")

	// len：切片当前元素个数
	// cap：从切片起始位置到底层数组末尾的元素个数

	arr := [10]int{0, 1, 2, 3, 4, 5, 6, 7, 8, 9}
	s := arr[2:5] // 元素 {2, 3, 4}

	fmt.Printf("arr: %#v\n", arr)
	fmt.Printf("s=arr[2:5]: %#v, len=%d, cap=%d\n", s, len(s), cap(s))
	// len=3 (元素个数)
	// cap=8 (从索引2到数组末尾: 10-2=8)

	// 切片可以扩展到 cap 范围内（重新切片）
	s2 := s[:cap(s)]
	fmt.Printf("s[:cap(s)]: %#v, len=%d, cap=%d\n", s2, len(s2), cap(s2))

	fmt.Println()
}

// 切片表达式
func exampleSliceExpression() {
	fmt.Println("3) 切片表达式:")

	arr := [10]int{0, 1, 2, 3, 4, 5, 6, 7, 8, 9}
	fmt.Printf("arr: %#v\n", arr)

	// 简单切片表达式：arr[low:high]
	// low 和 high 可以省略
	s1 := arr[2:5] // 索引 2,3,4
	s2 := arr[:5]  // 索引 0,1,2,3,4
	s3 := arr[2:]  // 索引 2,3,4,5,6,7,8,9
	s4 := arr[:]   // 全部

	fmt.Printf("arr[2:5]:  %#v\n", s1)
	fmt.Printf("arr[:5]:   %#v\n", s2)
	fmt.Printf("arr[2:]:   %#v\n", s3)
	fmt.Printf("arr[:]:    %#v\n", s4)

	// 完整切片表达式（Go 1.2+）：arr[low:high:max]
	// 限制 cap = max - low
	s5 := arr[2:5:7] // len=5-2=3, cap=7-2=5
	fmt.Printf("arr[2:5:7]: %#v, len=%d, cap=%d\n", s5, len(s5), cap(s5))

	fmt.Println()
}

// append 函数
func exampleAppend() {
	fmt.Println("4) append 函数:")

	s := []int{1, 2, 3}
	fmt.Printf("初始: %#v, len=%d, cap=%d\n", s, len(s), cap(s))

	// 追加单个元素
	s = append(s, 4)
	fmt.Printf("append(s, 4): %#v, len=%d, cap=%d\n", s, len(s), cap(s))

	// 追加多个元素
	s = append(s, 5, 6, 7)
	fmt.Printf("append(s, 5,6,7): %#v, len=%d, cap=%d\n", s, len(s), cap(s))

	// 追加另一个切片（使用 ... 展开）
	s2 := []int{8, 9, 10}
	s = append(s, s2...)
	fmt.Printf("append(s, s2...): %#v, len=%d, cap=%d\n", s, len(s), cap(s))

	// 在指定位置插入元素
	s = []int{1, 2, 4, 5}
	idx := 2
	s = append(s[:idx], append([]int{3}, s[idx:]...)...)
	fmt.Printf("在索引2插入3: %#v\n", s)

	fmt.Println()
}

// 扩容机制
func exampleGrowMechanism() {
	fmt.Println("5) 扩容机制:")

	// 当 cap 不够时，append 会重新分配更大的底层数组
	// 扩容规则：
	// - 如果 cap < 1024，新 cap 翻倍
	// - 如果 cap >= 1024，新 cap 增加 25%
	// - 最终还会考虑元素大小和内存对齐等因素

	s := make([]int, 0)
	fmt.Printf("初始: len=%d, cap=%d\n", len(s), cap(s))

	for i := 0; i < 20; i++ {
		oldCap := cap(s)
		s = append(s, i)
		if cap(s) != oldCap {
			fmt.Printf("append %2d: len=%2d, cap=%2d -> %2d (扩容)\n", i, len(s), oldCap, cap(s))
		}
	}

	fmt.Println()
}

// copy 函数
func exampleCopy() {
	fmt.Println("6) copy 函数:")

	src := []int{1, 2, 3, 4, 5}
	dst := make([]int, 3)

	n := copy(dst, src) // 复制 min(len(dst), len(src)) 个元素
	fmt.Printf("src: %#v\n", src)
	fmt.Printf("dst: %#v (copy了 %d 个元素)\n", dst, n)

	// 复制到 dst 的指定位置
	dst2 := make([]int, 5)
	copy(dst2[1:], src[:3])
	fmt.Printf("dst2: %#v (copy到dst2[1:])\n", dst2)

	// 从 src 的指定位置复制
	dst3 := make([]int, 3)
	copy(dst3, src[2:])
	fmt.Printf("dst3: %#v (从src[2:]复制)\n", dst3)

	// 切片复制（创建独立副本）
	original := []int{1, 2, 3}
	copySlice := make([]int, len(original))
	copy(copySlice, original)
	copySlice[0] = 99
	fmt.Printf("original: %#v\n", original)
	fmt.Printf("copySlice: %#v (修改不影响原切片)\n", copySlice)

	fmt.Println()
}

// 切片是引用类型
func exampleReferenceType() {
	fmt.Println("7) 切片是引用类型:")

	// 多个切片共享底层数组
	arr := [5]int{1, 2, 3, 4, 5}
	s1 := arr[:3]
	s2 := arr[2:]

	fmt.Printf("arr: %#v\n", arr)
	fmt.Printf("s1=arr[:3]: %#v\n", s1)
	fmt.Printf("s2=arr[2:]: %#v\n", s2)

	// 修改 s1 会影响 arr 和 s2
	s1[2] = 99
	fmt.Printf("\n修改 s1[2]=99:\n")
	fmt.Printf("arr: %#v\n", arr)
	fmt.Printf("s1: %#v\n", s1)
	fmt.Printf("s2: %#v (s2[0]也变了)\n", s2)

	// append 可能导致底层数组变化
	fmt.Println("\nappend 导致分离:")
	s3 := []int{1, 2, 3}
	s4 := s3
	fmt.Printf("s3: %#v, cap=%d\n", s3, cap(s3))
	s3 = append(s3, 4, 5, 6, 7) // 超过 cap，重新分配
	fmt.Printf("s3: %#v, cap=%d (扩容后新数组)\n", s3, cap(s3))
	fmt.Printf("s4: %#v (还是原数组)\n", s4)

	fmt.Println()
}

// 删除元素
func exampleDeleteElement() {
	fmt.Println("8) 删除元素:")

	// 删除指定索引元素
	s := []int{1, 2, 3, 4, 5}
	idx := 2
	fmt.Printf("原切片: %#v\n", s)
	s = append(s[:idx], s[idx+1:]...)
	fmt.Printf("删除索引2: %#v\n", s)

	// 删除首元素
	s2 := []int{1, 2, 3, 4, 5}
	s2 = s2[1:]
	fmt.Printf("删除首元素: %#v\n", s2)

	// 删除末尾元素
	s3 := []int{1, 2, 3, 4, 5}
	s3 = s3[:len(s3)-1]
	fmt.Printf("删除末尾元素: %#v\n", s3)

	// 删除连续多个元素
	s4 := []int{1, 2, 3, 4, 5}
	// 删除索引 1-2 (元素 2, 3)
	s4 = append(s4[:1], s4[3:]...)
	fmt.Printf("删除索引1-2: %#v\n", s4)

	fmt.Println()
}

// 切片比较
func exampleComparison() {
	fmt.Println("9) 切片比较:")

	// 切片只能与 nil 比较
	var s1 []int
	s2 := []int{}

	fmt.Printf("s1 == nil: %v (nil切片)\n", s1 == nil)
	fmt.Printf("s2 == nil: %v (空切片，不是nil)\n", s2 == nil)

	// 切片之间不能直接比较
	// fmt.Println(s1 == s2) // 编译错误

	// 判断切片是否相等需要自己实现
	s3 := []int{1, 2, 3}
	s4 := []int{1, 2, 3}
	fmt.Printf("\ns3: %#v\n", s3)
	fmt.Printf("s4: %#v\n", s4)
	fmt.Printf("slicesEqual(s3, s4): %v\n", slicesEqual(s3, s4))

	fmt.Println()
}

func slicesEqual(a, b []int) bool {
	if len(a) != len(b) {
		return false
	}
	for i := range a {
		if a[i] != b[i] {
			return false
		}
	}
	return true
}

// nil 切片 vs 空切片
func exampleNilVsEmpty() {
	fmt.Println("10) nil切片 vs 空切片:")

	// nil 切片
	var nilSlice []int
	fmt.Printf("nil切片: %#v, len=%d, cap=%d, ==nil: %v\n",
		nilSlice, len(nilSlice), cap(nilSlice), nilSlice == nil)

	// 空切片
	emptySlice := []int{}
	fmt.Printf("空切片:   %#v, len=%d, cap=%d, ==nil: %v\n",
		emptySlice, len(emptySlice), cap(emptySlice), emptySlice == nil)

	emptySlice2 := make([]int, 0)
	fmt.Printf("make空:   %#v, len=%d, cap=%d, ==nil: %v\n",
		emptySlice2, len(emptySlice2), cap(emptySlice2), emptySlice2 == nil)

	// 使用建议：
	// - 如果表示"不存在"，用 nil
	// - 如果表示"空集合"，用空切片
	// - JSON序列化时，nil -> null，空切片 -> []

	fmt.Println()
}

// 切片的内存优化
func exampleMemoryOptimize() {
	fmt.Println("11) 切片内存优化:")

	// 场景：大切片的少量数据可能占用大量内存
	// 因为底层数组不会被 GC 回收

	// 获取大切片的一小部分，但可能占用大内存
	largeSlice := make([]int, 1000000)
	for i := range largeSlice {
		largeSlice[i] = i
	}

	smallPart := largeSlice[:10]
	fmt.Printf("smallPart len=%d, cap=%d (cap很大，底层数组无法释放)\n",
		len(smallPart), cap(smallPart))

	// 解决：复制到新切片
	efficientSlice := make([]int, 10)
	copy(efficientSlice, smallPart)
	fmt.Printf("efficientSlice len=%d, cap=%d (独立小数组)\n",
		len(efficientSlice), cap(efficientSlice))

	fmt.Println()
}

// 切片技巧
func exampleSliceTricks() {
	fmt.Println("12) 切片常用技巧:")

	// 切片反转
	s := []int{1, 2, 3, 4, 5}
	fmt.Printf("原切片: %#v\n", s)
	for i, j := 0, len(s)-1; i < j; i, j = i+1, j-1 {
		s[i], s[j] = s[j], s[i]
	}
	fmt.Printf("反转后: %#v\n", s)

	// 切片过滤
	nums := []int{1, 2, 3, 4, 5, 6, 7, 8, 9, 10}
	evens := nums[:0] // 创建 len=0, cap=len(nums) 的切片
	for _, n := range nums {
		if n%2 == 0 {
			evens = append(evens, n)
		}
	}
	fmt.Printf("过滤偶数: %#v\n", evens)

	// 切片去重（需要排序）
	sorted := []int{1, 1, 2, 2, 2, 3, 4, 4, 5}
	unique := sorted[:0]
	for i, n := range sorted {
		if i == 0 || n != sorted[i-1] {
			unique = append(unique, n)
		}
	}
	fmt.Printf("去重后: %#v\n", unique)

	fmt.Println()
}

func main() {
	fmt.Println("=== Go 切片(Slice) 示例 ===\n")

	exampleCreateAndInit()
	exampleLenAndCap()
	exampleSliceExpression()
	exampleAppend()
	exampleGrowMechanism()
	exampleCopy()
	exampleReferenceType()
	exampleDeleteElement()
	exampleComparison()
	exampleNilVsEmpty()
	exampleMemoryOptimize()
	exampleSliceTricks()

	fmt.Println("=== 示例结束 ===")
}
