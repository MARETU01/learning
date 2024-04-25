import requests
from bs4 import BeautifulSoup
import pymysql

pre_url = 'https://leagueoflegends.fandom.com'

headers = {'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36 Edg/122.0.0.0'}

res = requests.get(pre_url + "/wiki/Rune_(League_of_Legends)", headers=headers)

soup1 = BeautifulSoup(res.content, "html.parser")

connection = pymysql.connect(host='127.0.0.1', port=3306, user='ld', db='test_lol', password='123456', charset='utf8')

cursor = connection.cursor()

body = soup1.select('#mw-content-text > div.mw-parser-output > table.article-table.rune-table > tbody')[0]

for tr in body.find_all('span', style='word-break: keep-all;;'):
    response = requests.get(pre_url + tr.a['href'], headers=headers)
    soup2 = BeautifulSoup(response.content, 'html.parser')
    name = soup2.find('h2', {'data-source': 'name'}).text.replace(' Edit', '')
    path = soup2.find('td', {'data-source': 'path'}).text
    effect = soup2.find('div', {'data-source': 'description'}).text.strip()
    query = 'insert into runes(name, path, effect) values (%s, %s, %s)'
    cursor.execute(query, (name, path, effect))
    connection.commit()

cursor.close()
connection.close()