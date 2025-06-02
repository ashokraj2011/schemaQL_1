package org.example.schema.model;

import java.util.List;
import java.util.Map;

public class Namespace {
    private String namespace;
    private List<String> alias;
    private String mandatoryKey;
    private List<String> primaryKey;
    private List<Field> fields;
    private AccessControl accessControl;
    private String resultJsonPath;
    private boolean cacheable;
    private long    cacheTTL;          // in seconds
    private String  cacheKeyPattern;   // e.g. "customers::{customer_id}"
    /** true if this namespace should be cached */
    public boolean isCacheable() {
        return cacheable;
    }
    public void setCacheable(boolean cacheable) {
        this.cacheable = cacheable;
    }
    public String getCacheKeyPattern() {
        return cacheKeyPattern;
    }
    public void setCacheKeyPattern(String cacheKeyPattern) {
        this.cacheKeyPattern = cacheKeyPattern;
    }

    /** TTL in seconds */
    public long getCacheTTL() {
        return cacheTTL;
    }
    public void setCacheTTL(long cacheTTL) {
        this.cacheTTL = cacheTTL;
    }
    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public List<String> getAlias() {
        return alias;
    }

    public void setAlias(List<String> alias) {
        this.alias = alias;
    }

    public String getMandatoryKey() {
        return mandatoryKey;
    }

    public void setMandatoryKey(String mandatoryKey) {
        this.mandatoryKey = mandatoryKey;
    }

    public List<String> getPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(List<String> primaryKey) {
        this.primaryKey = primaryKey;
    }

    public List<Field> getFields() {
        return fields;
    }

    public void setFields(List<Field> fields) {
        this.fields = fields;
    }

    public AccessControl getAccessControl() {
        return accessControl;
    }

    public void setAccessControl(AccessControl accessControl) {
        this.accessControl = accessControl;
    }

    public String getResultJsonPath() {
        return resultJsonPath;
    }

    public void setResultJsonPath(String resultJsonPath) {
        this.resultJsonPath = resultJsonPath;
    }
}
