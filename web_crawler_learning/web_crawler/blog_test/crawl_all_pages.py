from utils import url_manager
import re
from bs4 import BeautifulSoup
import requests

root_url = "http://www.crazyant.net"

urls = url_manager.UrlManager()
urls.add_new_url(root_url)

fout = open("craw_all_pages.txt", "w", encoding="utf-8")
while urls.has_new_url():
    now_url = urls.get_url()
    r = requests.get(now_url, timeout=3)

    if r.status_code != 200:
        print("error")
        continue

    soup = BeautifulSoup(r.text, "html.parser")
    title = soup.title.string

    fout.write(f"{now_url} {title}\n")
    fout.flush()

    print(f"sucess: {now_url} {title}", len(urls.new_urls))

    links = soup.find_all("a")
    for link in links:
        href = link["href"]
        pattern = r"^http://www.crazyant.net/\d+.html$"
        if re.match(pattern, href):
            urls.add_new_url(href)

fout.close()
