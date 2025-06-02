package org.example.query.datasource;

import org.example.query.model.QueryRequest;
import org.example.query.service.SqlGenerator;
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
 * Plugin implementation for JDBC-backed (database) data sources.
 */
@Component
public class JdbcDataSourcePlugin implements DataSourcePlugin {
    private static final Logger logger = LoggerFactory.getLogger(JdbcDataSourcePlugin.class);

    private final SqlGenerator sqlGenerator;

    @Autowired
    public JdbcDataSourcePlugin(SqlGenerator sqlGenerator) {
        this.sqlGenerator = sqlGenerator;
    }

    @Override
    public String getType() {
        return "jdbc";
    }

    @Override
    public String getDescription() {
        return "Executes SQL queries via JDBC";
    }

    @Override
    public boolean canHandle(String dataSourceType) {
        return "database".equalsIgnoreCase(dataSourceType)
                || "jdbc".equalsIgnoreCase(dataSourceType);
    }

    @Override
    public List<Map<String, Object>> execute(Namespace namespace,
                                             QueryRequest request,
                                             Schema schema) {
        List<String> requested = request.getFields();
        List<String> actualFields = namespace.getFields().stream()
                .filter(f -> requested.contains(f.getName())
                        || (f.getAliases() != null && f.getAliases().stream().anyMatch(requested::contains)))
                .map(f -> f.getName())
                .collect(Collectors.toList());

        String sql = sqlGenerator.generateSql(namespace, actualFields, request.getArguments());
        logger.info("JDBC plugin executing SQL: {}", sql);

        return sqlGenerator.executeSql(
                schema.getSource().getDbName().toLowerCase(),
                sql,
                request.getArguments(),
                actualFields,
                namespace
        );
    }
}