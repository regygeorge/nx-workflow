# Dynamic Multi-Tenant Database Configuration

This document describes how to use the dynamic multi-tenant database functionality in the MiniFlow application.

## Overview

The application supports multiple tenants, each with its own database connection. Tenant configurations are stored in the master database and can be dynamically added, updated, or removed at runtime without restarting the application.

The tenant is determined by the `X-TenantID` header in the HTTP request. If no header is provided or the tenant ID is invalid, the default tenant is used.

## Key Features

- **Dynamic Database Creation**: When a new tenant is created, a new database is automatically created with the tenant ID as the database name
- **Dynamic Data Source Management**: Tenant data sources are added and removed at runtime without restarting the application
- **Tenant-Specific Database Isolation**: Each tenant's data is stored in a separate database for complete isolation
- **Automatic Schema Migration**: Database migrations are automatically applied to new tenant databases

## Database Setup

Before running the application, you only need to create the master database:

```sql
CREATE DATABASE miniflow;
```

The application will automatically:
1. Create the tenant table in the master database using Flyway migrations
2. Create new databases for each tenant when they are added
3. Apply the necessary schema migrations to each tenant database

## Tenant Management API

The application provides a REST API for managing tenants:

### List all tenants

```bash
curl -X GET http://localhost:8084/api/tenants
```

### Get a tenant by ID

```bash
curl -X GET http://localhost:8084/api/tenants/{tenantId}
```

### Create a new tenant (and its database)

```bash
curl -X POST http://localhost:8084/api/tenants \
  -H "Content-Type: application/json" \
  -d '{
    "id": "tenant1",
    "name": "Tenant One",
    "dbUsername": "postgres",
    "dbPassword": "postgres",
    "driverClassName": "org.postgresql.Driver"
  }'
```

Note: The database URL is automatically generated using the tenant ID as the database name.

### Update a tenant

```bash
curl -X PUT http://localhost:8084/api/tenants/{tenantId} \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Updated Tenant Name",
    "dbUsername": "postgres",
    "dbPassword": "postgres",
    "driverClassName": "org.postgresql.Driver"
  }'
```

### Deactivate a tenant (keeps the database)

```bash
curl -X DELETE http://localhost:8084/api/tenants/{tenantId}
```

### Delete a tenant completely (drops the database)

```bash
curl -X DELETE http://localhost:8084/api/tenants/{tenantId}/delete
```

## Using Tenant-Specific Databases

To specify a tenant in your API requests, include the `X-TenantID` header:

```bash
curl -H "X-TenantID: tenant1" http://localhost:8084/api/your-endpoint
```

## Running the Application

Use the provided script to run the application:

```bash
./run-app.sh
```

This script will:
1. Build the application with Maven
2. Create the master database if it doesn't exist
3. Start the Spring Boot application

## Testing

You can test the dynamic multi-tenant functionality using the provided script:

```bash
./test-dynamic-tenant.sh
```

This script:
1. Lists all tenants
2. Creates two new tenants (and their databases)
3. Tests connections to both tenants
4. Updates a tenant
5. Deactivates a tenant
6. Completely deletes a tenant (including its database)

## Implementation Details

The dynamic multi-tenant functionality is implemented using the following components:

1. **Tenant Entity**: Stores tenant database connection details in the master database
2. **TenantRepo**: JPA repository for accessing tenant information
3. **TenantService**: Service for managing tenant configurations
4. **TenantContext**: Stores the current tenant ID in a ThreadLocal variable
5. **DynamicTenantDatabaseConfig**: Configures the master data source and multi-tenant data source
6. **MultiTenantDataSource**: Routes database connections based on the current tenant
7. **DynamicDataSourceService**: Adds and removes tenant data sources at runtime
8. **DatabaseCreationService**: Creates and initializes tenant databases
9. **TenantInterceptor**: Extracts the tenant ID from the request header
10. **WebConfig**: Configures the tenant interceptor

## Best Practices

1. Use secure passwords for tenant database connections
2. Implement proper authentication and authorization for tenant management APIs
3. Consider encrypting sensitive tenant information in the database
4. Monitor tenant database connections for performance issues
5. Implement connection pooling for each tenant database
6. Consider implementing a cache for frequently accessed tenant configurations
7. Use valid database names for tenant IDs (alphanumeric and underscores only)