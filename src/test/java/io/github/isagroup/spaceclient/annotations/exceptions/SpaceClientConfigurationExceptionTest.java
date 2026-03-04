package io.github.isagroup.spaceclient.annotations.exceptions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("SpaceClientConfigurationException Tests")
class SpaceClientConfigurationExceptionTest {
    
    @Test
    @DisplayName("Should create exception with message")
    void shouldCreateExceptionWithMessage() {
        String message = "SPACE server URL is not configured";
        SpaceClientConfigurationException exception = 
            new SpaceClientConfigurationException(message);
        
        assertThat(exception)
            .hasMessage(message)
            .hasNoCause();
    }
    
    @Test
    @DisplayName("Should create exception with message and cause")
    void shouldCreateExceptionWithMessageAndCause() {
        String message = "Failed to initialize SPACE client";
        RuntimeException cause = new RuntimeException("Connection refused");
        SpaceClientConfigurationException exception = 
            new SpaceClientConfigurationException(message, cause);
        
        assertThat(exception)
            .hasMessage(message)
            .hasCause(cause);
    }
    
    @Test
    @DisplayName("Should be a RuntimeException")
    void shouldBeARuntimeException() {
        SpaceClientConfigurationException exception = 
            new SpaceClientConfigurationException("Error");
        
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }
    
    @Test
    @DisplayName("Should contain helpful error messages")
    void shouldContainHelpfulErrorMessages() {
        SpaceClientConfigurationException exception = 
            new SpaceClientConfigurationException(
                "SPACE server URL not configured. " +
                "Set environment variable 'space.client.url' or 'SPACE_CLIENT_URL'"
            );
        
        assertThat(exception.getMessage())
            .contains("SPACE server URL")
            .contains("space.client.url")
            .contains("SPACE_CLIENT_URL");
    }
}
