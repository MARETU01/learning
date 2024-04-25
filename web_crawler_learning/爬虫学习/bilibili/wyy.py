import requests

url = "https://1880810886.qnqcdn.net:22443/qn-2JMsUWqkUKCFWIBAkVCUkI1EnGmQUMT4.vodkgeyttp8.vod.126.net/cloudmusic/a7a3/mv/d3f1/6f635701d145e3254988f8dee3692dce.mp4?wsSecret=dc29746dc6552d9c2906f4210726dc40&wsTime=1706685781"
header = {
    "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0 Safari/537.36 Edg/121.0.0.0"
}

response = requests.get(url, headers=header)

with open("wyy.mp4", "wb") as music:
    music.write(response.content)
