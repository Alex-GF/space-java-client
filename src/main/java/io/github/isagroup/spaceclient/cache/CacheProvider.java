package io.github.isagroup.spaceclient.cache;

import java.util.List;

/**
 * Generic cache provider interface
 */
public interface CacheProvider {
    
    /**
     * Get a value from the cache
     * @param key Cache key
     * @param <T> Value type
     * @return Cached value or null if not found or expired
     */
    <T> T get(String key, Class<T> type);
    
    /**
     * Set a value in the cache
     * @param key Cache key
     * @param value Value to cache
     * @param ttl Time to live in seconds (optional)
     * @param <T> Value type
     */
    <T> void set(String key, T value, Integer ttl);
    
    /**
     * Delete a value from the cache
     * @param key Cache key
     */
    void delete(String key);
    
    /**
     * Clear all cache entries
     */
    void clear();
    
    /**
     * Check if a key exists in the cache
     * @param key Cache key
     * @return true if exists and not expired
     */
    boolean has(String key);
    
    /**
     * Get all keys matching a pattern
     * @param pattern Pattern to match (glob-style)
     * @return List of matching keys
     */
    List<String> keys(String pattern);
    
    /**
     * Close the cache connection
     */
    void close();
}
