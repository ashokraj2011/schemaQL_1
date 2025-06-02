package org.example.query.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.HashSet;

/**
 * Simple in-memory cache service with per-entry TTL support.
 */
@Service
public class CacheService {
    private static final Logger logger = LoggerFactory.getLogger(CacheService.class);

    private static class CacheEntry {
        private final Object value;
        private final long expiresAt;

        CacheEntry(Object value, long ttlSeconds) {
            this.value = value;
            this.expiresAt = System.currentTimeMillis() + ttlSeconds * 1000;
        }

        boolean isExpired() {
            return System.currentTimeMillis() > expiresAt;
        }

        Object getValue() {
            return value;
        }

        long getExpiresAt() {
            return expiresAt;
        }
    }

    private final ConcurrentMap<String, CacheEntry> cache = new ConcurrentHashMap<>();

    /**
     * Retrieves a cached value, or null if absent or expired.
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        CacheEntry entry = cache.get(key);
        if (entry == null || entry.isExpired()) {
            logger.debug("CACHE MISS or EXPIRED for key='{}'", key);
            cache.remove(key);
            return null;
        }
        logger.debug("CACHE HIT for key='{}'", key);
        return (T) entry.getValue();
    }

    /**
     * Stores a value in the cache under the given key for ttlSeconds.
     */
    public void put(String key, Object value, long ttlSeconds) {
        if (ttlSeconds <= 0) {
            logger.debug("TTL non-positive for key='{}', not caching", key);
            return;
        }
        logger.debug("CACHING key='{}' for {}s", key, ttlSeconds);
        CacheEntry entry = new CacheEntry(value, ttlSeconds);
        cache.put(key, entry);
    }

    /**
     * Explicitly invalidates a cache entry.
     */
    public void evict(String key) {
        logger.debug("EVICTING key='{}'", key);
        cache.remove(key);
    }

    /**
     * Clears the entire cache.
     */
    public void clear() {
        logger.debug("CLEARING entire cache");
        cache.clear();
    }

    /**
     * Returns all live (non-expired) entries as a Map from key -> value.
     */
    @SuppressWarnings("unchecked")
    public Map<String,Object> getAllEntries() {
        long now = System.currentTimeMillis();
        return cache.entrySet().stream()
                .filter(e -> e.getValue().getExpiresAt() > now)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().getValue()
                ));
    }

    /**
     * Returns the set of all cache keys (may include expired; cleanup occurs on next access).
     */
    public Set<String> getAllKeys() {
        return new HashSet<>(cache.keySet());
    }
}
