package main

import (
	"fmt"
	"strconv"
	"strings"
	"unicode/utf8"
)

// === Go 字符串(String) ===
// 字符串是不可变的字节序列（只读的 []byte）
// 字符串底层是字节数组，不是字符数组
// 字符串默认是 UTF-8 编码

// 字符串基础
func exampleBasics() {
	fmt.Println("1) 字符串基础:")

	// 字符串声明
	var s1 string // 零值是空字符串 ""
	s2 := "hello" // 字面量
	s3 := `hello
world` // 原始字符串（支持换行，不支持转义）

	fmt.Printf("s1: %#v\n", s1)
	fmt.Printf("s2: %s\n", s2)
	fmt.Printf("s3: %s\n", s3)

	// 字符串不可变
	s4 := "hello"
	// s4[0] = 'H' // 编译错误：cannot assign to s4[0]

	// 字符串长度
	fmt.Printf("len(\"hello\"): %d\n", len("hello"))                                 // 字节数
	fmt.Printf("len(\"你好\"): %d\n", len("你好"))                                       // 6 (UTF-8中文3字节)
	fmt.Printf("utf8.RuneCountInString(\"你好\"): %d\n", utf8.RuneCountInString("你好")) // 2 (字符数)

	fmt.Println()
}

// 字符串与字节切片转换
func exampleByteConversion() {
	fmt.Println("2) 字符串与 []byte 转换:")

	s := "hello"

	// 字符串转 []byte（复制数据）
	b := []byte(s)
	fmt.Printf("[]byte: %#v\n", b)

	// []byte 转字符串
	s2 := string(b)
	fmt.Printf("string: %s\n", s2)

	// 修改字节切片不影响原字符串
	b[0] = 'H'
	fmt.Printf("修改后 byte: %s\n", string(b))
	fmt.Printf("原字符串: %s\n", s)

	// 零拷贝转换（不安全，仅作了解）
	// 使用 unsafe 包，性能敏感场景使用
	// 一般场景用标准转换即可

	fmt.Println()
}

// 字符串与 rune
func exampleRune() {
	fmt.Println("3) 字符串与 rune:")

	// rune 是 int32 的别名，表示一个 Unicode 码点
	// 字符串遍历按字节，需用 range 按字符遍历

	s := "你好世界"

	// 按字节遍历（可能乱码）
	fmt.Println("按字节遍历:")
	for i := 0; i < len(s); i++ {
		fmt.Printf("  s[%d]=%d\n", i, s[i])
	}

	// 按 rune 遍历（正确方式）
	fmt.Println("按 rune 遍历 (for-range):")
	for i, r := range s {
		fmt.Printf("  索引=%d, 字符=%c, rune=%d\n", i, r, r)
	}

	// 字符串转 []rune
	runes := []rune(s)
	fmt.Printf("[]rune: %v\n", runes)
	fmt.Printf("rune个数: %d\n", len(runes))

	// []rune 转字符串
	s2 := string(runes)
	fmt.Printf("string(runes): %s\n", s2)

	// 访问单个字符
	fmt.Printf("第一个字符: %c\n", []rune(s)[0])
	// 或
	r, _ := utf8.DecodeRuneInString(s)
	fmt.Printf("第一个字符: %c\n", r)

	fmt.Println()
}

// 字符串拼接
func exampleConcatenation() {
	fmt.Println("4) 字符串拼接:")

	// + 拼接（少量使用）
	s1 := "hello" + " " + "world"
	fmt.Printf("+ 拼接: %s\n", s1)

	// fmt.Sprintf（格式化拼接）
	s2 := fmt.Sprintf("%s %s", "hello", "world")
	fmt.Printf("Sprintf: %s\n", s2)

	// strings.Builder（大量拼接推荐）
	var builder strings.Builder
	builder.WriteString("hello")
	builder.WriteString(" ")
	builder.WriteString("world")
	s3 := builder.String()
	fmt.Printf("Builder: %s\n", s3)

	// strings.Join（有分隔符）
	parts := []string{"a", "b", "c"}
	s4 := strings.Join(parts, ",")
	fmt.Printf("Join: %s\n", s4)

	// 性能对比
	// strings.Builder > strings.Join > fmt.Sprintf > +
	// 大量拼接时推荐 strings.Builder

	fmt.Println()
}

// strings 包常用函数
func exampleStringsPackage() {
	fmt.Println("5) strings 包常用函数:")

	s := "  Hello, World!  "

	// 前后缀判断
	fmt.Printf("HasPrefix(\"Hello\"): %v\n", strings.HasPrefix(s, "Hello"))
	fmt.Printf("HasSuffix(\"!\"): %v\n", strings.HasSuffix(s, "!"))
	fmt.Printf("Contains(\"World\"): %v\n", strings.Contains(s, "World"))

	// 大小写
	fmt.Printf("ToUpper: %s\n", strings.ToUpper(s))
	fmt.Printf("ToLower: %s\n", strings.ToLower(s))

	// 去除空白
	fmt.Printf("TrimSpace: %s\n", strings.TrimSpace(s))
	fmt.Printf("Trim: %s\n", strings.Trim(s, " !"))

	// 分割
	parts := strings.Split("a,b,c", ",")
	fmt.Printf("Split: %#v\n", parts)

	// 替换
	replaced := strings.Replace("hello hello", "hello", "hi", 1)
	fmt.Printf("Replace(n=1): %s\n", replaced)
	replacedAll := strings.ReplaceAll("hello hello", "hello", "hi")
	fmt.Printf("ReplaceAll: %s\n", replacedAll)

	// 查找
	idx := strings.Index("hello", "l")
	fmt.Printf("Index(\"l\"): %d\n", idx)
	lastIdx := strings.LastIndex("hello", "l")
	fmt.Printf("LastIndex(\"l\"): %d\n", lastIdx)

	// 重复
	repeated := strings.Repeat("ab", 3)
	fmt.Printf("Repeat(\"ab\", 3): %s\n", repeated)

	// 计数
	count := strings.Count("hello", "l")
	fmt.Printf("Count(\"l\"): %d\n", count)

	fmt.Println()
}

// 字符串与数字转换
func exampleNumberConversion() {
	fmt.Println("6) 字符串与数字转换:")

	// 字符串转整数
	n, err := strconv.Atoi("123")
	if err == nil {
		fmt.Printf("Atoi(\"123\"): %d\n", n)
	}

	// 整数转字符串
	s1 := strconv.Itoa(123)
	fmt.Printf("Itoa(123): %s\n", s1)

	// 字符串转其他类型
	f, _ := strconv.ParseFloat("3.14", 64)
	fmt.Printf("ParseFloat: %f\n", f)

	b, _ := strconv.ParseBool("true")
	fmt.Printf("ParseBool: %v\n", b)

	// 其他类型转字符串
	s2 := strconv.FormatFloat(3.14159, 'f', 2, 64)
	fmt.Printf("FormatFloat: %s\n", s2)

	s3 := strconv.FormatBool(true)
	fmt.Printf("FormatBool: %s\n", s3)

	fmt.Println()
}

// 字符串比较
func exampleComparison() {
	fmt.Println("7) 字符串比较:")

	s1 := "apple"
	s2 := "banana"
	s3 := "apple"

	// == 比较
	fmt.Printf("\"apple\" == \"apple\": %v\n", s1 == s3)
	fmt.Printf("\"apple\" == \"banana\": %v\n", s1 == s2)

	// strings.Compare（返回 -1, 0, 1）
	cmp := strings.Compare(s1, s2)
	fmt.Printf("Compare(\"apple\", \"banana\"): %d (小于)\n", cmp)

	// 字典序比较（逐字节比较）
	fmt.Printf("\"a\" < \"b\": %v\n", "a" < "b")
	fmt.Printf("\"A\" < \"a\": %v (大写字母ASCII值更小)\n", "A" < "a")

	// 相等比较（忽略大小写）
	fmt.Printf("EqualFold(\"Go\", \"go\"): %v\n", strings.EqualFold("Go", "go"))

	fmt.Println()
}

// 子串操作
func exampleSubString() {
	fmt.Println("8) 子串操作:")

	s := "hello world"

	// 获取子串（切片）
	sub := s[0:5]
	fmt.Printf("s[0:5]: %s\n", sub)

	// 注意：子串和原字符串共享底层数据
	// 如果需要独立，复制出来
	subBytes := []byte(s[0:5])
	subIndependent := string(subBytes)
	_ = subIndependent

	// 查找子串位置
	idx := strings.Index(s, "world")
	fmt.Printf("Index(\"world\"): %d\n", idx)

	// 判断是否包含子串
	fmt.Printf("Contains(\"world\"): %v\n", strings.Contains(s, "world"))

	// 统计子串出现次数
	fmt.Printf("Count(\"l\"): %d\n", strings.Count(s, "l"))

	fmt.Println()
}

// 字符串遍历
func exampleIteration() {
	fmt.Println("9) 字符串遍历:")

	s := "Hello 世界"

	// 方式1：按字节遍历
	fmt.Println("按字节遍历:")
	for i := 0; i < len(s); i++ {
		fmt.Printf("  [%d]=%d(%q)\n", i, s[i], s[i])
	}

	// 方式2：按 rune 遍历（推荐）
	fmt.Println("\n按 rune 遍历:")
	for i, r := range s {
		fmt.Printf("  [%d]=%c\n", i, r)
	}

	// 方式3：转 []rune 后遍历
	fmt.Println("\n转 []rune 遍历:")
	runes := []rune(s)
	for i, r := range runes {
		fmt.Printf("  [%d]=%c\n", i, r)
	}

	fmt.Println()
}

// 字符串修改技巧
func exampleModify() {
	fmt.Println("10) 字符串修改技巧:")

	s := "hello"

	// 方法1：转 []byte 修改后转回
	b := []byte(s)
	b[0] = 'H'
	s1 := string(b)
	fmt.Printf("方法1: %s\n", s1)

	// 方法2：转 []rune 修改（适合 Unicode）
	s2 := "你好"
	runes := []rune(s2)
	runes[0] = '您'
	s3 := string(runes)
	fmt.Printf("方法2: %s\n", s3)

	// 方法3：使用 strings.Builder 构建
	var builder strings.Builder
	builder.WriteString("H")
	builder.WriteString(s[1:])
	s4 := builder.String()
	fmt.Printf("方法3: %s\n", s4)

	// 方法4：字符串拼接
	s5 := "H" + s[1:]
	fmt.Printf("方法4: %s\n", s5)

	fmt.Println()
}

// 常见陷阱
func examplePitfalls() {
	fmt.Println("11) 常见陷阱:")

	// 陷阱1：len() 返回字节数，不是字符数
	s1 := "你好"
	fmt.Printf("len(\"你好\") = %d (字节数，不是字符数)\n", len(s1))
	fmt.Printf("字符数 = %d\n", utf8.RuneCountInString(s1))

	// 陷阱2：子串可能占用大内存
	largeStr := strings.Repeat("x", 1000000)
	small := largeStr[:10]
	_ = small
	// small 虽然只有 10 字节，但 largeStr 的底层数组不会被 GC
	// 解决：复制出来
	independent := string([]byte(largeStr[:10]))
	_ = independent

	// 陷阱3：range 遍历的索引不是连续的
	s2 := "你好"
	fmt.Printf("\nrange 遍历 \"你好\" 的索引:\n")
	for i, r := range s2 {
		fmt.Printf("  i=%d, r=%c\n", i, r) // i=0, i=3（中文3字节）
	}

	// 陷阱4：字符串比较是区分大小写的
	fmt.Printf("\n\"A\" == \"a\": %v\n", "A" == "a")

	fmt.Println()
}

func main() {
	fmt.Println("=== Go 字符串(String) 示例 ===\n")

	exampleBasics()
	exampleByteConversion()
	exampleRune()
	exampleConcatenation()
	exampleStringsPackage()
	exampleNumberConversion()
	exampleComparison()
	exampleSubString()
	exampleIteration()
	exampleModify()
	examplePitfalls()

	fmt.Println("=== 示例结束 ===")
}
