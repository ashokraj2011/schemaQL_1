package org.example.schema.service;

import org.example.schema.model.Field;
import org.example.schema.model.Namespace;
import org.example.schema.model.Schema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SchemaService {

    private final SchemaReader schemaReader;

    @Autowired
    public SchemaService(SchemaReader schemaReader) {
        this.schemaReader = schemaReader;
    }
    
    public void printSchemaInfo(String schemaPath) {
        Schema schema = schemaReader.readSchema(schemaPath);
        System.out.println("=".repeat(80));
        System.out.println("Schema Name: " + schema.getSchemaName());
        System.out.println("Data Source: " + schema.getSource().getDataSource());
        System.out.println("Data Source Type: " + schema.getSource().getDataSourceType());
        System.out.println("Global Key: " + schema.getGlobalKey());
        
        if (schemaReader.isApiSchema(schema)) {
            System.out.println("API URL: " + schema.getSource().getApiUrl());
            System.out.println("HTTP Method: " + schema.getSource().getHttpMethod());
        } else if (schemaReader.isDbSchema(schema)) {
            System.out.println("Database Name: " + schema.getSource().getDbName());
        }
        
        System.out.println("Mandatory Parameters: " + schema.getSource().getMandatoryParams());
        System.out.println("-".repeat(80));
        
        System.out.println("Namespaces:");
        for (Namespace namespace : schema.getNamespaces()) {
            printNamespaceInfo(namespace, 1);
        }
        System.out.println("=".repeat(80));
    }
    
    private void printNamespaceInfo(Namespace namespace, int indentLevel) {
        String indent = "  ".repeat(indentLevel);
        System.out.println(indent + "Namespace: " + namespace.getNamespace());
        
        if (namespace.getAlias() != null && !namespace.getAlias().isEmpty()) {
            System.out.println(indent + "Aliases: " + namespace.getAlias());
        }
        
        System.out.println(indent + "Mandatory Key: " + namespace.getMandatoryKey());
        System.out.println(indent + "Primary Key: " + namespace.getPrimaryKey());
        
        if (namespace.getAccessControl() != null) {
            System.out.println(indent + "Access Control:");
            System.out.println(indent + "  Read: " + namespace.getAccessControl().getRead());
            System.out.println(indent + "  Write: " + namespace.getAccessControl().getWrite());
        }
        
        System.out.println(indent + "Fields:");
        for (Field field : namespace.getFields()) {
            printFieldInfo(field, indentLevel + 1);
        }
    }
    
    private void printFieldInfo(Field field, int indentLevel) {
        String indent = "  ".repeat(indentLevel);
        System.out.println(indent + "Field: " + field.getName());
        System.out.println(indent + "  Type: " + field.getType());
        System.out.println(indent + "  Required: " + field.isRequired());
        
        if (field.getAliases() != null && !field.getAliases().isEmpty()) {
            System.out.println(indent + "  Aliases: " + field.getAliases());
        }
        
        if (field.isFlatten()) {
            System.out.println(indent + "  Flattened: true");
        }
        
        if (field.isSensitive()) {
            System.out.println(indent + "  Sensitive: true");
        }
        
        if (field.isComputed()) {
            System.out.println(indent + "  Computed: true");
        }
        
        if (field.getTransformer() != null) {
            System.out.println(indent + "  Transformer:");
            System.out.println(indent + "    Type: " + field.getTransformer().getType());
            
            if (field.getTransformer().getPattern() != null) {
                System.out.println(indent + "    Pattern: " + field.getTransformer().getPattern());
            }
            
            if (field.getTransformer().getReplaceWith() != null) {
                System.out.println(indent + "    Replace With: " + field.getTransformer().getReplaceWith());
            }
            
            if (field.getTransformer().getFields() != null) {
                System.out.println(indent + "    Fields: " + field.getTransformer().getFields());
            }
            
            if (field.getTransformer().getSeparator() != null) {
                System.out.println(indent + "    Separator: " + field.getTransformer().getSeparator());
            }
        }
        
        if (field.getAccessControl() != null) {
            System.out.println(indent + "  Access Control:");
            System.out.println(indent + "    Read: " + field.getAccessControl().getRead());
            System.out.println(indent + "    Write: " + field.getAccessControl().getWrite());
        }
        
        if (field.getNestedFields() != null && !field.getNestedFields().isEmpty()) {
            System.out.println(indent + "  Nested Fields:");
            for (Field nestedField : field.getNestedFields()) {
                printFieldInfo(nestedField, indentLevel + 2);
            }
        }
    }
}
