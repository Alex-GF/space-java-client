package io.github.isagroup.space.springboot.client;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("space")
public class SpaceClientProperties {

    private static final String DEFAULT_BASE_PATH = "api/v1";

    private static final int DEFAULT_SPACE_SERVER_PORT = 5403;

    private Integer port = DEFAULT_SPACE_SERVER_PORT;

    private String basePath = DEFAULT_BASE_PATH;

    private String apiKey;

    public String getBasePath() {
        return basePath;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUrl() {
        return "http://localhost:" + port + "/" + basePath;
    }
}
