package io.github.isagroup.spaceclient.cache;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.*;
import static org.awaitility.Awaitility.await;

@DisplayName("BuiltinCacheProvider Tests")
class BuiltinCacheProviderTest {

    private BuiltinCacheProvider cacheProvider;

    @BeforeEach
    void setUp() {
        cacheProvider = new BuiltinCacheProvider(300);
    }

    @AfterEach
    void tearDown() {
        if (cacheProvider != null) {
            cacheProvider.close();
        }
    }

    @Test
    @DisplayName("Should store and retrieve values")
    void shouldStoreAndRetrieveValues() {
        cacheProvider.set("key1", "value1", null);
        
        String result = cacheProvider.get("key1", String.class);
        
        assertThat(result).isEqualTo("value1");
    }

    @Test
    @DisplayName("Should return null for non-existent keys")
    void shouldReturnNullForNonExistentKeys() {
        String result = cacheProvider.get("non-existent", String.class);
        
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Should delete cached values")
    void shouldDeleteCachedValues() {
        cacheProvider.set("key1", "value1", null);
        cacheProvider.delete("key1");
        
        String result = cacheProvider.get("key1", String.class);
        
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Should clear all cached values")
    void shouldClearAllCachedValues() {
        cacheProvider.set("key1", "value1", null);
        cacheProvider.set("key2", "value2", null);
        
        cacheProvider.clear();
        
        assertThat(cacheProvider.get("key1", String.class)).isNull();
        assertThat(cacheProvider.get("key2", String.class)).isNull();
    }

    @Test
    @DisplayName("Should check if key exists")
    void shouldCheckIfKeyExists() {
        cacheProvider.set("key1", "value1", null);
        
        assertThat(cacheProvider.has("key1")).isTrue();
        assertThat(cacheProvider.has("non-existent")).isFalse();
    }

    @Test
    @DisplayName("Should expire entries after TTL")
    void shouldExpireEntriesAfterTTL() {
        cacheProvider.set("expire-key", "value", 1); // 1 second TTL
        
        assertThat(cacheProvider.has("expire-key")).isTrue();
        
        await().atMost(2, TimeUnit.SECONDS)
                .until(() -> !cacheProvider.has("expire-key"));
    }

    @Test
    @DisplayName("Should get keys matching pattern")
    void shouldGetKeysMatchingPattern() {
        cacheProvider.set("user:123", "value1", null);
        cacheProvider.set("user:456", "value2", null);
        cacheProvider.set("product:789", "value3", null);
        
        List<String> userKeys = cacheProvider.keys("user:*");
        
        assertThat(userKeys).hasSize(2);
        assertThat(userKeys).containsExactlyInAnyOrder("user:123", "user:456");
    }

    @Test
    @DisplayName("Should provide cache statistics")
    void shouldProvideCacheStatistics() {
        cacheProvider.set("key1", "value1", null);
        cacheProvider.set("key2", "value2", null);
        
        BuiltinCacheProvider.CacheStats stats = cacheProvider.getStats();
        
        assertThat(stats.getTotal()).isGreaterThanOrEqualTo(2);
    }

    @Test
    @DisplayName("Should handle concurrent access safely")
    void shouldHandleConcurrentAccess() throws InterruptedException {
        int threadCount = 5;
        int operationsPerThread = 50;
        Thread[] threads = new Thread[threadCount];

        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            threads[i] = new Thread(() -> {
                for (int j = 0; j < operationsPerThread; j++) {
                    String key = "thread-" + threadId + "-key-" + j;
                    cacheProvider.set(key, "value-" + j, null);
                    cacheProvider.get(key, String.class);
                }
            });
            threads[i].start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        List<String> keys = cacheProvider.keys("thread-*");
        assertThat(keys).hasSizeGreaterThanOrEqualTo((int)(threadCount * operationsPerThread * 0.9));
    }
}
