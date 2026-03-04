package io.github.isagroup.spaceclient.spring.config;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DefaultSpringPricingConfigurator Tests")
class DefaultSpringPricingConfiguratorTest {
    
    @Mock
    private ProceedingJoinPoint joinPoint;
    
    @Mock
    private MethodSignature methodSignature;
    
    private DefaultSpringPricingConfigurator configurator;
    
    @BeforeEach
    void setUp() {
        configurator = new DefaultSpringPricingConfigurator();
        when(joinPoint.getSignature()).thenReturn(methodSignature);
    }
    
    @Test
    @DisplayName("Should resolve userId from method parameter")
    void shouldResolveUserIdFromMethodParameter() {
        String[] paramNames = {"userId", "action"};
        Object[] argValues = {"user123", "export"};
        
        when(methodSignature.getParameterNames()).thenReturn(paramNames);
        when(joinPoint.getArgs()).thenReturn(argValues);
        
        String userId = configurator.resolveUserId(joinPoint);
        
        assertThat(userId).isEqualTo("user123");
    }
    
    @Test
    @DisplayName("Should resolve userId from parameter with different case")
    void shouldResolveUserIdFromParameterWithDifferentCase() {
        String[] paramNames = {"UserId", "action"};
        Object[] argValues = {"user456", "export"};
        
        when(methodSignature.getParameterNames()).thenReturn(paramNames);
        when(joinPoint.getArgs()).thenReturn(argValues);
        
        String userId = configurator.resolveUserId(joinPoint);
        
        assertThat(userId).isEqualTo("user456");
    }
    
    @Test
    @DisplayName("Should return null when userId parameter is null")
    void shouldReturnNullWhenUserIdParameterIsNull() {
        String[] paramNames = {"userId", "action"};
        Object[] argValues = {null, "export"};
        
        when(methodSignature.getParameterNames()).thenReturn(paramNames);
        when(joinPoint.getArgs()).thenReturn(argValues);
        
        String userId = configurator.resolveUserId(joinPoint);
        
        assertThat(userId).isNull();
    }
    
    @Test
    @DisplayName("Should return null when userId parameter not found and no String parameters exist")
    void shouldReturnNullWhenUserIdParameterNotFound() {
        String[] paramNames = {"id", "action"};
        Object[] argValues = {123, true};  // No String parameters
        
        when(methodSignature.getParameterNames()).thenReturn(paramNames);
        when(joinPoint.getArgs()).thenReturn(argValues);
        
        String userId = configurator.resolveUserId(joinPoint);
        
        assertThat(userId).isNull();
    }
    
    @Test
    @DisplayName("Should use first String parameter as fallback")
    void shouldUseFirstStringParameterAsFallback() {
        String[] paramNames = {"id", "action"};
        Object[] argValues = {"123", "export"};
        
        when(methodSignature.getParameterNames()).thenReturn(paramNames);
        when(joinPoint.getArgs()).thenReturn(argValues);
        
        String userId = configurator.resolveUserId(joinPoint);
        
        assertThat(userId).isEqualTo("123");
    }
    
    @Test
    @DisplayName("Should handle exception gracefully")
    void shouldHandleExceptionGracefully() {
        when(methodSignature.getParameterNames()).thenThrow(new RuntimeException("Test exception"));
        
        String userId = configurator.resolveUserId(joinPoint);
        
        assertThat(userId).isNull();
    }
    
    @Test
    @DisplayName("Should resolve userId from user_id parameter")
    void shouldResolveUserIdFromUnderscoreParameter() {
        String[] paramNames = {"user_id", "action"};
        Object[] argValues = {"user789", "export"};
        
        when(methodSignature.getParameterNames()).thenReturn(paramNames);
        when(joinPoint.getArgs()).thenReturn(argValues);
        
        String userId = configurator.resolveUserId(joinPoint);
        
        assertThat(userId).isEqualTo("user789");
    }
    
    @Test
    @DisplayName("Should return null when no parameters")
    void shouldReturnNullWhenNoParameters() {
        String[] paramNames = {};
        Object[] argValues = {};
        
        when(methodSignature.getParameterNames()).thenReturn(paramNames);
        when(joinPoint.getArgs()).thenReturn(argValues);
        
        String userId = configurator.resolveUserId(joinPoint);
        
        assertThat(userId).isNull();
    }
    
    @Test
    @DisplayName("Should convert userId parameter to String")
    void shouldConvertUserIdParameterToString() {
        String[] paramNames = {"userId", "action"};
        Object[] argValues = {12345L, "export"};
        
        when(methodSignature.getParameterNames()).thenReturn(paramNames);
        when(joinPoint.getArgs()).thenReturn(argValues);
        
        String userId = configurator.resolveUserId(joinPoint);
        
        assertThat(userId).isEqualTo("12345");
    }
}
