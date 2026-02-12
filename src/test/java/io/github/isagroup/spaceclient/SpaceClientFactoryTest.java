package io.github.isagroup.spaceclient;

import io.github.isagroup.spaceclient.types.SpaceConnectionOptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("SpaceClientFactory Tests")
class SpaceClientFactoryTest {

    @Test
    @DisplayName("Should connect with valid options")
    void shouldConnectWithValidOptions() {
        SpaceConnectionOptions options = new SpaceConnectionOptions(
                "http://localhost:8080",
                "test-api-key"
        );

        SpaceClient client = SpaceClientFactory.connect(options);

        assertThat(client).isNotNull();
        client.disconnect();
    }

    @Test
    @DisplayName("Should throw exception for null options")
    void shouldThrowExceptionForNullOptions() {
        assertThatThrownBy(() -> SpaceClientFactory.connect(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Options cannot be null");
    }

    @Test
    @DisplayName("Should throw exception for empty URL")
    void shouldThrowExceptionForEmptyURL() {
        SpaceConnectionOptions options = new SpaceConnectionOptions(
                "",
                "test-api-key"
        );

        assertThatThrownBy(() -> SpaceClientFactory.connect(options))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("URL is required");
    }

    @Test
    @DisplayName("Should throw exception for empty API key")
    void shouldThrowExceptionForEmptyApiKey() {
        SpaceConnectionOptions options = new SpaceConnectionOptions(
                "http://localhost:8080",
                ""
        );

        assertThatThrownBy(() -> SpaceClientFactory.connect(options))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("API key is required");
    }
}
