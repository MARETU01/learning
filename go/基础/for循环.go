package main

import "fmt"

// 经典的 for 循环 (初始化; 条件; 递增)
func exampleClassicFor() {
	fmt.Println("1) 经典 for:")
	for i := 0; i < 5; i++ {
		fmt.Printf("i=%d\n", i)
	}
	fmt.Println()
}

// 类似 while 的用法: 只有条件部分
func exampleWhileStyle() {
	fmt.Println("2) 条件形式 (类似 while):")
	i := 0
	for i < 3 {
		fmt.Printf("i=%d\n", i)
		i++
	}
	fmt.Println()
}

// 无限循环 + break
func exampleInfiniteBreak() {
	fmt.Println("3) 无限循环 + break:")
	i := 0
	for {
		if i >= 3 {
			break
		}
		fmt.Printf("i=%d\n", i)
		i++
	}
	fmt.Println()
}

// 使用 range 遍历切片
func exampleRangeSlice() {
	fmt.Println("4) range 遍历切片:")
	s := []int{10, 20, 30}
	for idx, v := range s {
		fmt.Printf("index=%d value=%d\n", idx, v)
	}
	// 忽略索引
	fmt.Println("忽略索引：")
	for _, v := range s {
		fmt.Println(v)
	}
	fmt.Println()
}

// 使用 range 遍历 map（注意: map 的迭代顺序不固定）
func exampleRangeMap() {
	fmt.Println("5) range 遍历 map:")
	m := map[string]int{"apple": 3, "banana": 5, "pear": 2}
	for k, v := range m {
		fmt.Printf("key=%s value=%d\n", k, v)
	}
	fmt.Println()
}

// 使用 range 遍历字符串（按 rune）
func exampleRangeString() {
	fmt.Println("6) range 遍历字符串 (rune):")
	str := "你好，世界"
	for i, r := range str {
		fmt.Printf("byteIndex=%d rune=%c\n", i, r)
	}
	fmt.Println()
}

// 嵌套循环与标签（用于从内层循环跳出外层循环或继续外层循环）
func exampleNestedLoopLabel() {
	fmt.Println("7) 嵌套循环与标签:")
Outer:
	for i := 0; i < 3; i++ {
		for j := 0; j < 3; j++ {
			if j == 1 {
				// 跳到下一轮 Outer
				continue Outer
			}
			fmt.Printf("%d-%d\n", i, j)
		}
	}
	fmt.Println()
}

func main() {
	fmt.Println("Go 循环语句示例:\n")

	exampleClassicFor()
	exampleWhileStyle()
	exampleInfiniteBreak()
	exampleRangeSlice()
	exampleRangeMap()
	exampleRangeString()
	exampleNestedLoopLabel()
}
