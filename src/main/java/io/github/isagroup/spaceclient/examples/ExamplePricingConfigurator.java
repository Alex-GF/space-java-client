package io.github.isagroup.spaceclient.examples;

import io.github.isagroup.spaceclient.spring.config.SpringPricingConfigurator;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Simple example implementation of SpringPricingConfigurator.
 * 
 * This example demonstrates:
 * 1. Reading configuration from environment variables
 * 2. Resolving userId from method parameters
 * 
 * For production use, you can extend this to integrate with:
 * - Spring Security context
 * - JWT tokens
 * - HTTP headers
 * - Your own authentication system
 */
@Component
public class ExamplePricingConfigurator extends SpringPricingConfigurator {
    private static final Logger logger = LoggerFactory.getLogger(ExamplePricingConfigurator.class);
    
    @Override
    protected String getSpaceUrl() {
        // Try to read from custom environment variable first
        String url = System.getenv("SPACE_SERVER_URL");
        if (url == null || url.isEmpty()) {
            url = System.getenv("space.server.url");
        }
        if (url == null || url.isEmpty()) {
            // Fall back to parent implementation
            return super.getSpaceUrl();
        }
        return url;
    }
    
    @Override
    protected String getSpaceApiKey() {
        // Try to read from custom environment variable first
        String key = System.getenv("SPACE_API_KEY");
        if (key == null || key.isEmpty()) {
            key = System.getenv("space.api.key");
        }
        if (key == null || key.isEmpty()) {
            // Fall back to parent implementation
            return super.getSpaceApiKey();
        }
        return key;
    }
    
    @Override
    public String resolveUserId(ProceedingJoinPoint joinPoint) {
        try {
            // Try to resolve userId from method parameters
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            String[] paramNames = signature.getParameterNames();
            Object[] argValues = joinPoint.getArgs();
            
            // Look for a parameter named "userId" or "user_id"
            for (int i = 0; i < paramNames.length; i++) {
                if ("userId".equalsIgnoreCase(paramNames[i]) || 
                    "user_id".equalsIgnoreCase(paramNames[i])) {
                    Object value = argValues[i];
                    if (value != null) {
                        logger.debug("Resolved userId from method parameter: {}", value);
                        return value.toString();
                    }
                }
            }
            
            logger.warn("Could not resolve userId from method parameters. " +
                       "Ensure your method has a 'userId' parameter");
            return null;
            
        } catch (Exception e) {
            logger.error("Error resolving userId", e);
            return null;
        }
    }
    
    /**
     * Alternative implementation example using Spring Security.
     * To use this instead, uncomment the code and replace the resolveUserId() method above.
     * 
     * Requires: org.springframework.security:spring-security-core
     * 
     * &#64;Override
     * public String resolveUserId(ProceedingJoinPoint joinPoint) {
     *     try {
     *         var auth = org.springframework.security.core.context.SecurityContextHolder
     *             .getContext()
     *             .getAuthentication();
     *         
     *         if (auth != null && auth.isAuthenticated()) {
     *             logger.debug("Resolved userId from Spring Security: {}", auth.getName());
     *             return auth.getName();
     *         }
     *     } catch (Exception e) {
     *         logger.debug("Spring Security not available or user not authenticated");
     *     }
     *     
     *     // Fall back to method parameters
     *     return resolveFromMethodParameters(joinPoint);
     * }
     * 
     * private String resolveFromMethodParameters(ProceedingJoinPoint joinPoint) {
     *     try {
     *         MethodSignature signature = (MethodSignature) joinPoint.getSignature();
     *         String[] paramNames = signature.getParameterNames();
     *         Object[] argValues = joinPoint.getArgs();
     *         
     *         for (int i = 0; i < paramNames.length; i++) {
     *             if ("userId".equalsIgnoreCase(paramNames[i])) {
     *                 Object value = argValues[i];
     *                 return value != null ? value.toString() : null;
     *             }
     *         }
     *     } catch (Exception e) {
     *         logger.debug("Error resolving userId from method parameters", e);
     *     }
     *     return null;
     * }
     */
}
