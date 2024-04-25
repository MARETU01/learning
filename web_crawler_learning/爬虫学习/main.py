import urllib.request
import urllib.parse

url = 'https://www.pigai.org'
request_data = urllib.parse.urlencode({'username': '2636064351', 'password': 'luoding0729'}).encode('utf-8')

req = urllib.request.Request(url, data=request_data)
req.add_header('User-agent', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36 Edg/120.0.0.0')
response = urllib.request.urlopen(req)
html = response.read()
if '代航' in html.decode('utf-8'):
    print('login successfully')
with open('0.html', 'wb') as ht:
    ht.write(html)