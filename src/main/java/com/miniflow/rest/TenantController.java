package com.miniflow.rest;

import com.miniflow.config.tenant.TenantService;
import com.miniflow.persist.entity.Tenant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing tenants.
 */
@RestController
@RequestMapping("/api/tenants")
public class TenantController {

    @Autowired
    private TenantService tenantService;

    /**
     * Get all active tenants.
     *
     * @return List of active tenants
     */
    @GetMapping
    public List<Tenant> getAllTenants() {
        return tenantService.getAllActiveTenants();
    }

    /**
     * Get a tenant by ID.
     *
     * @param id The tenant ID
     * @return The tenant if found
     */
    @GetMapping("/{id}")
    public ResponseEntity<Tenant> getTenantById(@PathVariable String id) {
        Optional<Tenant> tenant = tenantService.getTenantById(id);
        return tenant.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Create a new tenant.
     * This will also create a new database with the tenant ID as the name.
     *
     * @param tenant The tenant to create
     * @return The created tenant
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Tenant createTenant(@RequestBody Tenant tenant) {
        return tenantService.createTenant(tenant);
    }

    /**
     * Update an existing tenant.
     *
     * @param id The tenant ID
     * @param tenant The tenant data to update
     * @return The updated tenant
     */
    @PutMapping("/{id}")
    public ResponseEntity<Tenant> updateTenant(@PathVariable String id, @RequestBody Tenant tenant) {
        if (!tenantService.getTenantById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        tenant.setId(id);
        return ResponseEntity.ok(tenantService.updateTenant(tenant));
    }

    /**
     * Deactivate a tenant.
     * This will mark the tenant as inactive but keep the database.
     *
     * @param id The tenant ID to deactivate
     * @return No content response
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivateTenant(@PathVariable String id) {
        if (!tenantService.getTenantById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        tenantService.deactivateTenant(id);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Delete a tenant completely.
     * This will remove the tenant from the database and drop the tenant's database.
     *
     * @param id The tenant ID to delete
     * @return No content response
     */
    @DeleteMapping("/{id}/delete")
    public ResponseEntity<Void> deleteTenant(@PathVariable String id) {
        if (!tenantService.getTenantById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        tenantService.deleteTenant(id);
        return ResponseEntity.noContent().build();
    }
}

// Made with Bob
