package org.example.schema.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class Field {
    private String name;
    private String type;
    @JsonProperty("jsonPath")
    private String jsonPath;

    private boolean required;
    private List<String> aliases;
    private boolean flatten;
    private boolean isSensitive;
    private List<Field> nestedFields;
    private AccessControl accessControl;
    private Transformer transformer;
    private boolean computed;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public List<String> getAliases() {
        return aliases;
    }

    public void setAliases(List<String> aliases) {
        this.aliases = aliases;
    }

    public boolean isFlatten() {
        return flatten;
    }

    public void setFlatten(boolean flatten) {
        this.flatten = flatten;
    }

    public boolean isSensitive() {
        return isSensitive;
    }

    public void setSensitive(boolean isSensitive) {
        this.isSensitive = isSensitive;
    }

    public List<Field> getNestedFields() {
        return nestedFields;
    }

    public void setNestedFields(List<Field> nestedFields) {
        this.nestedFields = nestedFields;
    }

    public AccessControl getAccessControl() {
        return accessControl;
    }

    public void setAccessControl(AccessControl accessControl) {
        this.accessControl = accessControl;
    }

    public Transformer getTransformer() {
        return transformer;
    }

    public void setTransformer(Transformer transformer) {
        this.transformer = transformer;
    }

    public boolean isComputed() {
        return computed;
    }

    public void setComputed(boolean computed) {
        this.computed = computed;
    }

    public String getJsonPath() {
        return jsonPath;
    }

    public void setJsonPath(String jsonPath) {
        this.jsonPath = jsonPath;
    }
}
