package org.example.query.controller;

import org.example.query.model.JsonQuery;
import org.example.query.model.JsonQueryResponse;
import org.example.query.service.QueryProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/query")
public class QueryController {

    private final QueryProcessor queryProcessor;
    
    @Autowired
    public QueryController(QueryProcessor queryProcessor) {
        this.queryProcessor = queryProcessor;
    }
    
    @PostMapping
    public ResponseEntity<JsonQueryResponse> executeQuery(@RequestBody JsonQuery jsonQuery) {
        JsonQueryResponse response = queryProcessor.processQuery(jsonQuery);
        return ResponseEntity.ok(response);
    }
}
