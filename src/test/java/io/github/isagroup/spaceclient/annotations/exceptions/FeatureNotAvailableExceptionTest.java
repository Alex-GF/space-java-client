package io.github.isagroup.spaceclient.annotations.exceptions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("FeatureNotAvailableException Tests")
class FeatureNotAvailableExceptionTest {
    
    @Test
    @DisplayName("Should create exception with feature ID and user ID")
    void shouldCreateExceptionWithFeatureIdAndUserId() {
        FeatureNotAvailableException exception = 
            new FeatureNotAvailableException("premium-export", "user123");
        
        assertThat(exception)
            .hasMessage("Feature 'premium-export' is not available for user 'user123'")
            .hasNoCause();
        assertThat(exception.getFeatureId()).isEqualTo("premium-export");
        assertThat(exception.getUserId()).isEqualTo("user123");
    }
    
    @Test
    @DisplayName("Should create exception with custom message")
    void shouldCreateExceptionWithCustomMessage() {
        String customMessage = "Custom error occurred";
        FeatureNotAvailableException exception = 
            new FeatureNotAvailableException("feature-id", "user123", customMessage);
        
        assertThat(exception)
            .hasMessage(customMessage)
            .hasNoCause();
        assertThat(exception.getFeatureId()).isEqualTo("feature-id");
        assertThat(exception.getUserId()).isEqualTo("user123");
    }
    
    @Test
    @DisplayName("Should create exception with cause")
    void shouldCreateExceptionWithCause() {
        RuntimeException cause = new RuntimeException("Root cause");
        FeatureNotAvailableException exception = 
            new FeatureNotAvailableException("feature-id", "user123", cause);
        
        assertThat(exception)
            .hasMessage("Feature 'feature-id' is not available for user 'user123'")
            .hasCause(cause);
        assertThat(exception.getFeatureId()).isEqualTo("feature-id");
        assertThat(exception.getUserId()).isEqualTo("user123");
    }
    
    @Test
    @DisplayName("Should be a RuntimeException")
    void shouldBeARuntimeException() {
        FeatureNotAvailableException exception = 
            new FeatureNotAvailableException("feature-id", "user123");
        
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }
}
