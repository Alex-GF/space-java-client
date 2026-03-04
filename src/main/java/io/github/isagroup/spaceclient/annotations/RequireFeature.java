package io.github.isagroup.spaceclient.annotations;

import java.lang.annotation.*;

/**
 * Annotation to enforce feature access control using SPACE.
 * 
 * This annotation requires a SpringPricingConfigurator to be configured in the Spring context.
 * 
 * When applied to a method, it will:
 * 1. Resolve the user ID from the application context (via SpringPricingConfigurator)
 * 2. Evaluate the specified feature for the current user
 * 3. Prevent method execution if the feature is not available
 * 4. Throw FeatureNotAvailableException if access is denied
 * 
 * Configuration required:
 * - A bean implementing SpringPricingConfigurator with &#64;Component or &#64;Bean
 * - &#64;EnableAspectJAutoProxy in your Spring configuration
 * 
 * Example:
 * &#64;RequireFeature(featureId = "myservice-premiumExport")
 * public void exportData() {
 *     // This will only execute if the feature is available for the current user
 * }
 * 
 * &#64;RequireFeature(featureId = "myservice-concurrentApiCalls", server = false)
 * public void handleConcurrent() {
 *     // This uses client-side evaluation
 * }
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequireFeature {
    
    /**
     * The feature identifier (format: serviceName-featureName)
     */
    String value();
    
    /**
     * Whether to use server-side evaluation (default: true)
     */
    boolean server() default true;
    
    /**
     * Optional consumption values as key=value pairs.
     * Format: {"requests=10", "storage=1024"}
     */
    String[] consumption() default {};
    
    /**
     * Whether to include details in the evaluation response (default: false)
     */
    boolean details() default false;
}
