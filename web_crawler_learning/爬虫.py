import requests
import re
import os

url = "https://pic.netbian.com/"
headers = {
    "user-agent": "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/89.0.4389.90 Safari/537.36"
}

response = requests.get(url=url,headers=headers)
response.encoding = response.apparent_encoding

"""
. 表示除空格外任意字符（除\n外）
* 表示匹配字符零次或多次
? 表示匹配字符零次或一次
.*? 非贪婪匹配
"""
parr = re.compile('src="(/u.*?)".alt="(.*?)"') # 匹配图片链接和图片名字
image = re.findall(parr,response.text)

path = "啦啦啦德玛西亚"
if not os.path.isdir(path): # 判断是否存在该文件夹，若不存在则创建
    os.mkdir(path) # 创建
    
# 对列表进行遍历
for i in image:
    link = i[0] # 获取链接
    name = i[1] # 获取名字
    """
    在文件夹下创建一个空jpg文件，打开方式以 'wb' 二进制读写方式
    @param res：图片请求的结果
    """
    with open(path+"/{}.jpg".format(name),"wb") as img:
        res = requests.get("https://pic.netbian.com"+link)
        img.write(res.content) # 将图片请求的结果内容写到jpg文件中
        print(res.content)
        img.close() # 关闭操作
        print(name+".jpg 获取成功······")
        break
