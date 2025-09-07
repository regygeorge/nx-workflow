package com.miniflow.config.tenant;

/**
 * Holds the current tenant identifier in a ThreadLocal variable
 * to make it accessible throughout the request processing.
 */
public class TenantContext {
    private static final ThreadLocal<String> CURRENT_TENANT = new ThreadLocal<>();

    /**
     * Set the current tenant ID
     * @param tenantId The tenant identifier
     */
    public static void setCurrentTenant(String tenantId) {
        CURRENT_TENANT.set(tenantId);
    }

    /**
     * Get the current tenant ID
     * @return The current tenant identifier
     */
    public static String getCurrentTenant() {
        return CURRENT_TENANT.get();
    }

    /**
     * Clear the current tenant ID
     */
    public static void clear() {
        CURRENT_TENANT.remove();
    }
}

// Made with Bob
