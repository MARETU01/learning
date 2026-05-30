import os
import re

import requests
from bs4 import BeautifulSoup
import execjs

headers = {
    "Accept": "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7",
    "Accept-Language": "zh-CN,zh;q=0.9",
    "Cache-Control": "no-cache",
    "Connection": "keep-alive",
    "Pragma": "no-cache",
    "Sec-Fetch-Dest": "document",
    "Sec-Fetch-Mode": "navigate",
    "Sec-Fetch-Site": "none",
    "Sec-Fetch-User": "?1",
    "Upgrade-Insecure-Requests": "1",
    "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/148.0.0.0 Safari/537.36",
    "sec-ch-ua": "\"Chromium\";v=\"148\", \"Google Chrome\";v=\"148\", \"Not/A)Brand\";v=\"99\"",
    "sec-ch-ua-mobile": "?0",
    "sec-ch-ua-platform": "\"Windows\""
}

base_url = 'https://www.1kkk.com/'

response = requests.get(base_url + 'manhua78074/', headers=headers)

html = response.text

soup = BeautifulSoup(html, 'html.parser')

lianzai_links = []
fanwai_links = []

# 连载
a_list = soup.select("ul#detail-list-select-1 li a")
# 番外
b_list = soup.select("ul#detail-list-select-3 li a")

for a in a_list:
    span_text = a.select_one('span').get_text(strip=True)
    num = ''.join([c for c in span_text if c.isdigit()])
    lianzai_links.append((a.get('href'), num, a.get_text(strip=True).split('（')[0]))
lianzai_links.reverse()

for b in b_list:
    span_text = b.select_one('span').get_text(strip=True)
    num = ''.join([c for c in span_text if c.isdigit()])
    fanwai_links.append((b.get('href'), num, b.get_text(strip=True).split('（')[0]))
fanwai_links.reverse()

for lianzai in lianzai_links:
    if not os.path.exists(f'{lianzai[2]}'):
        os.mkdir(f'{lianzai[2]}')
    chapter_headers = {
        "Accept": "*/*",
        "Accept-Language": "zh-CN,zh;q=0.9",
        "Cache-Control": "no-cache",
        "Connection": "keep-alive",
        "Pragma": "no-cache",
        "Referer": f"https://www.1kkk.com{lianzai[0]}",
        "Sec-Fetch-Dest": "empty",
        "Sec-Fetch-Mode": "cors",
        "Sec-Fetch-Site": "same-origin",
        "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/148.0.0.0 Safari/537.36",
        "X-Requested-With": "XMLHttpRequest",
        "sec-ch-ua": "\"Chromium\";v=\"148\", \"Google Chrome\";v=\"148\", \"Not/A)Brand\";v=\"99\"",
        "sec-ch-ua-mobile": "?0",
        "sec-ch-ua-platform": "\"Windows\""
    }
    response = requests.get(f'https://www.1kkk.com{lianzai[0]}', headers=headers)
    html = response.text
    sign = re.search(r'DM5_VIEWSIGN="([a-f0-9]+)";', html).group(1)
    dt = re.search(r'DM5_VIEWSIGN_DT="([0-9\-\s\:]+)";', html).group(1)

    url = f'https://www.1kkk.com{lianzai[0]}chapterfun.ashx'
    cid = lianzai[0].split('-')[-1].replace('/', '')
    params = {
        "cid": cid,
        "key": "",
        "language": "1",
        "gtk": "6",
        "_cid": cid,
        "_mid": "78074",
        "_dt": dt,
        "_sign": sign
    }
    img_headers = {
        "accept": "image/avif,image/webp,image/apng,image/svg+xml,image/*,*/*;q=0.8",
        "accept-language": "zh-CN,zh;q=0.9",
        "cache-control": "no-cache",
        "pragma": "no-cache",
        "priority": "u=1, i",
        "referer": f"https://www.1kkk.com{lianzai[0]}",
        "sec-ch-ua": "\"Chromium\";v=\"148\", \"Google Chrome\";v=\"148\", \"Not/A)Brand\";v=\"99\"",
        "sec-ch-ua-mobile": "?0",
        "sec-ch-ua-platform": "\"Windows\"",
        "sec-fetch-dest": "image",
        "sec-fetch-mode": "no-cors",
        "sec-fetch-site": "cross-site",
        "sec-fetch-storage-access": "active",
        "user-agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/148.0.0.0 Safari/537.36"
    }
    for n in range(1, int(lianzai[1]) + 1):
        params['page'] = n
        response = requests.get(url, headers=chapter_headers, params=params)

        ctx = execjs.compile(response.text)
        img_url = ctx.call("dm5imagefun")[0]
        print(img_url)
        response = requests.get(img_url, headers=img_headers)
        with open(f'{lianzai[2]}/{n}.jpg', 'wb') as f:
            f.write(response.content)

    print("===============================")

for i in range(1, len(fanwai_links)):
    fanwai = fanwai_links[i - 1]
    chapter_path = f'番外/{i}_{fanwai[2]}'
    os.makedirs(chapter_path, exist_ok=True)

    chapter_headers = {
        "Accept": "*/*",
        "Accept-Language": "zh-CN,zh;q=0.9",
        "Cache-Control": "no-cache",
        "Connection": "keep-alive",
        "Pragma": "no-cache",
        "Referer": f"https://www.1kkk.com{fanwai[0]}",
        "Sec-Fetch-Dest": "empty",
        "Sec-Fetch-Mode": "cors",
        "Sec-Fetch-Site": "same-origin",
        "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/148.0.0.0 Safari/537.36",
        "X-Requested-With": "XMLHttpRequest",
        "sec-ch-ua": "\"Chromium\";v=\"148\", \"Google Chrome\";v=\"148\", \"Not/A)Brand\";v=\"99\"",
        "sec-ch-ua-mobile": "?0",
        "sec-ch-ua-platform": "\"Windows\""
    }
    response = requests.get(f'https://www.1kkk.com{fanwai[0]}', headers=headers)
    html = response.text
    sign = re.search(r'DM5_VIEWSIGN="([a-f0-9]+)";', html).group(1)
    dt = re.search(r'DM5_VIEWSIGN_DT="([0-9\-\s\:]+)";', html).group(1)

    url = f'https://www.1kkk.com{fanwai[0]}chapterfun.ashx'
    cid = fanwai[0].split('-')[-1].replace('/', '').replace("other", "")
    params = {
        "cid": cid,
        "key": "",
        "language": "1",
        "gtk": "6",
        "_cid": cid,
        "_mid": "78074",
        "_dt": dt,
        "_sign": sign
    }
    img_headers = {
        "accept": "image/avif,image/webp,image/apng,image/svg+xml,image/*,*/*;q=0.8",
        "accept-language": "zh-CN,zh;q=0.9",
        "cache-control": "no-cache",
        "pragma": "no-cache",
        "priority": "u=1, i",
        "referer": f"https://www.1kkk.com{fanwai[0]}",
        "sec-ch-ua": "\"Chromium\";v=\"148\", \"Google Chrome\";v=\"148\", \"Not/A)Brand\";v=\"99\"",
        "sec-ch-ua-mobile": "?0",
        "sec-ch-ua-platform": "\"Windows\"",
        "sec-fetch-dest": "image",
        "sec-fetch-mode": "no-cors",
        "sec-fetch-site": "cross-site",
        "sec-fetch-storage-access": "active",
        "user-agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/148.0.0.0 Safari/537.36"
    }
    for n in range(1, int(fanwai[1]) + 1):
        params['page'] = n
        response = requests.get(url, headers=chapter_headers, params=params)

        ctx = execjs.compile(response.text)
        img_url = ctx.call("dm5imagefun")[0]
        print(img_url)
        response = requests.get(img_url, headers=img_headers)
        response.encoding = 'utf-8'
        with open(f'{chapter_path}/{n}.jpg', 'wb') as f:
            f.write(response.content)

    print("===============================")