package main

import (
	"colly/utils"
	"encoding/json"
	"fmt"
	"os"
	"path/filepath"
	"strconv"
	"strings"
	"sync"

	"github.com/gocolly/colly/v2"
)

const (
	yhdmBaseURL = "https://yhdm.one"
)

var (
	// 需要爬取的动漫ID列表
	yhdmVids = []int{
		// 关于邻家的天使大人不知不觉把我惯成了废人这档子事第二季
		2026601236,
		// 想结束这场“我爱你”的游戏
		2026321082,
	}
)

// yhdm视频链接信息
type yhdmVideoSource struct {
	VideoPlays  []yhdmVideoPlay `json:"video_plays"`  // 播放源列表
	HtmlContent string          `json:"html_content"` // 前端HTML内容
}
type yhdmVideoPlay struct {
	PlayData string `json:"play_data"` // m3u8 播放地址
	SrcSite  string `json:"src_site"`  // 线路标识
}

func main() {
	// 只需要一个收集器即可
	c := colly.NewCollector(
		colly.UserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36"),
		// 可选：添加并发和限速优化
		colly.Async(true), // 开启异步模式
	)

	// 处理主页面：提取动漫名称
	c.OnHTML("h1.names", func(e *colly.HTMLElement) {
		animeName := strings.TrimSpace(e.Text)
		fmt.Println("开始爬取:", animeName)

		// 创建文件夹
		if err := os.MkdirAll(animeName, 0755); err != nil {
			fmt.Printf("创建文件夹失败: %v\n", err)
			return
		}

		// 存储动漫名称到上下文
		e.Request.Ctx.Put("animeName", animeName)
	})

	// 处理主页面：提取所有集数
	c.OnHTML("ul.row.list-unstyled.gutters-1", func(e *colly.HTMLElement) {
		vid, animeName := e.Request.Ctx.Get("vid"), e.Request.Ctx.Get("animeName")
		if vid == "" || animeName == "" {
			return
		}

		// 获取 li 元素列表
		liElements := e.DOM.Find("li")
		if liElements.Length() == 0 {
			return
		}

		// 获取视频链接列表
		for i := 1; i <= liElements.Length(); i++ {
			url := fmt.Sprintf("%s/_get_plays/%s/ep%d", yhdmBaseURL, vid, i)
			ctx := e.Request.Ctx.Clone()
			ctx.Put("num", strconv.Itoa(i))
			c.Request("GET", url, nil, ctx, nil)
		}
	})

	// 提取视频播放链接并下载
	c.OnResponse(func(r *colly.Response) {
		if !strings.Contains(r.Request.URL.String(), "_get_plays") {
			return
		}

		// 从上下文获取必要信息
		vid, animeName, num := r.Request.Ctx.Get("vid"), r.Request.Ctx.Get("animeName"), r.Request.Ctx.Get("num")
		if vid == "" || animeName == "" || num == "" {
			return
		}

		var data yhdmVideoSource

		// 2. JSON 反序列化，绑定到结构体
		err := json.Unmarshal(r.Body, &data)
		if err != nil {
			fmt.Printf("解析JSON失败: %v\n", err)
			return
		}

		for _, play := range data.VideoPlays {
			if play.SrcSite == "jszy" {
				utils.DownloadVideoWithffmpeg(filepath.Join(animeName, fmt.Sprintf("%s.mp4", num)), play.PlayData)
				break
			}
		}
	})

	// 全局错误处理
	c.OnError(func(r *colly.Response, err error) {
		fmt.Printf("请求失败: %s, 错误: %v\n", r.Request.URL, err)
	})

	// 遍历所有动漫ID开始爬取
	var wg sync.WaitGroup
	for _, vid := range yhdmVids {
		wg.Go(func() {
			url := fmt.Sprintf("%s/vod/%d.html", yhdmBaseURL, vid)
			ctx := colly.NewContext()
			ctx.Put("vid", strconv.Itoa(vid))
			if err := c.Request("GET", url, nil, ctx, nil); err != nil {
				fmt.Printf("访问主页面失败: %s, 错误: %v\n", url, err)
			}
			// 等待当前动漫的所有详情页请求完成
			c.Wait()
		})
	}
	wg.Wait()

	fmt.Println("所有爬取任务完成！")
}
