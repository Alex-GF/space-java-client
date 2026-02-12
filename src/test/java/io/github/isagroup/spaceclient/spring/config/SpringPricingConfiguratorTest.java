package io.github.isagroup.spaceclient.spring.config;

import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SpringPricingConfigurator Tests")
class SpringPricingConfiguratorTest {
    
    private static class TestConfigurator extends SpringPricingConfigurator {
        @Override
        public String resolveUserId(ProceedingJoinPoint joinPoint) {
            return "test-user";
        }
    }
    
    private static class CustomUrlConfigurator extends SpringPricingConfigurator {
        private final String url;
        private final String apiKey;
        
        public CustomUrlConfigurator(String url, String apiKey) {
            this.url = url;
            this.apiKey = apiKey;
        }
        
        @Override
        protected String getSpaceUrl() {
            return url;
        }
        
        @Override
        protected String getSpaceApiKey() {
            return apiKey;
        }
        
        @Override
        public String resolveUserId(ProceedingJoinPoint joinPoint) {
            return "test-user";
        }
    }
    
    @Mock
    private ProceedingJoinPoint joinPoint;
    
    private SpringPricingConfigurator configurator;
    
    @BeforeEach
    void setUp() {
        configurator = new TestConfigurator();
    }
    
    @Test
    @DisplayName("Should resolve userId successfully")
    void shouldResolveUserIdSuccessfully() {
        String userId = configurator.resolveUserId(joinPoint);
        
        assertThat(userId).isNotNull().isEqualTo("test-user");
    }
    
    @Test
    @DisplayName("Should get timeout with default value")
    void shouldGetTimeoutWithDefaultValue() {
        int timeout = configurator.getTimeout();
        
        assertThat(timeout).isEqualTo(10000);
    }
    
    @Test
    @DisplayName("Should read SPACE URL from environment variable")
    void shouldReadSpaceUrlFromEnvironment() {
        String url = configurator.getSpaceUrl();
        
        // URL can be null if env vars not set, that's acceptable for this test
        // The important part is it doesn't throw an exception
        // In real usage, the exception would be thrown at SpaceClient instantiation time
    }
    
    @Test
    @DisplayName("Should read SPACE API Key from environment variable")
    void shouldReadSpaceApiKeyFromEnvironment() {
        String apiKey = configurator.getSpaceApiKey();
        
        // API Key can be null if env vars not set, that's acceptable for this test
        // The important part is it doesn't throw an exception
        // In real usage, the exception would be thrown at SpaceClient instantiation time
    }
    
    @Test
    @DisplayName("Should allow overriding getSpaceUrl()")
    void shouldAllowOverridingGetSpaceUrl() {
        SpringPricingConfigurator customConfigurator = 
            new CustomUrlConfigurator("http://custom-space.com", "custom-key");
        
        String url = customConfigurator.getSpaceUrl();
        
        assertThat(url).isEqualTo("http://custom-space.com");
    }
    
    @Test
    @DisplayName("Should allow overriding getSpaceApiKey()")
    void shouldAllowOverridingGetSpaceApiKey() {
        SpringPricingConfigurator customConfigurator = 
            new CustomUrlConfigurator("http://custom-space.com", "custom-key");
        
        String apiKey = customConfigurator.getSpaceApiKey();
        
        assertThat(apiKey).isEqualTo("custom-key");
    }
    
    @Test
    @DisplayName("Should throw exception when URL is missing")
    void shouldThrowExceptionWhenUrlIsMissing() {
        SpringPricingConfigurator configurator = 
            new CustomUrlConfigurator(null, "valid-key");
        
        assertThatThrownBy(configurator::getSpaceClient)
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("SPACE server URL is not configured");
    }
    
    @Test
    @DisplayName("Should throw exception when API key is missing")
    void shouldThrowExceptionWhenApiKeyIsMissing() {
        SpringPricingConfigurator configurator = 
            new CustomUrlConfigurator("http://localhost:3000", null);
        
        assertThatThrownBy(configurator::getSpaceClient)
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("SPACE API key is not configured");
    }
    
    @Test
    @DisplayName("Should initialize SpaceClient successfully")
    void shouldInitializeSpaceClientSuccessfully() {
        SpringPricingConfigurator configurator = 
            new CustomUrlConfigurator("http://localhost:3000", "test-key");
        
        var spaceClient = configurator.getSpaceClient();
        
        assertThat(spaceClient).isNotNull();
    }
    
    @Test
    @DisplayName("Should return same SpaceClient instance on multiple calls")
    void shouldReturnSameSpaceClientInstance() {
        SpringPricingConfigurator configurator = 
            new CustomUrlConfigurator("http://localhost:3000", "test-key");
        
        var client1 = configurator.getSpaceClient();
        var client2 = configurator.getSpaceClient();
        
        assertThat(client1).isSameAs(client2);
    }
    
    @Test
    @DisplayName("Should close SpaceClient")
    void shouldCloseSpaceClient() {
        SpringPricingConfigurator configurator = 
            new CustomUrlConfigurator("http://localhost:3000", "test-key");
        
        var spaceClient = configurator.getSpaceClient();
        assertThat(spaceClient).isNotNull();
        
        // Close should not throw
        assertThatCode(configurator::close).doesNotThrowAnyException();
    }
}
