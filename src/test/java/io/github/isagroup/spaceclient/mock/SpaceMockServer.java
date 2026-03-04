package io.github.isagroup.spaceclient.mock;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Mock server that simulates SPACE API responses based on the OpenAPI specification.
 * This allows tests to run without requiring a real SPACE instance.
 */
public class SpaceMockServer {
    
    private final MockWebServer mockWebServer;
    private final ObjectMapper objectMapper;
    private final Map<String, Object> contracts;
    private final Map<String, Map<String, Object>> featureEvaluations;
    private final Map<String, String> pricingTokens;
    
    public SpaceMockServer() {
        this.mockWebServer = new MockWebServer();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.contracts = new ConcurrentHashMap<>();
        this.featureEvaluations = new ConcurrentHashMap<>();
        this.pricingTokens = new ConcurrentHashMap<>();
        
        setupDispatcher();
    }
    
    /**
     * Sets up the dispatcher to route requests to appropriate handlers
     */
    private void setupDispatcher() {
        mockWebServer.setDispatcher(new Dispatcher() {
            @NotNull
            @Override
            public MockResponse dispatch(@NotNull RecordedRequest request) {
                String path = request.getPath();
                String method = request.getMethod();
                
                // Remove /api/v1 prefix if present
                if (path.startsWith("/api/v1")) {
                    path = path.substring(7);
                }
                
                try {
                    // Features endpoints - check pricing-token first (more specific)
                    if (path.matches("/features/[^/]+/pricing-token.*")) {
                        return handlePricingToken(request, path);
                    }
                    
                    if (path.matches("/features/[^/]+\\?.*revert=true.*") && "POST".equals(method)) {
                        return new MockResponse()
                            .setResponseCode(200)
                            .setHeader("Content-Type", "application/json")
                            .setBody("{\"success\":true,\"message\":\"Usage level reverted\"}");
                    }
                    
                    if (path.matches("/features/[^/]+/[^/]+.*")) {
                        return handleFeatureEvaluation(request, path);
                    }
                    
                    // Contracts endpoints
                    if (path.matches("/contracts/[^/]+/usageLevels.*") && "PUT".equals(method)) {
                        return handleUpdateUsageLevels(request, path);
                    }
                    
                    if (path.matches("/contracts/[^/]+.*")) {
                        if ("GET".equals(method)) {
                            return handleGetContract(request, path);
                        } else if ("PUT".equals(method)) {
                            return handleUpdateContract(request, path);
                        } else if ("DELETE".equals(method)) {
                            return handleDeleteContract(request, path);
                        }
                    }
                    
                    if ("/contracts".equals(path) && "POST".equals(method)) {
                        return handleCreateContract(request);
                    }
                    
                    // Services endpoints
                    if (path.matches("/services/[^/]+.*")) {
                        return handleGetService(request, path);
                    }
                    
                    // Organizations endpoints
                    if (path.matches("/organizations/[^/]+.*")) {
                        return handleGetOrganization(request, path);
                    }
                    
                    // Healthcheck
                    if ("/healthcheck".equals(path)) {
                        return handleHealthcheck();
                    }
                    
                    return new MockResponse()
                        .setResponseCode(404)
                        .setBody("{\"error\":\"Not Found\"}");
                        
                } catch (Exception e) {
                    return new MockResponse()
                        .setResponseCode(500)
                        .setBody("{\"error\":\"Internal Server Error: " + e.getMessage() + "\"}");
                }
            }
        });
    }
    
    /**
     * Handles feature evaluation requests
     * POST /api/v1/features/{userId}/{featureId}
     */
    private MockResponse handleFeatureEvaluation(RecordedRequest request, String path) throws IOException {
        String[] pathParts = path.split("/");
        
        if (pathParts.length < 4) {
            return new MockResponse().setResponseCode(400).setBody("{\"error\":\"Invalid path\"}");
        }
        
        String userId = pathParts[2];
        String featureId = pathParts[3].split("\\?")[0];
        String queryString = path.contains("?") ? path.substring(path.indexOf("?")) : "";
        
        // Check if this is a revert operation
        if (queryString.contains("revert=true")) {
            return new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("{\"success\":true,\"message\":\"Usage level reverted\"}");
        }
        
        // Parse expected consumption from request body
        String body = request.getBody().readUtf8();
        @SuppressWarnings("unchecked")
        Map<String, Object> expectedConsumption = objectMapper.readValue(body, Map.class);
        
        // Check if we have a pre-configured evaluation result
        String evalKey = userId + ":" + featureId;
        Map<String, Object> evaluation = featureEvaluations.get(evalKey);
        
        if (evaluation == null) {
            // Default: feature is available
            evaluation = createDefaultFeatureEvaluation(true, expectedConsumption);
        }
        
        String responseBody = objectMapper.writeValueAsString(evaluation);
        
        return new MockResponse()
            .setResponseCode(200)
            .setHeader("Content-Type", "application/json")
            .setBody(responseBody);
    }
    
    /**
     * Handles pricing token generation requests
     * POST /api/v1/features/{userId}/pricing-token
     */
    private MockResponse handlePricingToken(RecordedRequest request, String path) {
        String[] pathParts = path.split("/");
        String userId = pathParts[2];
        
        String token = pricingTokens.getOrDefault(userId, "mock_jwt_token_" + userId);
        
        String response = "{\"pricingToken\":\"" + token + "\"}";
        
        return new MockResponse()
            .setResponseCode(200)
            .setHeader("Content-Type", "application/json")
            .setBody(response);
    }
    
    /**
     * Handles get contract requests
     * GET /api/v1/contracts/{userId}
     */
    private MockResponse handleGetContract(RecordedRequest request, String path) throws IOException {
        String[] pathParts = path.split("/");
        String userId = pathParts[2].split("\\?")[0];
        
        Object contract = contracts.get(userId);
        
        if (contract == null) {
            // Return default contract
            contract = createDefaultContract(userId);
        }
        
        String responseBody = objectMapper.writeValueAsString(contract);
        
        return new MockResponse()
            .setResponseCode(200)
            .setHeader("Content-Type", "application/json")
            .setBody(responseBody);
    }
    
    /**
     * Handles create contract requests
     * POST /api/v1/contracts
     */
    private MockResponse handleCreateContract(RecordedRequest request) throws IOException {
        String body = request.getBody().readUtf8();
        @SuppressWarnings("unchecked")
        Map<String, Object> contractData = objectMapper.readValue(body, Map.class);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> userContact = (Map<String, Object>) contractData.get("userContact");
        String userId = (String) userContact.get("userId");
        
        // Create contract with provided data
        Map<String, Object> contract = createContractFromData(contractData);
        contracts.put(userId, contract);
        
        String responseBody = objectMapper.writeValueAsString(contract);
        
        return new MockResponse()
            .setResponseCode(201)
            .setHeader("Content-Type", "application/json")
            .setBody(responseBody);
    }
    
    /**
     * Handles update contract requests
     * PUT /api/v1/contracts/{userId}
     */
    private MockResponse handleUpdateContract(RecordedRequest request, String path) throws IOException {
        String[] pathParts = path.split("/");
        String userId = pathParts[2].split("\\?")[0];
        
        String body = request.getBody().readUtf8();
        @SuppressWarnings("unchecked")
        Map<String, Object> updates = objectMapper.readValue(body, Map.class);
        
        Object existingContract = contracts.get(userId);
        Map<String, Object> contract;
        
        if (existingContract == null) {
            contract = createDefaultContract(userId);
        } else {
            @SuppressWarnings("unchecked")
            Map<String, Object> existing = (Map<String, Object>) existingContract;
            contract = new HashMap<>(existing);
        }
        
        // Update contract fields
        if (updates.containsKey("subscriptionPlans")) {
            contract.put("subscriptionPlans", updates.get("subscriptionPlans"));
        }
        if (updates.containsKey("subscriptionAddOns")) {
            contract.put("subscriptionAddOns", updates.get("subscriptionAddOns"));
        }
        if (updates.containsKey("contractedServices")) {
            contract.put("contractedServices", updates.get("contractedServices"));
        }
        
        contracts.put(userId, contract);
        
        String responseBody = objectMapper.writeValueAsString(contract);
        
        return new MockResponse()
            .setResponseCode(200)
            .setHeader("Content-Type", "application/json")
            .setBody(responseBody);
    }
    
    /**
     * Handles update usage levels requests
     * PUT /api/v1/contracts/{userId}/usageLevels
     */
    private MockResponse handleUpdateUsageLevels(RecordedRequest request, String path) throws IOException {
        String[] pathParts = path.split("/");
        String userId = pathParts[2];
        
        String body = request.getBody().readUtf8();
        @SuppressWarnings("unchecked")
        Map<String, Object> usageLevelsUpdate = objectMapper.readValue(body, Map.class);
        
        Object existingContract = contracts.get(userId);
        Map<String, Object> contract;
        
        if (existingContract == null) {
            contract = createDefaultContract(userId);
        } else {
            @SuppressWarnings("unchecked")
            Map<String, Object> existing = (Map<String, Object>) existingContract;
            contract = new HashMap<>(existing);
        }
        
        // Update usage levels
        @SuppressWarnings("unchecked")
        Map<String, Object> usageLevels = (Map<String, Object>) contract.get("usageLevels");
        if (usageLevels == null) {
            usageLevels = new HashMap<>();
            contract.put("usageLevels", usageLevels);
        }
        
        // Merge the updates
        for (Map.Entry<String, Object> entry : usageLevelsUpdate.entrySet()) {
            String serviceName = entry.getKey();
            @SuppressWarnings("unchecked")
            Map<String, Object> serviceUsage = (Map<String, Object>) entry.getValue();
            
            @SuppressWarnings("unchecked")
            Map<String, Object> existingServiceUsage = (Map<String, Object>) usageLevels.get(serviceName);
            if (existingServiceUsage == null) {
                existingServiceUsage = new HashMap<>();
                usageLevels.put(serviceName, existingServiceUsage);
            }
            
            for (Map.Entry<String, Object> usageEntry : serviceUsage.entrySet()) {
                String limitName = usageEntry.getKey();
                Object newValue = usageEntry.getValue();
                
                Map<String, Object> limitData = new HashMap<>();
                limitData.put("consumed", newValue);
                existingServiceUsage.put(limitName, limitData);
            }
        }
        
        contracts.put(userId, contract);
        
        String responseBody = objectMapper.writeValueAsString(contract);
        
        return new MockResponse()
            .setResponseCode(200)
            .setHeader("Content-Type", "application/json")
            .setBody(responseBody);
    }
    
    /**
     * Handles delete contract requests
     * DELETE /api/v1/contracts/{userId}
     */
    private MockResponse handleDeleteContract(RecordedRequest request, String path) {
        String[] pathParts = path.split("/");
        String userId = pathParts[2].split("\\?")[0];
        
        contracts.remove(userId);
        
        return new MockResponse()
            .setResponseCode(204);
    }
    
    /**
     * Handles get service requests
     * GET /api/v1/services/{serviceName}
     */
    private MockResponse handleGetService(RecordedRequest request, String path) throws IOException {
        String[] pathParts = path.split("/");
        String serviceName = pathParts[2].split("\\?")[0];
        
        Map<String, Object> service = createDefaultService(serviceName);
        String responseBody = objectMapper.writeValueAsString(service);
        
        return new MockResponse()
            .setResponseCode(200)
            .setHeader("Content-Type", "application/json")
            .setBody(responseBody);
    }
    
    /**
     * Handles get organization requests
     * GET /api/v1/organizations/{organizationId}
     */
    private MockResponse handleGetOrganization(RecordedRequest request, String path) throws IOException {
        String[] pathParts = path.split("/");
        String organizationId = pathParts[2].split("\\?")[0];
        
        Map<String, Object> organization = createDefaultOrganization(organizationId);
        String responseBody = objectMapper.writeValueAsString(organization);
        
        return new MockResponse()
            .setResponseCode(200)
            .setHeader("Content-Type", "application/json")
            .setBody(responseBody);
    }
    
    /**
     * Handles healthcheck requests
     * GET /api/v1/healthcheck
     */
    private MockResponse handleHealthcheck() {
        return new MockResponse()
            .setResponseCode(200)
            .setHeader("Content-Type", "application/json")
            .setBody("{\"message\":\"Service is up and running!\"}");
    }
    
    // Helper methods to create default responses
    
    private Map<String, Object> createDefaultFeatureEvaluation(boolean allowed, Map<String, Object> consumption) {
        Map<String, Object> evaluation = new HashMap<>();
        evaluation.put("eval", allowed);
        evaluation.put("used", new HashMap<>());
        evaluation.put("limit", new HashMap<>());
        
        if (!allowed) {
            Map<String, Object> error = new HashMap<>();
            error.put("code", "FEATURE_NOT_AVAILABLE");
            error.put("message", "Feature is not available in the user's plan");
            evaluation.put("error", error);
        }
        
        return evaluation;
    }
    
    private Map<String, Object> createDefaultContract(String userId) {
        Map<String, Object> contract = new HashMap<>();
        contract.put("id", "contract_" + userId);
        contract.put("organizationId", "org_default");
        
        Map<String, Object> userContact = new HashMap<>();
        userContact.put("userId", userId);
        userContact.put("username", "user_" + userId);
        userContact.put("firstName", "John");
        userContact.put("lastName", "Doe");
        userContact.put("email", userId + "@example.com");
        contract.put("userContact", userContact);
        
        Map<String, Object> billingPeriod = new HashMap<>();
        billingPeriod.put("startDate", Instant.now().toString());
        billingPeriod.put("endDate", Instant.now().plusSeconds(31536000).toString()); // +1 year
        billingPeriod.put("autoRenew", true);
        billingPeriod.put("renewalDays", 30);
        contract.put("billingPeriod", billingPeriod);
        
        contract.put("contractedServices", new HashMap<>());
        contract.put("subscriptionPlans", new HashMap<>());
        contract.put("subscriptionAddOns", new HashMap<>());
        contract.put("usageLevels", new HashMap<>());
        contract.put("history", new java.util.ArrayList<>());
        
        return contract;
    }
    
    private Map<String, Object> createContractFromData(Map<String, Object> contractData) {
        Map<String, Object> contract = new HashMap<>();
        
        @SuppressWarnings("unchecked")
        Map<String, Object> userContact = (Map<String, Object>) contractData.get("userContact");
        String userId = (String) userContact.get("userId");
        
        contract.put("id", "contract_" + userId);
        contract.put("organizationId", "org_default");
        contract.put("userContact", userContact);
        
        Map<String, Object> billingPeriod = new HashMap<>();
        if (contractData.containsKey("billingPeriod")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> bp = (Map<String, Object>) contractData.get("billingPeriod");
            billingPeriod.putAll(bp);
        } else {
            billingPeriod.put("startDate", Instant.now().toString());
            billingPeriod.put("endDate", Instant.now().plusSeconds(31536000).toString());
            billingPeriod.put("autoRenew", true);
            billingPeriod.put("renewalDays", 30);
        }
        contract.put("billingPeriod", billingPeriod);
        
        contract.put("contractedServices", contractData.getOrDefault("contractedServices", new HashMap<>()));
        contract.put("subscriptionPlans", contractData.getOrDefault("subscriptionPlans", new HashMap<>()));
        contract.put("subscriptionAddOns", contractData.getOrDefault("subscriptionAddOns", new HashMap<>()));
        contract.put("usageLevels", new HashMap<>());
        contract.put("history", new java.util.ArrayList<>());
        
        return contract;
    }
    
    private Map<String, Object> createDefaultService(String serviceName) {
        Map<String, Object> service = new HashMap<>();
        service.put("name", serviceName);
        service.put("description", "Mock service: " + serviceName);
        service.put("organizationId", "org_default");
        service.put("available", true);
        service.put("pricings", new java.util.ArrayList<>());
        service.put("createdAt", Instant.now().toString());
        return service;
    }
    
    private Map<String, Object> createDefaultOrganization(String organizationId) {
        Map<String, Object> organization = new HashMap<>();
        organization.put("id", organizationId);
        organization.put("name", "Mock Organization");
        organization.put("owner", "admin");
        organization.put("default", false);
        organization.put("members", new java.util.ArrayList<>());
        organization.put("apiKeys", new java.util.ArrayList<>());
        organization.put("createdAt", Instant.now().toString());
        return organization;
    }
    
    // Public API for test configuration
    
    /**
     * Configures a specific contract for a user
     */
    public void setContract(String userId, Map<String, Object> contract) {
        contracts.put(userId, contract);
    }
    
    /**
     * Configures a feature evaluation result
     */
    public void setFeatureEvaluation(String userId, String featureId, boolean allowed) {
        String key = userId + ":" + featureId;
        featureEvaluations.put(key, createDefaultFeatureEvaluation(allowed, new HashMap<>()));
    }
    
    /**
     * Configures a feature evaluation result with details
     */
    public void setFeatureEvaluation(String userId, String featureId, Map<String, Object> evaluation) {
        String key = userId + ":" + featureId;
        featureEvaluations.put(key, evaluation);
    }
    
    /**
     * Configures a pricing token for a user
     */
    public void setPricingToken(String userId, String token) {
        pricingTokens.put(userId, token);
    }
    
    /**
     * Starts the mock server
     */
    public void start() throws IOException {
        mockWebServer.start();
    }
    
    /**
     * Stops the mock server
     */
    public void shutdown() throws IOException {
        mockWebServer.shutdown();
    }
    
    /**
     * Gets the base URL of the mock server
     */
    public String getUrl() {
        return mockWebServer.url("/").toString().replaceAll("/$", "");
    }
    
    /**
     * Gets the mock web server instance (for advanced configuration)
     */
    public MockWebServer getMockWebServer() {
        return mockWebServer;
    }
    
    /**
     * Resets all configured data
     */
    public void reset() {
        contracts.clear();
        featureEvaluations.clear();
        pricingTokens.clear();
    }
}
