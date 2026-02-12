package io.github.isagroup.spaceclient.annotations.config;

import io.github.isagroup.spaceclient.SpaceClient;
import io.github.isagroup.spaceclient.SpaceClientFactory;
import io.github.isagroup.spaceclient.types.SpaceConnectionOptions;
import io.github.isagroup.spaceclient.annotations.exceptions.SpaceClientConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Singleton manager for SpaceClient used by feature validation annotations.
 * Initializes the client from environment variables on first use.
 */
public class SpaceClientManager {
    private static final Logger logger = LoggerFactory.getLogger(SpaceClientManager.class);
    private static SpaceClient instance;
    private static Boolean initialized = false;
    private static Exception initializationError;
    
    private SpaceClientManager() {
        // Private constructor for singleton
    }
    
    /**
     * Gets or initializes the SpaceClient instance
     * 
     * @return the SpaceClient instance
     * @throws SpaceClientConfigurationException if the client cannot be initialized
     */
    public static synchronized SpaceClient getInstance() {
        if (initialized) {
            if (initializationError != null) {
                throw new SpaceClientConfigurationException(
                    "SpaceClient initialization failed previously", 
                    initializationError
                );
            }
            return instance;
        }
        
        initialized = true;
        
        try {
            String spaceUrl = System.getenv("space.client.url");
            String spaceApiKey = System.getenv("space.client.api-key");
            
            // Also try with underscores and hyphens variants
            if (spaceUrl == null || spaceUrl.isEmpty()) {
                spaceUrl = System.getenv("SPACE_CLIENT_URL");
            }
            if (spaceApiKey == null || spaceApiKey.isEmpty()) {
                spaceApiKey = System.getenv("SPACE_CLIENT_API_KEY");
            }
            
            if (spaceUrl == null || spaceUrl.isEmpty()) {
                throw new SpaceClientConfigurationException(
                    "SPACE client URL not configured. Set environment variable 'space.client.url' or 'SPACE_CLIENT_URL'"
                );
            }
            if (spaceApiKey == null || spaceApiKey.isEmpty()) {
                throw new SpaceClientConfigurationException(
                    "SPACE API key not configured. Set environment variable 'space.client.api-key' or 'SPACE_CLIENT_API_KEY'"
                );
            }
            
            logger.info("Initializing SPACE client from environment variables (URL: {})", spaceUrl);
            
            SpaceConnectionOptions options = new SpaceConnectionOptions(spaceUrl, spaceApiKey);
            instance = new SpaceClient(options);
            
            logger.info("SPACE client initialized successfully");
            return instance;
            
        } catch (Exception e) {
            initializationError = e;
            logger.error("Failed to initialize SPACE client", e);
            throw new SpaceClientConfigurationException("Failed to initialize SPACE client: " + e.getMessage(), e);
        }
    }
    
    /**
     * Closes the SpaceClient instance if it exists
     */
    public static synchronized void close() {
        if (instance != null) {
            try {
                instance.close();
                logger.info("SPACE client closed");
            } catch (Exception e) {
                logger.warn("Error closing SPACE client", e);
            } finally {
                instance = null;
                initialized = false;
                initializationError = null;
            }
        }
    }
    
    /**
     * Resets the manager state (useful for testing)
     */
    public static synchronized void reset() {
        close();
    }
}
