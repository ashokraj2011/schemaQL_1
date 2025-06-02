package org.example.query.controller;

import org.example.query.service.CacheService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Set;

/**
 * Debug endpoints for inspecting and managing the in-memory cache.
 */
@RestController
@RequestMapping("/debug/cache")
public class CacheDebugController {
    private final CacheService cacheService;

    public CacheDebugController(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    /**
     * Retrieves all cache keys (including expired entries).
     */
    @GetMapping("/keys")
    public Set<String> getAllKeys() {
        return cacheService.getAllKeys();
    }

    /**
     * Retrieves all live (non-expired) cache entries as key->value.
     */
    @GetMapping("/entries")
    public Map<String, Object> getAllEntries() {
        return cacheService.getAllEntries();
    }

    /**
     * Evicts a specific cache entry by key.
     */
    @DeleteMapping("/evict/{key}")
    public void evictKey(@PathVariable String key) {
        cacheService.evict(key);
    }

    /**
     * Clears the entire cache.
     */
    @DeleteMapping("/clear")
    public void clearCache() {
        cacheService.clear();
    }
}
