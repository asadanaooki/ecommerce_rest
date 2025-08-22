#!/bin/bash

DB_PASSWORD=${DB_PASSWORD:-password}

echo "Starting MySQL test container..."
sudo docker run --name mysql-test -e MYSQL_ROOT_PASSWORD=${DB_PASSWORD} -e MYSQL_DATABASE=ecommerce_test -p 3307:3306 -d mysql:8.0 --default-authentication-plugin=mysql_native_password

echo "Waiting for MySQL to be ready..."
sleep 15

echo "Dropping and recreating database..."
sudo docker exec mysql-test mysql -u root -p${DB_PASSWORD} -e "DROP DATABASE IF EXISTS ecommerce_test; CREATE DATABASE ecommerce_test;"

echo "Running Flyway repair..."
./mvnw flyway:repair -Dflyway.url=jdbc:mysql://localhost:3307/ecommerce_test -Dflyway.user=root -Dflyway.password=${DB_PASSWORD} -Dflyway.locations=classpath:db/migration_test

echo "Running Flyway migrate..."
./mvnw flyway:migrate -Dflyway.url=jdbc:mysql://localhost:3307/ecommerce_test -Dflyway.user=root -Dflyway.password=${DB_PASSWORD} -Dflyway.locations=classpath:db/migration_test

echo "Running tests..."
./mvnw test

echo "Stopping MySQL test container..."
sudo docker stop mysql-test
sudo docker rm mysql-test
