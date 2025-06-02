package org.example.schema.model;

import java.util.Collections;
import java.util.List;

/**
 * Represents either a real data source (API or DB) or a virtual view definition.
 */

public class DataSource {
    private String dataSource;
    private String dataSourceType;  // "api", "Database", or "view"

    // API-specific
    private String apiUrl;
    private String httpMethod;

    // DB-specific
    private String dbName;

    // common
    private List<String> mandatoryParams;
    private String globalKey;

    // view-specific field mappings
    private List<ViewField> viewFields;

    // For view schemas:
    private BaseDefinition base;
    private List<JoinDefinition> joins;

    // Getters & setters
    public String getDataSource() { return dataSource; }
    public void setDataSource(String dataSource) { this.dataSource = dataSource; }

    public String getDataSourceType() { return dataSourceType; }
    public void setDataSourceType(String dataSourceType) { this.dataSourceType = dataSourceType; }

    public String getApiUrl() { return apiUrl; }
    public void setApiUrl(String apiUrl) { this.apiUrl = apiUrl; }

    public String getHttpMethod() { return httpMethod; }
    public void setHttpMethod(String httpMethod) { this.httpMethod = httpMethod; }

    public String getDbName() { return dbName; }
    public void setDbName(String dbName) { this.dbName = dbName; }

    public List<String> getMandatoryParams() { return mandatoryParams; }
    public void setMandatoryParams(List<String> mandatoryParams) { this.mandatoryParams = mandatoryParams; }

    public String getGlobalKey() { return globalKey; }
    public void setGlobalKey(String globalKey) { this.globalKey = globalKey; }

    public List<ViewField> getViewFields() {
        return viewFields != null ? viewFields : Collections.emptyList();
    }
    public void setViewFields(List<ViewField> viewFields) { this.viewFields = viewFields; }

    public BaseDefinition getBase() { return base; }
    public void setBase(BaseDefinition base) { this.base = base; }

    public List<JoinDefinition> getJoins() { return joins; }
    public void setJoins(List<JoinDefinition> joins) { this.joins = joins; }

    public static class ViewField {
        private String from;
        private String as;

        public String getFrom() { return from; }
        public void setFrom(String from) { this.from = from; }

        public String getAs() { return as; }
        public void setAs(String as) { this.as = as; }
    }

    public static class BaseDefinition {
        private String schema;
        private String namespace;
        private String key;

        public String getSchema() { return schema; }
        public void setSchema(String schema) { this.schema = schema; }

        public String getNamespace() { return namespace; }
        public void setNamespace(String namespace) { this.namespace = namespace; }

        public String getKey() { return key; }
        public void setKey(String key) { this.key = key; }
    }

    public static class JoinDefinition {
        private String schema;
        private String namespace;
        private String key;
        private String type;

        public String getSchema() { return schema; }
        public void setSchema(String schema) { this.schema = schema; }

        public String getNamespace() { return namespace; }
        public void setNamespace(String namespace) { this.namespace = namespace; }

        public String getKey() { return key; }
        public void setKey(String key) { this.key = key; }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
    }
}
