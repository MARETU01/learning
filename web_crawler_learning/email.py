import smtplib
from email.mime.text import MIMEText
from email.mime.multipart import MIMEMultipart

# 配置邮箱信息
sender_email = "12306@qq.com"
sender_pwd = "zpnjzafphfdndjbg"
smtp_server = "smtp.qq.com"
smtp_port = 465

# 收件人、邮件内容
receiver_email = "12306@qq.com"
subject = "程序自动发送的测试邮件"
content = "这是程序通过QQSMTP发送的邮件，发送成功！"

# 组装邮件
msg = MIMEMultipart()
msg["From"] = sender_email
msg["To"] = receiver_email
msg["Subject"] = subject
msg.attach(MIMEText(content, "plain", "utf-8"))

# SSL连接发送
try:
    server = smtplib.SMTP_SSL(smtp_server, smtp_port)
    server.login(sender_email, sender_pwd)
    server.sendmail(sender_email, receiver_email, msg.as_string())
    print("邮件发送成功")
    server.quit()
except Exception as e:
    print("发送失败：", e)