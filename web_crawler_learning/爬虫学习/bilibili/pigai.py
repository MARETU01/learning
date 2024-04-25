import requests

url = 'https://www.pigai.org/index.php?c=write&f2=login'

headers = {
    'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0 Safari/537.36 Edg/121.0.0.0',
    'Cookie': 'old=2012; __root_domain_v=.pigai.org; _qddaz=QD.721084206156420; isPrize=0; PHPSESSID=lp6jmfd6c2gq1v5adennd3jr92; _JUKU_USER=%7B%22i%22%3A%2229868147%22%2C%22u%22%3A%22427A30BB6F47A3329284432D087ED6C2_1%22%2C%22u2%22%3A%22%5Cu7f57%5Cu9f0e%22%2C%22k%22%3A%22d78198e80e04ccd79a22f4c1dea46cfb%22%2C%22img%22%3A%2220220929%5C%2F29868147%22%2C%22ts%22%3A2%2C%22s%22%3A%22%5Cu534e%5Cu5357%5Cu5e08%5Cu8303%5Cu5927%5Cu5b66%22%2C%22iv%22%3A0%2C%22st%22%3A%220%22%2C%22no%22%3A%2220223802051%22%2C%22cl%22%3A%22%5Cu8f6f%5Cu4ef6%5Cu5de5%5Cu7a0b1%5Cu73ed%22%2C%22it%22%3A%222%22%7D; JK_GCNT=0; Hm_lvt_3f46f9c09663bf0ac2abdeeb95c7e516=1705662032,1706692623; Hm_lpvt_3f46f9c09663bf0ac2abdeeb95c7e516=1706692623'
}

res = requests.get(url, headers=headers)

print(res.text)