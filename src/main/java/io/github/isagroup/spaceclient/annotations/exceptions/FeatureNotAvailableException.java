package io.github.isagroup.spaceclient.annotations.exceptions;

/**
 * Exception thrown when a method requiring feature access is invoked but the feature is not available
 */
public class FeatureNotAvailableException extends RuntimeException {
    
    private final String featureId;
    private final String userId;
    
    public FeatureNotAvailableException(String featureId, String userId) {
        super(String.format("Feature '%s' is not available for user '%s'", featureId, userId));
        this.featureId = featureId;
        this.userId = userId;
    }
    
    public FeatureNotAvailableException(String featureId, String userId, String message) {
        super(message);
        this.featureId = featureId;
        this.userId = userId;
    }
    
    public FeatureNotAvailableException(String featureId, String userId, String message, Throwable cause) {
        super(message, cause);
        this.featureId = featureId;
        this.userId = userId;
    }
    
    public FeatureNotAvailableException(String featureId, String userId, Throwable cause) {
        super(String.format("Feature '%s' is not available for user '%s'", featureId, userId), cause);
        this.featureId = featureId;
        this.userId = userId;
    }
    
    public String getFeatureId() {
        return featureId;
    }
    
    public String getUserId() {
        return userId;
    }
}
