package io.github.isagroup.spaceclient.spring.config;

import io.github.isagroup.spaceclient.SpaceClient;
import io.github.isagroup.spaceclient.types.SpaceConnectionOptions;
import org.aspectj.lang.ProceedingJoinPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Abstract base class for Spring pricing integration configuration.
 * 
 * This class defines how user context is resolved within a Spring application.
 * The SpaceClient instance must be provided by your application as a @Bean.
 * 
 * Implementations must provide:
 * 1. User context resolution strategy via resolveUserId()
 * 
 * Optional:
 * 2. Override shouldEvaluateFeature() for custom evaluation logic
 * 
 * Example implementation:
 * <pre>
 * &#64;Component
 * public class MyPricingConfigurator extends SpringPricingConfigurator {
 *     
 *     &#64;Override
 *     public String resolveUserId(ProceedingJoinPoint joinPoint) {
 *         // Get user from Spring Security context
 *         Authentication auth = SecurityContextHolder.getContext().getAuthentication();
 *         return auth != null ? auth.getName() : null;
 *     }
 * }
 * 
 * // In your Spring configuration:
 * &#64;Configuration
 * public class SpaceClientConfiguration {
 *     &#64;Bean
 *     public SpaceClient spaceClient() {
 *         String url = System.getenv("SPACE_SERVER_URL");
 *         String apiKey = System.getenv("SPACE_API_KEY");
 *         SpaceConnectionOptions options = new SpaceConnectionOptions(url, apiKey);
 *         return new SpaceClient(options);
 *     }
 * }
 * </pre>
 */
public abstract class SpringPricingConfigurator {
    @Autowired
    private SpaceClient spaceClient;

    /**
     * Resolves the user ID from the current request/context.
     * This method must be implemented by subclasses to extract the user ID
     * from the application context (e.g., Spring Security, request headers, etc.)
     * 
     * @param joinPoint the AspectJ join point containing method information and arguments
     * @return the resolved user ID, or null if user is not authenticated
     */
    public abstract String resolveUserId(ProceedingJoinPoint joinPoint);
    
    /**
     * Determines whether feature evaluation should be performed for a given request.
     * This method can be overridden to implement custom logic for skipping feature evaluation
     * in certain contexts (e.g., admin users, testing environments, specific endpoints).
     * 
     * @param joinPoint the AspectJ join point containing method information and arguments
     * @return true if feature evaluation should be performed, false to skip evaluation
     */
    public boolean shouldEvaluateFeature(ProceedingJoinPoint joinPoint) {
        return true;
    }
    
    /**
     * Gets the SpaceClient instance configured as a Spring bean in the application.
     * The SpaceClient must be provided by the target application as a @Bean.
     * 
     * @return the SpaceClient instance
     * @throws IllegalStateException if SpaceClient is not configured as a bean
     */
    public SpaceClient getSpaceClient() {
        if (spaceClient == null) {
            throw new IllegalStateException(
                "SpaceClient is not configured as a Spring bean in your application. " +
                "Ensure you have a @Bean method that creates a SpaceClient instance. " +
                "Example: \n" +
                "@Bean\n" +
                "public SpaceClient spaceClient() {\n" +
                "    SpaceConnectionOptions options = new SpaceConnectionOptions(url, apiKey);\n" +
                "    return new SpaceClient(options);\n" +
                "}"
            );
        }
        return spaceClient;
    }
    

}
