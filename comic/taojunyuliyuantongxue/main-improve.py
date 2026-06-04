import os
import re

from PIL import Image
import requests
from bs4 import BeautifulSoup
import execjs

def merge_jpg_to_long_image(folder_path):
    img_files = [f for f in os.listdir(folder_path) if f.lower().endswith(".jpg")]
    if not img_files:
        return
    # 按文件名数字排序
    img_files.sort(key=lambda x: int(os.path.splitext(x)[0]))
    # 分组：同宽度为一组
    groups = []
    current_group = [img_files[0]]
    base_w = Image.open(os.path.join(folder_path, img_files[0])).width
    for fname in img_files[1:]:
        w = Image.open(os.path.join(folder_path, fname)).width
        if w == base_w:
            current_group.append(fname)
        else:
            groups.append(current_group)
            current_group = [fname]
            base_w = w
    groups.append(current_group)

    # 逐组合并
    for g in groups:
        start_num = int(os.path.splitext(g[0])[0])
        end_num = int(os.path.splitext(g[-1])[0])
        save_name = f"merge_{start_num}_{end_num}.jpg"
        imgs = [Image.open(os.path.join(folder_path, f)) for f in g]
        total_h = sum(i.height for i in imgs)
        w = imgs[0].width
        new_img = Image.new("RGB", (w, total_h))
        y = 0
        for im in imgs:
            new_img.paste(im, (0, y))
            y += im.height
        # 无损保存
        new_img.save(os.path.join(folder_path, save_name), "JPEG", quality=100)
        for im in imgs:
            im.close()
        new_img.close()

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

session = requests.session()
session.headers.update(headers)

response = session.get(base_url + 'manhua17161/')

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
    session.headers.update(chapter_headers)
    response = session.get(f'https://www.1kkk.com{lianzai[0]}')
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
        "_mid": "17161",
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
        session.headers.update(chapter_headers)
        response = session.get(url, params=params)

        ctx = execjs.compile(response.text)
        img_url = ctx.call("dm5imagefun")[0]
        print(img_url)
        session.headers.update(img_headers)
        response = session.get(img_url)
        with open(f'{lianzai[2]}/{n}.jpg', 'wb') as f:
            f.write(response.content)

    merge_jpg_to_long_image(f'{lianzai[2]}')
    print("===============================")

for i in range(1, len(fanwai_links) + 1):
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
    session.headers.update(chapter_headers)
    response = session.get(f'https://www.1kkk.com{fanwai[0]}')
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
        "_mid": "17161",
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
        "sec-ch-ua": "\"Google Chrome\";v=\"149\", \"Chromium\";v=\"149\", \"Not)A;Brand\";v=\"24\"",
        "sec-ch-ua-mobile": "?0",
        "sec-ch-ua-platform": "\"Windows\"",
        "sec-fetch-dest": "image",
        "sec-fetch-mode": "no-cors",
        "sec-fetch-site": "cross-site",
        "sec-fetch-storage-access": "active",
        "user-agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36"
    }
    img_params = {
        "cid": "186690",
        "key": "d9035bff1358b7407ea90b804a8cc493"
    }
    for n in range(1, int(fanwai[1]) + 1):
        params['page'] = n
        session.headers.update(chapter_headers)
        response = session.get(url, params=params)

        ctx = execjs.compile(response.text)
        img_url = ctx.call("dm5imagefun")[0]
        print(img_url)
        session.headers.update(img_headers)
        response = session.get(img_url)
        response.encoding = 'utf-8'
        with open(f'{chapter_path}/{n}.jpg', 'wb') as f:
            f.write(response.content)

    merge_jpg_to_long_image(f'{chapter_path}')
    print("===============================")