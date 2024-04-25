Overview:
This project is a hotel management web application used to manage hotel rooms and user information. The project uses MySQL as the database and implements it using Python's Flask framework, flask-salalchemy extension, and pymysql library.

Install project dependencies:
pip install flask
pip install flask-salalchemy
pip install pymysql	(not must)

Create a MySQL database and configure:
create a databse named "hotel", then you can modify the parameters in the config.py.

The database configuration in config.py details are as follows:
HOSTNAME = "127.0.0.1"
PORT = "3306"
USERNAME = "root"
PASSWORD = "123456"
DATABASE = "hotel"

After configuration, you can run app.py in python, visit the web application on http://127.0.0.1:80 (localhost). Other people can visit this application by entering the second url showed python's run. Please ensure you are linked to the same WIFI with the running computer.

If you run the app.py, it will create a admin account, you can login with username "root" and password "123456". It will create 30 rooms if not exists, each type has 10 rooms.

Test:
In the test_app.py, we have made a simple test. This test includes some connections of the page, which is all about user interface. We create an account "Test user" and add it to database. Then we post the information to the web application, create a session and link to the user interface. In the test, it will use GET method and get the statu_code. If login in successfully, the status code should be 200.