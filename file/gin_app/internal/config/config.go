package config

import (
	"log"
	"os"
	"path/filepath"
	"strings"
)

const (
	MaxContentLength = 50 * 1024 * 1024 // 单次请求最大 50MB
	ChunkSize        = 5 * 1024 * 1024  // 默认分块大小 5MB
	ServerPort       = ":80"
)

var (
	UploadFolder string
	TempFolder   string
	BaseDir      string
)

func Init() {
	exePath, err := os.Executable()
	if err != nil {
		log.Fatal(err)
	}
	BaseDir = filepath.Dir(exePath)

	// go run 或 IDE 临时构建目录时，使用当前工作目录
	if strings.Contains(BaseDir, "go-build") || strings.Contains(BaseDir, "\\go-build") ||
		strings.Contains(BaseDir, "JetBrains") || strings.Contains(BaseDir, "\\JetBrains") {
		BaseDir, _ = os.Getwd()
	}

	UploadFolder = filepath.Join(BaseDir, "uploads")
	TempFolder = filepath.Join(BaseDir, "temp_uploads")

	os.MkdirAll(UploadFolder, 0755)
	os.MkdirAll(TempFolder, 0755)

	// 清理上次残留的临时分块
	if entries, err := os.ReadDir(TempFolder); err == nil {
		for _, entry := range entries {
			path := filepath.Join(TempFolder, entry.Name())
			if entry.IsDir() {
				os.RemoveAll(path)
			}
		}
	}

	log.Printf("上传目录: %s", UploadFolder)
	log.Printf("临时目录: %s", TempFolder)
}
