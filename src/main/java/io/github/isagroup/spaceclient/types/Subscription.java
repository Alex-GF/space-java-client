package io.github.isagroup.spaceclient.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

/**
 * Subscription update request
 */
public class Subscription {
    
    @JsonProperty("contractedServices")
    private Map<String, String> contractedServices;
    
    @JsonProperty("subscriptionPlans")
    private Map<String, String> subscriptionPlans;
    
    @JsonProperty("subscriptionAddOns")
    private Map<String, Map<String, Integer>> subscriptionAddOns;

    public Subscription() {
    }

    public Subscription(Map<String, String> contractedServices, Map<String, String> subscriptionPlans,
                       Map<String, Map<String, Integer>> subscriptionAddOns) {
        this.contractedServices = contractedServices;
        this.subscriptionPlans = subscriptionPlans;
        this.subscriptionAddOns = subscriptionAddOns;
    }

    // Getters and Setters
    public Map<String, String> getContractedServices() {
        return contractedServices;
    }

    public void setContractedServices(Map<String, String> contractedServices) {
        this.contractedServices = contractedServices;
    }

    public Map<String, String> getSubscriptionPlans() {
        return subscriptionPlans;
    }

    public void setSubscriptionPlans(Map<String, String> subscriptionPlans) {
        this.subscriptionPlans = subscriptionPlans;
    }

    public Map<String, Map<String, Integer>> getSubscriptionAddOns() {
        return subscriptionAddOns;
    }

    public void setSubscriptionAddOns(Map<String, Map<String, Integer>> subscriptionAddOns) {
        this.subscriptionAddOns = subscriptionAddOns;
    }
}
