package org.example.query.model;

import java.util.List;

public class JsonQuery {
    private List<QueryRequest> queries;
    private boolean includeDataTypes;

    public List<QueryRequest> getQueries() {
        return queries;
    }

    public void setQueries(List<QueryRequest> queries) {
        this.queries = queries;
    }

    public boolean isIncludeDataTypes() {
        return includeDataTypes;
    }

    public void setIncludeDataTypes(boolean includeDataTypes) {
        this.includeDataTypes = includeDataTypes;
    }
}
