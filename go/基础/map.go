package main

import (
	"fmt"
	"sort"
	"sync"
)

// 创建与初始化 map
func exampleCreateAndInit() {
	fmt.Println("1) 创建与初始化 map:")

	// 声明但不初始化：零值是 nil map
	var m1 map[string]int
	fmt.Printf("m1 == nil ? %v, len=%d\n", m1 == nil, len(m1))

	// make 初始化
	m2 := make(map[string]int)
	m2["apple"] = 3
	fmt.Printf("m2: %#v\n", m2)

	// 字面量初始化
	m3 := map[string]int{"banana": 5, "pear": 2}
	fmt.Printf("m3: %#v\n", m3)

	// make 的容量提示（hint），不影响 len，只用于减少扩容次数（并非硬限制）
	m4 := make(map[string]int, 10)
	fmt.Printf("m4 len=%d (cap hint=10)\n", len(m4))

	fmt.Println()
}

// 读取与 comma ok：区分 key 不存在 vs 值刚好等于零值
func exampleReadAndCommaOk() {
	fmt.Println("2) 读取与 comma ok:")

	m := map[string]int{"a": 1, "b": 2}

	// 直接读取：key 不存在也会返回零值
	fmt.Printf("m[\"a\"]=%d\n", m["a"])
	fmt.Printf("m[\"missing\"]=%d (missing 也返回零值)\n", m["missing"])

	// 使用 comma ok 判断是否存在
	if v, ok := m["b"]; ok {
		fmt.Printf("key=b exists, value=%d\n", v)
	}
	if v, ok := m["missing"]; !ok {
		fmt.Printf("key=missing not exists, value=%d (这里的 v 是零值)\n", v)
	}

	fmt.Println()
}

// 写入/更新/删除/len，以及 nil map 的注意事项
func exampleWriteUpdateDeleteLenNil() {
	fmt.Println("3) 写入/更新/删除/len 以及 nil map 注意:")

	m := make(map[string]string)
	m["name"] = "Alice" // 新增
	m["name"] = "Bob"   // 更新（覆盖）
	m["city"] = "Shenzhen"

	fmt.Printf("after set: %#v, len=%d\n", m, len(m))

	delete(m, "city")       // 删除存在 key
	delete(m, "not_exists") // 删除不存在 key 也安全
	fmt.Printf("after delete: %#v, len=%d\n", m, len(m))

	// nil map：能读、能 range、len=0；但是不能写（写会 panic）
	var nilMap map[string]int
	fmt.Printf("nilMap == nil ? %v, len=%d\n", nilMap == nil, len(nilMap))
	fmt.Printf("read from nilMap: nilMap[\"x\"]=%d\n", nilMap["x"])
	// nilMap["x"] = 1 // 注意：这里会 panic: assignment to entry in nil map

	fmt.Println()
}

// 遍历与稳定输出：map 遍历顺序不固定，若想稳定输出需要排序 key
func exampleRangeOrderAndSortedOutput() {
	fmt.Println("4) 遍历与稳定输出(排序 key):")

	m := map[string]int{"apple": 3, "banana": 5, "pear": 2}

	fmt.Println("直接 range 输出（顺序不保证）：")
	for k, v := range m {
		fmt.Printf("%s=%d\n", k, v)
	}

	fmt.Println("\n稳定输出（对 key 排序后再遍历）：")
	keys := make([]string, 0, len(m))
	for k := range m {
		keys = append(keys, k)
	}
	sort.Strings(keys)
	for _, k := range keys {
		fmt.Printf("%s=%d\n", k, m[k])
	}

	fmt.Println()
}

// map 是引用语义：赋值/传参会共享底层数据；要独立副本需要手动拷贝
func exampleMapIsReferenceType() {
	fmt.Println("5) 引用语义与拷贝:")

	m1 := map[string]int{"x": 1}
	m2 := m1 // m2 和 m1 指向同一份底层数据
	m2["x"] = 999
	fmt.Printf("m1=%#v\n", m1)
	fmt.Printf("m2=%#v\n", m2)

	// 想要独立拷贝，需要手动复制
	m3 := make(map[string]int, len(m1))
	for k, v := range m1 {
		m3[k] = v
	}
	m3["x"] = 100
	fmt.Printf("m3(copy)=%#v (修改 m3 不影响 m1)\n", m3)

	fmt.Println()
}

type user struct {
	Name string
	Age  int
}

// map value 是 struct 时，从 map 取出的值是拷贝，修改后要再写回
func exampleMapValueStructPitfall() {
	fmt.Println("6) map value 是 struct 的常见坑:")

	users := map[string]user{
		"u1": {Name: "Alice", Age: 18},
	}

	u := users["u1"]
	u.Age++
	users["u1"] = u // 修改后写回
	fmt.Printf("users after update: %#v\n", users)

	// 下面这种写法在 Go 中是编译错误，因为 map 下标表达式的结果不可寻址：
	// users["u1"].Age++
	// 解决办法：1) 取出来改完再写回（如上） 2) value 改成指针类型 map[string]*user

	fmt.Println()
}

// 并发访问说明：map 不是并发安全的（这里只做说明，不实际触发并发读写）
func exampleConcurrentAccessNote() {
	fmt.Println("7) 并发访问说明:")
	fmt.Println("- 普通 map 不是并发安全的，并发读写可能触发 runtime panic 或数据竞争")
	fmt.Println("- 需要并发安全时：用 sync.Mutex 保护，或使用 sync.Map(适合特定场景)")
	fmt.Println()
}

// map 的 key 类型限制：key 必须是可比较的类型
func exampleKeyTypeError() {
	fmt.Println("8) key 类型限制:")

	// ✅ 允许：基本类型、指针、数组、struct、interface
	m1 := map[[2]int]string{{1, 2}: "point"} // 数组可以作为 key
	fmt.Printf("m1=%#v\n", m1)

	// ❌ 不允许：slice、map、func（这些类型不可比较）
	// map[[]int]string        // 编译错误: invalid map key type []int
	// map[map[string]int]int  // 编译错误: invalid map key type map[string]int
	// map[func()]string       // 编译错误: invalid map key type func()

	fmt.Println("key 类型必须可比较：slice/map/func 不能作为 key")
	fmt.Println()
}

// map 不能直接比较：map 只能与 nil 比较
func exampleMapComparison() {
	fmt.Println("9) map 比较限制:")

	m1 := map[string]int{"a": 1}
	m2 := map[string]int{"a": 1}
	// fmt.Println(m1 == m2)  // 编译错误: map can only be compared to nil
	fmt.Printf("m1 == nil ? %v\n", m1 == nil) // ✅ 只能与 nil 比较
	fmt.Printf("m2 == nil ? %v\n", m2 == nil)

	fmt.Println("map 只能与 nil 比较，不能与其他 map 比较")
	fmt.Println()
}

// map 清空的方法
func exampleClearMap() {
	fmt.Println("10) map 清空方法:")

	// 方法1：重新 make（推荐，让原 map 被 GC 回收）
	m1 := map[string]int{"a": 1, "b": 2}
	fmt.Printf("m1 before clear: %#v\n", m1)
	m1 = make(map[string]int)
	fmt.Printf("m1 after make: %#v\n", m1)

	// 方法2：循环删除（适用于需要保留原 map 引用的场景）
	m2 := map[string]int{"x": 10, "y": 20}
	fmt.Printf("m2 before delete: %#v\n", m2)
	for k := range m2 {
		delete(m2, k)
	}
	fmt.Printf("m2 after delete all: %#v\n", m2)

	fmt.Println()
}

// 判断两个 map 是否相等
func exampleMapsEqual() {
	fmt.Println("11) 判断两个 map 是否相等:")

	m1 := map[string]int{"a": 1, "b": 2}
	m2 := map[string]int{"a": 1, "b": 2}
	m3 := map[string]int{"a": 1, "b": 3}

	fmt.Printf("m1=%#v\n", m1)
	fmt.Printf("m2=%#v\n", m2)
	fmt.Printf("m3=%#v\n", m3)
	fmt.Printf("mapsEqual(m1, m2)=%v\n", mapsEqual(m1, m2))
	fmt.Printf("mapsEqual(m1, m3)=%v\n", mapsEqual(m1, m3))

	fmt.Println()
}

// mapsEqual 判断两个 map 是否相等（泛型版本）
func mapsEqual[K, V comparable](m1, m2 map[K]V) bool {
	if len(m1) != len(m2) {
		return false
	}
	for k, v1 := range m1 {
		if v2, ok := m2[k]; !ok || v1 != v2 {
			return false
		}
	}
	return true
}

// sync.Map 实际使用示例：适合读多写少、key 相对稳定的场景
func exampleSyncMap() {
	fmt.Println("12) sync.Map 使用:")

	var sm sync.Map

	// 存储
	sm.Store("name", "Alice")
	sm.Store("age", 18)
	sm.Store(1, "one") // key 可以是不同类型

	// 读取
	if v, ok := sm.Load("name"); ok {
		fmt.Printf("Load \"name\": %v\n", v)
	}

	// LoadOrStore：不存在则存储，存在则返回已有值
	actual, loaded := sm.LoadOrStore("name", "Bob")
	fmt.Printf("LoadOrStore \"name\": actual=%v, loaded=%v\n", actual, loaded)

	// 遍历
	fmt.Println("Range all:")
	sm.Range(func(k, v any) bool {
		fmt.Printf("  %v=%v\n", k, v)
		return true // 返回 false 可中止遍历
	})

	// 删除
	sm.Delete("age")
	fmt.Println("After Delete \"age\":")

	// 清空（Go 1.21+）
	// sm.Clear() // 清空所有键值对

	fmt.Println()
}

// map 的合并操作
func exampleMergeMaps() {
	fmt.Println("13) map 合并操作:")

	m1 := map[string]int{"a": 1, "b": 2}
	m2 := map[string]int{"b": 20, "c": 3}

	fmt.Printf("m1=%#v\n", m1)
	fmt.Printf("m2=%#v\n", m2)

	// 合并 m2 到 m1（相同 key 会被覆盖）
	mergeMaps(m1, m2)
	fmt.Printf("after merge m2 to m1: %#v\n", m1)

	// 创建新 map 合并（不影响原 map）
	src1 := map[string]int{"x": 1}
	src2 := map[string]int{"y": 2}
	dst := make(map[string]int)
	mergeMaps(dst, src1)
	mergeMaps(dst, src2)
	fmt.Printf("new merged map: %#v\n", dst)

	fmt.Println()
}

// mergeMaps 将 src 合并到 dst（相同 key 会被覆盖）
func mergeMaps[K comparable, V any](dst, src map[K]V) {
	for k, v := range src {
		dst[k] = v
	}
}

func main() {
	fmt.Println("Go map 示例:\n")

	exampleCreateAndInit()
	exampleReadAndCommaOk()
	exampleWriteUpdateDeleteLenNil()
	exampleRangeOrderAndSortedOutput()
	exampleMapIsReferenceType()
	exampleMapValueStructPitfall()
	exampleConcurrentAccessNote()
	exampleKeyTypeError()
	exampleMapComparison()
	exampleClearMap()
	exampleMapsEqual()
	exampleSyncMap()
	exampleMergeMaps()
}
