package io.github.isagroup.spaceclient.annotations;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static org.assertj.core.api.Assertions.*;

@DisplayName("@RequireFeature Annotation Tests")
class RequireFeatureAnnotationTest {
    
    @Test
    @DisplayName("Annotation should have RUNTIME retention")
    void annotationShouldHaveRuntimeRetention() {
        Retention retention = RequireFeature.class.getAnnotation(Retention.class);
        
        assertThat(retention).isNotNull();
        assertThat(retention.value().toString()).contains("RUNTIME");
    }
    
    @Test
    @DisplayName("Annotation should target methods")
    void annotationShouldTargetMethods() {
        Target target = RequireFeature.class.getAnnotation(Target.class);
        
        assertThat(target).isNotNull();
        assertThat(target.value()).contains(java.lang.annotation.ElementType.METHOD);
    }
    
    @Test
    @DisplayName("Annotation should be documented")
    void annotationShouldBeDocumented() {
        assertThat(RequireFeature.class.getAnnotation(java.lang.annotation.Documented.class))
            .isNotNull();
    }
    
    @Test
    @DisplayName("Annotation value element should be present and required")
    void annotationValueElementShouldBePresentAndRequired() throws NoSuchMethodException {
        java.lang.reflect.Method valueMethod = RequireFeature.class.getMethod("value");
        
        assertThat(valueMethod).isNotNull();
        assertThat(valueMethod.getReturnType()).isEqualTo(String.class);
    }
    
    @Test
    @DisplayName("Annotation server element should have default value true")
    void annotationServerElementShouldHaveDefaultValueTrue() throws NoSuchMethodException {
        java.lang.reflect.Method serverMethod = RequireFeature.class.getMethod("server");
        
        assertThat(serverMethod).isNotNull();
        assertThat(serverMethod.getReturnType()).isEqualTo(boolean.class);
    }
    
    @Test
    @DisplayName("Annotation consumption element should have default empty array")
    void annotationConsumptionElementShouldHaveDefaultEmptyArray() throws NoSuchMethodException {
        java.lang.reflect.Method consumptionMethod = RequireFeature.class.getMethod("consumption");
        
        assertThat(consumptionMethod).isNotNull();
        assertThat(consumptionMethod.getReturnType()).isEqualTo(String[].class);
    }
    
    @Test
    @DisplayName("Annotation details element should have default value false")
    void annotationDetailsElementShouldHaveDefaultValueFalse() throws NoSuchMethodException {
        java.lang.reflect.Method detailsMethod = RequireFeature.class.getMethod("details");
        
        assertThat(detailsMethod).isNotNull();
        assertThat(detailsMethod.getReturnType()).isEqualTo(boolean.class);
    }
    
    @Test
    @DisplayName("Annotation should be usable on test methods")
    void annotationShouldBeUsableOnTestMethods() throws NoSuchMethodException {
        java.lang.reflect.Method testMethod = AnnotatedTestClass.class.getMethod("annotatedMethod");
        RequireFeature annotation = testMethod.getAnnotation(RequireFeature.class);
        
        assertThat(annotation).isNotNull();
        assertThat(annotation.value()).isEqualTo("test-feature");
        assertThat(annotation.server()).isTrue();
        assertThat(annotation.details()).isFalse();
    }
    
    @Test
    @DisplayName("Annotation should support consumption parameter")
    void annotationShouldSupportConsumptionParameter() throws NoSuchMethodException {
        java.lang.reflect.Method testMethod = AnnotatedTestClass.class.getMethod("annotatedMethodWithConsumption");
        RequireFeature annotation = testMethod.getAnnotation(RequireFeature.class);
        
        assertThat(annotation).isNotNull();
        assertThat(annotation.consumption()).containsExactly("requests=100", "storage=2048");
    }
    
    @Test
    @DisplayName("Annotation should support details parameter")
    void annotationShouldSupportDetailsParameter() throws NoSuchMethodException {
        java.lang.reflect.Method testMethod = AnnotatedTestClass.class.getMethod("annotatedMethodWithDetails");
        RequireFeature annotation = testMethod.getAnnotation(RequireFeature.class);
        
        assertThat(annotation).isNotNull();
        assertThat(annotation.details()).isTrue();
    }
    
    @Test
    @DisplayName("Annotation should support server-side evaluation")
    void annotationShouldSupportServerSideEvaluation() throws NoSuchMethodException {
        java.lang.reflect.Method testMethod = AnnotatedTestClass.class.getMethod("annotatedMethodServerEval");
        RequireFeature annotation = testMethod.getAnnotation(RequireFeature.class);
        
        assertThat(annotation).isNotNull();
        assertThat(annotation.server()).isTrue();
    }
    
    @Test
    @DisplayName("Annotation should support client-side evaluation")
    void annotationShouldSupportClientSideEvaluation() throws NoSuchMethodException {
        java.lang.reflect.Method testMethod = AnnotatedTestClass.class.getMethod("annotatedMethodClientEval");
        RequireFeature annotation = testMethod.getAnnotation(RequireFeature.class);
        
        assertThat(annotation).isNotNull();
        assertThat(annotation.server()).isFalse();
    }
    
    /**
     * Test class with various @RequireFeature annotations
     */
    public static class AnnotatedTestClass {
        
        @RequireFeature("test-feature")
        public void annotatedMethod() {
        }
        
        @RequireFeature(
            value = "feature-with-consumption",
            consumption = {"requests=100", "storage=2048"}
        )
        public void annotatedMethodWithConsumption() {
        }
        
        @RequireFeature(
            value = "feature-with-details",
            details = true
        )
        public void annotatedMethodWithDetails() {
        }
        
        @RequireFeature(
            value = "feature-server-eval",
            server = true
        )
        public void annotatedMethodServerEval() {
        }
        
        @RequireFeature(
            value = "feature-client-eval",
            server = false
        )
        public void annotatedMethodClientEval() {
        }
    }
}
