# Multi-Tenant Database Configuration

This document describes how to use the multi-tenant database functionality in the MiniFlow application.

## Overview

The application supports multiple tenants, each with its own database connection. The tenant is determined by the `X-TenantID` header in the HTTP request. If no header is provided or the tenant ID is invalid, the default tenant is used.

## Configuration

Tenant database connections are configured in the `application.yml` file:

```yaml
tenants:
  default-tenant: default
  configs:
    default:
      url: jdbc:postgresql://localhost:5432/miniflow
      username: postgres
      password: postgres
      driver-class-name: org.postgresql.Driver
    tenant1:
      url: jdbc:postgresql://localhost:5432/miniflow_tenant1
      username: postgres
      password: postgres
      driver-class-name: org.postgresql.Driver
    tenant2:
      url: jdbc:postgresql://localhost:5432/miniflow_tenant2
      username: postgres
      password: postgres
      driver-class-name: org.postgresql.Driver
```

## Database Setup

Before running the application, you need to create the databases for each tenant:

```sql
CREATE DATABASE miniflow;
CREATE DATABASE miniflow_tenant1;
CREATE DATABASE miniflow_tenant2;
```

Then run the Flyway migrations for each database to create the required tables.

## Testing

You can test the multi-tenant functionality using the provided script:

```bash
./test-multi-tenant.sh
```

This script sends requests to the API with different tenant headers and displays the results.

## API Usage

To specify a tenant in your API requests, include the `X-TenantID` header:

```bash
curl -H "X-TenantID: tenant1" http://localhost:8084/api/your-endpoint
```

## Implementation Details

The multi-tenant functionality is implemented using the following components:

1. **TenantContext**: Stores the current tenant ID in a ThreadLocal variable
2. **TenantProperties**: Loads tenant configurations from application.yml
3. **MultiTenantDataSource**: Routes database connections based on the current tenant
4. **TenantInterceptor**: Extracts the tenant ID from the request header
5. **WebConfig**: Configures the tenant interceptor

The application automatically switches to the appropriate database connection based on the tenant ID in the request header.