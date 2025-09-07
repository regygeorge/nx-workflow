#!/bin/bash

# Create namespace
kubectl create namespace miniflow

# Apply secrets first
kubectl apply -f k8s/app/secret.yaml -n miniflow

# Apply ConfigMap
kubectl apply -f k8s/app/configmap.yaml -n miniflow

# Deploy database
kubectl apply -f k8s/db/postgres.yaml -n miniflow

# Deploy Kafka and Schema Registry
kubectl apply -f k8s/kafka/kafka.yaml -n miniflow

# Wait for database and Kafka to be ready
echo "Waiting for database and Kafka to be ready..."
kubectl wait --for=condition=available --timeout=300s deployment/miniflow-db -n miniflow
kubectl wait --for=condition=available --timeout=300s deployment/kafka -n miniflow
kubectl wait --for=condition=available --timeout=300s deployment/schema-registry -n miniflow

# Deploy the application
kubectl apply -f k8s/app/deployment.yaml -n miniflow
kubectl apply -f k8s/app/service.yaml -n miniflow

echo "Deployment completed. You can check the status with:"
echo "kubectl get pods -n miniflow"
echo "kubectl get services -n miniflow"

# Made with Bob
