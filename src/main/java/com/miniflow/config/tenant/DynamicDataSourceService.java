package com.miniflow.config.tenant;

import com.miniflow.persist.entity.Tenant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * Service for dynamically adding and removing tenant data sources at runtime.
 */
@Service
public class DynamicDataSourceService {

    private static final Logger logger = LoggerFactory.getLogger(DynamicDataSourceService.class);

    private final DataSource dataSource;

    @Autowired
    public DynamicDataSourceService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Adds a new tenant data source at runtime.
     *
     * @param tenant The tenant configuration
     * @return true if the data source was added successfully, false otherwise
     */
    public boolean addTenantDataSource(Tenant tenant) {
        try {
            if (!(dataSource instanceof AbstractRoutingDataSource)) {
                logger.error("DataSource is not an instance of AbstractRoutingDataSource");
                return false;
            }

            // Create a new data source for the tenant
            DataSource tenantDataSource = createTenantDataSource(tenant);

            // Use reflection to access the targetDataSources map in AbstractRoutingDataSource
            Method getTargetDataSources = AbstractRoutingDataSource.class.getDeclaredMethod("getTargetDataSources");
            getTargetDataSources.setAccessible(true);
            Map<Object, Object> targetDataSources = (Map<Object, Object>) getTargetDataSources.invoke(dataSource);

            // Add the new data source to the map
            targetDataSources.put(tenant.getId(), tenantDataSource);

            // Call afterPropertiesSet to refresh the resolved data sources
            Method afterPropertiesSet = AbstractRoutingDataSource.class.getDeclaredMethod("afterPropertiesSet");
            afterPropertiesSet.setAccessible(true);
            afterPropertiesSet.invoke(dataSource);

            logger.info("Added data source for tenant: {}", tenant.getId());
            return true;
        } catch (Exception e) {
            logger.error("Error adding tenant data source", e);
            return false;
        }
    }

    /**
     * Removes a tenant data source at runtime.
     *
     * @param tenantId The tenant ID
     * @return true if the data source was removed successfully, false otherwise
     */
    public boolean removeTenantDataSource(String tenantId) {
        try {
            if (!(dataSource instanceof AbstractRoutingDataSource)) {
                logger.error("DataSource is not an instance of AbstractRoutingDataSource");
                return false;
            }

            // Use reflection to access the targetDataSources map in AbstractRoutingDataSource
            Method getTargetDataSources = AbstractRoutingDataSource.class.getDeclaredMethod("getTargetDataSources");
            getTargetDataSources.setAccessible(true);
            Map<Object, Object> targetDataSources = (Map<Object, Object>) getTargetDataSources.invoke(dataSource);

            // Remove the data source from the map
            targetDataSources.remove(tenantId);

            // Call afterPropertiesSet to refresh the resolved data sources
            Method afterPropertiesSet = AbstractRoutingDataSource.class.getDeclaredMethod("afterPropertiesSet");
            afterPropertiesSet.setAccessible(true);
            afterPropertiesSet.invoke(dataSource);

            logger.info("Removed data source for tenant: {}", tenantId);
            return true;
        } catch (Exception e) {
            logger.error("Error removing tenant data source", e);
            return false;
        }
    }

    /**
     * Creates a data source for a specific tenant.
     *
     * @param tenant The tenant configuration
     * @return The tenant-specific DataSource
     */
    private DataSource createTenantDataSource(Tenant tenant) {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setUrl(tenant.getDbUrl());
        dataSource.setUsername(tenant.getDbUsername());
        dataSource.setPassword(tenant.getDbPassword());
        dataSource.setDriverClassName(tenant.getDriverClassName());
        return dataSource;
    }
}

// Made with Bob
