import requests

headers = {'Usaer-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36 Edg/123.0.0.0',
           'Referer': 'https://www.manhuaren.com/m478549/'}

url = 'https://manhua1032zjcdn26.cdnmanhua.net/37/36055/618763/44_5184.jpg?cid=618763&key=4133c7e522892573d677c1ae41589282&type=1'

res = requests.get(url, headers=headers)

with open('第24话/44.jpeg', 'wb') as f:
    f.write(res.content)
