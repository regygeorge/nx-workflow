package com.miniflow.config.tenant;

import com.miniflow.persist.entity.Tenant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Configuration for dynamic multi-tenant database connections.
 * This class creates a master DataSource for tenant configuration
 * and a MultiTenantDataSource that dynamically routes to tenant-specific databases.
 */
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
    basePackages = "com.miniflow.persist.repo",
    entityManagerFactoryRef = "entityManagerFactory",
    transactionManagerRef = "transactionManager"
)
public class DynamicTenantDatabaseConfig {

    private static final Logger logger = LoggerFactory.getLogger(DynamicTenantDatabaseConfig.class);
    
    @Autowired
    private JpaProperties jpaProperties;

    /**
     * Creates the master data source for accessing tenant configurations.
     * This data source connects to the master database where tenant information is stored.
     *
     * @return The master DataSource properties
     */
    @Bean
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSourceProperties masterDataSourceProperties() {
        return new DataSourceProperties();
    }

    /**
     * Creates the master data source bean.
     *
     * @return The master DataSource
     */
    @Bean(name = "masterDataSource")
    public DataSource masterDataSource() {
        return masterDataSourceProperties().initializeDataSourceBuilder().build();
    }

    /**
     * Creates the multi-tenant data source that routes to the appropriate tenant database.
     * This data source is initialized with tenant configurations from the master database.
     *
     * @param masterDataSource The master data source
     * @return The configured MultiTenantDataSource
     */
    @Bean
    @Primary
    public DataSource dataSource(@Qualifier("masterDataSource") DataSource masterDataSource) {
        MultiTenantDataSource dataSource = new MultiTenantDataSource();
        
        // Set the master data source as the default
        dataSource.setDefaultTargetDataSource(masterDataSource);
        
        // Create a map of tenant data sources
        Map<Object, Object> resolvedDataSources = new HashMap<>();
        resolvedDataSources.put("master", masterDataSource);
        resolvedDataSources.put("default", masterDataSource);
        
        // Set the resolved data sources
        dataSource.setTargetDataSources(resolvedDataSources);
        dataSource.afterPropertiesSet();
        
        return dataSource;
    }
    
    /**
     * Creates the entity manager factory for JPA.
     *
     * @param dataSource The multi-tenant data source
     * @param builder The entity manager factory builder
     * @return The entity manager factory
     */
    @Bean
    @Primary
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
            DataSource dataSource, 
            EntityManagerFactoryBuilder builder) {
        
        Map<String, Object> properties = new HashMap<>(jpaProperties.getProperties());
        
        return builder
                .dataSource(dataSource)
                .packages("com.miniflow.persist.entity")
                .properties(properties)
                .build();
    }
    
    /**
     * Creates the transaction manager for JPA.
     *
     * @param entityManagerFactory The entity manager factory
     * @return The transaction manager
     */
    @Bean
    @Primary
    public PlatformTransactionManager transactionManager(
            LocalContainerEntityManagerFactoryBean entityManagerFactory) {
        return new JpaTransactionManager(Objects.requireNonNull(entityManagerFactory.getObject()));
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
