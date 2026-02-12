package io.github.pgmarc.space;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.time.Duration;

import org.junit.jupiter.api.Test;

import okhttp3.HttpUrl;

class SpaceClientTest {

    @Test
    void givenRequiredParametersShouldCreateClient() {

        String host = "example.com";
        String apiKey = "prueba";

        SpaceClient client = SpaceClient.builder(host, apiKey).build();

        HttpUrl url = HttpUrl.parse("http://example.com:5403/api/v1");

        assertThat(client).hasFieldOrPropertyWithValue("baseUrl", url);
    }

    @Test
    void givenOptionalParametersShouldCreate() {

        String host = "example.com";
        String alternativePath = "foo/bar";
        int port = 8080;

        Duration readTimeout = Duration.ofMillis(500);
        Duration writeTimeout = Duration.ofMillis(1500);

        SpaceClient.Builder builder = SpaceClient.builder(host, "prueba")
                .withPort(port)
                .withPath(alternativePath)
                .withReadTimeout(readTimeout)
                .withWriteTimeout(writeTimeout);

        assertDoesNotThrow(builder::build);

    }

    @Test
    void givenNullPathShouldCreateClient() {

        SpaceClient.Builder builder = SpaceClient.builder("example.com", "prueba");

        assertDoesNotThrow(() -> builder.withPath(null));
    }

    @Test
    void givenNullHostShouldThrow() {

        SpaceClient.Builder spaceClientBuilder = SpaceClient.builder(null, "prueba");

        assertThatExceptionOfType(NullPointerException.class)
                .isThrownBy(spaceClientBuilder::build)
                .withMessage("host must not be null");
    }

    @Test
    void givenBlankHostShouldThrow() {

        SpaceClient.Builder spaceClientBuilder = SpaceClient.builder("", "preuba");

        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(spaceClientBuilder::build)
                .withMessage("host must not be blank");
    }

    @Test
    void givenNullApiKeyShouldThrow() {

        SpaceClient.Builder spaceClientBuilder = SpaceClient.builder("example.com", null);

        assertThatExceptionOfType(NullPointerException.class)
                .isThrownBy(spaceClientBuilder::build)
                .withMessage("api key must not be null");
    }

    @Test
    void givenBlankApiKeyShouldThrow() {

        SpaceClient.Builder spaceClientBuilder = SpaceClient.builder("example.com", "");

        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(spaceClientBuilder::build)
                .withMessage("api key must not be blank");
    }

}
