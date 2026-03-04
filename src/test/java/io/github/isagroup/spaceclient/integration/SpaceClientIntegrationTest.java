package io.github.isagroup.spaceclient.integration;

import io.github.isagroup.spaceclient.SpaceClient;
import io.github.isagroup.spaceclient.mock.SpaceMockServer;
import io.github.isagroup.spaceclient.types.*;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests using the SpaceMockServer to simulate real SPACE API responses.
 * These tests demonstrate how the client interacts with the API without requiring
 * a real SPACE instance.
 */
@DisplayName("SpaceClient Integration Tests with Mock Server")
class SpaceClientIntegrationTest {
    
    private static SpaceMockServer mockServer;
    private SpaceClient client;
    
    @BeforeAll
    static void setupMockServer() throws IOException {
        mockServer = new SpaceMockServer();
        mockServer.start();
    }
    
    @AfterAll
    static void shutdownMockServer() throws IOException {
        mockServer.shutdown();
    }
    
    @BeforeEach
    void setUp() {
        mockServer.reset();
        
        SpaceConnectionOptions options = new SpaceConnectionOptions(
            mockServer.getUrl(),
            "test-api-key"
        );
        options.setCache(new CacheOptions(false, CacheOptions.CacheType.BUILTIN, 300));
        
        client = new SpaceClient(options);
    }
    
    @AfterEach
    void tearDown() {
        if (client != null) {
            client.disconnect();
        }
    }
    
    @Test
    @DisplayName("Should get contract from SPACE API")
    void shouldGetContractFromApi() {
        // Arrange
        String userId = "user123";
        
        // Act
        Contract contract = client.contracts.getContract(userId);
        
        // Assert
        assertThat(contract).isNotNull();
        assertThat(contract.getUserId()).isEqualTo(userId);
        assertThat(contract.getUserContact()).isNotNull();
        assertThat(contract.getBillingPeriod()).isNotNull();
    }
    
    @Test
    @DisplayName("Should create contract via SPACE API")
    void shouldCreateContractViaApi() {
        // Arrange
        String userId = "user456";
        
        UserContact userContact = new UserContact();
        userContact.setUserId(userId);
        userContact.setUsername("john_doe");
        userContact.setFirstName("John");
        userContact.setLastName("Doe");
        userContact.setEmail("john@example.com");
        
        ContractToCreate.BillingPeriodToCreate billingPeriod = new ContractToCreate.BillingPeriodToCreate();
        billingPeriod.setAutoRenew(true);
        billingPeriod.setRenewalDays(30);
        
        Map<String, String> contractedServices = new HashMap<>();
        contractedServices.put("zoom", "2025");
        
        Map<String, String> subscriptionPlans = new HashMap<>();
        subscriptionPlans.put("zoom", "ENTERPRISE");
        
        Map<String, Map<String, Integer>> subscriptionAddOns = new HashMap<>();
        Map<String, Integer> zoomAddons = new HashMap<>();
        zoomAddons.put("extraSeats", 2);
        subscriptionAddOns.put("zoom", zoomAddons);
        
        ContractToCreate contractToCreate = new ContractToCreate();
        contractToCreate.setUserContact(userContact);
        contractToCreate.setBillingPeriod(billingPeriod);
        contractToCreate.setContractedServices(contractedServices);
        contractToCreate.setSubscriptionPlans(subscriptionPlans);
        contractToCreate.setSubscriptionAddOns(subscriptionAddOns);
        
        // Act
        Contract contract = client.contracts.addContract(contractToCreate);
        
        // Assert
        assertThat(contract).isNotNull();
        assertThat(contract.getUserId()).isEqualTo(userId);
        assertThat(contract.getContractedServices()).containsEntry("zoom", "2025");
        assertThat(contract.getSubscriptionPlans()).containsEntry("zoom", "ENTERPRISE");
    }
    
    @Test
    @DisplayName("Should update contract subscription via SPACE API")
    void shouldUpdateContractSubscriptionViaApi() {
        // Arrange
        String userId = "user789";
        
        // First create a contract
        Map<String, Object> initialContract = new HashMap<>();
        Map<String, Object> userContact = new HashMap<>();
        userContact.put("userId", userId);
        userContact.put("username", "jane_doe");
        initialContract.put("userContact", userContact);
        
        Map<String, String> initialServices = new HashMap<>();
        initialServices.put("zoom", "2024");
        initialContract.put("contractedServices", initialServices);
        
        Map<String, String> initialPlans = new HashMap<>();
        initialPlans.put("zoom", "PRO");
        initialContract.put("subscriptionPlans", initialPlans);
        
        mockServer.setContract(userId, initialContract);
        
        // Prepare update
        Map<String, String> newPlans = new HashMap<>();
        newPlans.put("zoom", "ENTERPRISE");
        
        Map<String, Map<String, Integer>> newAddOns = new HashMap<>();
        Map<String, Integer> zoomAddons = new HashMap<>();
        zoomAddons.put("extraSeats", 5);
        newAddOns.put("zoom", zoomAddons);
        
        Subscription newSubscription = new Subscription();
        newSubscription.setSubscriptionPlans(newPlans);
        newSubscription.setSubscriptionAddOns(newAddOns);
        
        // Act
        Contract updatedContract = client.contracts.updateContractSubscription(userId, newSubscription);
        
        // Assert
        assertThat(updatedContract).isNotNull();
        assertThat(updatedContract.getSubscriptionPlans()).containsEntry("zoom", "ENTERPRISE");
        assertThat(updatedContract.getSubscriptionAddOns()).containsKey("zoom");
    }
    
    @Test
    @DisplayName("Should update usage levels via SPACE API")
    void shouldUpdateUsageLevelsViaApi() {
        // Arrange
        String userId = "user999";
        String serviceName = "zoom";
        
        Map<String, Number> usageLevels = new HashMap<>();
        usageLevels.put("maxSeats", 10);
        usageLevels.put("maxMeetings", 50);
        
        // Act
        Contract updatedContract = client.contracts.updateContractUsageLevels(userId, serviceName, usageLevels);
        
        // Assert
        assertThat(updatedContract).isNotNull();
        assertThat(updatedContract.getUsageLevels()).isNotNull();
    }
    
    @Test
    @DisplayName("Should evaluate feature as available via SPACE API")
    void shouldEvaluateFeatureAsAvailableViaApi() {
        // Arrange
        String userId = "user111";
        String featureId = "zoom-premium-export";
        
        // Configure mock to return feature as available
        mockServer.setFeatureEvaluation(userId, featureId, true);
        
        // Act
        FeatureEvaluationResult result = client.features.evaluate(userId, featureId);
        
        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getEval()).isTrue();
    }
    
    @Test
    @DisplayName("Should evaluate feature as not available via SPACE API")
    void shouldEvaluateFeatureAsNotAvailableViaApi() {
        // Arrange
        String userId = "user222";
        String featureId = "zoom-enterprise-only";
        
        // Configure mock to return feature as not available
        mockServer.setFeatureEvaluation(userId, featureId, false);
        
        // Act
        FeatureEvaluationResult result = client.features.evaluate(userId, featureId);
        
        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getEval()).isFalse();
    }
    
    @Test
    @DisplayName("Should evaluate feature with consumption tracking via SPACE API")
    void shouldEvaluateFeatureWithConsumptionViaApi() {
        // Arrange
        String userId = "user333";
        String featureId = "zoom-concurrent-meetings";
        
        Map<String, Number> expectedConsumption = new HashMap<>();
        expectedConsumption.put("meetings", 5);
        expectedConsumption.put("bandwidth", 100.5);
        
        // Configure mock
        mockServer.setFeatureEvaluation(userId, featureId, true);
        
        // Act
        FeatureEvaluationResult result = client.features.evaluate(userId, featureId, expectedConsumption);
        
        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getEval()).isTrue();
    }
    
    @Test
    @DisplayName("Should evaluate feature with server-side evaluation via SPACE API")
    void shouldEvaluateFeatureWithServerSideEvaluationViaApi() {
        // Arrange
        String userId = "user444";
        String featureId = "zoom-recording";
        
        Map<String, Number> expectedConsumption = new HashMap<>();
        
        // Configure mock
        mockServer.setFeatureEvaluation(userId, featureId, true);
        
        // Act
        FeatureEvaluationResult result = client.features.evaluate(
            userId, 
            featureId, 
            expectedConsumption,
            false, // details
            true   // server-side
        );
        
        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getEval()).isTrue();
    }
    
    @Test
    @DisplayName("Should revert feature evaluation via SPACE API")
    void shouldRevertFeatureEvaluationViaApi() {
        // Arrange
        String userId = "user555";
        String featureId = "zoom-storage";
        
        // Act
        boolean success = client.features.revertEvaluation(userId, featureId, true);
        
        // Assert
        assertThat(success).isTrue();
    }
    
    @Test
    @DisplayName("Should get pricing token via SPACE API")
    void shouldGetPricingTokenViaApi() {
        // Arrange
        String userId = "user666";
        String expectedToken = "custom_jwt_token_for_test";
        
        mockServer.setPricingToken(userId, expectedToken);
        
        // Act
        String token = client.features.generateUserPricingToken(userId);
        
        // Assert
        assertThat(token).isNotNull();
        assertThat(token).isEqualTo(expectedToken);
    }
    
    @Test
    @DisplayName("Should handle multiple sequential operations correctly")
    void shouldHandleMultipleSequentialOperationsCorrectly() {
        // Arrange
        String userId = "user777";
        
        // Create contract
        UserContact userContact = new UserContact();
        userContact.setUserId(userId);
        userContact.setUsername("test_user");
        userContact.setEmail("test@example.com");
        
        ContractToCreate.BillingPeriodToCreate billingPeriod = new ContractToCreate.BillingPeriodToCreate();
        billingPeriod.setAutoRenew(true);
        
        Map<String, String> contractedServices = new HashMap<>();
        contractedServices.put("petclinic", "2024");
        
        Map<String, String> subscriptionPlans = new HashMap<>();
        subscriptionPlans.put("petclinic", "GOLD");
        
        ContractToCreate contractToCreate = new ContractToCreate();
        contractToCreate.setUserContact(userContact);
        contractToCreate.setBillingPeriod(billingPeriod);
        contractToCreate.setContractedServices(contractedServices);
        contractToCreate.setSubscriptionPlans(subscriptionPlans);
        contractToCreate.setSubscriptionAddOns(new HashMap<>());
        
        // Act & Assert
        
        // 1. Create contract
        Contract contract = client.contracts.addContract(contractToCreate);
        assertThat(contract).isNotNull();
        assertThat(contract.getUserId()).isEqualTo(userId);
        
        // 2. Get contract
        Contract retrievedContract = client.contracts.getContract(userId);
        assertThat(retrievedContract).isNotNull();
        assertThat(retrievedContract.getUserId()).isEqualTo(userId);
        
        // 3. Update usage levels
        Map<String, Number> usageLevels = new HashMap<>();
        usageLevels.put("maxPets", 5);
        Contract updatedContract = client.contracts.updateContractUsageLevels(userId, "petclinic", usageLevels);
        assertThat(updatedContract).isNotNull();
        
        // 4. Evaluate feature
        mockServer.setFeatureEvaluation(userId, "petclinic-adoption-center", true);
        FeatureEvaluationResult evaluation = client.features.evaluate(userId, "petclinic-adoption-center");
        assertThat(evaluation).isNotNull();
        assertThat(evaluation.getEval()).isTrue();
    }
    
    @Test
    @DisplayName("Should handle API errors gracefully")
    void shouldHandleApiErrorsGracefully() {
        // Arrange - configure a custom feature evaluation with error
        String userId = "user888";
        String featureId = "invalid-feature";
        
        Map<String, Object> errorEvaluation = new HashMap<>();
        errorEvaluation.put("eval", false);
        errorEvaluation.put("used", new HashMap<>());
        errorEvaluation.put("limit", new HashMap<>());
        
        Map<String, Object> error = new HashMap<>();
        error.put("code", "FEATURE_NOT_FOUND");
        error.put("message", "The specified feature does not exist");
        errorEvaluation.put("error", error);
        
        mockServer.setFeatureEvaluation(userId, featureId, errorEvaluation);
        
        // Act
        FeatureEvaluationResult result = client.features.evaluate(userId, featureId);
        
        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getEval()).isFalse();
        assertThat(result.getError()).isNotNull();
        assertThat(result.getError().getCode()).isEqualTo("FEATURE_NOT_FOUND");
    }
}
