package utils

import (
	"fmt"
	"os"
	"os/exec"
	"path/filepath"
	"runtime"

	"github.com/joho/godotenv"
)

// 全局变量存储ffmpeg路径，程序启动时自动加载
var ffmpegPath string

// init 包初始化函数，导入utils包自动执行，加载utils/.env
func init() {
	// 获取当前utils包文件所在目录
	// __FILE__ 等价实现：获取当前go文件路径
	_, thisFile, _, ok := runtime.Caller(0)
	if !ok {
		panic("无法获取utils包目录，加载.env失败")
	}
	// 拼接 utils/.env 完整路径
	envFilePath := filepath.Join(filepath.Dir(thisFile), ".env")

	// 加载env文件
	err := godotenv.Load(envFilePath)
	if err != nil {
		panic(fmt.Sprintf("加载utils/.env文件失败: %v，请检查文件是否存在", err))
	}

	// 读取环境变量
	ffmpegPath = os.Getenv("FFMPEG")
	if ffmpegPath == "" {
		panic("utils/.env 中未配置 FFMPEG")
	}
}

// DownloadVideoWithffmpeg 使用ffmpeg下载视频
func DownloadVideoWithffmpeg(savePath, videoURL string) {
	fmt.Printf("开始下载 %s...\n", savePath)

	// 构造ffmpeg命令
	cmd := exec.Command(
		ffmpegPath,
		"-i", videoURL,
		"-c", "copy",
		"-y", // 覆盖已存在的文件
		savePath,
	)

	fmt.Println(cmd)

	// 执行命令并等待完成
	output, err := cmd.CombinedOutput()
	if err != nil {
		fmt.Printf("下载 %s 失败: %v\n输出: %s\n", savePath, err, string(output))
		return
	}

	fmt.Printf("%s 下载完成！\n", savePath)
	fmt.Println("=====================================")
}
