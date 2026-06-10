package main

import (
	"fmt"
	"os"
	"os/exec"
	"path/filepath"
	"regexp"
	"strings"
	"sync"

	"github.com/gocolly/colly/v2"
)

const (
	ffmpegPath = `C:\Program Files\Cubase 13\Externals\FFmpeg\5.1.1\ffmpeg.exe`
	baseURL    = "https://m.fcdm.org.cn"
)

var (
	// 预编译正则表达式
	nameRegex = regexp.MustCompile(`【(.*?)】`)
	urlRegex  = regexp.MustCompile(`"url":"(.*?)"`)
)

// 视频链接信息
type VideoLink struct {
	Name  string // 动漫名称
	Title string // 集数标题
	URL   string // 详情页链接
}

func main() {
	// 需要爬取的动漫ID列表
	vids := []int{
		// 关于邻家的天使大人不知不觉把我惯成了废人这档子事第二季
		40207,
	}

	// 创建主收集器
	cmain := colly.NewCollector(
		colly.AllowedDomains("m.fcdm.org.cn"),
		colly.UserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36"),
	)

	// 创建详情页收集器（继承主收集器的配置）
	cdetail := cmain.Clone()

	// 使用OnXML处理主页面（XPath方式）
	cmain.OnXML("//div[contains(@class, 'video_info')]/h4", func(e *colly.XMLElement) {
		// 提取动漫名称
		titleText := strings.TrimSpace(e.Text)
		matches := nameRegex.FindStringSubmatch(titleText)
		if len(matches) < 2 {
			fmt.Println("无法提取动漫名称:", titleText)
			return
		}
		animeName := matches[1]
		fmt.Println("开始爬取:", animeName)

		// 创建文件夹
		if err := os.MkdirAll(animeName, 0755); err != nil {
			fmt.Printf("创建文件夹失败: %v\n", err)
			return
		}

		// 存储动漫名称到上下文，供后续使用
		e.Request.Ctx.Put("animeName", animeName)
	})

	// 使用OnXML提取所有集数链接（XPath方式）
	cmain.OnXML("//div[contains(@class, 'vlink')]//a", func(e *colly.XMLElement) {
		animeName := e.Request.Ctx.Get("animeName")
		if animeName == "" {
			return
		}

		// 提取链接和标题
		href := e.Attr("href")
		title := strings.TrimSpace(e.Text)

		if href == "" || title == "" {
			return
		}

		// 构造完整URL并访问详情页
		fullURL := baseURL + href
		videoLink := VideoLink{
			Name:  animeName,
			Title: title,
			URL:   fullURL,
		}

		// 将视频链接信息传递给详情页收集器
		ctx := colly.NewContext()
		ctx.Put("videoLink", videoLink)
		cdetail.Request("GET", fullURL, nil, ctx, nil)
	})

	// 使用OnXML处理详情页，提取包含player_aaaa的script标签
	cdetail.OnXML("//script[contains(text(), 'player_aaaa')]", func(e *colly.XMLElement) {
		videoLink, ok := e.Request.Ctx.GetAny("videoLink").(VideoLink)
		if !ok {
			fmt.Println("获取视频链接信息失败")
			return
		}

		// 从script内容中提取视频URL
		scriptContent := e.Text
		matches := urlRegex.FindStringSubmatch(scriptContent)
		if len(matches) < 2 {
			fmt.Printf("无法提取视频URL: %s\n", videoLink.Title)
			return
		}

		// 处理转义字符
		videoURL := strings.ReplaceAll(matches[1], "\\/", "/")
		fmt.Printf("%s: %s\n", videoLink.Title, videoURL)

		// 下载视频
		downloadVideo(videoLink, videoURL)
	})

	// 错误处理
	cmain.OnError(func(r *colly.Response, err error) {
		fmt.Printf("请求失败: %s, 错误: %v\n", r.Request.URL, err)
	})

	cdetail.OnError(func(r *colly.Response, err error) {
		fmt.Printf("详情页请求失败: %s, 错误: %v\n", r.Request.URL, err)
	})

	// 遍历所有动漫ID开始爬取
	var wg sync.WaitGroup
	for _, vid := range vids {
		wg.Add(1)
		go func(id int) {
			defer wg.Done()
			url := fmt.Sprintf("%s/p/%d/", baseURL, id)
			if err := cmain.Visit(url); err != nil {
				fmt.Printf("访问主页面失败: %s, 错误: %v\n", url, err)
			}
		}(vid)
	}
	wg.Wait()

	fmt.Println("所有爬取任务完成！")
}

// 使用ffmpeg下载视频
func downloadVideo(link VideoLink, videoURL string) {
	savePath := filepath.Join(link.Name, fmt.Sprintf("%s.mp4", link.Title))
	fmt.Printf("开始下载 %s...\n", link.Title)

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
		fmt.Printf("下载 %s 失败: %v\n输出: %s\n", link.Title, err, string(output))
		return
	}

	fmt.Printf("%s.mp4 下载完成！\n", link.Title)
	fmt.Println("=====================================")
}
