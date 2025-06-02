package org.example.query.datasource;

import org.example.query.model.QueryRequest;
import org.example.schema.model.Namespace;
import org.example.schema.model.Schema;

import java.util.List;
import java.util.Map;
public interface DataSourcePlugin {
    /**
     * Unique type that this plugin handles, e.g. "api", "jdbc".
     */
    String getType();

    /**
     * Human-readable description of the plugin.
     */
    String getDescription();

    /**
     * Whether this plugin can handle the given dataSourceType.
     */
    boolean canHandle(String dataSourceType);

    /**
     * Execute a query against the given namespace with its schema.
     * @param namespace Schema namespace configuration (fields, JsonPaths, etc.)
     * @param request   The original QueryRequest (args, fields, etc.)
     * @param schema    Full Schema object (for API URL, DB config, etc.)
     * @return List of rows (fieldName->value)
     */
    List<Map<String, Object>> execute(Namespace namespace, QueryRequest request, Schema schema);
}