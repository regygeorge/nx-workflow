package com.miniflow.config.tenant;

import com.miniflow.persist.entity.Tenant;
import com.miniflow.persist.repo.TenantRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing tenant configurations.
 */
@Service
public class TenantService {
    
    private static final Logger logger = LoggerFactory.getLogger(TenantService.class);
    
    private final TenantRepo tenantRepo;
    private final DynamicDataSourceService dataSourceService;
    private final DatabaseCreationService databaseCreationService;
    
    /**
     * Constructor with dependencies injected.
     * Using @Lazy for dataSourceService to avoid circular dependency.
     */
    @Autowired
    public TenantService(
            TenantRepo tenantRepo,
            @Lazy DynamicDataSourceService dataSourceService,
            DatabaseCreationService databaseCreationService) {
        this.tenantRepo = tenantRepo;
        this.dataSourceService = dataSourceService;
        this.databaseCreationService = databaseCreationService;
    }
    
    /**
     * Get all active tenants.
     * 
     * @return List of active tenants
     */
    public List<Tenant> getAllActiveTenants() {
        return tenantRepo.findByActiveTrue();
    }
    
    /**
     * Get a tenant by ID.
     * 
     * @param tenantId The tenant ID
     * @return The tenant if found
     */
    public Optional<Tenant> getTenantById(String tenantId) {
        return tenantRepo.findById(tenantId);
    }
    
    /**
     * Create a new tenant.
     * This method will:
     * 1. Create a new database with the tenant ID as the name
     * 2. Save the tenant configuration in the master database
     * 3. Add the tenant data source dynamically
     * 
     * @param tenant The tenant to create
     * @return The created tenant
     */
    @Transactional
    public Tenant createTenant(Tenant tenant) {
        tenant.setCreatedAt(OffsetDateTime.now());
        tenant.setUpdatedAt(OffsetDateTime.now());
        tenant.setActive(true);
        
        if (tenant.getDriverClassName() == null) {
            tenant.setDriverClassName("org.postgresql.Driver");
        }
        
        // Set the database URL using the tenant ID as the database name
        String dbUrl = "jdbc:postgresql://localhost:5432/" + tenant.getId();
        tenant.setDbUrl(dbUrl);
        
        logger.info("Creating new tenant: {}", tenant.getId());
        
        // Create the database for the tenant
        boolean databaseCreated = databaseCreationService.createDatabase(tenant.getId());
        if (!databaseCreated) {
            logger.error("Failed to create database for tenant: {}", tenant.getId());
            throw new RuntimeException("Failed to create database for tenant: " + tenant.getId());
        }
        
        // Save the tenant configuration
        Tenant savedTenant = tenantRepo.save(tenant);
        
        // Add the tenant data source dynamically
        boolean dataSourceAdded = dataSourceService.addTenantDataSource(savedTenant);
        if (!dataSourceAdded) {
            logger.warn("Failed to add data source for tenant: {}", tenant.getId());
        }
        
        return savedTenant;
    }
    
    /**
     * Update an existing tenant.
     * 
     * @param tenant The tenant to update
     * @return The updated tenant
     */
    @Transactional
    public Tenant updateTenant(Tenant tenant) {
        tenant.setUpdatedAt(OffsetDateTime.now());
        logger.info("Updating tenant: {}", tenant.getId());
        Tenant savedTenant = tenantRepo.save(tenant);
        
        // Update the tenant data source dynamically
        boolean dataSourceUpdated = dataSourceService.removeTenantDataSource(tenant.getId());
        if (dataSourceUpdated) {
            dataSourceUpdated = dataSourceService.addTenantDataSource(savedTenant);
        }
        
        if (!dataSourceUpdated) {
            logger.warn("Failed to update data source for tenant: {}", tenant.getId());
        }
        
        return savedTenant;
    }
    
    /**
     * Deactivate a tenant.
     * This method will:
     * 1. Mark the tenant as inactive in the master database
     * 2. Remove the tenant data source dynamically
     * 
     * @param tenantId The tenant ID to deactivate
     */
    @Transactional
    public void deactivateTenant(String tenantId) {
        Optional<Tenant> optionalTenant = tenantRepo.findById(tenantId);
        if (optionalTenant.isPresent()) {
            Tenant tenant = optionalTenant.get();
            tenant.setActive(false);
            tenant.setUpdatedAt(OffsetDateTime.now());
            tenantRepo.save(tenant);
            logger.info("Deactivated tenant: {}", tenantId);
            
            // Remove the tenant data source dynamically
            boolean dataSourceRemoved = dataSourceService.removeTenantDataSource(tenantId);
            if (!dataSourceRemoved) {
                logger.warn("Failed to remove data source for tenant: {}", tenantId);
            }
        } else {
            logger.warn("Tenant not found for deactivation: {}", tenantId);
        }
    }
    
    /**
     * Delete a tenant completely.
     * This method will:
     * 1. Remove the tenant from the master database
     * 2. Remove the tenant data source dynamically
     * 3. Drop the tenant database
     * 
     * @param tenantId The tenant ID to delete
     */
    @Transactional
    public void deleteTenant(String tenantId) {
        Optional<Tenant> optionalTenant = tenantRepo.findById(tenantId);
        if (optionalTenant.isPresent()) {
            // Remove the tenant data source dynamically
            boolean dataSourceRemoved = dataSourceService.removeTenantDataSource(tenantId);
            if (!dataSourceRemoved) {
                logger.warn("Failed to remove data source for tenant: {}", tenantId);
            }
            
            // Delete the tenant from the master database
            tenantRepo.deleteById(tenantId);
            logger.info("Deleted tenant: {}", tenantId);
            
            // Drop the tenant database
            boolean databaseDropped = databaseCreationService.dropDatabase(tenantId);
            if (!databaseDropped) {
                logger.warn("Failed to drop database for tenant: {}", tenantId);
            }
        } else {
            logger.warn("Tenant not found for deletion: {}", tenantId);
        }
    }
}

// Made with Bob
