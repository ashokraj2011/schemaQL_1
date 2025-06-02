package org.example.query.controller;

import org.example.query.datasource.DataSourcePlugin;
import org.example.schema.model.Field;
import org.example.schema.model.Namespace;
import org.example.schema.model.Schema;
import org.example.schema.service.SchemaReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

@RestController
public class IntrospectionController {

    private final SchemaReader schemaReader;
    private final ResourceLoader resourceLoader;
    private final List<DataSourcePlugin> plugins;

    @Value("${schema.directory}")
    private String schemaDirectory;

    @Autowired
    public IntrospectionController(
            SchemaReader schemaReader,
            ResourceLoader resourceLoader,
            List<DataSourcePlugin> plugins
    ) {
        this.schemaReader = schemaReader;
        this.resourceLoader = resourceLoader;
        this.plugins = plugins;
    }

    @GetMapping("/introspect")
    public Map<String, Object> introspect() throws IOException {
        // 1) Collect all schema names from fs + in-memory cache
        Set<String> names = new TreeSet<>();

        // From filesystem
        try (DirectoryStream<Path> ds =
                     Files.newDirectoryStream(Paths.get(schemaDirectory), "*.json")) {
            for (Path p : ds) {
                String fn = p.getFileName().toString()
                        .replaceFirst("\\.json$", "");
                names.add(fn);
            }
        } catch (NoSuchFileException e) {
            // directory might not exist yet
        }

        // From cache
        names.addAll(schemaReader.getCachedSchemaNames());

        // 2) Summarize each schema
        List<Map<String, Object>> schemas = names.stream()
                .map(this::loadAndSummarize)
                .collect(Collectors.toList());

        // 3) Summarize plugins
        List<Map<String, String>> pluginList = plugins.stream()
                .map(p -> {
                    Map<String, String> m = new HashMap<>();
                    m.put("type",        p.getType());
                    m.put("description", p.getDescription());
                    return m;
                })
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("schemas", schemas);
        result.put("plugins", pluginList);
        return result;
    }

    private Map<String, Object> loadAndSummarize(String name) {
        Schema s;
        try {
            if (schemaReader.isCached(name)) {
                s = schemaReader.getFromCache(name);
            } else {
                s = schemaReader.readSchema(
                        Paths.get(schemaDirectory, name + ".json").toString()
                );
            }
        } catch (Exception e) {
            // fallback to classpath
            Resource r = resourceLoader.getResource("classpath:schemas/" + name + ".json");
            s = schemaReader.readSchemaFromResource(r);
        }

        // Summarize namespaces
        List<Map<String, Object>> namespaces = s.getNamespaces().stream()
                .map(ns -> {
                    // Summarize fields
                    List<Map<String, Object>> fields = ns.getFields().stream()
                            .map(f -> {
                                Map<String, Object> fm = new HashMap<>();
                                fm.put("name",      f.getName());
                                fm.put("type",      f.getType());
                                fm.put("cacheable", ns.isCacheable());
                                return fm;
                            })
                            .collect(Collectors.toList());

                    // If you want to reverse the order:
                    Collections.reverse(fields);

                    Map<String, Object> nm = new HashMap<>();
                    nm.put("namespace",  ns.getNamespace());
                    nm.put("cacheable",  ns.isCacheable());
                    nm.put("primaryKey", ns.getPrimaryKey());
                    nm.put("fields",     fields);
                    return nm;
                })
                .collect(Collectors.toList());

        // Top-level schema summary
        Map<String, Object> sm = new HashMap<>();
        sm.put("schemaName",     s.getSchemaName());
        sm.put("dataSourceType", s.getSource().getDataSourceType());
        sm.put("namespaces",     namespaces);
        return sm;
    }
}
