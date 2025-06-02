package org.example.query.datasource;

import org.example.query.model.QueryRequest;
import org.example.query.service.ApiClient;
import org.example.schema.model.Namespace;
import org.example.schema.model.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
/**
 * Plugin implementation for API-backed data sources.
 */
@Component
public class ApiDataSourcePlugin implements DataSourcePlugin {
    private static final Logger logger = LoggerFactory.getLogger(ApiDataSourcePlugin.class);

    private final ApiClient apiClient;

    @Autowired
    public ApiDataSourcePlugin(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public String getType() {
        return "api";
    }

    @Override
    public String getDescription() {
        return "Executes REST API calls via ApiClient";
    }

    @Override
    public boolean canHandle(String dataSourceType) {
        return "api".equalsIgnoreCase(dataSourceType);
    }

    @Override
    public List<Map<String, Object>> execute(Namespace namespace,
                                             QueryRequest request,
                                             Schema schema) {
        // Only include the fields requested in the query
        List<String> requested = request.getFields();
        var fieldsConfig = namespace.getFields().stream()
                .filter(f -> requested.contains(f.getName())
                        || (f.getAliases() != null && f.getAliases().stream().anyMatch(requested::contains)))
                .collect(Collectors.toList());

        logger.info("API plugin executing namespace {} fields {}",
                namespace.getNamespace(), fieldsConfig.stream().map(f -> f.getName()).collect(Collectors.toList()));

        return apiClient.executeQuery(
                schema.getSource().getApiUrl(),
                schema.getSource().getHttpMethod(),
                request.getArguments(),
                namespace.getResultJsonPath(),
                fieldsConfig
        );
    }
}