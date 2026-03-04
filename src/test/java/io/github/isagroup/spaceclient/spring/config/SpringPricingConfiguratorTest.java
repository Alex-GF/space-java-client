package io.github.isagroup.spaceclient.spring.config;

import io.github.isagroup.spaceclient.SpaceClient;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;

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
    
    @Mock
    private ProceedingJoinPoint joinPoint;
    
    @Mock
    private SpaceClient mockSpaceClient;
    
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
    @DisplayName("Should throw exception when SpaceClient is not injected")
    void shouldThrowExceptionWhenSpaceClientNotInjected() {
        assertThatThrownBy(configurator::getSpaceClient)
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("SpaceClient is not configured as a Spring bean")
            .hasMessageContaining("@Bean");
    }
    
    @Test
    @DisplayName("Should return injected SpaceClient")
    void shouldReturnInjectedSpaceClient() throws Exception {
        // Use reflection to inject the mock SpaceClient
        Field spaceClientField = SpringPricingConfigurator.class.getDeclaredField("spaceClient");
        spaceClientField.setAccessible(true);
        spaceClientField.set(configurator, mockSpaceClient);
        
        SpaceClient result = configurator.getSpaceClient();
        
        assertThat(result).isSameAs(mockSpaceClient);
    }
    
    @Test
    @DisplayName("Should return same SpaceClient instance on multiple calls")
    void shouldReturnSameSpaceClientInstance() throws Exception {
        // Use reflection to inject the mock SpaceClient
        Field spaceClientField = SpringPricingConfigurator.class.getDeclaredField("spaceClient");
        spaceClientField.setAccessible(true);
        spaceClientField.set(configurator, mockSpaceClient);
        
        SpaceClient client1 = configurator.getSpaceClient();
        SpaceClient client2 = configurator.getSpaceClient();
        
        assertThat(client1).isSameAs(client2);
    }
}
