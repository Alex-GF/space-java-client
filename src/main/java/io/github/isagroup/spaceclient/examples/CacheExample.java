package io.github.isagroup.spaceclient.examples;

import io.github.isagroup.spaceclient.SpaceClient;
import io.github.isagroup.spaceclient.SpaceClientFactory;
import io.github.isagroup.spaceclient.types.*;
import io.github.isagroup.spaceclient.types.CacheOptions.*;

/**
 * Examples of using cache with Space Java Client
 */
public class CacheExample {

    public static void main(String[] args) {
        // Example 1: Built-in cache
        builtinCacheExample();

        // Example 2: Redis cache
        redisCacheExample();
    }

    private static void builtinCacheExample() {
        System.out.println("=== Built-in Cache Example ===");

        CacheOptions cacheOptions = new CacheOptions(true, CacheType.BUILTIN, 300);

        SpaceConnectionOptions options = new SpaceConnectionOptions(
            "http://localhost:3000",
            "your-api-key",
            5000,
            cacheOptions
        );

        SpaceClient client = SpaceClientFactory.connect(options);

        try {
            // First call - fetches from API
            long start = System.currentTimeMillis();
            client.contracts.getContract("user123");
            long firstCall = System.currentTimeMillis() - start;
            System.out.println("First call (from API): " + firstCall + "ms");

            // Second call - fetches from cache
            start = System.currentTimeMillis();
            client.contracts.getContract("user123");
            long secondCall = System.currentTimeMillis() - start;
            System.out.println("Second call (from cache): " + secondCall + "ms");

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            client.close();
        }
    }

    private static void redisCacheExample() {
        System.out.println("\n=== Redis Cache Example ===");

        RedisConfig redisConfig = new RedisConfig("localhost", 6379);
        // redisConfig.setPassword("your-password"); // if needed

        ExternalCacheConfig externalConfig = new ExternalCacheConfig();
        externalConfig.setRedis(redisConfig);

        CacheOptions cacheOptions = new CacheOptions(true, CacheType.REDIS, 300);
        cacheOptions.setExternal(externalConfig);

        SpaceConnectionOptions options = new SpaceConnectionOptions(
            "http://localhost:3000",
            "your-api-key",
            5000,
            cacheOptions
        );

        SpaceClient client = SpaceClientFactory.connect(options);

        try {
            // First call - fetches from API and caches in Redis
            long start = System.currentTimeMillis();
            client.contracts.getContract("user123");
            long firstCall = System.currentTimeMillis() - start;
            System.out.println("First call (from API): " + firstCall + "ms");

            // Second call - fetches from Redis cache
            start = System.currentTimeMillis();
            client.contracts.getContract("user123");
            long secondCall = System.currentTimeMillis() - start;
            System.out.println("Second call (from Redis): " + secondCall + "ms");

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            client.close();
        }
    }
}
