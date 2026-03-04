package io.github.isagroup.spaceclient;

import io.github.isagroup.spaceclient.types.SpaceConnectionOptions;

/**
 * Utility class for creating SpaceClient instances
 */
public class SpaceClientFactory {

    /**
     * Connect to Space platform with the provided options
     *
     * @param options Connection options including URL and API key
     * @return A new SpaceClient instance
     * @throws IllegalArgumentException if options are invalid
     */
    public static SpaceClient connect(SpaceConnectionOptions options) {
        if (options == null) {
            throw new IllegalArgumentException("Options cannot be null");
        }

        if (options.getUrl() == null || options.getUrl().isEmpty()) {
            throw new IllegalArgumentException("URL is required to connect to Space");
        }

        if (options.getApiKey() == null || options.getApiKey().isEmpty()) {
            throw new IllegalArgumentException("API key is required to connect to Space");
        }

        if (options.getTimeout() != null && options.getTimeout() <= 0) {
            throw new IllegalArgumentException("Invalid timeout value. It must be a positive number");
        }

        if (!options.getUrl().startsWith("http://") && !options.getUrl().startsWith("https://")) {
            throw new IllegalArgumentException("Invalid URL. It must start with 'http://' or 'https://'");
        }

        return new SpaceClient(options);
    }

    /**
     * Connect to Space platform with URL and API key
     *
     * @param url The Space server URL
     * @param apiKey The API key for authentication
     * @return A new SpaceClient instance
     */
    public static SpaceClient connect(String url, String apiKey) {
        SpaceConnectionOptions options = new SpaceConnectionOptions(url, apiKey);
        return connect(options);
    }

    /**
     * Connect to Space platform with URL, API key, and timeout
     *
     * @param url The Space server URL
     * @param apiKey The API key for authentication
     * @param timeout Connection timeout in milliseconds
     * @return A new SpaceClient instance
     */
    public static SpaceClient connect(String url, String apiKey, int timeout) {
        SpaceConnectionOptions options = new SpaceConnectionOptions(url, apiKey, timeout);
        return connect(options);
    }
}
