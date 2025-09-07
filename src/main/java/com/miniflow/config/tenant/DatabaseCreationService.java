package com.miniflow.config.tenant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.regex.Pattern;

/**
 * Service for creating and initializing tenant databases.
 */
@Service
public class DatabaseCreationService {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseCreationService.class);
    private static final Pattern VALID_DB_NAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]+$");

    @Autowired
    @Qualifier("masterDataSource")
    private DataSource masterDataSource;
    
    @Value("${spring.datasource.username}")
    private String dbUsername;
    
    @Value("${spring.datasource.password}")
    private String dbPassword;

    /**
     * Creates a new database for a tenant.
     * The database name will be the same as the tenant ID.
     *
     * @param tenantId The tenant ID, which will be used as the database name
     * @return true if the database was created successfully, false otherwise
     */
    public boolean createDatabase(String tenantId) {
        // Validate the tenant ID to prevent SQL injection
        if (!isValidDatabaseName(tenantId)) {
            logger.error("Invalid tenant ID for database name: {}", tenantId);
            return false;
        }
        
        try (Connection connection = masterDataSource.getConnection()) {
            // Use a prepared statement would be ideal, but PostgreSQL doesn't support
            // parameterized CREATE DATABASE statements, so we validate the input instead
            try (Statement statement = connection.createStatement()) {
                String createDbSql = "CREATE DATABASE " + tenantId;
                statement.execute(createDbSql);
                logger.info("Created database for tenant: {}", tenantId);
            }
            
            // Apply migrations to the new database
            applyMigrations(tenantId);
            
            return true;
        } catch (SQLException e) {
            logger.error("Error creating database for tenant: {}", tenantId, e);
            return false;
        }
    }

    /**
     * Applies database migrations to a tenant database.
     * This method executes the same migration scripts that are used for the master database.
     *
     * @param tenantId The tenant ID (database name)
     */
    private void applyMigrations(String tenantId) {
        try {
            // Create a connection to the new database
            String url = "jdbc:postgresql://localhost:5432/" + tenantId;
            
            // Create a temporary data source for the new database
            org.springframework.jdbc.datasource.DriverManagerDataSource dataSource = 
                new org.springframework.jdbc.datasource.DriverManagerDataSource();
            dataSource.setUrl(url);
            dataSource.setUsername(dbUsername);
            dataSource.setPassword(dbPassword);
            dataSource.setDriverClassName("org.postgresql.Driver");
            
            // Apply the migration scripts
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
            
            // Apply V1__miniflow.sql
            ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
            populator.addScript(new ClassPathResource("db/migration/V1__miniflow.sql"));
            populator.execute(dataSource);
            
            logger.info("Applied migrations to database for tenant: {}", tenantId);
        } catch (Exception e) {
            logger.error("Error applying migrations to database for tenant: {}", tenantId, e);
        }
    }

    /**
     * Drops a tenant database.
     *
     * @param tenantId The tenant ID (database name)
     * @return true if the database was dropped successfully, false otherwise
     */
    public boolean dropDatabase(String tenantId) {
        // Validate the tenant ID to prevent SQL injection
        if (!isValidDatabaseName(tenantId)) {
            logger.error("Invalid tenant ID for database name: {}", tenantId);
            return false;
        }
        
        try (Connection connection = masterDataSource.getConnection()) {
            // Use a prepared statement would be ideal, but PostgreSQL doesn't support
            // parameterized DROP DATABASE statements, so we validate the input instead
            try (Statement statement = connection.createStatement()) {
                String dropDbSql = "DROP DATABASE IF EXISTS " + tenantId;
                statement.execute(dropDbSql);
                logger.info("Dropped database for tenant: {}", tenantId);
            }
            
            return true;
        } catch (SQLException e) {
            logger.error("Error dropping database for tenant: {}", tenantId, e);
            return false;
        }
    }
    
    /**
     * Validates that a database name is safe to use in SQL statements.
     * This helps prevent SQL injection attacks.
     *
     * @param dbName The database name to validate
     * @return true if the name is valid, false otherwise
     */
    private boolean isValidDatabaseName(String dbName) {
        return dbName != null && VALID_DB_NAME_PATTERN.matcher(dbName).matches();
    }
}

// Made with Bob
