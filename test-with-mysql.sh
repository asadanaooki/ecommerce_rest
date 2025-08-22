#!/bin/bash

export DB_PASSWORD=${DB_PASSWORD:-password}

echo "Starting MySQL test container..."
docker-compose -f docker-compose.test.yml up -d

echo "Waiting for MySQL to be ready..."
sleep 10

echo "Running tests..."
./mvnw test

echo "Stopping MySQL test container..."
docker-compose -f docker-compose.test.yml down
