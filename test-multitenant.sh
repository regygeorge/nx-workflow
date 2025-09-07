#!/bin/bash

# Test script for multi-tenant functionality

# Base URL for the API
BASE_URL="http://localhost:8080"

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}=== Testing Multi-Tenant Functionality ===${NC}"

# Step 1: Create a new tenant
echo -e "\n${BLUE}Step 1: Creating a new tenant 'tenant1'${NC}"
TENANT_RESPONSE=$(curl -s -X POST "${BASE_URL}/api/tenants" \
  -H "Content-Type: application/json" \
  -d '{
    "tenantId": "tenant1",
    "name": "Test Tenant 1",
    "databaseName": "tenant1_db"
  }')

echo "Response: $TENANT_RESPONSE"

if [[ $TENANT_RESPONSE == *"tenant1"* ]]; then
  echo -e "${GREEN}✓ Tenant created successfully${NC}"
else
  echo -e "${RED}✗ Failed to create tenant${NC}"
  exit 1
fi

# Wait for tenant database to be created and initialized
echo "Waiting for tenant database to be initialized..."
sleep 5

# Step 2: Deploy a workflow for the tenant
echo -e "\n${BLUE}Step 2: Deploying a workflow for tenant 'tenant1'${NC}"
DEPLOY_RESPONSE=$(curl -s -X POST "${BASE_URL}/api/workflows/deploy" \
  -H "X-TenantID: tenant1" \
  -H "Content-Type: application/xml" \
  --data-binary @src/main/resources/bpmn/HIMS.bpmn)

echo "Response: $DEPLOY_RESPONSE"

if [[ $DEPLOY_RESPONSE == *"hims"* ]]; then
  echo -e "${GREEN}✓ Workflow deployed successfully${NC}"
else
  echo -e "${RED}✗ Failed to deploy workflow${NC}"
  exit 1
fi

# Step 3: Start a workflow instance for the tenant
echo -e "\n${BLUE}Step 3: Starting a workflow instance for tenant 'tenant1'${NC}"
START_RESPONSE=$(curl -s -X POST "${BASE_URL}/api/workflows/start" \
  -H "X-TenantID: tenant1" \
  -H "Content-Type: application/json" \
  -d '{
    "processDefinitionKey": "hims",
    "businessKey": "TEST-001",
    "variables": {
      "patientName": "John Doe",
      "patientId": "P12345"
    }
  }')

echo "Response: $START_RESPONSE"

if [[ $START_RESPONSE == *"instanceId"* ]]; then
  INSTANCE_ID=$(echo $START_RESPONSE | grep -o '"instanceId":"[^"]*' | cut -d'"' -f4)
  echo -e "${GREEN}✓ Workflow instance started successfully with ID: $INSTANCE_ID${NC}"
else
  echo -e "${RED}✗ Failed to start workflow instance${NC}"
  exit 1
fi

# Step 4: Get workflow instance details
echo -e "\n${BLUE}Step 4: Getting workflow instance details for tenant 'tenant1'${NC}"
INSTANCE_RESPONSE=$(curl -s -X GET "${BASE_URL}/api/workflows/instances/$INSTANCE_ID" \
  -H "X-TenantID: tenant1")

echo "Response: $INSTANCE_RESPONSE"

if [[ $INSTANCE_RESPONSE == *"$INSTANCE_ID"* ]]; then
  echo -e "${GREEN}✓ Successfully retrieved workflow instance details${NC}"
else
  echo -e "${RED}✗ Failed to retrieve workflow instance details${NC}"
  exit 1
fi

# Step 5: Create another tenant
echo -e "\n${BLUE}Step 5: Creating another tenant 'tenant2'${NC}"
TENANT2_RESPONSE=$(curl -s -X POST "${BASE_URL}/api/tenants" \
  -H "Content-Type: application/json" \
  -d '{
    "tenantId": "tenant2",
    "name": "Test Tenant 2",
    "databaseName": "tenant2_db"
  }')

echo "Response: $TENANT2_RESPONSE"

if [[ $TENANT2_RESPONSE == *"tenant2"* ]]; then
  echo -e "${GREEN}✓ Tenant 2 created successfully${NC}"
else
  echo -e "${RED}✗ Failed to create tenant 2${NC}"
  exit 1
fi

# Wait for tenant database to be created and initialized
echo "Waiting for tenant database to be initialized..."
sleep 5

# Step 6: Deploy a workflow for tenant2
echo -e "\n${BLUE}Step 6: Deploying a workflow for tenant 'tenant2'${NC}"
DEPLOY2_RESPONSE=$(curl -s -X POST "${BASE_URL}/api/workflows/deploy" \
  -H "X-TenantID: tenant2" \
  -H "Content-Type: application/xml" \
  --data-binary @src/main/resources/bpmn/op-flow.bpmn)

echo "Response: $DEPLOY2_RESPONSE"

if [[ $DEPLOY2_RESPONSE == *"Process_0dkm9yd"* ]]; then
  echo -e "${GREEN}✓ Workflow deployed successfully for tenant2${NC}"
else
  echo -e "${RED}✗ Failed to deploy workflow for tenant2${NC}"
  exit 1
fi

# Step 7: Start a workflow instance for tenant2
echo -e "\n${BLUE}Step 7: Starting a workflow instance for tenant 'tenant2'${NC}"
START2_RESPONSE=$(curl -s -X POST "${BASE_URL}/api/workflows/start" \
  -H "X-TenantID: tenant2" \
  -H "Content-Type: application/json" \
  -d '{
    "processDefinitionKey": "Process_0dkm9yd",
    "businessKey": "TEST-002",
    "variables": {
      "operationName": "Surgery",
      "operationId": "OP12345"
    }
  }')

echo "Response: $START2_RESPONSE"

if [[ $START2_RESPONSE == *"instanceId"* ]]; then
  INSTANCE2_ID=$(echo $START2_RESPONSE | grep -o '"instanceId":"[^"]*' | cut -d'"' -f4)
  echo -e "${GREEN}✓ Workflow instance started successfully for tenant2 with ID: $INSTANCE2_ID${NC}"
else
  echo -e "${RED}✗ Failed to start workflow instance for tenant2${NC}"
  exit 1
fi

# Step 8: List all tenants
echo -e "\n${BLUE}Step 8: Listing all tenants${NC}"
TENANTS_RESPONSE=$(curl -s -X GET "${BASE_URL}/api/tenants")

echo "Response: $TENANTS_RESPONSE"

if [[ $TENANTS_RESPONSE == *"tenant1"* && $TENANTS_RESPONSE == *"tenant2"* ]]; then
  echo -e "${GREEN}✓ Successfully retrieved all tenants${NC}"
else
  echo -e "${RED}✗ Failed to retrieve all tenants${NC}"
  exit 1
fi

echo -e "\n${GREEN}=== Multi-Tenant Test Completed Successfully ===${NC}"

# Made with Bob
