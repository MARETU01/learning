package main

import (
	"errors"
	"fmt"
)

// === Go 错误处理(Error) ===
// Go 使用 error 类型表示错误，而不是异常
// error 是一个内置接口：type error interface { Error() string }
// 函数通常返回 error 作为最后一个返回值

// 基本错误创建
func exampleCreateError() {
	fmt.Println("1) 创建错误:")

	// 使用 errors.New 创建简单错误
	err1 := errors.New("something went wrong")
	fmt.Printf("err1: %v\n", err1)

	// 使用 fmt.Errorf 创建格式化错误
	name := "test"
	err2 := fmt.Errorf("failed to process %s", name)
	fmt.Printf("err2: %v\n", err2)

	// fmt.Errorf 支持 %w 包装错误（Go 1.13+）
	inner := errors.New("inner error")
	err3 := fmt.Errorf("outer error: %w", inner)
	fmt.Printf("err3: %v\n", err3)

	fmt.Println()
}

// 返回错误
func divide(a, b int) (int, error) {
	if b == 0 {
		return 0, errors.New("division by zero")
	}
	return a / b, nil
}

func exampleReturnError() {
	fmt.Println("2) 返回错误:")

	// 正常情况
	if result, err := divide(10, 2); err != nil {
		fmt.Printf("错误: %v\n", err)
	} else {
		fmt.Printf("结果: %d\n", result)
	}

	// 错误情况
	if result, err := divide(10, 0); err != nil {
		fmt.Printf("错误: %v\n", err)
	} else {
		fmt.Printf("结果: %d\n", result)
	}

	fmt.Println()
}

// 自定义错误类型
type ValidationError struct {
	Field   string
	Message string
}

func (e *ValidationError) Error() string {
	return fmt.Sprintf("validation error: %s - %s", e.Field, e.Message)
}

type NotFoundError struct {
	Resource string
	ID       int
}

func (e *NotFoundError) Error() string {
	return fmt.Sprintf("%s with ID %d not found", e.Resource, e.ID)
}

func exampleCustomError() {
	fmt.Println("3) 自定义错误类型:")

	err1 := &ValidationError{
		Field:   "email",
		Message: "invalid email format",
	}
	fmt.Printf("err1: %v\n", err1)

	err2 := &NotFoundError{
		Resource: "User",
		ID:       123,
	}
	fmt.Printf("err2: %v\n", err2)

	fmt.Println()
}

// 错误类型断言
func processUser(id int) error {
	if id <= 0 {
		return &ValidationError{
			Field:   "id",
			Message: "must be positive",
		}
	}
	if id == 404 {
		return &NotFoundError{
			Resource: "User",
			ID:       id,
		}
	}
	return nil
}

func exampleErrorTypeAssertion() {
	fmt.Println("4) 错误类型断言:")

	err := processUser(-1)
	if err != nil {
		// 方式1：类型断言
		if ve, ok := err.(*ValidationError); ok {
			fmt.Printf("验证错误: Field=%s, Message=%s\n", ve.Field, ve.Message)
		} else if ne, ok := err.(*NotFoundError); ok {
			fmt.Printf("未找到: Resource=%s, ID=%d\n", ne.Resource, ne.ID)
		} else {
			fmt.Printf("其他错误: %v\n", err)
		}
	}

	fmt.Println()
}

// errors.Is 和 errors.As（Go 1.13+）
func exampleErrorsIsAs() {
	fmt.Println("5) errors.Is 和 errors.As:")

	// errors.Is：检查错误链中是否包含特定错误
	var ErrNotFound = errors.New("not found")
	err := fmt.Errorf("get user: %w", ErrNotFound)

	fmt.Printf("errors.Is(err, ErrNotFound): %v\n", errors.Is(err, ErrNotFound))

	// errors.As：提取错误链中特定类型的错误
	var validationErr *ValidationError
	err2 := &ValidationError{Field: "name", Message: "required"}
	wrappedErr := fmt.Errorf("validate: %w", err2)

	if errors.As(wrappedErr, &validationErr) {
		fmt.Printf("提取到 ValidationError: Field=%s\n", validationErr.Field)
	}

	fmt.Println()
}

// 错误包装和解包
func exampleErrorWrapping() {
	fmt.Println("6) 错误包装和解包:")

	// 原始错误
	original := errors.New("connection refused")

	// 第一层包装
	wrapped1 := fmt.Errorf("connect to db: %w", original)

	// 第二层包装
	wrapped2 := fmt.Errorf("initialize app: %w", wrapped1)

	fmt.Printf("wrapped2: %v\n", wrapped2)

	// 解包
	if unwrapped := errors.Unwrap(wrapped2); unwrapped != nil {
		fmt.Printf("unwrapped: %v\n", unwrapped)
	}

	// 检查错误链
	fmt.Printf("Is original: %v\n", errors.Is(wrapped2, original))

	fmt.Println()
}

// 多错误处理（Go 1.20+）
func exampleMultiError() {
	fmt.Println("7) 多错误处理:")

	// 使用 errors.Join 合并多个错误
	err1 := errors.New("error 1")
	err2 := errors.New("error 2")
	err3 := errors.New("error 3")

	combined := errors.Join(err1, err2, err3)
	fmt.Printf("combined: %v\n", combined)

	// 检查是否包含特定错误
	fmt.Printf("Is err2: %v\n", errors.Is(combined, err2))

	fmt.Println()
}

// 错误处理最佳实践
func exampleBestPractices() {
	fmt.Println("8) 错误处理最佳实践:")

	// 1. 不要忽略错误
	// result, _ := someFunc() // 不推荐

	// 2. 尽早返回
	fmt.Println("原则: 尽早返回，减少嵌套")
	// 推荐：
	// if err != nil {
	//     return err
	// }
	// // 继续处理

	// 3. 添加上下文
	fmt.Println("原则: 添加有意义的上下文")
	// return fmt.Errorf("failed to load user %d: %w", userID, err)

	// 4. 定义哨兵错误
	var ErrNotFound = errors.New("not found")
	fmt.Printf("哨兵错误: %v\n", ErrNotFound)

	// 5. 使用自定义错误类型提供更多信息
	fmt.Println("原则: 使用自定义错误类型提供结构化信息")

	fmt.Println()
}

// panic 和 recover
func examplePanicRecover() {
	fmt.Println("9) panic 和 recover:")

	// 使用 defer + recover 捕获 panic
	defer func() {
		if r := recover(); r != nil {
			fmt.Printf("Recovered from panic: %v\n", r)
		}
	}()

	fmt.Println("About to panic...")
	panic("something terrible happened")
	// 这行不会执行
	fmt.Println("After panic")
}

// 实际应用示例
type UserService struct {
	users map[int]string
}

func (s *UserService) GetUser(id int) (string, error) {
	if id <= 0 {
		return "", &ValidationError{
			Field:   "id",
			Message: "must be positive",
		}
	}
	if name, ok := s.users[id]; ok {
		return name, nil
	}
	return "", &NotFoundError{
		Resource: "User",
		ID:       id,
	}
}

func exampleRealWorld() {
	fmt.Println("10) 实际应用示例:")

	service := &UserService{
		users: map[int]string{
			1: "Alice",
			2: "Bob",
		},
	}

	// 正常情况
	if name, err := service.GetUser(1); err != nil {
		fmt.Printf("Error: %v\n", err)
	} else {
		fmt.Printf("Found user: %s\n", name)
	}

	// 验证错误
	if name, err := service.GetUser(-1); err != nil {
		var ve *ValidationError
		if errors.As(err, &ve) {
			fmt.Printf("Validation failed: %s - %s\n", ve.Field, ve.Message)
		}
	}

	// 未找到错误
	if name, err := service.GetUser(999); err != nil {
		var nf *NotFoundError
		if errors.As(err, &nf) {
			fmt.Printf("%s (ID: %d)\n", nf.Resource, nf.ID)
		}
	}

	fmt.Println()
}

func main() {
	fmt.Println("=== Go 错误处理(Error) 示例 ===\n")

	exampleCreateError()
	exampleReturnError()
	exampleCustomError()
	exampleErrorTypeAssertion()
	exampleErrorsIsAs()
	exampleErrorWrapping()
	exampleMultiError()
	exampleBestPractices()
	examplePanicRecover()
	exampleRealWorld()

	fmt.Println("=== 示例结束 ===")
}
