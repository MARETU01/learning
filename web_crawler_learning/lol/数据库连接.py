import pymysql

# 数据库连接参数
host = '127.0.0.1'  # 数据库服务器地址，此处为本地主机
port = 3306          # 数据库端口，MySQL默认端口为3306
user = 'ld'        # 数据库用户名
password = '123456'    # 数据库密码
db = 'dreamhome'     # 要连接的数据库名称
charset = 'utf8'     # 字符编码

# 建立连接
connection = pymysql.connect(
    host=host,
    port=port,
    user=user,
    password=password,
    db=db,
    charset=charset
)

# 创建游标对象
cursor = connection.cursor()

# 执行SQL查询（例如：查询weather表中的数据）
query = 'SELECT * FROM branch'
cursor.execute(query)

# 获取查询结果
results = cursor.fetchall()
for row in results:
    print(row)

# 提交事务（如果数据库支持事务）
connection.commit()

# 关闭游标和连接
cursor.close()
connection.close()
