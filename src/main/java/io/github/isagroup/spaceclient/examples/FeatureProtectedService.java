package io.github.isagroup.spaceclient.examples;

import io.github.isagroup.spaceclient.annotations.RequireFeature;
import io.github.isagroup.spaceclient.annotations.exceptions.FeatureNotAvailableException;
import org.springframework.stereotype.Service;

/**
 * Example service demonstrating the @RequireFeature annotation for feature access control.
 * 
 * This example requires:
 * 1. Spring Framework with AspectJ enabled
 * 2. A SpringPricingConfigurator bean in the Spring context
 * 3. &#64;EnableAspectJAutoProxy on your Spring configuration
 * 
 * The user ID is automatically resolved from your SpringPricingConfigurator implementation,
 * so you don't need to specify it in the annotation.
 * 
 * Usage:
 * <pre>
 * &#64;Configuration
 * &#64;EnableAspectJAutoProxy
 * public class AppConfig {
 *     &#64;Bean
 *     public SpringPricingConfigurator pricingConfigurator() {
 *         return new MyPricingConfigurator();
 *     }
 * }
 * </pre>
 */
@Service
public class FeatureProtectedService {
    
    /**
     * Simple premium operation that requires feature availability.
     * The user ID is automatically resolved from the ApplicationContext.
     * 
     * If the feature is not available, FeatureNotAvailableException will be thrown
     * and this method will NOT be executed.
     * 
     * @throws FeatureNotAvailableException if the feature is not available
     */
    @RequireFeature("premium-service-export-data")
    public void exportData() {
        System.out.println("✓ Executing premium export operation");
        // Premium operation logic here
    }
    
    /**
     * Advanced operation with consumption tracking and client-side evaluation.
     * 
     * @throws FeatureNotAvailableException if the feature is not available
     */
    @RequireFeature(
        value = "premium-service-advanced-reports",
        server = false,
        consumption = {"report_size=1024", "export_format=2"}
    )
    public void generateAdvancedReport() {
        System.out.println("✓ Executing advanced report generation");
        // Advanced operation logic here
    }
    
    /**
     * Operation using server-side evaluation with detailed results.
     * 
     * @throws FeatureNotAvailableException if the feature is not available
     */
    @RequireFeature(
        value = "api-service-high-concurrency",
        server = true,
        details = true,
        consumption = {"concurrent_connections=100"}
    )
    public void handleHighConcurrency() {
        System.out.println("✓ Handling high concurrency operation");
        // High concurrency logic here
    }
    
    /**
     * Example of a method without @RequireFeature annotation.
     * This will always execute without feature validation.
     */
    public void executeBasicOperation() {
        System.out.println("✓ Executing basic operation (no feature validation)");
        // Basic operation logic here - no feature validation
    }
}
