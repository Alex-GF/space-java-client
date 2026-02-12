package io.github.isagroup.spaceclient.annotations.exceptions;

/**
 * Exception thrown when the SPACE client cannot be initialized due to missing configuration
 */
public class SpaceClientConfigurationException extends RuntimeException {
    
    public SpaceClientConfigurationException(String message) {
        super(message);
    }
    
    public SpaceClientConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
