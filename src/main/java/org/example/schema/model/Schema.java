package org.example.schema.model;

import java.util.List;

public class Schema {
    private String schemaName;
    private DataSource source;

    private String globalKey;
    private List<Namespace> namespaces;

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    public DataSource getSource() {
        return source;
    }

    public void setSource(DataSource source) {
        this.source = source;
    }

    public String getGlobalKey() {
        return globalKey;
    }

    public void setGlobalKey(String globalKey) {
        this.globalKey = globalKey;
    }

    public List<Namespace> getNamespaces() {
        return namespaces;
    }

    public void setNamespaces(List<Namespace> namespaces) {
        this.namespaces = namespaces;
    }
}
