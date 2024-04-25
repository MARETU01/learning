import requests
from bs4 import BeautifulSoup
import pymysql

pre_url = 'https://leagueoflegends.fandom.com'

headers = {'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36 Edg/122.0.0.0'}

res = requests.get(pre_url + "/wiki/Champion", headers=headers)

soup1 = BeautifulSoup(res.content, "html.parser")

ul_elements = soup1.find_all('ul', class_='champion_roster')

# for ul in ul_elements:
#     for link in ul.find_all('a', href=True):
#         print(link['href'])

# for ul in ul_elements:
#     num = 1
#     for link in ul.find_all('a', href=True):
#         with open(f'111/{num}.html', 'wb') as f:
#             url = pre_url + link['href']
#             response = requests.get(url, headers=headers)
#             f.write(response.content)
#             num += 1

connection = pymysql.connect(host='127.0.0.1', port=3306, user='ld', db='test_lol', password='123456', charset='utf8')

cursor = connection.cursor()

for ul in ul_elements:
    for link in ul.find_all('a', href=True):
        url = pre_url + link['href']
        response = requests.get(url, headers=headers)
        soup2 = BeautifulSoup(response.content, 'html.parser')
        name = soup2.select('#infobox-champion-container > aside > h2.pi-item.pi-item-spacing.pi-title.pi-secondary-background > span')[0].text
        position = soup2.select('#infobox-champion-container > aside > div:nth-child(8) > div > span > a:nth-child(2)')[0].text
        price = soup2.select('#infobox-champion-container > aside > div:nth-child(12) > div > a:nth-child(4)')[0].text
        print(name, position, price)
        query = 'insert into champions(name, position, price) values (%s, %s, %s)'
        cursor.execute(query, (name, position, price))
        connection.commit()

cursor.close()
connection.close()
