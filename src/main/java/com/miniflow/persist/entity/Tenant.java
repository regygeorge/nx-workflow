package com.miniflow.persist.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Entity representing a tenant and its database connection details.
 * This entity is stored in the master database.
 */
@Entity
@Table(name = "tenant")
public class Tenant {
    
    @Id
    private String id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false, name = "db_url")
    private String dbUrl;
    
    @Column(nullable = false, name = "db_username")
    private String dbUsername;
    
    @Column(nullable = false, name = "db_password")
    private String dbPassword;
    
    @Column(nullable = false, name = "driver_class_name")
    private String driverClassName = "org.postgresql.Driver";
    
    @Column(nullable = false)
    private boolean active = true;
    
    @Column(nullable = false, name = "created_at")
    private OffsetDateTime createdAt;
    
    @Column(nullable = false, name = "updated_at")
    private OffsetDateTime updatedAt;

    // Getters and setters
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDbUrl() {
        return dbUrl;
    }

    public void setDbUrl(String dbUrl) {
        this.dbUrl = dbUrl;
    }

    public String getDbUsername() {
        return dbUsername;
    }

    public void setDbUsername(String dbUsername) {
        this.dbUsername = dbUsername;
    }

    public String getDbPassword() {
        return dbPassword;
    }

    public void setDbPassword(String dbPassword) {
        this.dbPassword = dbPassword;
    }

    public String getDriverClassName() {
        return driverClassName;
    }

    public void setDriverClassName(String driverClassName) {
        this.driverClassName = driverClassName;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}

// Made with Bob
