package org.example.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestTemplate;

import javax.sql.DataSource;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class AppConfig {
    private static final Logger logger = LoggerFactory.getLogger(AppConfig.class);

    @Value("${db.schemas.customer.connection.url:jdbc:postgresql://localhost:5432/customerDB}")
    private String customerDbUrl;

    @Value("${db.schemas.customer.connection.username:postgres}")
    private String customerDbUsername;

    @Value("${db.schemas.customer.connection.password:password}")
    private String customerDbPassword;

    @Value("${db.schemas.customer.connection.driver-class-name:org.postgresql.Driver}")
    private String customerDbDriverClassName;

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplateBuilder()
                .setConnectTimeout(Duration.ofMillis(5000))
                .setReadTimeout(Duration.ofMillis(30000))
                .build();
    }
    
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return objectMapper;
    }
    
    // We need to create a primary DataSource bean for Spring Boot autoconfiguration
    @Primary
    @Bean(name = "dataSource")
    public DataSource primaryDataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(customerDbUrl);
        config.setUsername(customerDbUsername);
        config.setPassword(customerDbPassword);
        config.setDriverClassName(customerDbDriverClassName);
        config.setPoolName("PrimaryPool");
        return new HikariDataSource(config);
    }
    
    @Bean
    public Map<String, DataSource> dataSources() {
        Map<String, DataSource> dataSources = new HashMap<>();
        
        // Configure CustomerDB DataSource
        HikariConfig customerDbConfig = new HikariConfig();
        customerDbConfig.setJdbcUrl(customerDbUrl);
        customerDbConfig.setUsername(customerDbUsername);
        customerDbConfig.setPassword(customerDbPassword);
        customerDbConfig.setDriverClassName(customerDbDriverClassName);
        customerDbConfig.setPoolName("CustomerDBPool");
        
        // Add with explicit keys that match our schema
        DataSource customerDs = new HikariDataSource(customerDbConfig);
        dataSources.put("customerdb", customerDs);
        dataSources.put("CustomerDB", customerDs);
        
        // Also add with the raw name from the schema file
        String schemaSourceName = "customerdb"; // This should match exactly what's in schema file
        dataSources.put(schemaSourceName, customerDs);
        
        // Log all datasource keys to help with debugging
        logger.info("Registered datasources with keys: {}", dataSources.keySet());
        
        return dataSources;
    }
}
