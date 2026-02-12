package io.github.isagroup.spaceclient.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Built-in cache provider using ConcurrentHashMap for in-memory storage
 * This provider is suitable for single-instance applications
 */
public class BuiltinCacheProvider implements CacheProvider {
    private static final Logger logger = LoggerFactory.getLogger(BuiltinCacheProvider.class);
    
    private final Map<String, CacheEntry<Object>> cache;
    private final ScheduledExecutorService cleanupExecutor;
    private final int defaultTtl;
    private final ObjectMapper objectMapper;

    public BuiltinCacheProvider(int defaultTtl) {
        this.defaultTtl = defaultTtl;
        this.cache = new ConcurrentHashMap<>();
        this.objectMapper = new ObjectMapper();
        this.cleanupExecutor = Executors.newSingleThreadScheduledExecutor();
        
        // Start cleanup task every 5 minutes
        startCleanupInterval();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> type) {
        CacheEntry<Object> entry = cache.get(key);
        
        if (entry == null) {
            return null;
        }

        // Check if expired
        if (entry.isExpired()) {
            cache.remove(key);
            return null;
        }

        try {
            // Handle type conversion
            Object value = entry.getValue();
            if (value == null) {
                return null;
            }
            
            if (type.isInstance(value)) {
                return (T) value;
            }
            
            // Try to convert using ObjectMapper
            return objectMapper.convertValue(value, type);
        } catch (Exception e) {
            logger.error("Error converting cached value for key: {}", key, e);
            return null;
        }
    }

    @Override
    public <T> void set(String key, T value, Integer ttl) {
        int actualTtl = ttl != null ? ttl : defaultTtl;
        CacheEntry<Object> entry = new CacheEntry<>(value, actualTtl);
        cache.put(key, entry);
    }

    @Override
    public void delete(String key) {
        cache.remove(key);
    }

    @Override
    public void clear() {
        cache.clear();
    }

    @Override
    public boolean has(String key) {
        CacheEntry<Object> entry = cache.get(key);
        
        if (entry == null) {
            return false;
        }

        // Check if expired
        if (entry.isExpired()) {
            cache.remove(key);
            return false;
        }

        return true;
    }

    @Override
    public List<String> keys(String pattern) {
        if (pattern == null || pattern.isEmpty()) {
            return new ArrayList<>(cache.keySet());
        }

        // Convert glob-style pattern to regex
        String regex = pattern
                .replace(".", "\\.")
                .replace("*", ".*")
                .replace("?", ".");
        
        Pattern regexPattern = Pattern.compile(regex);

        return cache.keySet().stream()
                .filter(key -> regexPattern.matcher(key).matches())
                .collect(Collectors.toList());
    }

    @Override
    public void close() {
        if (cleanupExecutor != null && !cleanupExecutor.isShutdown()) {
            cleanupExecutor.shutdown();
            try {
                if (!cleanupExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                    cleanupExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                cleanupExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        cache.clear();
    }

    /**
     * Get cache statistics
     */
    public CacheStats getStats() {
        long now = System.currentTimeMillis();
        int expired = 0;
        int active = 0;

        for (CacheEntry<Object> entry : cache.values()) {
            if (entry.isExpired()) {
                expired++;
            } else {
                active++;
            }
        }

        return new CacheStats(cache.size(), active, expired);
    }

    /**
     * Start the cleanup interval to remove expired entries
     */
    private void startCleanupInterval() {
        cleanupExecutor.scheduleAtFixedRate(
                this::cleanupExpiredEntries,
                5,
                5,
                TimeUnit.MINUTES
        );
    }

    /**
     * Remove expired entries from the cache
     */
    private void cleanupExpiredEntries() {
        try {
            List<String> keysToRemove = new ArrayList<>();
            
            for (Map.Entry<String, CacheEntry<Object>> entry : cache.entrySet()) {
                if (entry.getValue().isExpired()) {
                    keysToRemove.add(entry.getKey());
                }
            }

            keysToRemove.forEach(cache::remove);
            
            if (!keysToRemove.isEmpty()) {
                logger.debug("Cleaned up {} expired cache entries", keysToRemove.size());
            }
        } catch (Exception e) {
            logger.error("Error during cache cleanup", e);
        }
    }

    /**
     * Cache statistics
     */
    public static class CacheStats {
        private final int total;
        private final int active;
        private final int expired;

        public CacheStats(int total, int active, int expired) {
            this.total = total;
            this.active = active;
            this.expired = expired;
        }

        public int getTotal() {
            return total;
        }

        public int getActive() {
            return active;
        }

        public int getExpired() {
            return expired;
        }
    }
}
