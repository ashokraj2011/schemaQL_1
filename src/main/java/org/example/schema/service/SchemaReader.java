package org.example.schema.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.schema.model.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Reads and caches schema JSON files from the filesystem or classpath.
 */
@Service
public class SchemaReader {
    private static final Logger logger = LoggerFactory.getLogger(SchemaReader.class);
    private final ObjectMapper objectMapper;
    private final ResourceLoader resourceLoader;
    private final Map<String, Schema> schemaCache = new ConcurrentHashMap<>();

    @Value("${schema.directory}")
    private String schemaDirectory;

    @Autowired
    public SchemaReader(ObjectMapper objectMapper,
                        ResourceLoader resourceLoader) {
        this.objectMapper = objectMapper;
        this.resourceLoader = resourceLoader;
    }
    public List<Schema> getAllSchemas() {
        return new ArrayList<>(schemaCache.values().stream()
                .collect(Collectors.toMap(Schema::getSchemaName, s -> s, (a, b) -> a))
                .values());
    }
    @PostConstruct
    public void init() {
        Path dir = Paths.get(schemaDirectory);
        if (!Files.exists(dir)) {
            logger.warn("Schema directory does not exist: {}. Creating.", schemaDirectory);
            try {
                Files.createDirectories(dir);
            } catch (IOException e) {
                logger.error("Failed to create schema directory: {}", schemaDirectory, e);
            }
        }
    }
    public Set<String> getCachedSchemaNames() {
        Set<String> names = new HashSet<>();
        for (String key : schemaCache.keySet()) {
            // skip file paths, include only simple names
            if (!key.contains("/") && !key.contains("\\")) {
                names.add(key);
            }
        }
        return names;
    }
    /**
     * Reads a schema JSON from filesystem or classpath.
     * Always reloads fresh from disk, updating the cache.
     */
    public Schema readSchema(String schemaPath) {
        Path path = Paths.get(schemaPath);
        String name = path.getFileName().toString().replaceFirst("\\.json$", "");
        try {
            if (Files.exists(path)) {
                String content = Files.readString(path);
                Schema schema = objectMapper.readValue(content, Schema.class);
                schemaCache.put(schemaPath, schema);
                schemaCache.put(name, schema);
                logger.info("Loaded schema from file: {}", name);
                return schema;
            } else {
                Resource res = resourceLoader.getResource("classpath:schemas/" + path.getFileName());
                if (res.exists()) {
                    Schema schema = loadSchemaFromResource(res);
                    schemaCache.put(name, schema);
                    logger.info("Loaded schema from classpath: {}", name);
                    return schema;
                }
                throw new IOException("Schema not found at " + schemaPath);
            }
        } catch (IOException e) {
            logger.error("Error reading schema {}", schemaPath, e);
            throw new RuntimeException("Failed to load schema: " + name, e);
        }
    }

    /**
     * Reads a schema from a Spring Resource (classpath), caching it by name.
     */
    public Schema readSchemaFromResource(Resource resource) {
        String name = resource.getFilename().replaceFirst("\\.json$", "");
        try {
            Schema schema = loadSchemaFromResource(resource);
            schemaCache.put(name, schema);
            logger.info("Loaded schema from resource: {}", name);
            return schema;
        } catch (IOException e) {
            logger.error("Error reading schema resource {}", name, e);
            throw new RuntimeException("Failed to load schema from resource: " + name, e);
        }
    }

    private Schema loadSchemaFromResource(Resource resource) throws IOException {
        String content = new String(resource.getInputStream().readAllBytes());
        return objectMapper.readValue(content, Schema.class);
    }

    /**
     * Returns true if a schema is cached under the given name or path.
     */
    public boolean isCached(String name) {
        return schemaCache.containsKey(name);
    }

    /**
     * Retrieves a cached schema by name.
     */
    public Schema getFromCache(String name) {
        return schemaCache.get(name);
    }

    /**
     * Determines if the schema is an API schema.
     */
    public boolean isApiSchema(Schema schema) {
        return schema.getSource() != null
                && "api".equalsIgnoreCase(schema.getSource().getDataSourceType());
    }

    /**
     * Determines if the schema is a DB schema (not API).
     */
    public boolean isDbSchema(Schema schema) {
        return schema.getSource() != null
                && !"api".equalsIgnoreCase(schema.getSource().getDataSourceType());
    }

    /**
     * Clears the schema cache.
     */
    public void clearCache() {
        schemaCache.clear();
        logger.info("Cleared schema cache");
    }
}