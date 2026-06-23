package main

import (
	"colly/utils"
	"fmt"
	"os"
	"path/filepath"
	"regexp"
	"strings"
	"sync"

	"github.com/gocolly/colly/v2"
)

const (
	fcdmBaseURL = "https://m.fcdm.org.cn"
)

var (
	// 需要爬取的动漫ID列表
	fcdmVids = []int{
		// 关于邻家的天使大人不知不觉把我惯成了废人这档子事第二季
		//40207,
		// 大正少女御伽话
		//41887,
	}
	// 预编译正则表达式
	fcdmNameRegex = regexp.MustCompile(`【(.*?)】`)
	fcdmRrlRegex  = regexp.MustCompile(`"url":"(.*?)"`)
)

// 视频链接信息
type fcdmVideoLink struct {
	Name  string // 动漫名称
	Title string // 集数标题
	Href  string // 详情页链接
}

func main() {
	// 只需要一个收集器即可
	c := colly.NewCollector(
		colly.AllowedDomains("m.fcdm.org.cn"),
		colly.UserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36"),
		// 可选：添加并发和限速优化
		colly.Async(true), // 开启异步模式
	)

	// 处理主页面：提取动漫名称
	c.OnXML("//div[contains(@class, 'video_info')]/h4", func(e *colly.XMLElement) {
		titleText := strings.TrimSpace(e.Text)
		matches := fcdmNameRegex.FindStringSubmatch(titleText)
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

		// 存储动漫名称到上下文
		e.Request.Ctx.Put("animeName", animeName)
	})

	// 处理主页面：提取所有集数链接
	c.OnXML("(//div[contains(@class, 'vlink')])[1]//a", func(e *colly.XMLElement) {
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
		videoLink := fcdmVideoLink{
			Name:  animeName,
			Title: title,
			Href:  href,
		}

		// 传递上下文并访问详情页（直接用同一个收集器）
		ctx := colly.NewContext()
		ctx.Put("videoLink", videoLink)
		c.Request("GET", fcdmBaseURL+href, nil, ctx, nil)
	})

	// 处理详情页：提取视频播放地址
	c.OnXML("//script[contains(text(), 'player_aaaa')]", func(e *colly.XMLElement) {
		videoLink, ok := e.Request.Ctx.GetAny("videoLink").(fcdmVideoLink)
		if !ok {
			fmt.Println("获取视频链接信息失败")
			return
		}

		// 从script内容中提取视频URL
		scriptContent := e.Text
		matches := fcdmRrlRegex.FindStringSubmatch(scriptContent)
		if len(matches) < 2 {
			fmt.Printf("无法提取视频URL: %s\n", videoLink.Title)
			return
		}

		// 处理转义字符
		videoURL := strings.ReplaceAll(matches[1], "\\/", "/")
		fmt.Printf("%s: %s\n", videoLink.Title, videoURL)

		// 下载视频
		utils.DownloadVideoWithffmpeg(filepath.Join(videoLink.Name, fmt.Sprintf("%s.mp4", videoLink.Title)), videoURL)
	})

	// 全局错误处理
	c.OnError(func(r *colly.Response, err error) {
		fmt.Printf("请求失败: %s, 错误: %v\n", r.Request.URL, err)
	})

	// 遍历所有动漫ID开始爬取
	var wg sync.WaitGroup
	for _, vid := range fcdmVids {
		wg.Go(func() {
			url := fmt.Sprintf("%s/p/%d/", fcdmBaseURL, vid)
			if err := c.Visit(url); err != nil {
				fmt.Printf("访问主页面失败: %s, 错误: %v\n", url, err)
			}
			// 等待当前动漫的所有详情页请求完成
			c.Wait()
		})
	}
	wg.Wait()

	fmt.Println("所有爬取任务完成！")
}
