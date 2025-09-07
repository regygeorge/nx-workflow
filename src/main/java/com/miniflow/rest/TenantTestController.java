package com.miniflow.rest;

import com.miniflow.config.tenant.TenantContext;
import com.miniflow.persist.entity.WfInstance;
import com.miniflow.persist.repo.WfInstanceRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller for testing multi-tenant functionality.
 * This controller provides endpoints to verify that tenant switching works correctly.
 */
@RestController
@RequestMapping("/api/tenant")
public class TenantTestController {

    @Autowired
    private WfInstanceRepo instanceRepo;

    /**
     * Returns information about the current tenant and its workflow instances.
     * This endpoint can be used to verify that tenant switching works correctly.
     * 
     * @return A map containing tenant information and workflow instances
     */
    @GetMapping("/info")
    public Map<String, Object> getTenantInfo() {
        Map<String, Object> result = new HashMap<>();
        
        // Get the current tenant ID from the context
        String currentTenant = TenantContext.getCurrentTenant();
        result.put("currentTenant", currentTenant);
        
        // Get workflow instances for the current tenant
        List<WfInstance> instances = instanceRepo.findAll();
        result.put("instanceCount", instances.size());
        result.put("instances", instances);
        
        return result;
    }
}

// Made with Bob
