cd facc-back/facc-auth
mvn clean install -DskipTests
cd ../facc-control
mvn clean install -DskipTests
cd ../../
docker compose up -d --build