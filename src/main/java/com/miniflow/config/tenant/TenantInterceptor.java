package com.miniflow.config.tenant;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Interceptor that extracts the tenant ID from the request header
 * and sets it in the TenantContext for the duration of the request.
 */
@Component
public class TenantInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(TenantInterceptor.class);
    private static final String TENANT_HEADER = "X-TenantID";
    private static final String DEFAULT_TENANT = "default";

    @Autowired
    private TenantService tenantService;

    /**
     * Extracts the tenant ID from the request header and sets it in the TenantContext.
     * If no tenant ID is provided or it's invalid, the default tenant is used.
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String tenantId = request.getHeader(TENANT_HEADER);
        
        // If no tenant ID provided, use default
        if (tenantId == null || tenantId.isEmpty()) {
            tenantId = DEFAULT_TENANT;
            logger.debug("No tenant ID found in request, using default tenant: {}", tenantId);
        } 
        // Validate that the tenant exists in the database
        else if (!tenantService.getTenantById(tenantId).isPresent()) {
            logger.debug("Tenant ID {} not found in database, using default tenant", tenantId);
            tenantId = DEFAULT_TENANT;
        } else {
            logger.debug("Using tenant ID from request header: {}", tenantId);
        }
        
        TenantContext.setCurrentTenant(tenantId);
        return true;
    }

    /**
     * Clears the tenant ID from the TenantContext after the request is completed.
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        TenantContext.clear();
    }
}

// Made with Bob
