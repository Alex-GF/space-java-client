package io.github.isagroup.spaceclient.annotations.aspect;

import io.github.isagroup.spaceclient.SpaceClient;
import io.github.isagroup.spaceclient.annotations.RequireFeature;
import io.github.isagroup.spaceclient.annotations.exceptions.FeatureNotAvailableException;
import io.github.isagroup.spaceclient.spring.config.SpringPricingConfigurator;
import io.github.isagroup.spaceclient.types.FeatureEvaluationResult;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * AspectJ aspect that intercepts methods annotated with @RequireFeature
 * and validates feature access before method execution.
 * 
 * This aspect requires a SpringPricingConfigurator bean to be present in the Spring context.
 */
@Aspect
@Component
public class RequireFeatureAspect {
    private static final Logger logger = LoggerFactory.getLogger(RequireFeatureAspect.class);
    
    @Autowired
    public SpringPricingConfigurator pricingConfigurator;
    
    @Around("@annotation(requireFeature)")
    public Object validateFeatureAccess(ProceedingJoinPoint joinPoint, RequireFeature requireFeature) throws Throwable {
        String featureId = requireFeature.value();
        String userId = null;
        
        logger.debug("Validating feature '{}' on method '{}'", featureId, joinPoint.getSignature().getName());
        
        // Check if feature evaluation should be performed
        if (!pricingConfigurator.shouldEvaluateFeature(joinPoint)) {
            logger.debug("Feature evaluation skipped for '{}' by configurator", featureId);
            return joinPoint.proceed();
        }
        
        try {
            // Resolve user ID from context
            userId = pricingConfigurator.resolveUserId(joinPoint);
            
            if (userId == null || userId.isEmpty()) {
                logger.warn("Could not resolve user ID for feature validation. " +
                           "Ensure your SpringPricingConfigurator.resolveUserId() returns a valid user ID");
                throw new FeatureNotAvailableException(
                    featureId,
                    "<<UNKNOWN>>",
                    "User ID could not be resolved"
                );
            }
            
            logger.debug("Resolved user ID: {}", userId);
            
            // Get the SpaceClient instance
            SpaceClient spaceClient = pricingConfigurator.getSpaceClient();
            
            // Parse consumption if provided
            Map<String, Number> consumption = parseConsumption(requireFeature.consumption());
            
            // Evaluate the feature
            FeatureEvaluationResult result = spaceClient.features.evaluate(
                userId,
                featureId,
                consumption,
                requireFeature.details(),
                requireFeature.server()
            );
            
            // Check if feature is available
            if (!result.getEval()) {
                logger.warn("Feature '{}' is not available for user '{}'. " +
                           "Method '{}' will not be executed",
                           featureId, userId, joinPoint.getSignature().getName());
                throw new FeatureNotAvailableException(featureId, userId);
            }
            
            logger.debug("Feature '{}' is available for user '{}'. Proceeding with method execution",
                        featureId, userId);
            
            // Proceed with method execution
            return joinPoint.proceed();
            
        } catch (FeatureNotAvailableException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error validating feature '{}': {}",
                        featureId, e.getMessage(), e);
            throw new FeatureNotAvailableException(
                featureId,
                userId != null ? userId : "<<UNKNOWN>>",
                e
            );
        }
    }
    
    /**
     * Parses consumption array into a Map<String, Number>
     * Format: ["requests=10", "storage=1024"]
     */
    private Map<String, Number> parseConsumption(String[] consumptionArray) {
        Map<String, Number> consumption = new HashMap<>();
        
        if (consumptionArray == null || consumptionArray.length == 0) {
            return consumption;
        }
        
        for (String entry : consumptionArray) {
            if (entry == null || entry.isEmpty()) {
                continue;
            }
            
            String[] parts = entry.split("=");
            if (parts.length == 2) {
                String key = parts[0].trim();
                try {
                    Number value;
                    if (parts[1].contains(".")) {
                        value = Double.parseDouble(parts[1].trim());
                    } else {
                        value = Long.parseLong(parts[1].trim());
                    }
                    consumption.put(key, value);
                } catch (NumberFormatException e) {
                    logger.warn("Invalid consumption value for key '{}': {}", key, parts[1]);
                }
            }
        }
        
        return consumption;
    }
}
