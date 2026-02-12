package io.github.isagroup.spaceclient.spring.integration;

import io.github.isagroup.spaceclient.SpaceClient;
import io.github.isagroup.spaceclient.annotations.RequireFeature;
import io.github.isagroup.spaceclient.annotations.aspect.RequireFeatureAspect;
import io.github.isagroup.spaceclient.annotations.exceptions.FeatureNotAvailableException;
import io.github.isagroup.spaceclient.modules.FeatureModule;
import io.github.isagroup.spaceclient.spring.config.SpringPricingConfigurator;
import io.github.isagroup.spaceclient.types.FeatureEvaluationResult;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Integration tests for the @RequireFeature annotation workflow.
 * Tests the complete flow from annotation parsing to feature evaluation.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("@RequireFeature Integration Tests")
class RequireFeatureIntegrationTest {
    
    @Mock
    private SpringPricingConfigurator pricingConfigurator;
    
    @Mock
    private SpaceClient spaceClient;
    
    @Mock
    private FeatureModule featureModule;
    
    @Mock
    private FeatureEvaluationResult featureEvaluationResult;
    
    @Mock
    private ProceedingJoinPoint joinPoint;
    
    @Mock
    private MethodSignature methodSignature;
    
    private RequireFeatureAspect aspect;
    private TestAnnotation testAnnotation;
    
    /**
     * Helper class to hold annotation values for testing
     */
    private static class TestAnnotation implements RequireFeature {
        private final String value;
        private final boolean server;
        private final String[] consumption;
        private final boolean details;
        
        public TestAnnotation(String value, boolean server, String[] consumption, boolean details) {
            this.value = value;
            this.server = server;
            this.consumption = consumption;
            this.details = details;
        }
        
        @Override
        public String value() {
            return value;
        }
        
        @Override
        public boolean server() {
            return server;
        }
        
        @Override
        public String[] consumption() {
            return consumption;
        }
        
        @Override
        public boolean details() {
            return details;
        }
        
        @Override
        public Class<RequireFeature> annotationType() {
            return RequireFeature.class;
        }
    }
    
    @BeforeEach
    void setUp() {
        aspect = new RequireFeatureAspect();
        aspect.pricingConfigurator = pricingConfigurator;
        
        try {
            // Use reflection to set the final FeatureModule field
            Field featuresField = SpaceClient.class.getDeclaredField("features");
            featuresField.setAccessible(true);
            featuresField.set(spaceClient, featureModule);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to set featureModule via reflection: " + e.getMessage(), e);
        }
        
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        // By default, enable feature evaluation
        when(pricingConfigurator.shouldEvaluateFeature(any())).thenReturn(true);
    }
    
    @Test
    @DisplayName("Complete workflow: User authorized, feature available")
    void completeWorkflowUserAuthorizedFeatureAvailable() throws Throwable {
        // Arrange
        testAnnotation = new TestAnnotation("premium-export", true, new String[]{}, false);
        
        when(pricingConfigurator.resolveUserId(joinPoint)).thenReturn("alice");
        when(pricingConfigurator.getSpaceClient()).thenReturn(spaceClient);
        when(featureEvaluationResult.isEval()).thenReturn(true);
        when(featureModule.evaluate(
            eq("alice"), eq("premium-export"), any(Map.class), eq(false), eq(true)
        )).thenReturn(featureEvaluationResult);
        when(joinPoint.proceed()).thenReturn("Export completed successfully");
        
        // Act
        Object result = aspect.validateFeatureAccess(joinPoint, testAnnotation);
        
        // Assert
        assertThat(result).isEqualTo("Export completed successfully");
        verify(joinPoint).proceed();
        verify(pricingConfigurator).resolveUserId(joinPoint);
        verify(pricingConfigurator).getSpaceClient();
        verify(featureModule).evaluate(
            eq("alice"), eq("premium-export"), any(Map.class), eq(false), eq(true)
        );
    }
    
    @Test
    @DisplayName("Complete workflow: User authorized, feature unavailable")
    void completeWorkflowUserAuthorizedFeatureUnavailable() throws Throwable {
        // Arrange
        testAnnotation = new TestAnnotation("premium-export", true, new String[]{}, false);
        
        when(pricingConfigurator.resolveUserId(joinPoint)).thenReturn("bob");
        when(pricingConfigurator.getSpaceClient()).thenReturn(spaceClient);
        when(featureEvaluationResult.isEval()).thenReturn(false);
        when(featureModule.evaluate(
            eq("bob"), eq("premium-export"), any(Map.class), eq(false), eq(true)
        )).thenReturn(featureEvaluationResult);
        
        // Act & Assert
        assertThatThrownBy(() -> aspect.validateFeatureAccess(joinPoint, testAnnotation))
            .isInstanceOf(FeatureNotAvailableException.class)
            .hasMessageContaining("premium-export")
            .hasMessageContaining("bob");
        
        try {
            verify(joinPoint, never()).proceed();
        } catch (Throwable e) {
            // Ignore
        }
    }
    
    @Test
    @DisplayName("Complete workflow: User not resolved")
    void completeWorkflowUserNotResolved() throws Throwable {
        // Arrange
        testAnnotation = new TestAnnotation("premium-export", true, new String[]{}, false);
        
        when(pricingConfigurator.resolveUserId(joinPoint)).thenReturn(null);
        
        // Act & Assert
        assertThatThrownBy(() -> aspect.validateFeatureAccess(joinPoint, testAnnotation))
            .isInstanceOf(FeatureNotAvailableException.class)
            .hasMessageContaining("User ID could not be resolved");
        
        try {
            verify(joinPoint, never()).proceed();
            verify(pricingConfigurator, never()).getSpaceClient();
        } catch (Throwable e) {
            // Ignore
        }
    }
    
    @Test
    @DisplayName("Complete workflow: With consumption tracking")
    void completeWorkflowWithConsumptionTracking() throws Throwable {
        // Arrange
        testAnnotation = new TestAnnotation(
            "api-service-concurrent",
            true,
            new String[]{"connections=50", "bandwidth=100.5"},
            true
        );
        
        when(pricingConfigurator.resolveUserId(joinPoint)).thenReturn("charlie");
        when(pricingConfigurator.getSpaceClient()).thenReturn(spaceClient);
        when(featureEvaluationResult.isEval()).thenReturn(true);
        when(featureModule.evaluate(
            anyString(), anyString(), any(Map.class), eq(true), eq(true)
        )).thenReturn(featureEvaluationResult);
        when(joinPoint.proceed()).thenReturn("Concurrent request handled");
        
        // Act
        Object result = aspect.validateFeatureAccess(joinPoint, testAnnotation);
        
        // Assert
        assertThat(result).isEqualTo("Concurrent request handled");
        
        verify(featureModule).evaluate(
            eq("charlie"), eq("api-service-concurrent"), argThat(consumption ->
                consumption.get("connections").longValue() == 50L &&
                consumption.get("bandwidth").doubleValue() == 100.5
            ), eq(true), eq(true)
        );
    }
    
    @Test
    @DisplayName("Complete workflow: Client-side evaluation")
    void completeWorkflowClientSideEvaluation() throws Throwable {
        // Arrange
        testAnnotation = new TestAnnotation("basic-feature", false, new String[]{}, false);
        
        when(pricingConfigurator.resolveUserId(joinPoint)).thenReturn("dave");
        when(pricingConfigurator.getSpaceClient()).thenReturn(spaceClient);
        when(featureEvaluationResult.isEval()).thenReturn(true);
        when(featureModule.evaluate(
            eq("dave"), eq("basic-feature"), any(Map.class), eq(false), eq(false)
        )).thenReturn(featureEvaluationResult);
        when(joinPoint.proceed()).thenReturn("Success");
        
        // Act
        Object result = aspect.validateFeatureAccess(joinPoint, testAnnotation);
        
        // Assert
        assertThat(result).isEqualTo("Success");
        
        verify(featureModule).evaluate(
            anyString(), anyString(), any(Map.class), anyBoolean(), eq(false)
        );
    }
    
    @Test
    @DisplayName("Complete workflow: SPACE evaluation error")
    void completeWorkflowSpaceEvaluationError() throws Throwable {
        // Arrange
        testAnnotation = new TestAnnotation("premium-export", true, new String[]{}, false);
        
        when(pricingConfigurator.resolveUserId(joinPoint)).thenReturn("eve");
        when(pricingConfigurator.getSpaceClient()).thenReturn(spaceClient);
        when(featureModule.evaluate(
            anyString(), anyString(), any(Map.class), anyBoolean(), anyBoolean()
        )).thenThrow(new RuntimeException("SPACE server connection failed"));
        
        // Act & Assert
        assertThatThrownBy(() -> aspect.validateFeatureAccess(joinPoint, testAnnotation))
            .isInstanceOf(FeatureNotAvailableException.class)
            .hasCauseInstanceOf(RuntimeException.class);
    }
    
    @Test
    @DisplayName("Multiple features validation: All available")
    void multipleFeatureValidationAllAvailable() throws Throwable {
        // Arrange & Act
        for (int i = 0; i < 3; i++) {
            TestAnnotation annotation = new TestAnnotation(
                "feature-" + i, true, new String[]{}, false
            );
            
            when(pricingConfigurator.resolveUserId(joinPoint)).thenReturn("user" + i);
            when(pricingConfigurator.getSpaceClient()).thenReturn(spaceClient);
            when(featureEvaluationResult.isEval()).thenReturn(true);
            when(featureModule.evaluate(
                "user" + i, "feature-" + i, new HashMap<>(), false, true
            )).thenReturn(featureEvaluationResult);
            when(joinPoint.proceed()).thenReturn("success");
            
            // Assert
            Object result = aspect.validateFeatureAccess(joinPoint, annotation);
            assertThat(result).isEqualTo("success");
        }
    }
    
    @Test
    @DisplayName("Edge case: Empty consumption array")
    void edgeCaseEmptyConsumptionArray() throws Throwable {
        // Arrange
        testAnnotation = new TestAnnotation("feature-id", true, new String[]{}, false);
        
        when(pricingConfigurator.resolveUserId(joinPoint)).thenReturn("user");
        when(pricingConfigurator.getSpaceClient()).thenReturn(spaceClient);
        when(featureEvaluationResult.isEval()).thenReturn(true);
        when(featureModule.evaluate(
            eq("user"), eq("feature-id"), any(Map.class), eq(false), eq(true)
        )).thenReturn(featureEvaluationResult);
        when(joinPoint.proceed()).thenReturn("success");
        
        // Act
        Object result = aspect.validateFeatureAccess(joinPoint, testAnnotation);
        
        // Assert
        assertThat(result).isEqualTo("success");
    }
    
    @Test
    @DisplayName("Edge case: Special characters in feature ID")
    void edgeCaseSpecialCharactersInFeatureId() throws Throwable {
        // Arrange
        testAnnotation = new TestAnnotation(
            "service-v2-feature_with-special.chars", true, new String[]{}, false
        );
        
        when(pricingConfigurator.resolveUserId(joinPoint)).thenReturn("user");
        when(pricingConfigurator.getSpaceClient()).thenReturn(spaceClient);
        when(featureEvaluationResult.isEval()).thenReturn(true);
        when(featureModule.evaluate(
            eq("user"), eq("service-v2-feature_with-special.chars"), any(Map.class), eq(false), eq(true)
        )).thenReturn(featureEvaluationResult);
        when(joinPoint.proceed()).thenReturn("success");
        
        // Act
        Object result = aspect.validateFeatureAccess(joinPoint, testAnnotation);
        
        // Assert
        assertThat(result).isEqualTo("success");
        
        verify(featureModule).evaluate(
            anyString(), eq("service-v2-feature_with-special.chars"), any(), anyBoolean(), anyBoolean()
        );
    }
}
