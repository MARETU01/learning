import json
import smtplib
import time
from email.header import Header
from email.mime.text import MIMEText

from selenium import webdriver
from selenium.webdriver.chrome.service import Service
from selenium.webdriver.common.by import By


def send_email(subject, content, to_addr=None):
    # 从配置文件读取邮箱配置
    with open('email_config.json', 'r', encoding='utf-8') as f:
        config = json.load(f)
    smtp_server = config['smtp_server']
    port = config.get('port', 465)  # 支持端口配置，默认465
    from_addr = config['from_addr']
    password = config['password']
    if to_addr is None:
        to_addr = config['to_addr']

    msg = MIMEText(content, 'plain', 'utf-8')
    msg['From'] = from_addr
    msg['To'] = to_addr
    msg['Subject'] = Header(subject, 'utf-8')

    try:
        server = smtplib.SMTP_SSL(smtp_server, port)
        server.login(from_addr, password)
        server.sendmail(from_addr, [to_addr], msg.as_string())
        server.quit()
        print('邮件发送成功')
    except Exception as e:
        print('邮件发送失败:', e)

try:
    # 设置Chrome驱动路径（请根据实际情况修改路径）
    chrome_driver_path = 'C:\\Users\\DELL\\Desktop\\program\\chromedriver-win64\\chromedriver.exe'
    service = Service(chrome_driver_path)

    # 创建浏览器对象
    options = webdriver.ChromeOptions()
    options.add_argument("--incognito")  # 无痕模式，不使用持久化数据
    options.add_argument("--no-sandbox")
    options.add_argument("--disable-dev-shm-usage")
    # 可选：不显示浏览器窗口
    options.add_argument('--headless')
    driver = webdriver.Chrome(service=service, options=options)

    # ========== 爬取中国移动首页 ===========
    mobile_url = 'https://www.10086.cn'
    driver.get(mobile_url)
    time.sleep(5)  # 等待页面渲染

    # 爬取首页主要信息
    page_content = []
    
    # 获取页面标题
    title = driver.title
    page_content.append(f"页面标题: {title}")
    print(f"页面标题: {title}")
    print("===============================")
    
    # 尝试获取导航菜单项
    try:
        nav_items = driver.find_elements(By.XPATH, "//nav//a | //div[contains(@class, 'nav')]//a | //ul[contains(@class, 'menu')]//li//a")
        if nav_items:
            page_content.append("导航菜单:")
            for item in nav_items[:10]:  # 限制数量避免过多
                if item.text.strip():
                    page_content.append(f"  - {item.text.strip()}")
                    print(f"  - {item.text.strip()}")
            print("===============================")
    except Exception as e:
        print(f"获取导航菜单失败: {e}")
    
    # 尝试获取主要新闻或公告
    try:
        news_items = driver.find_elements(By.XPATH, "//div[contains(@class, 'news')]//a | //div[contains(@class, 'notice')]//a | //div[contains(@class, 'announcement')]//a")
        if news_items:
            page_content.append("主要新闻/公告:")
            for item in news_items[:5]:  # 限制数量
                if item.text.strip():
                    page_content.append(f"  - {item.text.strip()}")
                    print(f"  - {item.text.strip()}")
            print("===============================")
    except Exception as e:
        print(f"获取新闻/公告失败: {e}")
    
    # 尝试获取服务或产品信息
    try:
        service_items = driver.find_elements(By.XPATH, "//div[contains(@class, 'service')]//a | //div[contains(@class, 'product')]//a | //div[contains(@class, 'business')]//a")
        if service_items:
            page_content.append("服务/产品:")
            for item in service_items[:5]:  # 限制数量
                if item.text.strip():
                    page_content.append(f"  - {item.text.strip()}")
                    print(f"  - {item.text.strip()}")
            print("===============================")
    except Exception as e:
        print(f"获取服务/产品信息失败: {e}")
    
    # 获取页面URL
    current_url = driver.current_url
    page_content.append(f"当前URL: {current_url}")
    print(f"当前URL: {current_url}")
    
    # 拼接所有内容
    mail_content = "\n".join(page_content)
    
    # 发送邮件通知，收件人自动从配置文件读取
    send_email(
        subject="中国移动首页信息",
        content=mail_content
    )

    time.sleep(3)
    # ========== 结束 ===========
finally:
    # 确保浏览器正确关闭
    if 'driver' in locals():
