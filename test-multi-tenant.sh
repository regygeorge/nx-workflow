#!/bin/bash

# Test script for multi-tenant functionality
# This script sends requests to the API with different tenant headers

echo "Testing default tenant..."
curl -s -H "Content-Type: application/json" http://localhost:8084/api/tenant/info | jq

echo -e "\nTesting tenant1..."
curl -s -H "Content-Type: application/json" -H "X-TenantID: tenant1" http://localhost:8084/api/tenant/info | jq

echo -e "\nTesting tenant2..."
curl -s -H "Content-Type: application/json" -H "X-TenantID: tenant2" http://localhost:8084/api/tenant/info | jq

echo -e "\nTesting invalid tenant (should use default)..."
curl -s -H "Content-Type: application/json" -H "X-TenantID: invalid" http://localhost:8084/api/tenant/info | jq

# Made with Bob
