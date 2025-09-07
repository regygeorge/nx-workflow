package com.miniflow.config.tenant;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration properties for multi-tenant database connections.
 * This class maps to the 'tenants' section in application.yml.
 */
@Component
@ConfigurationProperties(prefix = "tenants")
public class TenantProperties {
    
    /**
     * Default tenant ID to use when no tenant is specified
     */
    private String defaultTenant = "default";
    
    /**
     * Map of tenant configurations keyed by tenant ID
     */
    private Map<String, TenantConfig> configs = new HashMap<>();

    public String getDefaultTenant() {
        return defaultTenant;
    }

    public void setDefaultTenant(String defaultTenant) {
        this.defaultTenant = defaultTenant;
    }

    public Map<String, TenantConfig> getConfigs() {
        return configs;
    }

    public void setConfigs(Map<String, TenantConfig> configs) {
        this.configs = configs;
    }

    /**
     * Configuration for a single tenant's database connection
     */
    public static class TenantConfig {
        private String url;
        private String username;
        private String password;
        private String driverClassName = "org.postgresql.Driver";

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getDriverClassName() {
            return driverClassName;
        }

        public void setDriverClassName(String driverClassName) {
            this.driverClassName = driverClassName;
        }
    }
}

// Made with Bob
