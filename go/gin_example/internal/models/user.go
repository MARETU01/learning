package models

import "time"

// User 用户模型
type User struct {
	ID        uint64    `json:"id" gorm:"primaryKey;autoIncrement"`
	Username  string    `json:"username" gorm:"type:varchar(50);uniqueIndex;not null;comment:用户名"`
	Email     string    `json:"email" gorm:"type:varchar(100);uniqueIndex;not null;comment:邮箱"`
	Age       uint      `json:"age" gorm:"default:0;comment:年龄"`
	Status    int8      `json:"status" gorm:"default:1;comment:状态: 0-禁用, 1-启用"`
	CreatedAt time.Time `json:"created_at" gorm:"autoCreateTime;comment:创建时间"`
	UpdatedAt time.Time `json:"updated_at" gorm:"autoUpdateTime;comment:更新时间"`
}

// TableName 指定表名
func (User) TableName() string {
	return "users"
}

// CreateUserRequest 创建用户请求
type CreateUserRequest struct {
	Username string `json:"username" binding:"required,min=2,max=50"`
	Email    string `json:"email" binding:"required,email"`
	Age      uint   `json:"age" binding:"min=0,max=150"`
}

// UpdateUserRequest 更新用户请求
type UpdateUserRequest struct {
	Username string `json:"username" binding:"omitempty,min=2,max=50"`
	Email    string `json:"email" binding:"omitempty,email"`
	Age      uint   `json:"age" binding:"omitempty,min=0,max=150"`
	Status   *int8  `json:"status" binding:"omitempty,oneof=0 1"`
}
