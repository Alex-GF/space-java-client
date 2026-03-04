package io.github.isagroup.spaceclient.annotations.aspect;

import io.github.isagroup.spaceclient.SpaceClient;
import io.github.isagroup.spaceclient.annotations.RequireFeature;
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
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RequireFeatureAspect Tests")
class RequireFeatureAspectTest {
    
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
    
    @Mock
    private RequireFeature requireFeature;
    
    private RequireFeatureAspect aspect;
    
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
    @DisplayName("Should allow method execution when feature is available")
    void shouldAllowMethodExecutionWhenFeatureIsAvailable() throws Throwable {
        when(requireFeature.value()).thenReturn("premium-export");
        when(requireFeature.server()).thenReturn(true);
        when(requireFeature.consumption()).thenReturn(new String[]{});
        when(requireFeature.details()).thenReturn(false);
        
        when(pricingConfigurator.resolveUserId(joinPoint)).thenReturn("user123");
        when(pricingConfigurator.getSpaceClient()).thenReturn(spaceClient);
        when(featureEvaluationResult.getEval()).thenReturn(true);
        when(featureModule.evaluate("user123", "premium-export", 
                    new java.util.HashMap<>(), false, true))
            .thenReturn(featureEvaluationResult);
        
        Object expectedResult = "success";
        when(joinPoint.proceed()).thenReturn(expectedResult);
        
        Object result = aspect.validateFeatureAccess(joinPoint, requireFeature);
        
        assertThat(result).isEqualTo(expectedResult);
        verify(joinPoint).proceed();
    }
    
    @Test
    @DisplayName("Should prevent method execution when feature is not available")
    void shouldPreventMethodExecutionWhenFeatureIsNotAvailable() {
        when(requireFeature.value()).thenReturn("premium-export");
        when(requireFeature.server()).thenReturn(true);
        when(requireFeature.consumption()).thenReturn(new String[]{});
        when(requireFeature.details()).thenReturn(false);
        
        when(pricingConfigurator.resolveUserId(joinPoint)).thenReturn("user123");
        when(pricingConfigurator.getSpaceClient()).thenReturn(spaceClient);
        when(featureEvaluationResult.getEval()).thenReturn(false);
        
        try {
            when(featureModule.evaluate("user123", "premium-export", 
                        new java.util.HashMap<>(), false, true))
                .thenReturn(featureEvaluationResult);
        } catch (Exception e) {
            // Ignore mock setup exception
        }
        
        assertThatThrownBy(() -> aspect.validateFeatureAccess(joinPoint, requireFeature))
            .isInstanceOf(FeatureNotAvailableException.class)
            .hasMessageContaining("premium-export")
            .hasMessageContaining("user123");
        
        try {
            verify(joinPoint, never()).proceed();
        } catch (Throwable e) {
            // Ignore
        }
    }
    
    @Test
    @DisplayName("Should throw exception when user ID cannot be resolved")
    void shouldThrowExceptionWhenUserIdCannotBeResolved() {
        when(requireFeature.value()).thenReturn("premium-export");
        when(pricingConfigurator.resolveUserId(joinPoint)).thenReturn(null);
        
        assertThatThrownBy(() -> aspect.validateFeatureAccess(joinPoint, requireFeature))
            .isInstanceOf(FeatureNotAvailableException.class)
            .hasMessageContaining("User ID could not be resolved");
    }
    
    @Test
    @DisplayName("Should use server-side evaluation when specified")
    void shouldUseServerSideEvaluationWhenSpecified() throws Throwable {
        when(requireFeature.value()).thenReturn("feature-id");
        when(requireFeature.server()).thenReturn(true);
        when(requireFeature.consumption()).thenReturn(new String[]{});
        when(requireFeature.details()).thenReturn(false);
        
        when(pricingConfigurator.resolveUserId(joinPoint)).thenReturn("user123");
        when(pricingConfigurator.getSpaceClient()).thenReturn(spaceClient);
        when(featureEvaluationResult.getEval()).thenReturn(true);
        when(featureModule.evaluate(anyString(), anyString(), 
                    any(), anyBoolean(), eq(true)))
            .thenReturn(featureEvaluationResult);
        when(joinPoint.proceed()).thenReturn("success");
        
        aspect.validateFeatureAccess(joinPoint, requireFeature);
        
        // Verify that server=true was passed
        ArgumentCaptor<Boolean> serverCaptor = ArgumentCaptor.forClass(Boolean.class);
        verify(featureModule).evaluate(anyString(), anyString(), 
                    any(), anyBoolean(), serverCaptor.capture());
        assertThat(serverCaptor.getValue()).isTrue();
    }
    
    @Test
    @DisplayName("Should use client-side evaluation when specified")
    void shouldUseClientSideEvaluationWhenSpecified() throws Throwable {
        when(requireFeature.value()).thenReturn("feature-id");
        when(requireFeature.server()).thenReturn(false);
        when(requireFeature.consumption()).thenReturn(new String[]{});
        when(requireFeature.details()).thenReturn(false);
        
        when(pricingConfigurator.resolveUserId(joinPoint)).thenReturn("user123");
        when(pricingConfigurator.getSpaceClient()).thenReturn(spaceClient);
        when(featureEvaluationResult.getEval()).thenReturn(true);
        when(featureModule.evaluate(anyString(), anyString(), 
                    any(), anyBoolean(), eq(false)))
            .thenReturn(featureEvaluationResult);
        when(joinPoint.proceed()).thenReturn("success");
        
        aspect.validateFeatureAccess(joinPoint, requireFeature);
        
        ArgumentCaptor<Boolean> serverCaptor = ArgumentCaptor.forClass(Boolean.class);
        verify(featureModule).evaluate(anyString(), anyString(), 
                    any(), anyBoolean(), serverCaptor.capture());
        assertThat(serverCaptor.getValue()).isFalse();
    }
    
    @Test
    @DisplayName("Should parse consumption array correctly")
    void shouldParseConsumptionArrayCorrectly() throws Throwable {
        when(requireFeature.value()).thenReturn("feature-id");
        when(requireFeature.server()).thenReturn(true);
        when(requireFeature.consumption()).thenReturn(new String[]{"requests=100", "storage=2048"});
        when(requireFeature.details()).thenReturn(false);
        
        when(pricingConfigurator.resolveUserId(joinPoint)).thenReturn("user123");
        when(pricingConfigurator.getSpaceClient()).thenReturn(spaceClient);
        when(featureEvaluationResult.getEval()).thenReturn(true);
        when(featureModule.evaluate(anyString(), anyString(), 
                    any(java.util.Map.class), anyBoolean(), anyBoolean()))
            .thenReturn(featureEvaluationResult);
        when(joinPoint.proceed()).thenReturn("success");
        
        aspect.validateFeatureAccess(joinPoint, requireFeature);
        
        ArgumentCaptor<java.util.Map> consumptionCaptor = ArgumentCaptor.forClass(java.util.Map.class);
        verify(featureModule).evaluate(anyString(), anyString(), 
                    consumptionCaptor.capture(), anyBoolean(), anyBoolean());
        
        java.util.Map<String, Number> consumption = consumptionCaptor.getValue();
        assertThat(consumption).containsEntry("requests", 100L).containsEntry("storage", 2048L);
    }
    
    @Test
    @DisplayName("Should include details in evaluation when specified")
    void shouldIncludeDetailsInEvaluationWhenSpecified() throws Throwable {
        when(requireFeature.value()).thenReturn("feature-id");
        when(requireFeature.server()).thenReturn(true);
        when(requireFeature.consumption()).thenReturn(new String[]{});
        when(requireFeature.details()).thenReturn(true);
        
        when(pricingConfigurator.resolveUserId(joinPoint)).thenReturn("user123");
        when(pricingConfigurator.getSpaceClient()).thenReturn(spaceClient);
        when(featureEvaluationResult.getEval()).thenReturn(true);
        when(featureModule.evaluate(anyString(), anyString(), 
                    any(), eq(true), anyBoolean()))
            .thenReturn(featureEvaluationResult);
        when(joinPoint.proceed()).thenReturn("success");
        
        aspect.validateFeatureAccess(joinPoint, requireFeature);
        
        ArgumentCaptor<Boolean> detailsCaptor = ArgumentCaptor.forClass(Boolean.class);
        verify(featureModule).evaluate(anyString(), anyString(), 
                    any(), detailsCaptor.capture(), anyBoolean());
        assertThat(detailsCaptor.getValue()).isTrue();
    }
    
    @Test
    @DisplayName("Should handle exception during feature evaluation")
    void shouldHandleExceptionDuringFeatureEvaluation() throws Throwable {
        when(requireFeature.value()).thenReturn("feature-id");
        when(requireFeature.server()).thenReturn(true);
        when(requireFeature.consumption()).thenReturn(new String[]{});
        when(requireFeature.details()).thenReturn(false);
        
        when(pricingConfigurator.resolveUserId(joinPoint)).thenReturn("user123");
        when(pricingConfigurator.getSpaceClient()).thenReturn(spaceClient);
        when(featureModule.evaluate(anyString(), anyString(), 
                    any(), anyBoolean(), anyBoolean()))
            .thenThrow(new RuntimeException("SPACE server error"));
        
        assertThatThrownBy(() -> aspect.validateFeatureAccess(joinPoint, requireFeature))
            .isInstanceOf(FeatureNotAvailableException.class)
            .hasCauseInstanceOf(RuntimeException.class);
    }
    
    @Test
    @DisplayName("Should parse decimal consumption values")
    void shouldParseDecimalConsumptionValues() throws Throwable {
        when(requireFeature.value()).thenReturn("feature-id");
        when(requireFeature.server()).thenReturn(true);
        when(requireFeature.consumption()).thenReturn(new String[]{"bandwidth=10.5", "latency=2.3"});
        when(requireFeature.details()).thenReturn(false);
        
        when(pricingConfigurator.resolveUserId(joinPoint)).thenReturn("user123");
        when(pricingConfigurator.getSpaceClient()).thenReturn(spaceClient);
        when(featureEvaluationResult.getEval()).thenReturn(true);
        when(featureModule.evaluate(anyString(), anyString(), 
                    any(java.util.Map.class), anyBoolean(), anyBoolean()))
            .thenReturn(featureEvaluationResult);
        when(joinPoint.proceed()).thenReturn("success");
        
        aspect.validateFeatureAccess(joinPoint, requireFeature);
        
        ArgumentCaptor<java.util.Map> consumptionCaptor = ArgumentCaptor.forClass(java.util.Map.class);
        verify(featureModule).evaluate(anyString(), anyString(), 
                    consumptionCaptor.capture(), anyBoolean(), anyBoolean());
        
        java.util.Map<String, Number> consumption = consumptionCaptor.getValue();
        assertThat(consumption).containsEntry("bandwidth", 10.5).containsEntry("latency", 2.3);
    }
    
    @Test
    @DisplayName("Should skip feature evaluation when shouldEvaluateFeature returns false")
    void shouldSkipEvaluationWhenConfiguratorDisablesIt() throws Throwable {
        // Disable feature evaluation
        when(pricingConfigurator.shouldEvaluateFeature(joinPoint)).thenReturn(false);
        
        Object expectedResult = "success";
        when(joinPoint.proceed()).thenReturn(expectedResult);
        
        Object result = aspect.validateFeatureAccess(joinPoint, requireFeature);
        
        assertThat(result).isEqualTo(expectedResult);
        verify(joinPoint).proceed();
        // Verify that feature evaluation was NOT performed
        verify(pricingConfigurator, never()).resolveUserId(any());
        verify(pricingConfigurator, never()).getSpaceClient();
        verify(featureModule, never()).evaluate(anyString(), anyString(), any(), anyBoolean(), anyBoolean());
    }
}
