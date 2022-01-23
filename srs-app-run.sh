mysql_root_password="$1"
if [ -z "$mysql_root_password" ]
then
  echo "Please provide MySQL root password to be used"
else
  ./gradlew clean build -x test
  cd ./srs-app-package || exit
  DOCKER_BUILDKIT=0 MYSQL_ROOT_PASSWORD="$mysql_root_password" docker-compose up
fi