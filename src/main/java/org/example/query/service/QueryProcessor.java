
        package org.example.query.service;

import org.example.query.datasource.DataSourcePlugin;
import org.example.query.model.JsonQuery;
import org.example.query.model.JsonQueryResponse;
import org.example.query.model.QueryRequest;
import org.example.query.model.QueryResponse;
import org.example.schema.model.DataSource;
import org.example.schema.model.Field;
import org.example.schema.model.Namespace;
import org.example.schema.model.Schema;
import org.example.schema.service.SchemaReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class QueryProcessor {
    private static final Logger logger = LoggerFactory.getLogger(QueryProcessor.class);

    private final SchemaReader schemaReader;
    private final CacheService cacheService;
    private final ResourceLoader resourceLoader;
    private final List<DataSourcePlugin> plugins;
    private final SqlGenerator sqlGenerator;

    @Value("${schema.directory}")
    private String schemaDirectory;

    @Autowired
    public QueryProcessor(
            SchemaReader schemaReader,
            CacheService cacheService,
            ResourceLoader resourceLoader,
            List<DataSourcePlugin> plugins,
            SqlGenerator sqlGenerator
    ) {
        this.schemaReader = schemaReader;
        this.cacheService = cacheService;
        this.resourceLoader = resourceLoader;
        this.plugins = plugins;
        this.sqlGenerator = sqlGenerator;
    }

    public JsonQueryResponse processQuery(JsonQuery query) {
        logger.info("Processing query with {} sub-queries", query.getQueries().size());
        List<CompletableFuture<QueryResponse>> futures = query.getQueries().stream()
                .map(req -> CompletableFuture.supplyAsync(() -> processSingle(req, query.isIncludeDataTypes())))
                .collect(Collectors.toList());

        List<QueryResponse> results = futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());

        JsonQueryResponse resp = new JsonQueryResponse();
        resp.setResults(results);
        resp.setIncludeDataTypes(query.isIncludeDataTypes());
        return resp;
    }

    private QueryResponse processSingle(QueryRequest req, boolean includeDataTypes) {
        // 1) Load schema & data-source
        Schema schema = loadSchema(req.getSchema());
        DataSource ds = schema.getSource();

        // 2) Determine namespace
        String namespaceName = ds.getDataSourceType().equalsIgnoreCase("view")
                ? schema.getSchemaName()
                : req.getNamespace();
        Namespace ns = requireNamespace(schema, namespaceName);

        // 3) Attempt cache
        if (ns.isCacheable()) {
            String cacheKey = buildCacheKey(ns.getCacheKeyPattern(), req.getArguments());
            @SuppressWarnings("unchecked")
            List<Map<String,Object>> cached = (List<Map<String,Object>>) cacheService.get(cacheKey);
            if (cached != null) {
                logger.debug("Cache hit for '{}'; returning cached rows", cacheKey);
                return buildResponse(namespaceName, ds.getDataSource(), schema.getSchemaName(), cached, includeDataTypes, schema);
            }
        }

        // 4) Execute via plugin or view logic
        List<Map<String,Object>> rows;
        if (ds.getDataSourceType().equalsIgnoreCase("view")) {
            rows = processViewQuery(schema, req);
        } else {
            DataSourcePlugin plugin = plugins.stream()
                    .filter(p -> p.canHandle(ds.getDataSourceType()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("No plugin for: " + ds.getDataSourceType()));
            rows = plugin.execute(ns, req, schema);
        }

        // 5) Populate cache
        if (ns.isCacheable()) {
            String cacheKey = buildCacheKey(ns.getCacheKeyPattern(), req.getArguments());
            cacheService.put(cacheKey, rows, ns.getCacheTTL());
            logger.debug("Cached {} rows under key='{}' TTL={}s", rows.size(), cacheKey, ns.getCacheTTL());
        }

        // 6) Build and return
        return buildResponse(namespaceName, ds.getDataSource(), schema.getSchemaName(), rows, includeDataTypes, schema);
    }

    private String buildCacheKey(String pattern, Map<String,Object> args) {
        String key = pattern;
        for (var e : args.entrySet()) {
            key = key.replace("{" + e.getKey() + "}", String.valueOf(e.getValue()));
        }
        return key;
    }

    private QueryResponse buildResponse(
            String namespace,
            String dataSource,
            String schemaName,
            List<Map<String,Object>> data,
            boolean includeDataTypes,
            Schema schema
    ) {
        QueryResponse resp = new QueryResponse();
        resp.setNamespace(namespace);
        resp.setDataSource(dataSource);
        resp.setSchema(schemaName);
        resp.setData(data);
        if (includeDataTypes) {
            Map<String,String> types = new LinkedHashMap<>();
            requireNamespace(schema, namespace)
                    .getFields()
                    .forEach(f -> types.put(f.getName(), f.getType()));
            resp.setDataTypes(types);
        }
        return resp;
    }

    private Namespace requireNamespace(Schema schema, String name) {
        return schema.getNamespaces().stream()
                .filter(n -> n.getNamespace().equals(name))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Namespace not found: " + name));
    }

    private Schema loadSchema(String name) {
        try {
            if (schemaReader.isCached(name)) return schemaReader.getFromCache(name);
            Path p = Paths.get(schemaDirectory, name + ".json");
            return schemaReader.readSchema(p.toString());
        } catch (Exception e) {
            Resource r = resourceLoader.getResource("classpath:schemas/" + name + ".json");
            return schemaReader.readSchemaFromResource(r);
        }
    }

    private List<Map<String,Object>> processViewQuery(Schema viewSchema, QueryRequest req) {
        DataSource ds = viewSchema.getSource();
        String key = ds.getGlobalKey();
        var baseDef = ds.getBase();
        var joins = ds.getJoins();

        // 1) Base table: expand computed fields
        Schema dbSchema = loadSchema(baseDef.getSchema());
        Namespace dbNs = requireNamespace(dbSchema, baseDef.getNamespace());
        Map<String,Object> baseParams = Map.of(key, req.getArguments().get(key));
        List<String> baseFields = new ArrayList<>();
        for (var fm : ds.getViewFields()) {
            if (fm.getFrom().startsWith(baseDef.getNamespace() + ".")) {
                String fname = fm.getFrom().substring(baseDef.getNamespace().length() + 1);
                Field fdef = dbNs.getFields().stream()
                        .filter(f -> f.getName().equals(fname))
                        .findFirst().orElseThrow();
                if (Boolean.TRUE.equals(fdef.isComputed()) && fdef.getTransformer()!=null) {
                    baseFields.addAll(fdef.getTransformer().getFields());
                } else {
                    baseFields.add(fname);
                }
            }
        }
        if (!baseFields.contains(key)) baseFields.add(0, key);
        String baseSql = sqlGenerator.generateSql(dbNs, baseFields, baseParams);
        List<Map<String,Object>> baseRows = sqlGenerator.executeSql(
                dbSchema.getSource().getDbName().toLowerCase(),
                baseSql, baseParams, baseFields, dbNs
        );

        // compute computed fields in-memory
        for (var row : baseRows) {
            for (var fm : ds.getViewFields()) {
                if (fm.getFrom().startsWith(baseDef.getNamespace() + ".")) {
                    String fname = fm.getFrom().substring(baseDef.getNamespace().length() + 1);
                    Field fdef = dbNs.getFields().stream()
                            .filter(f -> f.getName().equals(fname))
                            .findFirst().orElseThrow();
                    if (Boolean.TRUE.equals(fdef.isComputed()) && fdef.getTransformer()!=null) {
                        List<String> parts = fdef.getTransformer().getFields();
                        String sep = fdef.getTransformer().getSeparator();
                        StringBuilder sb = new StringBuilder();
                        for (int i=0; i<parts.size(); i++) {
                            if (i>0) sb.append(sep);
                            sb.append(row.get(parts.get(i)));
                        }
                        row.put(fname, sb.toString());
                    }
                }
            }
        }

        // 2) Build join maps
        List<Map<String,Map<String,Object>>> joinMaps = new ArrayList<>();
        for (var jd : joins) {
            Schema joinSchema = loadSchema(jd.getSchema());
            Namespace joinNs = requireNamespace(joinSchema, jd.getNamespace());
            QueryRequest jr = new QueryRequest();
            jr.setSchema(jd.getSchema());
            jr.setNamespace(jd.getNamespace());
            jr.setArguments(req.getArguments());
            jr.setFields(ds.getViewFields().stream()
                    .filter(fm -> fm.getFrom().startsWith(jd.getNamespace() + "."))
                    .map(fm -> fm.getFrom().substring(jd.getNamespace().length()+1))
                    .collect(Collectors.toList())
            );
            DataSourcePlugin plugin = plugins.stream()
                    .filter(p -> p.canHandle(joinSchema.getSource().getDataSourceType()))
                    .findFirst().orElseThrow();
            List<Map<String,Object>> joinRows = plugin.execute(joinNs, jr, joinSchema);
            Map<String,Map<String,Object>> map = joinRows.stream()
                    .collect(Collectors.toMap(
                            r -> String.valueOf(r.get(jd.getKey())),
                            r -> r,
                            (a,b) -> a
                    ));
            joinMaps.add(map);
        }

        // 3) Merge base + joins
        List<Map<String,Object>> out = new ArrayList<>();
        for (var base : baseRows) {
            String baseVal = String.valueOf(base.get(key));
            Map<String,Object> rec = new HashMap<>();
            for (var fm : ds.getViewFields()) {
                String[] parts = fm.getFrom().split("\\.");
                String nsName = parts[0], fld = parts[1];
                if (nsName.equals(baseDef.getNamespace())) {
                    rec.put(fm.getAs(), base.get(fld));
                } else {
                    for (int i=0; i<joins.size(); i++) {
                        if (nsName.equals(joins.get(i).getNamespace())) {
                            rec.put(fm.getAs(), joinMaps.get(i).getOrDefault(baseVal, Collections.emptyMap()).get(fld));
                        }
                    }
                }
            }
            out.add(rec);
        }
        return out;
    }
}