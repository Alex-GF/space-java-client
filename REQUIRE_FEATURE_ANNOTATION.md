# @RequireFeature Annotation Documentation

## Overview

The `@RequireFeature` annotation provides a simple, declarative way to enforce feature access control in your Spring application using SPACE. When applied to a method, it automatically:

1. Resolves the current user from your application context (via `SpringPricingConfigurator`)
2. Evaluates the specified feature for that user
3. Prevents method execution if the feature is not available
4. Throws `FeatureNotAvailableException` if access is denied

## Architecture

The annotation system is built on three core components:

```
┌─────────────────────────────────────────────────────┐
│         @RequireFeature Annotation                  │
│  (Declarative - what feature to validate)           │
└─────────────────────┬───────────────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────────────┐
│      RequireFeatureAspect (AspectJ)                 │
│  (Intercepts method calls and triggers validation)  │
└─────────────────────┬───────────────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────────────┐
│   SpringPricingConfigurator (Abstract)              │
│  (Knows HOW to get user ID and SPACE credentials)   │
└─────────────────────┬───────────────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────────────┐
│   Your Implementation (e.g., ExamplePricingConfig)  │
│  (YOUR business logic for user resolution)          │
└─────────────────────────────────────────────────────┘
```

## Prerequisites

To use the `@RequireFeature` annotation, you need:

1. **Spring Framework** with AspectJ support
2. **AspectJ** weaving enabled (`@EnableAspectJAutoProxy`)
3. **A `SpringPricingConfigurator` bean** in your Spring context
4. **SPACE server** configured with URL and API key

## Quick Start

### 1. Create Your SpringPricingConfigurator Implementation

```java
import io.github.isagroup.spaceclient.spring.config.SpringPricingConfigurator;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.stereotype.Component;
import org.springframework.security.core.context.SecurityContextHolder;

@Component
public class MyPricingConfigurator extends SpringPricingConfigurator {
    
    @Override
    public String resolveUserId(ProceedingJoinPoint joinPoint) {
        // Get the current user from Spring Security
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : null;
    }
}
```

### 2. Enable AspectJ in Your Spring Configuration

```java
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Configuration
@EnableAspectJAutoProxy
public class AppConfig {
    // Your other beans and configurations
}
```

### 3. Use the Annotation

```java
import io.github.isagroup.spaceclient.annotations.RequireFeature;
import org.springframework.stereotype.Service;

@Service
public class PremiumService {
    
    @RequireFeature("premium-service-export-data")
    public void exportData() {
        System.out.println("Exporting data...");
        // This only executes if feature is available
    }
}
```

### 4. Set Environment Variables

```bash
export space.client.url="http://localhost:3000"
export space.client.api-key="your-api-key"
```

## Annotation Attributes

| Attribute | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| `value` | String | ✓ Yes | - | Feature identifier (`serviceName-featureName`) |
| `server` | boolean | ✗ No | true | Use server-side evaluation |
| `consumption` | String[] | ✗ No | {} | Expected consumption values |
| `details` | boolean | ✗ No | false | Include details in evaluation |

## Usage Examples

### 1. Basic Feature Validation

```java
@RequireFeature("premium-service-export")
public void exportPremiumReport() {
    // Method only executes if feature is available
    System.out.println("Exporting premium report");
}
```

### 2. With Consumption Tracking

```java
@RequireFeature(
    value = "storage-service-large-files",
    consumption = {"file_size=5120", "bandwidth=100"}
)
public void uploadLargeFile() {
    // Validates with consumption expectations
    System.out.println("Uploading large file");
}
```

### 3. With Client-Side Evaluation

```java
@RequireFeature(
    value = "api-service-batch-operations",
    server = false  // Use client-side evaluation
)
public void processBatch() {
    System.out.println("Processing batch operation");
}
```

### 4. With Detailed Results

```java
@RequireFeature(
    value = "analytics-service-advanced",
    details = true
)
public void generateAdvancedAnalytics() {
    // Includes detailed evaluation results
    System.out.println("Generating advanced analytics");
}
```

## SpringPricingConfigurator: The Core

### Abstract Methods

Your implementation must override `resolveUserId()`:

```java
public abstract String resolveUserId(ProceedingJoinPoint joinPoint);
```

### Protected Methods (Optional to Override)

```java
protected String getSpaceUrl() {
    // Returns SPACE server URL
    // Default: reads from environment variables
}

protected String getSpaceApiKey() {
    // Returns SPACE API key
    // Default: reads from environment variables
}

protected int getTimeout() {
    // Returns connection timeout in milliseconds
    // Default: 10000
}
```

## Implementation Examples

### Example 1: Spring Security Integration

```java
@Component
public class SecurityPricingConfigurator extends SpringPricingConfigurator {
    
    @Override
    public String resolveUserId(ProceedingJoinPoint joinPoint) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.isAuthenticated() 
            ? auth.getName() 
            : null;
    }
}
```

### Example 2: Multi-Source Resolution

```java
@Component
public class ComprehensivePricingConfigurator extends SpringPricingConfigurator {
    
    @Override
    public String resolveUserId(ProceedingJoinPoint joinPoint) {
        // Try Spring Security first
        String userId = getFromSpringSecurityContext();
        if (userId != null) return userId;
        
        // Try HTTP request context
        userId = getFromHttpRequest();
        if (userId != null) return userId;
        
        // Try method parameters
        return getFromMethodParameters(joinPoint);
    }
    
    private String getFromSpringSecurityContext() {
        try {
            var auth = SecurityContextHolder.getContext().getAuthentication();
            return auth != null ? auth.getName() : null;
        } catch (Exception e) {
            return null;
        }
    }
    
    private String getFromHttpRequest() {
        try {
            var request = RequestContextHolder.getRequestAttributes();
            var httpRequest = ((ServletRequestAttributes) request).getRequest();
            return httpRequest.getHeader("X-User-ID");
        } catch (Exception e) {
            return null;
        }
    }
    
    private String getFromMethodParameters(ProceedingJoinPoint joinPoint) {
        // Implementation to extract userId from method params
        // ...
    }
}
```

### Example 3: Configuration File Integration

```java
@Component
public class ConfiguredPricingConfigurator extends SpringPricingConfigurator {
    
    @Value("${space.server.url}")
    private String spaceUrl;
    
    @Value("${space.api.key:}")
    private String spaceApiKey;
    
    @Override
    protected String getSpaceUrl() {
        return spaceUrl;
    }
    
    @Override
    protected String getSpaceApiKey() {
        return spaceApiKey;
    }
    
    @Override
    public String resolveUserId(ProceedingJoinPoint joinPoint) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : null;
    }
}
```

## Exception Handling

### FeatureNotAvailableException

Thrown when a method with `@RequireFeature` is called but the feature is not available:

```java
try {
    premiumService.exportData();
} catch (FeatureNotAvailableException e) {
    System.err.println("Feature not available: " + e.getFeatureId());
    System.err.println("User: " + e.getUserId());
    System.err.println("Details: " + e.getMessage());
}
```

### Common Issues

| Issue | Cause | Solution |
|-------|-------|----------|
| Annotation not intercepting | AspectJ not enabled | Add `@EnableAspectJAutoProxy` to config |
| "Could not resolve userId" | `SpringPricingConfigurator.resolveUserId()` returns null | Ensure your implementation returns a valid user ID |
| "SpaceClient not initialized" | Missing environment variables | Set `space.client.url` and `space.client.api-key` |
| Method still executes | Aspect not applied to Spring bean | Ensure method is public and class is a `@Component` or `@Service` |

## Best Practices

1. **Create one `SpringPricingConfigurator` per application**
   - Mark it with `@Component` or define as `@Bean`
   - Make it a singleton

2. **Always validate user resolution**
   - Log when `resolveUserId()` returns null
   - Provide meaningful error messages

3. **Use meaningful feature IDs**
   - Follow the format: `serviceName-featureName`
   - Keep them consistent across your application

4. **Handle exceptions gracefully**
   - Catch `FeatureNotAvailableException` at appropriate levels
   - Return appropriate HTTP status codes in web applications

5. **Test your configuration**
   - Verify `resolveUserId()` works in your context
   - Test with different user roles and permissions

6. **Document your consumption values**
   - Make it clear what consumption arrays mean
   - Coordinate with your SPACE configuration

## Complete Application Example

```java
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;

@SpringBootApplication
@EnableAspectJAutoProxy
public class PricingApplication {
    public static void main(String[] args) {
        SpringApplication.run(PricingApplication.class, args);
    }
}

@Component
public class AppPricingConfigurator extends SpringPricingConfigurator {
    @Override
    public String resolveUserId(ProceedingJoinPoint joinPoint) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : null;
    }
}

@RestController
@Service
public class PremiumController {
    
    @Autowired
    private PremiumService premiumService;
    
    @PostMapping("/export")
    public void exportData() {
        try {
            premiumService.exportData();
            return "Export successful";
        } catch (FeatureNotAvailableException e) {
            return "Feature not available for your plan";
        }
    }
}

@Service
public class PremiumService {
    
    @RequireFeature("premium-service-export")
    public void exportData() {
        // This only runs if feature is available
        System.out.println("Exporting premium data...");
    }
}
```

## Configuration via Environment Variables

```bash
# Standard variables
export space.client.url="http://localhost:3000"
export space.client.api-key="your-api-key"

# Uppercase variants
export SPACE_CLIENT_URL="http://localhost:3000"
export SPACE_CLIENT_API_KEY="your-api-key"
```

## Troubleshooting

### Annotation not being applied
- Verify `@EnableAspectJAutoProxy` is present
- Check that the method is public
- Ensure the class is a Spring bean

### "Could not resolve user ID"
- Verify your `SpringPricingConfigurator.resolveUserId()` implementation
- Add logging to see what's available during execution

### SpaceClient initialization fails
- Check environment variables are set correctly
- Verify SPACE server is accessible
- Confirm API key is valid

