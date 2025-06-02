package org.example.query.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import org.example.schema.model.Field;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ApiClient {
    private static final Logger logger = LoggerFactory.getLogger(ApiClient.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    public ApiClient(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Execute an API query with hybrid JsonPath support.
     * Only the requested Field objects are processed.
     */
    public List<Map<String, Object>> executeQuery(
            String apiUrl,
            String httpMethod,
            Map<String, Object> queryParams,
            String namespaceJsonPath,
            List<Field> fields
    ) {
        logger.info("Making API request to: {}", apiUrl);

        // Build URL with query parameters
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(apiUrl);
        queryParams.forEach((key, value) -> builder.queryParam(key, value));

        // Set up headers
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        HttpEntity<?> entity = new HttpEntity<>(headers);

        // Make API call
        HttpMethod method = HttpMethod.valueOf(httpMethod);
        ResponseEntity<String> response = restTemplate.exchange(
                builder.toUriString(),
                method,
                entity,
                String.class
        );
        String rawBody = response.getBody();

        try {
            // 1. Extract namespace subtree via JsonPath or default to full JSON
            Object extracted = (namespaceJsonPath != null && !namespaceJsonPath.trim().isEmpty())
                    ? JsonPath.read(rawBody, namespaceJsonPath)
                    : objectMapper.readTree(rawBody);

            // 2. Normalize to List<JsonNode>
            List<JsonNode> nodes;
            if (extracted instanceof List) {
                @SuppressWarnings("unchecked") List<Object> rawList = (List<Object>) extracted;
                nodes = new ArrayList<>();
                for (Object obj : rawList) {
                    nodes.add(objectMapper.valueToTree(obj));
                }
            } else {
                JsonNode single = objectMapper.valueToTree(extracted);
                nodes = Collections.singletonList(single);
            }
            logger.debug("After namespace JsonPath ('{}'), found {} node(s)", namespaceJsonPath, nodes.size());

            // 3. Extract only requested fields
            List<Map<String, Object>> result = new ArrayList<>();
            for (JsonNode node : nodes) {
                Map<String, Object> row = new HashMap<>();
                for (Field f : fields) {
                    // Debug: log the jsonPath value from schema
                    logger.debug("Processing field '{}' with configured jsonPath='{}'", f.getName(), f.getJsonPath());
                    Object value = null;
                    String fieldPath = null;

                    // Determine JsonPath: field-level override or namespace default
                    if (f.getJsonPath() != null && !f.getJsonPath().trim().isEmpty()) {
                        fieldPath = f.getJsonPath();
                    } else if (namespaceJsonPath != null && !namespaceJsonPath.trim().isEmpty()) {
                        fieldPath = namespaceJsonPath + "." + f.getName();
                    }

                    // Attempt extraction via JsonPath
                    if (fieldPath != null) {
                        try {
                            value = JsonPath.read(rawBody, fieldPath);
                            logger.debug("Extracted '{}' via JsonPath '{}' -> {}", f.getName(), fieldPath, value);
                        } catch (Exception e) {
                            logger.warn("JsonPath '{}' for field '{}' did not match", fieldPath, f.getName());
                        }
                    }

                    // Fallback to namespace subtree lookup
                    if (value == null && node.has(f.getName())) {
                        JsonNode vNode = node.get(f.getName());
                        value = vNode.isTextual()
                                ? vNode.asText()
                                : objectMapper.treeToValue(vNode, Object.class);
                    }

                    row.put(f.getName(), value);
                }
                result.add(row);
            }

            return result;
        } catch (Exception e) {
            logger.error("Error processing API response", e);
            throw new RuntimeException("Error processing API response", e);
        }
    }
}
