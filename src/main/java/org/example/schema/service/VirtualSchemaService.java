package org.example.schema.service;

import org.example.schema.model.Schema;
import org.example.schema.model.Namespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Service for composing "virtual" schemas by merging multiple API and DB schemas.
 */
@Service
public class VirtualSchemaService {
    private static final Logger logger = LoggerFactory.getLogger(VirtualSchemaService.class);

    private final SchemaReader schemaReader;

    @Autowired
    public VirtualSchemaService(SchemaReader schemaReader) {
        this.schemaReader = schemaReader;
    }

    /**
     * Builds a virtual schema by joining multiple existing schemas.
     * Namespaces with the same name across schemas will be merged;
     * fields are combined, avoiding duplicates.
     *
     * @param virtualName      Name for the virtual schema
     * @param components       List of schema names to join
     * @param namespaceAliases Optional map to rename namespaces in the virtual schema
     * @return Combined Schema object
     */
    public Schema buildVirtualSchema(
            String virtualName,
            List<String> components,
            Map<String, String> namespaceAliases
    ) {
        Schema virtual = new Schema();
        virtual.setSchemaName(virtualName);
        List<Namespace> mergedNamespaces = new ArrayList<>();
        Set<String> seenNamespaces = new HashSet<>();

        for (String comp : components) {
            // Load each component schema (fresh from reader)
            Schema compSchema = schemaReader.readSchema(comp);
            logger.info("Merging schema {} into virtual {}", compSchema.getSchemaName(), virtualName);

            for (Namespace ns : compSchema.getNamespaces()) {
                // Determine output namespace name (alias or original)
                String outName = namespaceAliases.getOrDefault(ns.getNamespace(), ns.getNamespace());
                Namespace target = findNamespace(mergedNamespaces, outName);
                if (target == null) {
                    // Clone namespace under new name
                    Namespace copy = cloneNamespace(ns);
                    copy.setNamespace(outName);
                    mergedNamespaces.add(copy);
                    seenNamespaces.add(outName);
                } else {
                    // Merge fields into existing namespace
                    mergeFields(target, ns);
                }
            }
        }

        virtual.setNamespaces(mergedNamespaces);
        return virtual;
    }

    // Helper: find a namespace by name
    private Namespace findNamespace(List<Namespace> list, String name) {
        return list.stream()
                .filter(n -> n.getNamespace().equals(name))
                .findFirst()
                .orElse(null);
    }

    // Deep-clone a Namespace (excluding dataSource info)
    private Namespace cloneNamespace(Namespace src) {
        Namespace copy = new Namespace();
        copy.setNamespace(src.getNamespace());
        copy.setAlias(src.getAlias());
        copy.setMandatoryKey(src.getMandatoryKey());
        copy.setPrimaryKey(src.getPrimaryKey());
        copy.setResultJsonPath(src.getResultJsonPath());
        // clone fields list
        List<org.example.schema.model.Field> fields = new ArrayList<>();
        for (org.example.schema.model.Field f : src.getFields()) {
            fields.add(f); // shallow copy; assume Field is immutable or use a proper clone
        }
        copy.setFields(fields);
        copy.setAccessControl(src.getAccessControl());
        return copy;
    }

    // Merge fields from src into target, avoiding duplicates by field name
    private void mergeFields(Namespace target, Namespace src) {
        Set<String> existing = new HashSet<>();
        for (org.example.schema.model.Field f : target.getFields()) {
            existing.add(f.getName());
        }
        for (org.example.schema.model.Field f : src.getFields()) {
            if (!existing.contains(f.getName())) {
                target.getFields().add(f);
                existing.add(f.getName());
            }
        }
    }
}
