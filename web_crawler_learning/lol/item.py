import requests
from bs4 import BeautifulSoup
import pymysql

pre_url = 'https://leagueoflegends.fandom.com'

headers = {'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36 Edg/122.0.0.0'}

res = requests.get(pre_url + "/wiki/Item_(League_of_Legends)", headers=headers)

soup1 = BeautifulSoup(res.content, "html.parser")

connection = pymysql.connect(host='127.0.0.1', port=3306, user='ld', db='test_lol', password='123456', charset='utf8')

cursor = connection.cursor()

divs = soup1.find_all('div', class_='tlist')
num = 1
for div in divs:
    for link in div.find_all('a', href=True):
        if num == 241:
            break
        response = requests.get(pre_url + link['href'], headers=headers)
        soup2 = BeautifulSoup(response.content, 'html.parser')
        name = soup2.select('h2.pi-item-spacing.pi-title.pi-secondary-background')[0].text.replace(" edit", "")
        cost_td = soup2.find('td', {'data-source': 'buy'})
        cost = cost_td.find('span').get_text().strip() if cost_td and cost_td.find('span') else 'Special'
        sell_td = soup2.find('td', {'data-source': 'sell'})
        sell = sell_td.find('span').get_text().strip() if cost_td and cost_td.find('span') else 'Special'
        effect = soup2.find('h2', string='Passive').parent.find('div', class_='pi-data-value pi-font').text if soup2.find('h2', string='Passive') else None
        if soup2.find('h2', string='Stats'):
            stats_total = soup2.find('h2', string='Stats').parent.find_all('div', class_='pi-data-value pi-font')
            stats_list = []
            for stat_div in stats_total:
                stat_text = stat_div.get_text().strip()
                stats_list.append(stat_text)
            stats = ', '.join(stats_list)
        else:
            stats = None
        print(name, cost, sell, '-------', num)
        num += 1
        query = 'insert into items(name, cost, sell, stats, effect) values (%s, %s, %s, %s, %s)'
        cursor.execute(query, (name, cost, sell, stats, effect))
        connection.commit()

cursor.close()
connection.close()