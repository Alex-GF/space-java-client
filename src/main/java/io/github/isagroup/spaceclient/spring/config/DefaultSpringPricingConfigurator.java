package io.github.isagroup.spaceclient.spring.config;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of SpringPricingConfigurator.
 * 
 * This implementation resolves the user ID from method parameters.
 * It looks for a parameter named "userId" or with annotation @UserId.
 * 
 * For most use cases, you should create a custom implementation that integrates
 * with your application's authentication mechanism (e.g., Spring Security).
 * 
 * Example of custom implementation:
 * <pre>
 * &#64;Component
 * public class CustomPricingConfigurator extends SpringPricingConfigurator {
 *     &#64;Override
 *     public String resolveUserId(ProceedingJoinPoint joinPoint) {
 *         Authentication auth = SecurityContextHolder.getContext().getAuthentication();
 *         return auth != null ? auth.getName() : null;
 *     }
 * }
 * </pre>
 */
public class DefaultSpringPricingConfigurator extends SpringPricingConfigurator {
    private static final Logger logger = LoggerFactory.getLogger(DefaultSpringPricingConfigurator.class);
    
    @Override
    public String resolveUserId(ProceedingJoinPoint joinPoint) {
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            String[] paramNames = signature.getParameterNames();
            Object[] argValues = joinPoint.getArgs();
            
            // Look for userId parameter
            for (int i = 0; i < paramNames.length; i++) {
                if (paramNames[i].equalsIgnoreCase("userId")) {
                    Object value = argValues[i];
                    return value != null ? value.toString() : null;
                }
            }
            
            // Look for first String parameter as fallback
            for (int i = 0; i < argValues.length; i++) {
                if (argValues[i] instanceof String) {
                    logger.debug("Using first String parameter as userId");
                    return (String) argValues[i];
                }
            }
            
            logger.warn("Could not resolve userId from method parameters");
            return null;
            
        } catch (Exception e) {
            logger.error("Error resolving userId", e);
            return null;
        }
    }
}
