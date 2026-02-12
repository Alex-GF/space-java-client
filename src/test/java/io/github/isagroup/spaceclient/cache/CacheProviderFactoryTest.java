package io.github.isagroup.spaceclient.cache;

import io.github.isagroup.spaceclient.types.CacheOptions;
import io.github.isagroup.spaceclient.types.CacheOptions.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("CacheProviderFactory Tests")
class CacheProviderFactoryTest {

    @Test
    @DisplayName("Should create builtin cache provider")
    void shouldCreateBuiltinCacheProvider() {
        CacheOptions options = new CacheOptions(true, CacheType.BUILTIN, 300);

        CacheProvider provider = CacheProviderFactory.create(options);

        assertThat(provider).isInstanceOf(BuiltinCacheProvider.class);
        provider.close();
    }

    @Test
    @DisplayName("Should throw exception when Redis config is missing")
    void shouldThrowExceptionWhenRedisConfigIsMissing() {
        CacheOptions options = new CacheOptions(true, CacheType.REDIS, 300);

        assertThatThrownBy(() -> CacheProviderFactory.create(options))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Redis configuration is required");
    }

    @Test
    @DisplayName("Should validate cache options successfully")
    void shouldValidateCacheOptionsSuccessfully() {
        CacheOptions options = new CacheOptions(true, CacheType.BUILTIN, 300);

        assertThatCode(() -> CacheProviderFactory.validate(options))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should throw exception for invalid TTL")
    void shouldThrowExceptionForInvalidTTL() {
        CacheOptions options = new CacheOptions(true, CacheType.BUILTIN, -1);

        assertThatThrownBy(() -> CacheProviderFactory.validate(options))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("TTL must be a positive number");
    }
}
