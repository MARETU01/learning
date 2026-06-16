import os
import time
import requests
from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.chrome.service import Service
from selenium.webdriver.chrome.options import Options

driver_path = r"C:\Users\DELL\Desktop\program\chromedriver-win64\chromedriver.exe"
service = Service(driver_path)
options = Options()
options.add_experimental_option("detach", True)
driver = webdriver.Chrome(service=service, options=options)

# driver.get('https://www.manhuaren.com/m478549/')
driver.get('https://www.manhuaren.com/m1527463/')
time.sleep(3)

driver.find_element(By.XPATH, "//*[@id='lb-win']/div/a[3]").click()
driver.find_element(By.XPATH, '/html/body/div[8]/img').click()
next = driver.find_element(By.CSS_SELECTOR, 'body > ul > li:nth-child(3) > a')

num, page = 96, 1
headers = {'Usaer-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36 Edg/123.0.0.0',
           'Referer': 'https://www.manhuaren.com/m478549/'}

while 1:
    if not os.path.exists(f'第{num}话'):
        os.mkdir(f'第{num}话')
    # if driver.find_element(By.XPATH, '/html/body/div[8]/img'):
    #     driver.find_element(By.XPATH, '/html/body/div[8]/img').click()
    if not driver.find_element(By.XPATH, '/html/body/div[5]').get_attribute('style'):
        num += 1
        page = 1
        driver.find_element(By.XPATH, '/html/body/div[5]/a[2]').click()
        next = driver.find_element(By.CSS_SELECTOR, 'body > ul > li:nth-child(3) > a')
        continue
    time.sleep(1)
    img = driver.find_element(By.XPATH, '//*[@id="cp_img"]/img')
    img_src = img.get_attribute('src')
    if img_src == "https://css122us.cdnmanhua.net/v202402291242/manhuaren/images/m/mshowmanga/page_default_img.png":
        continue
    print(img_src)
    res = requests.get(img_src, headers=headers)
    with open(f'第{num}话/{page}.jpeg', 'wb') as f:
        f.write(res.content)
    page += 1
    next.click()
