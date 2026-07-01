package utils

import (
	"fmt"
	"os/exec"
)

const aria2cPath = `C:\Users\26360\Desktop\Projects\Tools\aria2c\aria2c.exe`

func DownloadMP4Witharia2c(savePath, videoURL string) {
	fmt.Printf("开始下载 %s...\n", savePath)

	// 构造ffmpeg命令
	cmd := exec.Command(
		aria2cPath,
		"-x16",
		"-s16",
		"-o", savePath,
		videoURL,
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
