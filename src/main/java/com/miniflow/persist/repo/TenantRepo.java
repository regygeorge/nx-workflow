package com.miniflow.persist.repo;

import com.miniflow.persist.entity.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for accessing tenant information from the master database.
 */
@Repository
public interface TenantRepo extends JpaRepository<Tenant, String> {
    
    /**
     * Find a tenant by its ID.
     * 
     * @param id The tenant ID
     * @return The tenant if found
     */
    Optional<Tenant> findById(String id);
    
    /**
     * Find all active tenants.
     * 
     * @return List of active tenants
     */
    List<Tenant> findByActiveTrue();
}

// Made with Bob
