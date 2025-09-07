package com.miniflow.config.tenant;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

/**
 * A multi-tenant data source that routes to the appropriate tenant database
 * based on the current tenant ID stored in TenantContext.
 */
public class MultiTenantDataSource extends AbstractRoutingDataSource {

    /**
     * Determines the current lookup key for selecting the appropriate DataSource.
     * This implementation returns the current tenant ID from TenantContext.
     * 
     * @return The current tenant ID as the lookup key
     */
    @Override
    protected Object determineCurrentLookupKey() {
        return TenantContext.getCurrentTenant();
    }
}

// Made with Bob
