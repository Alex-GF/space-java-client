package io.github.isagroup.spaceclient.cache;

import io.github.isagroup.spaceclient.types.CacheOptions;

/**
 * Factory class for creating cache providers
 */
public class CacheProviderFactory {

    /**
     * Create a cache provider based on the provided options
     */
    public static CacheProvider create(CacheOptions options) {
        CacheOptions.CacheType type = options.getType();
        int ttl = options.getTtl();

        if (type == CacheOptions.CacheType.REDIS) {
            if (options.getExternal() == null || options.getExternal().getRedis() == null) {
                throw new IllegalArgumentException("Redis configuration is required when using Redis cache type");
            }
            return new RedisCacheProvider(options.getExternal().getRedis(), ttl);
        } else {
            return new BuiltinCacheProvider(ttl);
        }
    }

    /**
     * Validate cache options
     */
    public static void validate(CacheOptions options) {
        if (!options.isEnabled()) {
            return;
        }

        if (options.getType() == CacheOptions.CacheType.REDIS) {
            if (options.getExternal() == null || options.getExternal().getRedis() == null) {
                throw new IllegalArgumentException("Redis configuration is required when using Redis cache type");
            }

            CacheOptions.RedisConfig redis = options.getExternal().getRedis();

            if (redis.getHost() == null || redis.getHost().isEmpty()) {
                throw new IllegalArgumentException("Redis host is required");
            }

            Integer port = redis.getPort();
            if (port != null && (port < 1 || port > 65535)) {
                throw new IllegalArgumentException("Redis port must be between 1 and 65535");
            }

            Integer db = redis.getDb();
            if (db != null && (db < 0 || db > 15)) {
                throw new IllegalArgumentException("Redis database number must be between 0 and 15");
            }

            Integer timeout = redis.getConnectTimeout();
            if (timeout != null && timeout <= 0) {
                throw new IllegalArgumentException("Redis connect timeout must be a positive number");
            }
        }

        if (options.getTtl() != null && options.getTtl() <= 0) {
            throw new IllegalArgumentException("TTL must be a positive number");
        }
    }
}
