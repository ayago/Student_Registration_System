-- MYSQL_ROOT_HOST doesn't update the host of root user. Instead it adds a new record with user='root' and hostname='%'
DELETE FROM mysql.user WHERE host='localhost' AND user='root';
FLUSH PRIVILEGES;