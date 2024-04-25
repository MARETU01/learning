from bs4 import BeautifulSoup

tag = BeautifulSoup(open("test.html"), "html.parser")
a = tag.find_all('a')
print(a)