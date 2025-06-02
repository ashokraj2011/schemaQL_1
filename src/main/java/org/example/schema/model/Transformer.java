package org.example.schema.model;

import java.util.List;

public class Transformer {
    private String type;
    private String pattern;
    private String replaceWith;
    private List<String> fields;
    private String separator;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public String getReplaceWith() {
        return replaceWith;
    }

    public void setReplaceWith(String replaceWith) {
        this.replaceWith = replaceWith;
    }

    public List<String> getFields() {
        return fields;
    }

    public void setFields(List<String> fields) {
        this.fields = fields;
    }

    public String getSeparator() {
        return separator;
    }

    public void setSeparator(String separator) {
        this.separator = separator;
    }
}
