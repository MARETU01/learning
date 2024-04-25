import requests

url = 'https://upos-sz-estgoss.bilivideo.com/upgcxcode/90/91/1417689190/1417689190-1-100022.m4s?e=ig8euxZM2rNcNbdlhoNvNC8BqJIzNbfqXBvEqxTEto8BTrNvN0GvT90W5JZMkX_YN0MvXg8gNEV4NC8xNEV4N03eN0B5tZlqNxTEto8BTrNvNeZVuJ10Kj_g2UB02J0mN0B5tZlqNCNEto8BTrNvNC7MTX502C8f2jmMQJ6mqF2fka1mqx6gqj0eN0B599M=&uipk=5&nbs=1&deadline=1706782859&gen=playurlv2&os=upos&oi=2028294736&trid=48020bad3f3a4e07ae0297175e523463u&mid=0&platform=pc&upsig=bbd75fe3bb749ae96b8918b0140a1ff1&uparams=e,uipk,nbs,deadline,gen,os,oi,trid,mid,platform&bvc=vod&nettype=0&orderid=0,3&buvid=3D6C316C-B69C-A128-A973-3D7B1E3DBEB473804infoc&build=0&f=u_0_0&agrr=1&bw=7539&logo=80000000'
headers = {
    'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36',
    'Referer': 'https://www.bilibili.com/video/BV1UT4y1b7Th/?p=61&vd_source=8fd684e12fcd77ad574b7fe7f1de6d5c'
}

res = requests.get(url, headers=headers)

with open('b.mp4', 'wb') as f:
    f.write(res.content)
