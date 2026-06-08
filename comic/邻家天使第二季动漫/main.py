import re
import subprocess

import requests
from bs4 import BeautifulSoup

headers = {
    "accept": "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7",
    "accept-language": "zh-CN,zh;q=0.9",
    "cache-control": "no-cache",
    "pragma": "no-cache",
    "priority": "u=0, i",
    "sec-ch-ua": "\"Google Chrome\";v=\"149\", \"Chromium\";v=\"149\", \"Not)A;Brand\";v=\"24\"",
    "sec-ch-ua-mobile": "?0",
    "sec-ch-ua-platform": "\"Windows\"",
    "sec-fetch-dest": "document",
    "sec-fetch-mode": "navigate",
    "sec-fetch-site": "none",
    "sec-fetch-user": "?1",
    "upgrade-insecure-requests": "1",
    "user-agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36"
}

ffmpeg = r"C:\Program Files\Cubase 13\Externals\FFmpeg\5.1.1\ffmpeg.exe"


base_url = "https://m.fcdm.org.cn"
vid = "40207"

response = requests.get(base_url + f"/p/{vid}/", headers=headers)

html = response.text
soup = BeautifulSoup(html, 'html.parser')

all_links = []

# 查找所有 class="vlink" 下面的 <a> 标签
vlink_div = soup.find(class_="vlink")
a_tags = vlink_div.find_all("a")  # 找到这个 div 里所有的 a 标签
for a in a_tags:
    href = a.get("href")  # 获取链接
    title = a.text.strip()  # 获取集数名称
    all_links.append({"title": title, "url": href})

# 输出结果
for link in all_links:
    response = requests.get(base_url + link["url"], headers=headers)

    html = response.text
    soup = BeautifulSoup(html, 'html.parser')

    script_tag = soup.find('script', string=re.compile('player_aaaa'))

    # 再用正则提取 url
    pattern = r'"url":"(.*?)"'
    video_url = re.search(pattern, script_tag.string).group(1).replace("\\/", "/")

    print(link["title"], video_url)

    cmd = [ffmpeg, "-i", video_url, "-c", "copy", "-y", f"{link['title']}.mp4"]

    print(f"开始下载{link['title']}...")
    subprocess.run(cmd, shell=True)
    print(f"{link['title']}.mp4下载完成！")
    print("=====================================")
