package main

import (
	"fmt"
	"os"
	"path/filepath"
	"runtime"
	"strings"
)

const (
	targetIP   = "10.8.0.3"
	targetHost = "ipv6.maretu.top"
)

// hostsPath 返回当前系统的 hosts 文件路径
func hostsPath() string {
	if runtime.GOOS == "windows" {
		root := os.Getenv("SystemRoot")
		if root == "" {
			root = `C:\Windows`
		}
		return filepath.Join(root, "System32", "drivers", "etc", "hosts")
	}
	return "/etc/hosts"
}

// containsHost 判断一行里是否包含目标主机名（无论该行是否被注释）
func containsHost(line, host string) bool {
	s := strings.TrimSpace(line)
	// 去掉行首的 # 标记（可能有多层）
	for strings.HasPrefix(s, "#") {
		s = strings.TrimSpace(s[1:])
	}
	// 去掉行内注释
	if i := strings.IndexByte(s, '#'); i >= 0 {
		s = s[:i]
	}
	for _, f := range strings.Fields(s) {
		if strings.EqualFold(f, host) {
			return true
		}
	}
	return false
}

// isCommented 判断该行是否被注释
func isCommented(line string) bool {
	return strings.HasPrefix(strings.TrimSpace(line), "#")
}

// toggleComment 翻转注释状态，保留原有缩进
func toggleComment(line string) string {
	indentLen := len(line) - len(strings.TrimLeft(line, " \t"))
	indent, rest := line[:indentLen], line[indentLen:]

	if strings.HasPrefix(rest, "#") {
		// 取消注释：去掉一个 # 和紧随其后的一个空白
		rest = rest[1:]
		if len(rest) > 0 && (rest[0] == ' ' || rest[0] == '\t') {
			rest = rest[1:]
		}
		return indent + rest
	}
	// 添加注释
	return indent + "# " + rest
}

func main() {
	path := hostsPath()

	data, err := os.ReadFile(path)
	if err != nil {
		fmt.Fprintf(os.Stderr, "读取 %s 失败: %v\n", path, err)
		fmt.Fprintln(os.Stderr, "请以管理员(Linux/macOS 用 sudo)权限运行本程序。")
		os.Exit(1)
	}

	lines := strings.Split(string(data), "\n")

	found := false
	commented := false
	changed := ""

	for i, raw := range lines {
		cr := ""
		line := raw
		if strings.HasSuffix(line, "\r") { // 兼容 CRLF
			cr, line = "\r", line[:len(line)-1]
		}
		if !containsHost(line, targetHost) {
			continue
		}
		commented = isCommented(line)
		newLine := toggleComment(line)
		lines[i] = newLine + cr
		changed = newLine
		found = true
		break // 只处理第一个匹配项
	}

	// 没找到就追加一条（未注释状态）
	if !found {
		newEntry := targetIP + " " + targetHost
		crlf := ""
		if len(lines) > 0 && strings.HasSuffix(lines[0], "\r") {
			crlf = "\r"
		}
		if len(lines) > 0 && lines[len(lines)-1] == "" {
			lines[len(lines)-1] = newEntry + crlf
			lines = append(lines, "")
		} else {
			lines = append(lines, newEntry+crlf, "")
		}
		changed = newEntry
	}

	out := strings.Join(lines, "\n")

	mode := os.FileMode(0o644)
	if info, err := os.Stat(path); err == nil {
		mode = info.Mode().Perm()
	}
	if err := os.WriteFile(path, []byte(out), mode); err != nil {
		fmt.Fprintf(os.Stderr, "写入 %s 失败: %v\n", path, err)
		fmt.Fprintln(os.Stderr, "请以管理员(Linux/macOS 用 sudo)权限运行本程序。")
		os.Exit(1)
	}

	switch {
	case !found:
		fmt.Printf("未找到 %s，已追加: %s\n", targetHost, changed)
	case commented:
		fmt.Printf("已取消注释: %s\n", strings.TrimSpace(changed))
	default:
		fmt.Printf("已添加注释: %s\n", strings.TrimSpace(changed))
	}
	fmt.Printf("文件位置: %s\n", path)

	fmt.Println()
	fmt.Print("按回车键退出...")
	var input string
	fmt.Scanln(&input)
}
