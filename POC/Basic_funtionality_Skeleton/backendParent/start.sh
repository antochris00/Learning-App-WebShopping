echo "####################################################"
echo "####################################################"
echo  Compiling Modules
echo "####################################################"
echo "####################################################"

mvn clean install

echo "####################################################"
echo "####################################################"
echo  Starting Server
echo "####################################################"
echo "####################################################"

cd service
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=8081"