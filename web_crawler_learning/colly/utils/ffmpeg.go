package utils

import (
	"fmt"
	"os/exec"
)

const ffmpegPath = `C:\Program Files\Cubase 13\Externals\FFmpeg\5.1.1\ffmpeg.exe`

// 使用ffmpeg下载视频
func DownloadVideoWithffmpeg(savePath string, videoURL string) {
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
