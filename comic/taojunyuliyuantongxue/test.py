import time
import requests
from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.chrome.service import Service
from selenium.webdriver.chrome.options import Options
from bs4 import BeautifulSoup

driver_path = r"C:\Users\DELL\Desktop\program\chromedriver-win64\chromedriver.exe"
service = Service(driver_path)
options = Options()
options.add_experimental_option("detach", True)
driver = webdriver.Chrome(service=service, options=options)

# driver.get('https://www.manhuaren.com/m186690/')
driver.get('https://www.manhuaren.com/m1149823/')
time.sleep(1)

headers = {'Referer': 'https://www.manhuaren.com/m1149823/',
           'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.0.0 Safari/537.36'}

driver.find_element(By.XPATH, "//*[@id='lb-win']/div/a[3]").click()
html_content = driver.page_source
soup = BeautifulSoup(html_content, 'html.parser')
num = 1
for i in soup.find_all('img', class_='lazy'):
    print(i.get('src'))
    res = requests.get(i.get('src'), headers=headers)
    with open(f'最终话/{num}.jpeg', 'wb') as f:
        f.write(res.content)
    num += 1

driver.quit()