package utils

import (
	"bytes"
	"encoding/json"
	"fmt"
	"io"
	"net/http"
	"os"
	"os/exec"
	"path/filepath"
	"runtime"

	"github.com/joho/godotenv"
)

var (
	aria2cPath string
	rpcSecret  string
)

const rpcURL = "http://127.0.0.1:6800/jsonrpc"

// Aria2RPCRequest aria2 rpc 请求体
type Aria2RPCRequest struct {
	JSONRPC string        `json:"jsonrpc"`
	Method  string        `json:"method"`
	ID      string        `json:"id"`
	Params  []interface{} `json:"params"`
}

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
	aria2cPath = os.Getenv("ARIA2C")
	if aria2cPath == "" {
		panic("utils/.env 中未配置 ARIA2C")
	}
	// 读取环境变量
	rpcSecret = os.Getenv("ARIA2C_RPC_SECRET")
	if rpcSecret == "" {
		panic("utils/.env 中未配置 ARIA2C_RPC_SECRET")
	}
}

func DownloadMP4Witharia2c(savePath, videoURL string) {
	fmt.Printf("开始下载 %s...\n", savePath)

	// 构造aria2c命令
	cmd := exec.Command(
		aria2cPath,
		"-x16",
		"-s16",
		"-o", savePath,
		videoURL,
	)

	fmt.Println("cmd: ", cmd)

	// 执行命令并等待完成
	output, err := cmd.CombinedOutput()
	if err != nil {
		fmt.Printf("下载 %s 失败: %v\n输出: %s\n", savePath, err, string(output))
		return
	}

	fmt.Printf("%s 下载完成！\n", savePath)
	fmt.Println("=====================================")
}

func DownloadM3U8Witharia2c(txt, savePath string) {
	fmt.Printf("开始下载 %s...\n", savePath)

	// 构造aria2c命令
	cmd := exec.Command(
		aria2cPath,
		"-x16",
		"-s16",
		"-i", txt,
		"-d", savePath,
	)

	fmt.Println("cmd: ", cmd)

	// 执行命令并等待完成
	output, err := cmd.CombinedOutput()
	if err != nil {
		fmt.Printf("下载 %s 失败: %v\n输出: %s\n", savePath, err, string(output))
		return
	}

	fmt.Printf("%s 下载完成！\n", savePath)
	fmt.Println("=====================================")
}

func DownloadWithRpc(saveDir, fileName, downloadURL string) {
	// 任务选项，等价于 -s16 -x16
	options := map[string]interface{}{
		"max-split":                 16,
		"max-connection-per-server": 16,
		"dir":                       saveDir,
		"out":                       fileName,
	}

	reqBody := Aria2RPCRequest{
		JSONRPC: "2.0",
		Method:  "aria2.addUri",
		ID:      "colly-task",
		Params: []interface{}{
			"token:" + rpcSecret,
			[]string{downloadURL},
			options,
		},
	}

	jsonData, err := json.Marshal(reqBody)
	if err != nil {
		fmt.Printf("JSON序列化失败: %v\n", err)
		return
	}

	resp, err := http.Post(rpcURL, "application/json", bytes.NewBuffer(jsonData))
	if err != nil {
		fmt.Printf("提交任务失败: %v\n", err)
		return
	}
	defer resp.Body.Close()

	body, err := io.ReadAll(resp.Body)
	if err != nil {
		fmt.Printf("读取响应失败: %v\n", err)
		return
	}
	fmt.Printf("rpc返回内容: %s\n", string(body))

	fmt.Printf("成功提交任务: %s\n", downloadURL)
}
