#!/bin/bash

# Check if minikube is installed
if ! command -v minikube &> /dev/null; then
    echo "minikube is not installed. Please install it first."
    exit 1
fi

# Start minikube if not running
if ! minikube status | grep -q "Running"; then
    echo "Starting minikube..."
    minikube start
fi

# Enable ingress addon
minikube addons enable ingress

# Set docker env to use minikube's docker daemon
eval $(minikube docker-env)

# Build the Docker image
echo "Building Docker image..."
docker build -t miniflow:latest .

# Deploy to minikube
echo "Deploying to minikube..."
./deploy.sh

# Wait for all pods to be ready
echo "Waiting for all pods to be ready..."
kubectl wait --for=condition=ready pod --all -n miniflow --timeout=300s

# Port forward the service for local access
echo "Setting up port forwarding..."
kubectl port-forward svc/miniflow -n miniflow 8084:8084 &
PORT_FORWARD_PID=$!

echo "Application is accessible at http://localhost:8084"
echo "Press Ctrl+C to stop port forwarding and clean up"

# Trap to clean up port forwarding
trap "kill $PORT_FORWARD_PID; echo 'Port forwarding stopped'" INT TERM

# Wait for user to press Ctrl+C
wait $PORT_FORWARD_PID

# Made with Bob
