#!/bin/bash

# Script to run the Spring Boot application

echo "Building the application..."
mvn clean package -DskipTests

echo "Creating master database..."
# You may need to adjust these commands based on your PostgreSQL setup
psql -U postgres -c "CREATE DATABASE miniflow WITH ENCODING 'UTF8';" || echo "Database miniflow already exists or couldn't be created"

echo "Running the application..."
java -jar target/miniflow-0.1.0.jar

# Made with Bob
