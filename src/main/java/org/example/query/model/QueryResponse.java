package org.example.query.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QueryResponse {
    private String namespace;
    private String dataSource;
    private String schema;
    private List<Map<String, Object>> data;
    
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Map<String, String> dataTypes;
    
    public QueryResponse() {
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getDataSource() {
        return dataSource;
    }

    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public List<Map<String, Object>> getData() {
        return data;
    }

    public void setData(List<Map<String, Object>> data) {
        this.data = data;
    }

    public Map<String, String> getDataTypes() {
        return dataTypes;
    }

    public void setDataTypes(Map<String, String> dataTypes) {
        this.dataTypes = dataTypes;
    }
    
    public void addDataType(String fieldName, String type) {
        if (this.dataTypes == null) {
            this.dataTypes = new HashMap<>();
        }
        this.dataTypes.put(fieldName, type);
    }
}
