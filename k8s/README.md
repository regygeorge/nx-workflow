# Kubernetes Deployment for MiniFlow

This directory contains Kubernetes configuration files for deploying the MiniFlow application.

## Prerequisites

- Kubernetes cluster (or minikube for local testing)
- kubectl configured to connect to your cluster
- Docker

## Directory Structure

- `app/`: Contains configuration for the MiniFlow application
- `db/`: Contains configuration for PostgreSQL database
- `kafka/`: Contains configuration for Kafka and Schema Registry

## Deployment

You can deploy the entire application stack using the provided script:

```bash
./deploy.sh
```

This will deploy:
1. PostgreSQL database
2. Kafka and Zookeeper
3. Schema Registry
4. MiniFlow application

## Local Testing

For local testing with minikube, use:

```bash
./local-test.sh
```

This script will:
1. Start minikube if it's not running
2. Build the Docker image
3. Deploy all components to minikube
4. Set up port forwarding for accessing the application

## Configuration

The application is configured using:

- ConfigMap (`app/configmap.yaml`): Contains application.yml with all configuration
- Secret (`app/secret.yaml`): Contains database credentials

## Components

### MiniFlow Application

- Deployment: 1 replica
- Service: ClusterIP on port 8084
- Uses ConfigMap for configuration
- Uses Secret for database credentials

### PostgreSQL Database

- Deployment: 1 replica
- Service: ClusterIP on port 5432
- Persistent Volume Claim for data storage

### Kafka and Schema Registry

- Zookeeper: 1 replica with persistent storage
- Kafka: 1 replica with persistent storage
- Schema Registry: 1 replica
- All exposed as ClusterIP services

## Troubleshooting

If you encounter issues:

1. Check pod status: `kubectl get pods -n miniflow`
2. Check logs: `kubectl logs <pod-name> -n miniflow`
3. Check services: `kubectl get services -n miniflow`
4. Check persistent volumes: `kubectl get pvc -n miniflow`