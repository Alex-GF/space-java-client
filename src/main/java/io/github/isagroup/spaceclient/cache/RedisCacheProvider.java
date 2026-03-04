package io.github.isagroup.spaceclient.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.isagroup.spaceclient.types.CacheOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.exceptions.JedisException;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Redis cache provider for external caching
 * Uses Jedis for Redis connection and operations
 */
public class RedisCacheProvider implements CacheProvider {
    private static final Logger logger = LoggerFactory.getLogger(RedisCacheProvider.class);
    
    private final CacheOptions.RedisConfig config;
    private JedisPool jedisPool;
    private boolean connected;
    private final int defaultTtl;
    private final ObjectMapper objectMapper;

    public RedisCacheProvider(CacheOptions.RedisConfig config, int defaultTtl) {
        this.config = config;
        this.defaultTtl = defaultTtl;
        this.objectMapper = new ObjectMapper();
        this.connected = false;
        
        connect();
    }

    /**
     * Initialize the Redis connection
     */
    private void connect() {
        if (connected && jedisPool != null && !jedisPool.isClosed()) {
            return;
        }

        try {
            JedisPoolConfig poolConfig = new JedisPoolConfig();
            poolConfig.setMaxTotal(10);
            poolConfig.setMaxIdle(5);
            poolConfig.setMinIdle(1);
            poolConfig.setTestOnBorrow(true);
            poolConfig.setTestOnReturn(true);
            poolConfig.setTestWhileIdle(true);

            String host = config.getHost();
            int port = config.getPort();
            int timeout = config.getConnectTimeout();
            int database = config.getDb();
            String password = config.getPassword();

            if (password != null && !password.isEmpty()) {
                jedisPool = new JedisPool(poolConfig, host, port, timeout, password, database);
            } else {
                jedisPool = new JedisPool(poolConfig, host, port, timeout, null, database);
            }

            // Test connection
            try (Jedis jedis = jedisPool.getResource()) {
                jedis.ping();
                logger.info("[RedisCacheProvider] Connected to Redis at {}:{}", host, port);
                connected = true;
            }
        } catch (Exception e) {
            connected = false;
            logger.error("[RedisCacheProvider] Failed to connect to Redis", e);
            throw new RuntimeException("Failed to connect to Redis: " + e.getMessage(), e);
        }
    }

    /**
     * Get the prefixed key
     */
    private String getKey(String key) {
        return config.getKeyPrefix() + key;
    }

    @Override
    public <T> T get(String key, Class<T> type) {
        if (!connected || jedisPool == null || jedisPool.isClosed()) {
            try {
                connect();
            } catch (Exception e) {
                logger.error("[RedisCacheProvider] Failed to reconnect", e);
                return null;
            }
        }

        try (Jedis jedis = jedisPool.getResource()) {
            String value = jedis.get(getKey(key));
            if (value == null) {
                return null;
            }

            return objectMapper.readValue(value, type);
        } catch (JedisException e) {
            logger.error("[RedisCacheProvider] Error getting key: {}", key, e);
            return null;
        } catch (JsonProcessingException e) {
            logger.error("[RedisCacheProvider] Error deserializing value for key: {}", key, e);
            return null;
        }
    }

    @Override
    public <T> void set(String key, T value, Integer ttl) {
        if (!connected || jedisPool == null || jedisPool.isClosed()) {
            try {
                connect();
            } catch (Exception e) {
                logger.error("[RedisCacheProvider] Failed to reconnect", e);
                return; // Fail silently for cache operations
            }
        }

        try (Jedis jedis = jedisPool.getResource()) {
            String serializedValue = objectMapper.writeValueAsString(value);
            int actualTtl = ttl != null ? ttl : defaultTtl;

            if (actualTtl > 0) {
                jedis.setex(getKey(key), actualTtl, serializedValue);
            } else {
                jedis.set(getKey(key), serializedValue);
            }
        } catch (JedisException e) {
            logger.error("[RedisCacheProvider] Error setting key: {}", key, e);
        } catch (JsonProcessingException e) {
            logger.error("[RedisCacheProvider] Error serializing value for key: {}", key, e);
        }
    }

    @Override
    public void delete(String key) {
        if (!connected || jedisPool == null || jedisPool.isClosed()) {
            return;
        }

        try (Jedis jedis = jedisPool.getResource()) {
            jedis.del(getKey(key));
        } catch (JedisException e) {
            logger.error("[RedisCacheProvider] Error deleting key: {}", key, e);
        }
    }

    @Override
    public void clear() {
        if (!connected || jedisPool == null || jedisPool.isClosed()) {
            return;
        }

        try (Jedis jedis = jedisPool.getResource()) {
            // Get all keys with the prefix and delete them
            Set<String> keys = jedis.keys(config.getKeyPrefix() + "*");
            if (keys != null && !keys.isEmpty()) {
                jedis.del(keys.toArray(new String[0]));
            }
        } catch (JedisException e) {
            logger.error("[RedisCacheProvider] Error clearing cache", e);
        }
    }

    @Override
    public boolean has(String key) {
        if (!connected || jedisPool == null || jedisPool.isClosed()) {
            return false;
        }

        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.exists(getKey(key));
        } catch (JedisException e) {
            logger.error("[RedisCacheProvider] Error checking key existence: {}", key, e);
            return false;
        }
    }

    @Override
    public List<String> keys(String pattern) {
        if (!connected || jedisPool == null || jedisPool.isClosed()) {
            return new ArrayList<>();
        }

        try (Jedis jedis = jedisPool.getResource()) {
            String fullPattern = pattern != null && !pattern.isEmpty() 
                    ? config.getKeyPrefix() + pattern 
                    : config.getKeyPrefix() + "*";
            
            Set<String> keys = jedis.keys(fullPattern);
            
            if (keys == null || keys.isEmpty()) {
                return new ArrayList<>();
            }

            // Remove the prefix from returned keys
            String prefix = config.getKeyPrefix();
            List<String> result = new ArrayList<>();
            for (String key : keys) {
                if (key.startsWith(prefix)) {
                    result.add(key.substring(prefix.length()));
                }
            }
            return result;
        } catch (JedisException e) {
            logger.error("[RedisCacheProvider] Error getting keys with pattern: {}", pattern, e);
            return new ArrayList<>();
        }
    }

    @Override
    public void close() {
        if (jedisPool != null && !jedisPool.isClosed()) {
            jedisPool.close();
            logger.info("[RedisCacheProvider] Redis connection closed");
        }
        connected = false;
    }

    public boolean isConnected() {
        return connected && jedisPool != null && !jedisPool.isClosed();
    }
}
