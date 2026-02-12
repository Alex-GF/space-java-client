package io.github.isagroup.spaceclient.spring.config;

import io.github.isagroup.spaceclient.SpaceClient;
import io.github.isagroup.spaceclient.types.SpaceConnectionOptions;
import org.aspectj.lang.ProceedingJoinPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class for Spring pricing integration configuration.
 * 
 * This class defines how the SPACE client is initialized and how user context is resolved
 * within a Spring application. Implementations should provide:
 * 
 * 1. Connection details (URL and API key)
 * 2. User context resolution strategy
 * 3. Optional consumption tracking
 * 
 * Example implementation:
 * <pre>
 * &#64;Component
 * public class MyPricingConfigurator extends SpringPricingConfigurator {
 *     
 *     &#64;Override
 *     protected String getSpaceUrl() {
 *         return System.getenv("SPACE_SERVER_URL");
 *     }
 *     
 *     &#64;Override
 *     protected String getSpaceApiKey() {
 *         return System.getenv("SPACE_API_KEY");
 *     }
 *     
 *     &#64;Override
 *     public String resolveUserId(ProceedingJoinPoint joinPoint) {
 *         // Get user from Spring Security context
 *         Authentication auth = SecurityContextHolder.getContext().getAuthentication();
 *         return auth != null ? auth.getName() : null;
 *     }
 * }
 * </pre>
 */
public abstract class SpringPricingConfigurator {
    private static final Logger logger = LoggerFactory.getLogger(SpringPricingConfigurator.class);
    
    private SpaceClient spaceClient;
    private boolean initialized = false;
    
    /**
     * Gets the SPACE server URL.
     * Can be overridden to read from configuration files, environment variables, etc.
     * 
     * @return the SPACE server URL
     */
    protected String getSpaceUrl() {
        String url = System.getenv("space.client.url");
        if (url == null || url.isEmpty()) {
            url = System.getenv("SPACE_CLIENT_URL");
        }
        return url;
    }
    
    /**
     * Gets the SPACE API key.
     * Can be overridden to read from secure configuration, environment variables, etc.
     * 
     * @return the SPACE API key
     */
    protected String getSpaceApiKey() {
        String key = System.getenv("space.client.api-key");
        if (key == null || key.isEmpty()) {
            key = System.getenv("SPACE_CLIENT_API_KEY");
        }
        return key;
    }
    
    /**
     * Gets the connection timeout in milliseconds.
     * Override to customize timeout settings.
     * 
     * @return timeout in milliseconds (default: 10000)
     */
    protected int getTimeout() {
        return 10000;
    }
    
    /**
     * Resolves the user ID from the current request/context.
     * This method must be implemented by subclasses to extract the user ID
     * from the application context (e.g., Spring Security, request headers, etc.)
     * 
     * @param joinPoint the AspectJ join point containing method information and arguments
     * @return the resolved user ID, or null if user is not authenticated
     */
    public abstract String resolveUserId(ProceedingJoinPoint joinPoint);
    
    /**
     * Determines whether feature evaluation should be performed for a given request.
     * This method can be overridden to implement custom logic for skipping feature evaluation
     * in certain contexts (e.g., admin users, testing environments, specific endpoints).
     * 
     * @param joinPoint the AspectJ join point containing method information and arguments
     * @return true if feature evaluation should be performed, false to skip evaluation
     */
    public boolean shouldEvaluateFeature(ProceedingJoinPoint joinPoint) {
        return true;
    }
    
    /**
     * Gets the SpaceClient instance, initializing it if needed.
     * 
     * @return the SpaceClient instance
     * @throws IllegalStateException if configuration is invalid
     */
    public synchronized SpaceClient getSpaceClient() {
        if (!initialized) {
            initializeSpaceClient();
            initialized = true;
        }
        
        if (spaceClient == null) {
            throw new IllegalStateException("SpaceClient is not properly initialized");
        }
        
        return spaceClient;
    }
    
    /**
     * Initializes the SpaceClient with connection options.
     */
    private void initializeSpaceClient() {
        try {
            String url = getSpaceUrl();
            String apiKey = getSpaceApiKey();
            
            if (url == null || url.isEmpty()) {
                throw new IllegalStateException(
                    "SPACE server URL is not configured. " +
                    "Override getSpaceUrl() or set environment variables: " +
                    "space.client.url or SPACE_CLIENT_URL"
                );
            }
            
            if (apiKey == null || apiKey.isEmpty()) {
                throw new IllegalStateException(
                    "SPACE API key is not configured. " +
                    "Override getSpaceApiKey() or set environment variables: " +
                    "space.client.api-key or SPACE_CLIENT_API_KEY"
                );
            }
            
            logger.info("Initializing SPACE client (URL: {})", url);
            
            SpaceConnectionOptions options = new SpaceConnectionOptions(url, apiKey);
            options.setTimeout(getTimeout());
            
            this.spaceClient = new io.github.isagroup.spaceclient.SpaceClient(options);
            
            logger.info("SPACE client initialized successfully");
            
        } catch (Exception e) {
            logger.error("Failed to initialize SPACE client", e);
            throw new IllegalStateException("Cannot initialize SPACE client: " + e.getMessage(), e);
        }
    }
    
    /**
     * Closes the SpaceClient and releases resources.
     * Call this during application shutdown.
     */
    public synchronized void close() {
        if (spaceClient != null) {
            try {
                spaceClient.close();
                logger.info("SPACE client closed");
            } catch (Exception e) {
                logger.warn("Error closing SPACE client", e);
            } finally {
                spaceClient = null;
                initialized = false;
            }
        }
    }
}
