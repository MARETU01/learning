docker run -d \
  --name mysql \
  -p 3306:3306 \
  -e MYSQL_ROOT_PASSWORD=123456 \
  -e MYSQL_DATABASE=demo \
  -e TZ=Asia/Shanghai \
  -v $(pwd)/mysql:/docker-entrypoint-initdb.d \
  -v $(pwd)/mysql/my.cnf:/etc/mysql/conf.d/my.cnf \
  mysql:9.6.0
