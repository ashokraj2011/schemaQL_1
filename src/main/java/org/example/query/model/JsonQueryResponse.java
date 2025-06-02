package org.example.query.model;

import java.util.List;

public class JsonQueryResponse {
    private List<QueryResponse> results;
    private boolean includeDataTypes;

    public List<QueryResponse> getResults() {
        return results;
    }

    public void setResults(List<QueryResponse> results) {
        this.results = results;
    }

    public boolean isIncludeDataTypes() {
        return includeDataTypes;
    }

    public void setIncludeDataTypes(boolean includeDataTypes) {
        this.includeDataTypes = includeDataTypes;
    }
}
