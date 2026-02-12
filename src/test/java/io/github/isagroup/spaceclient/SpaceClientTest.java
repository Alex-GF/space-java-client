package io.github.isagroup.spaceclient;

import io.github.isagroup.spaceclient.types.CacheOptions;
import io.github.isagroup.spaceclient.types.SpaceConnectionOptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("SpaceClient Basic Tests")
class SpaceClientTest {

    @Test
    @DisplayName("Should create client with valid options")
    void shouldCreateClientWithValidOptions() {
        SpaceConnectionOptions options = new SpaceConnectionOptions(
                "http://localhost:8080",
                "test-api-key"
        );

        SpaceClient client = new SpaceClient(options);

        assertThat(client).isNotNull();
        assertThat(client.contracts).isNotNull();
        assertThat(client.features).isNotNull();
        assertThat(client.cache).isNotNull();
        
        client.disconnect();
    }

    @Test
    @DisplayName("Should create client with cache enabled")
    void shouldCreateClientWithCacheEnabled() {
        SpaceConnectionOptions options = new SpaceConnectionOptions(
                "http://localhost:8080",
                "test-api-key"
        );
        options.setCache(new CacheOptions(true, CacheOptions.CacheType.BUILTIN, 300));

        SpaceClient client = new SpaceClient(options);

        assertThat(client.cache).isNotNull();
        assertThat(client.cache.isEnabled()).isTrue();
        
        client.disconnect();
    }

    @Test
    @DisplayName("Should allow creating options (validation happens in factory)")
    void shouldAllowCreatingOptions() {
        // Options class itself doesn't validate, factory does
        SpaceConnectionOptions options1 = new SpaceConnectionOptions(null, "api-key");
        SpaceConnectionOptions options2 = new SpaceConnectionOptions("http://localhost", null);
        
        assertThat(options1).isNotNull();
        assertThat(options2).isNotNull();
    }

    @Test
    @DisplayName("Should create client using factory")
    void shouldCreateClientUsingFactory() {
        SpaceConnectionOptions options = new SpaceConnectionOptions(
                "http://localhost:8080",
                "test-api-key"
        );

        SpaceClient client = SpaceClientFactory.connect(options);

        assertThat(client).isNotNull();
        client.disconnect();
    }

    @Test
    @DisplayName("Should register event listeners")
    void shouldRegisterEventListeners() {
        SpaceConnectionOptions options = new SpaceConnectionOptions(
                "http://localhost:8080",
                "test-api-key"
        );
        SpaceClient client = new SpaceClient(options);

        assertThatCode(() -> {
            client.on("synchronized", (data) -> {
                // Event handler
            });
        }).doesNotThrowAnyException();

        client.disconnect();
    }
}
