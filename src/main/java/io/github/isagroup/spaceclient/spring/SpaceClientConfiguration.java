package io.github.isagroup.spaceclient.spring;

import io.github.isagroup.spaceclient.SpaceClient;
import io.github.isagroup.spaceclient.SpaceClientFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Spring Configuration for SPACE Java Client integration.
 * 
 * This configuration class provides a Spring Bean for SpaceClient that can be injected
 * into other services and components throughout the application.
 * 
 * The URL and API key are read from Spring properties with the following precedence:
 * 1. Environment variables: SPACE_CLIENT_URL and SPACE_CLIENT_API_KEY
 * 2. Application properties: space.client.url and space.client.api-key
 * 3. Default values (if provided)
 * 
 * Example application.properties:
 * <pre>
 * space.client.url=https://space.example.com
 * space.client.api-key=your-api-key-here
 * space.client.timeout=10000
 * </pre>
 * 
 * Usage:
 * <pre>
 * &#64;Service
 * public class MyService {
 *     
 *     &#64;Autowired
 *     private SpaceClient spaceClient;
 *     
 *     public void myMethod() {
 *         var contracts = spaceClient.contracts.list();
 *         // ...
 *     }
 * }
 * </pre>
 */
@Configuration
public class SpaceClientConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(SpaceClientConfiguration.class);

    @Value("${space.client.url:}")
    private String spaceUrl;

    @Value("${space.client.api-key:}")
    private String spaceApiKey;

    @Value("${space.client.timeout:10000}")
    private int timeout;

    /**
     * Creates and configures a SpaceClient Bean.
     * 
     * The client is initialized with the URL and API key from Spring properties.
     * If either URL or API key is missing, an IllegalArgumentException will be thrown
     * at runtime when the bean is instantiated.
     * 
     * @return A SpaceClient instance configured with Spring properties
     * @throws IllegalArgumentException if URL or API key is empty
     */
    @Bean
    public SpaceClient spaceClient() {
        logger.info("Creating SpaceClient bean with URL: {}", spaceUrl);
        
        if (spaceUrl == null || spaceUrl.isEmpty()) {
            throw new IllegalArgumentException(
                "SPACE client URL is required. Set 'space.client.url' property or 'SPACE_CLIENT_URL' environment variable"
            );
        }
        
        if (spaceApiKey == null || spaceApiKey.isEmpty()) {
            throw new IllegalArgumentException(
                "SPACE client API key is required. Set 'space.client.api-key' property or 'SPACE_CLIENT_API_KEY' environment variable"
            );
        }
        
        return SpaceClientFactory.connect(spaceUrl, spaceApiKey, timeout);
    }
}
