package main

import (
	"fmt"
	"net/http"
	"net/url"
	"os"
	"path/filepath"
	"regexp"
	"strconv"
	"strings"
	"sync"

	"github.com/dop251/goja"
	"github.com/gocolly/colly/v2"
)

const (
	kkkBaseURL = "https://www.1kkk.com"
)

var (
	// 需要爬取的漫话ID列表
	kkkMids = []int{
		// 吵闹的你不肯住口
		//78074,
		// 小桃小栗love love物语
		//17161,
		// 恋人手中四叶草
		86279,
	}
	// 预编译正则表达式
	kkkNameRegex   = regexp.MustCompile(`DM5_COMIC_MNAME="(.*?)"`)
	kkkPagesRegex  = regexp.MustCompile(`（(\d+(?:\.\d+)?)P）`)
	kkkScriptRegex = regexp.MustCompile(`DM5_MID=(\d+).*?DM5_CID=(\d+).*?DM5_VIEWSIGN="(.*?)".*?DM5_VIEWSIGN_DT="(.*?)"`)
	// 全局JS虚拟机 + 互斥锁（解决并发安全问题）
	jsVM   = goja.New()
	jsVMMu sync.Mutex // 必须加锁，否则并发会导致崩溃
)

// 视频链接信息
type kkkComicLink struct {
	Name     string // 漫话名称
	Title    string // 集数标题
	Pages    int    // 集数页数
	Href     string // 详情页链接
	IsFanWai bool   // 是否为番外
}

func execDM5ImageFun(jsCode string) (string, error) {
	// 加锁保护全局VM的并发访问
	jsVMMu.Lock()
	defer jsVMMu.Unlock()
	// 执行JS代码（定义dm5imagefun函数）
	_, err := jsVM.RunString(jsCode)
	if err != nil {
		return "", err
	}

	// 调用dm5imagefun函数
	dm5imagefun, ok := goja.AssertFunction(jsVM.Get("dm5imagefun"))
	if !ok {
		return "", nil
	}
	result, err := dm5imagefun(goja.Undefined()) // 第一个参数是this值
	if err != nil {
		return "", err
	}

	var imgUrls []string
	err = jsVM.ExportTo(result, &imgUrls)
	if err != nil {
		return "", err
	}
	return imgUrls[0], nil
}

func main() {
	// 只需要一个收集器即可
	c := colly.NewCollector(
		colly.UserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36"),
		// 可选：添加并发和限速优化
		//colly.Async(true), // 开启异步模式
	)

	// 处理主页面：提取漫画名称
	c.OnHTML("script:contains(\"DM5_COMIC_MNAME\")", func(e *colly.HTMLElement) {
		titleText := strings.TrimSpace(e.Text)
		matches := kkkNameRegex.FindStringSubmatch(titleText)
		if len(matches) < 2 {
			fmt.Println("无法提取漫话名称:", titleText)
			return
		}
		comicName := matches[1]
		fmt.Println("开始爬取:", comicName)

		// 创建文件夹
		if err := os.MkdirAll(comicName, 0755); err != nil {
			fmt.Printf("创建文件夹失败: %v\n", err)
			return
		}

		// 存储漫画名称到上下文
		e.Request.Ctx.Put("comicName", comicName)
	})

	// 处理主页面：提取所有集数链接
	c.OnHTML("#detail-list-select-1 li a, #detail-list-select-3 li a", func(e *colly.HTMLElement) {
		comicName := e.Request.Ctx.Get("comicName")
		if comicName == "" {
			return
		}

		// 提取链接、标题和页数
		href := e.Attr("href")
		titleText := strings.TrimSpace(e.Text)
		match := kkkPagesRegex.FindStringSubmatch(titleText)
		pages := 0
		if len(match) > 1 {
			// 字符串转数字
			pages, _ = strconv.Atoi(match[1])
		}
		title := kkkPagesRegex.ReplaceAllString(titleText, "")
		title = strings.TrimSpace(title)

		if href == "" || title == "" || pages == 0 {
			return
		}

		var isFanWai bool
		ulElement := e.DOM.Closest("ul")
		if ulID, _ := ulElement.Attr("id"); ulID == "detail-list-select-3" {
			isFanWai = true
		}

		// 构造完整URL并访问详情页
		comicLink := kkkComicLink{
			Name:     comicName,
			Title:    title,
			Pages:    pages,
			Href:     href,
			IsFanWai: isFanWai,
		}

		// 传递上下文并访问详情页（直接用同一个收集器）
		ctx := colly.NewContext()
		ctx.Put("comicLink", comicLink)
		c.Request("GET", kkkBaseURL+comicLink.Href, nil, ctx, nil)
	})

	// 处理详情页：提取图片地址
	c.OnHTML("script:contains(\"DM5_VIEWSIGN\")", func(e *colly.HTMLElement) {
		comicLink, ok := e.Request.Ctx.GetAny("comicLink").(kkkComicLink)
		if ok == false {
			return
		}

		signText := strings.TrimSpace(e.Text)
		matches := kkkScriptRegex.FindStringSubmatch(signText)
		if len(matches) < 5 {
			fmt.Println("无法提取sign:", signText)
			return
		}

		mid, cid, viewSign, viewSignDt := matches[1], matches[2], matches[3], matches[4]

		// 创建文件夹
		var chapterDir string
		if comicLink.IsFanWai {
			chapterDir = filepath.Join(comicLink.Name, "番外", comicLink.Title)
		} else {
			chapterDir = filepath.Join(comicLink.Name, comicLink.Title)
		}
		e.Request.Ctx.Put("chapterDir", chapterDir)

		// 创建章节文件夹
		if err := os.MkdirAll(chapterDir, 0755); err != nil {
			fmt.Printf("创建章节文件夹失败: %v\n", err)
			return
		}

		// 拼接图片脚本url
		params := url.Values{}
		params.Set("cid", cid)
		params.Set("key", "")
		params.Set("language", "1")
		params.Set("gtk", "6")
		params.Set("_cid", cid)
		params.Set("_mid", mid)
		params.Set("_dt", viewSignDt)
		params.Set("_sign", viewSign)

		for i := 1; i <= comicLink.Pages; i++ {
			params.Set("page", strconv.Itoa(i))
			e.Request.Ctx.Put("page", strconv.Itoa(i))
			imgScriptUrl := fmt.Sprintf("%s%schapterfun.ashx?%s", kkkBaseURL, comicLink.Href, params.Encode())
			c.Request("GET", imgScriptUrl, nil, e.Request.Ctx, http.Header{
				"Referer":          []string{kkkBaseURL + comicLink.Href},
				"X-Requested-With": []string{"XMLHttpRequest"},
			})
		}
	})

	// 处理图片脚本响应：解析JS获取图片URL并发起请求
	c.OnResponse(func(r *colly.Response) {
		if !strings.Contains(r.Request.URL.String(), "chapterfun.ashx") {
			return
		}
		// 从上下文获取必要信息
		comicLink, ok := r.Ctx.GetAny("comicLink").(kkkComicLink)
		if !ok {
			fmt.Println("chapterfun.ashx响应 无法获取章节信息")
			return
		}
		chapterDir, page := r.Ctx.Get("chapterDir"), r.Ctx.Get("page")
		imgUrl, err := execDM5ImageFun(string(r.Body))
		if err != nil {
			fmt.Printf("❌ %s %s 第%s页JS解析失败: %v\n", comicLink.Name, comicLink.Title, page, err)
			return
		}

		ctx := colly.NewContext()
		ctx.Put("savePath", filepath.Join(chapterDir, fmt.Sprintf("%s.jpg", page)))
		c.Request("GET", imgUrl, nil, ctx, http.Header{
			"Referer":  []string{kkkBaseURL + comicLink.Href},
			"priority": []string{"u=1, i"},
		})
	})

	// 获取图片响应并下载图片
	c.OnResponse(func(r *colly.Response) {
		if !strings.Contains(r.Request.URL.String(), ".jpg") {
			return
		}

		savePath := r.Ctx.Get("savePath")
		if savePath == "" {
			return
		}

		err := os.WriteFile(savePath, r.Body, 0644)
		if err != nil {
			fmt.Printf("❌ 保存图片失败 %s: %v\n", savePath, err)
			return
		}

		fmt.Printf("✅ 已下载: %s\n", savePath)
	})

	// 全局错误处理
	c.OnError(func(r *colly.Response, err error) {
		fmt.Printf("请求失败: %s, 错误: %v\n", r.Request.URL, err)
	})

	// 遍历所有漫画ID开始爬取
	var wg sync.WaitGroup
	for _, mid := range kkkMids {
		wg.Add(1)
		go func(id int) {
			defer wg.Done()
			url := fmt.Sprintf("%s/manhua%d/", kkkBaseURL, id)
			if err := c.Visit(url); err != nil {
				fmt.Printf("访问主页面失败: %s, 错误: %v\n", url, err)
			}
			// 等待当前漫画的所有详情页请求完成
			c.Wait()
		}(mid)
	}
	wg.Wait()
}
