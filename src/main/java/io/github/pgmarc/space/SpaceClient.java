package io.github.pgmarc.space;

import java.time.Duration;
import java.util.Objects;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;

public final class SpaceClient {

    private final OkHttpClient httpClient;
    private final HttpUrl baseUrl;
    private final String apiKey;

    private ContractsEndpoint contracts;
    private FeaturesEndpoint features;

    private SpaceClient(OkHttpClient httpClient, HttpUrl baseUrl, String apiKey) {
        this.httpClient = httpClient;
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
    }

    public ContractsEndpoint contracts() {
        if (contracts == null) {
            contracts = new ContractsEndpoint(httpClient, baseUrl, apiKey);
        }
        return contracts;
    }

    public FeaturesEndpoint features() {
        if (features == null) {
            features = new FeaturesEndpoint(httpClient, baseUrl, apiKey);
        }
        return features;
    }

    public static Builder builder(String host, String apiKey) {
        return new Builder(host, apiKey);
    }

    public static final class Builder {

        private static final String DEFAULT_SCHEME = "http";
        private static final int DEFAULT_PORT = 5403;
        private static final String DEFAULT_API_VERSION = "api/v1";

        private final String apiKey;
        private final String host;
        private int port = DEFAULT_PORT;
        private String prefixPath = DEFAULT_API_VERSION;
        private Duration readTimeout;
        private Duration writeTimeout;

        private Builder(String host, String apiKey) {
            this.apiKey = apiKey;
            this.host = host;
        }

        public Builder withPort(int port) {
            this.port = port;
            return this;
        }

        public Builder withPath(String prefixPath) {
            if (prefixPath != null) {
                this.prefixPath = prefixPath;
            }
            return this;
        }

        public Builder withReadTimeout(Duration duration) {
            if (duration != null) {
                this.readTimeout = duration;
            }
            return this;
        }

        public Builder withWriteTimeout(Duration duration) {
            if (duration != null) {
                this.writeTimeout = duration;
            }
            return this;
        }

        public SpaceClient build() {
            Objects.requireNonNull(this.host, "host must not be null");
            Objects.requireNonNull(this.apiKey, "api key must not be null");
            if (apiKey.isBlank()) {
                throw new  IllegalArgumentException("api key must not be blank");
            }
            if (host.isBlank()) {
                throw new IllegalArgumentException("host must not be blank");
            }
            OkHttpClient.Builder httpClientBuilder = new OkHttpClient().newBuilder();
            
            if (readTimeout != null) {
                httpClientBuilder.readTimeout(readTimeout);
            }

            if (writeTimeout != null) {
                httpClientBuilder.writeTimeout(writeTimeout);
            }
            
            HttpUrl baseUrl = new HttpUrl.Builder()
                    .scheme(DEFAULT_SCHEME)
                    .host(host)
                    .port(port)
                    .addPathSegments(prefixPath).build();
            return new SpaceClient(httpClientBuilder.build(), baseUrl, this.apiKey);
        }

    }

}
