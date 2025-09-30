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
    chrome_driver_path = "C:\\Users\\DELL\\Desktop\\program\\chromedriver-win64\\chromedriver.exe"
    service = Service(chrome_driver_path)

    # 创建浏览器对象
    options = webdriver.ChromeOptions()
    options.add_argument("--incognito")  # 无痕模式，不使用持久化数据
    options.add_argument("--no-sandbox")
    options.add_argument("--disable-dev-shm-usage")
    # 可选：不显示浏览器窗口
    options.add_argument('--headless')
    driver = webdriver.Chrome(service=service, options=options)

    # ========== 爬取指定职位列表页面 ===========
    job_url = 'https://job.10086.cn/personal/campus/campus_job_list.html?cId=39'
    driver.get(job_url)
    time.sleep(3)  # 等待页面渲染

    jobs = driver.find_elements(By.XPATH, "/html/body/div[1]/div[2]/div/div/div[5]/ul[2]/li[*]")
    if len(jobs) != 0:
        # 拼接所有职位信息
        job_list = []
        for job in jobs:
            job_list.append(job.text)
            print(job.text)
            print("===============================")
        mail_content = "\n===============================\n".join(job_list)
        # 发送邮件通知，收件人自动从配置文件读取
        send_email(
            subject="广东移动校园招聘职位",
            content=mail_content
        )

    time.sleep(5)
    # ========== 结束 ===========
finally:
    # 确保浏览器正确关闭
    if 'driver' in locals():
        driver.quit()
