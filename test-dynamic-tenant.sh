#!/bin/bash

# Test script for dynamic multi-tenant functionality
# This script creates, tests, and removes tenants dynamically

# Base URL for the API
BASE_URL="http://localhost:8084/api"

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${YELLOW}Testing Dynamic Multi-Tenant Functionality${NC}\n"

# 1. List all tenants
echo -e "${YELLOW}1. Listing all tenants:${NC}"
curl -s -X GET "${BASE_URL}/tenants" | jq
echo -e "\n"

# 2. Create a new tenant - the database will be created automatically
echo -e "${YELLOW}2. Creating a new tenant 'tenant1':${NC}"
curl -s -X POST "${BASE_URL}/tenants" \
  -H "Content-Type: application/json" \
  -d '{
    "id": "tenant1",
    "name": "Tenant One",
    "dbUsername": "postgres",
    "dbPassword": "postgres",
    "driverClassName": "org.postgresql.Driver"
  }' | jq
echo -e "\n"

# 3. Create another tenant - the database will be created automatically
echo -e "${YELLOW}3. Creating another tenant 'tenant2':${NC}"
curl -s -X POST "${BASE_URL}/tenants" \
  -H "Content-Type: application/json" \
  -d '{
    "id": "tenant2",
    "name": "Tenant Two",
    "dbUsername": "postgres",
    "dbPassword": "postgres",
    "driverClassName": "org.postgresql.Driver"
  }' | jq
echo -e "\n"

# 4. List all tenants again to verify creation
echo -e "${YELLOW}4. Listing all tenants again:${NC}"
curl -s -X GET "${BASE_URL}/tenants" | jq
echo -e "\n"

# 5. Test tenant1 connection
echo -e "${YELLOW}5. Testing tenant1 connection:${NC}"
curl -s -X GET "${BASE_URL}/tenant/info" \
  -H "X-TenantID: tenant1" | jq
echo -e "\n"

# 6. Test tenant2 connection
echo -e "${YELLOW}6. Testing tenant2 connection:${NC}"
curl -s -X GET "${BASE_URL}/tenant/info" \
  -H "X-TenantID: tenant2" | jq
echo -e "\n"

# 7. Test default tenant connection
echo -e "${YELLOW}7. Testing default tenant connection:${NC}"
curl -s -X GET "${BASE_URL}/tenant/info" | jq
echo -e "\n"

# 8. Update tenant1
echo -e "${YELLOW}8. Updating tenant1:${NC}"
curl -s -X PUT "${BASE_URL}/tenants/tenant1" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Tenant One Updated",
    "dbUsername": "postgres",
    "dbPassword": "postgres",
    "driverClassName": "org.postgresql.Driver"
  }' | jq
echo -e "\n"

# 9. Test tenant1 connection after update
echo -e "${YELLOW}9. Testing tenant1 connection after update:${NC}"
curl -s -X GET "${BASE_URL}/tenant/info" \
  -H "X-TenantID: tenant1" | jq
echo -e "\n"

# 10. Deactivate tenant2 (this doesn't drop the database)
echo -e "${YELLOW}10. Deactivating tenant2:${NC}"
curl -s -X DELETE "${BASE_URL}/tenants/tenant2" -w "%{http_code}\n"
echo -e "\n"

# 11. List all tenants to verify deactivation
echo -e "${YELLOW}11. Listing all tenants after deactivation:${NC}"
curl -s -X GET "${BASE_URL}/tenants" | jq
echo -e "\n"

# 12. Try to access deactivated tenant2
echo -e "${YELLOW}12. Trying to access deactivated tenant2 (should use default):${NC}"
curl -s -X GET "${BASE_URL}/tenant/info" \
  -H "X-TenantID: tenant2" | jq
echo -e "\n"

# 13. Completely delete tenant1 (this will drop the database)
echo -e "${YELLOW}13. Completely deleting tenant1:${NC}"
curl -s -X DELETE "${BASE_URL}/tenants/tenant1/delete" -w "%{http_code}\n"
echo -e "\n"

# 14. List all tenants to verify deletion
echo -e "${YELLOW}14. Listing all tenants after deletion:${NC}"
curl -s -X GET "${BASE_URL}/tenants" | jq
echo -e "\n"

echo -e "${GREEN}Dynamic Multi-Tenant Testing Complete!${NC}"

# Made with Bob
