package main

import (
	"colly/utils"
	"encoding/base64"
	"fmt"
	"net/url"
	"os"
	"path"
	"regexp"
	"strings"
	"sync"

	"github.com/dop251/goja"
	"github.com/gocolly/colly/v2"
)

const (
	pBaseURL = "https://www.z1c9b.top"
)

var (
	// 需要爬取的ID列表
	pids = []string{
		"cYcL3NoaXBpbi9wbGF5LTQyNTczNS5odG1s",
	}
	// 预编译正则表达式
	videoRegex = regexp.MustCompile(`var video\s*=\s*decodeString\('([^']+)'\)`)
	hostRegex  = regexp.MustCompile(`var m3u8_host\s*=\s*decodeString\('([^']+)'\)`)
	titleRegex = regexp.MustCompile(`<div\s+class="play-title">\s*<a[^>]*\s+title="([^"]+)"[^>]*>`)
	tsRegex    = regexp.MustCompile(`https?://[^\s#,]+?\.ts`)
	keyRegex   = regexp.MustCompile(`#EXT-X-KEY:.*?URI="([^"]+)"`)
	// 全局JS虚拟机 + 互斥锁（解决并发安全问题）
	once    sync.Once
	pjsVM   = goja.New()
	pjsVMMu sync.Mutex // 必须加锁，否则并发会导致崩溃
	cnRegex = regexp.MustCompile(`\p{Han}`)
)

func encryptJSDecrypt(jsCode, data string) (string, error) {
	match := cnRegex.FindStringSubmatch(data)
	if len(match) != 0 {
		return data, nil
	}

	var err error
	once.Do(func() {
		crypto, _ := os.ReadFile(path.Join("utils", "js", "crypto-js.min.js"))
		_, _ = pjsVM.RunString(string(crypto))
		_ = pjsVM.Set("window", pjsVM.NewObject())
		_, err = pjsVM.RunString(jsCode)
	})
	if err != nil {
		return "", err
	}

	if data == "" {
		return "", nil
	}

	// 加锁保护全局VM的并发访问
	pjsVMMu.Lock()
	defer pjsVMMu.Unlock()

	// 调用window.Decrypt函数
	decrypt, ok := goja.AssertFunction(pjsVM.Get("Decrypt"))
	if !ok {
		return "", nil
	}
	result, err := decrypt(goja.Undefined(), pjsVM.ToValue(strings.TrimSpace(data)), pjsVM.ToValue("883346"))
	if err != nil {
		return "", err
	}

	return strings.ReplaceAll(result.String(), `"`, ""), nil
}

func main() {
	// 只需要一个收集器即可
	c := colly.NewCollector(
		colly.UserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36"),
		// 可选：添加并发和限速优化
		colly.Async(true), // 开启异步模式
	)

	c.OnResponse(func(r *colly.Response) {
		if !strings.Contains(r.Request.URL.String(), "encrypt.js") {
			return
		}
		_, err := encryptJSDecrypt(strings.ReplaceAll(string(r.Body), "window.Decrypt", "Decrypt"), "")
		if err != nil {
			fmt.Println("解密JS加载失败:", err)
		}
	})

	c.OnHTML("script:contains(m3u8_host)", func(e *colly.HTMLElement) {
		scriptText := e.Text
		titleMatches := titleRegex.FindStringSubmatch(string(e.Response.Body))
		if len(titleMatches) < 2 {
			return
		}
		title, _ := encryptJSDecrypt("", titleMatches[1])
		fmt.Println("视频标题:", title)

		hostMatches := hostRegex.FindStringSubmatch(scriptText)
		if len(hostMatches) < 2 {
			return
		}
		videoMatches := videoRegex.FindStringSubmatch(scriptText)
		if len(videoMatches) < 2 {
			return
		}

		videoHost, _ := base64.StdEncoding.DecodeString(hostMatches[1])
		videoLink, _ := base64.StdEncoding.DecodeString(videoMatches[1])
		videoURL := string(videoHost) + string(videoLink)
		fmt.Println("视频链接:", videoURL)
		ctx := colly.NewContext()
		ctx.Put("title", title)
		c.Request("GET", videoURL, nil, ctx, nil)
	})

	c.OnResponse(func(r *colly.Response) {
		m3u8Url := r.Request.URL.String()
		if !strings.Contains(m3u8Url, "m3u8") {
			return
		}
		// 从上下文获取必要信息
		title := r.Request.Ctx.Get("title")
		savePath := path.Join("p", title)
		_ = os.MkdirAll(savePath, 0755)

		m3u8 := string(r.Body)
		//fmt.Println(m3u8)
		var wg sync.WaitGroup
		keyChan, tsChan := make(chan string, 1), make(chan string, 10)
		// 下载key
		wg.Go(func() {
			keyMatches := keyRegex.FindStringSubmatch(m3u8)
			if len(keyMatches) < 2 {
				return
			}
			key := keyMatches[1]
			keyChan <- key
			close(keyChan)
			// 解析 m3u8 页面的基础URL
			baseURL, _ := url.Parse(m3u8Url)
			keyURL := baseURL.ResolveReference(&url.URL{Path: key})
			fullKeyUrl := keyURL.String()
			fmt.Println("完整密钥链接:", fullKeyUrl)

			c.Request("GET", fullKeyUrl, nil, r.Ctx, nil)
		})
		// 制作txt和aria2c下载ts分片
		wg.Go(func() {
			tsList, result := tsRegex.FindAllString(m3u8, -1), strings.Builder{}
			for _, ts := range tsList {
				tsChan <- ts
				result.WriteString(ts)
				result.WriteByte('\n')
			}
			close(tsChan)
			os.WriteFile(path.Join("p", title, "ts_list.txt"), []byte(result.String()), 0644)

			utils.DownloadM3U8Witharia2c(path.Join("p", title, "ts_list.txt"), path.Join("p", title))
		})
		// 制作local.m3u8
		wg.Go(func() {
			for key := range keyChan {
				m3u8 = strings.Replace(m3u8, key, "ts.key", 1)
			}

			for ts := range tsChan {
				newTs := strings.Split(ts, "/")
				m3u8 = strings.Replace(m3u8, ts, newTs[len(newTs)-1], 1)
			}

			os.WriteFile(path.Join("p", title, "local.m3u8"), []byte(m3u8), 0644)
		})
		wg.Wait()
		// 合并
		utils.ConcatTsWithffmpeg(path.Join("p", title, "local.m3u8"), path.Join("p", title+".mp4"))
		os.RemoveAll(path.Join("p", title)) // 删除临时文件夹
	})

	c.OnResponse(func(r *colly.Response) {
		if !strings.Contains(r.Request.URL.String(), "ts.key") {
			return
		}
		// 从上下文获取必要信息
		title := r.Request.Ctx.Get("title")
		os.WriteFile(path.Join("p", title, "ts.key"), r.Body, 0644)
	})

	// 解密js
	c.Visit(pBaseURL + "/assets/js/common/encrypt.js")

	// 遍历所有ID开始爬取
	var wg sync.WaitGroup
	for _, id := range pids {
		wg.Go(func() {
			url := fmt.Sprintf("%s/%s.html", pBaseURL, id)
			if err := c.Visit(url); err != nil {
				fmt.Printf("访问主页面失败: %s, 错误: %v\n", url, err)
			}
			c.Wait()
		})
	}
	wg.Wait()

	fmt.Println("所有爬取任务完成！")
}
