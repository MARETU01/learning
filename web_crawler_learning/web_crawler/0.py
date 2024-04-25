import requests
from bs4 import BeautifulSoup

url = 'https://www.msn.cn/zh-cn/news/other/%E7%A8%B3%E5%AE%9A%E8%BE%93%E5%87%BA-%E9%B2%8D%E5%A8%81%E5%B0%94%E5%85%A8%E5%9C%BA8%E4%B8%AD5%E5%BE%97%E5%88%B011%E5%88%865%E7%AF%AE%E6%9D%BF-%E6%9B%BC8%E4%B8%AD3%E5%BE%977%E5%88%865%E7%AF%AE%E6%9D%BF/ar-AA1mTIFU?ocid=msedgntp&pc=DCTS&cvid=65a291fb3c9548d18d2b9692d1856feb&ei=7'

headers = {'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36 Edg/120.0.0.0'}

html = requests.get(url=url, headers=headers)
soup = BeautifulSoup(html.text, 'lxml')
img = soup.select_one('div > a.article-image-height-wrapper.article-image-height-wrapper-new > img')

if img:
    img_url = img['src']
    img_data = requests.get(img_url).content
    with open('鲍威尔.jpg', 'wb') as img_file:
        img_file.write(img_data)
else:
    print("未找到相关图片信息")