import os
import re
import subprocess

import requests
from bs4 import BeautifulSoup


ffmpeg = r"C:\Program Files\Cubase 13\Externals\FFmpeg\5.1.1\ffmpeg.exe"
base_url = "https://m.fcdm.org.cn"

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

def main_page(anime_id):
    response = requests.get(base_url + f"/p/{anime_id}/", headers=headers)

    html = response.text
    soup = BeautifulSoup(html, 'html.parser')

    name = re.search(r'【(.*?)】', soup.find(class_="video_info").find("h4").get_text(strip=True)).group(1)

    if not os.path.exists(name):
        os.mkdir(name)

    # 查找所有 class="vlink" 下面的 <a> 标签
    vlink_div = soup.find(class_="vlink")
    a_tags = vlink_div.find_all("a")  # 找到这个 div 里所有的 a 标签
    for a in a_tags:
        href = a.get("href")  # 获取链接
        title = a.text.strip()  # 获取集数名称
        detail_page({"name": name, "title": title, "url": href})

def detail_page(link):
    response = requests.get(base_url + link["url"], headers=headers)

    html = response.text
    soup = BeautifulSoup(html, 'html.parser')

    script_tag = soup.find('script', string=re.compile('player_aaaa'))

    # 再用正则提取 url
    pattern = r'"url":"(.*?)"'
    video_url = re.search(pattern, script_tag.string).group(1).replace("\\/", "/")

    print(link["title"], video_url)

    save_path = os.path.join(link["name"], f"{link['title']}.mp4")

    cmd = [ffmpeg, "-i", video_url, "-c", "copy", "-y", save_path]

    print(f"开始下载{link['title']}...")
    subprocess.run(cmd, shell=True)
    print(f"{link['title']}.mp4下载完成！")
    print("=====================================")

if __name__ == "__main__":
    vids = [
        # 关于邻家的天使大人不知不觉把我惯成了废人这档子事第二季
        40207,
    ]
    for vid in vids:
        main_page(vid)
