package io.github.isagroup.spaceclient.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import java.util.Map;

/**
 * Contract history entry
 */
public class ContractHistoryEntry {
    
    @JsonProperty("startDate")
    private Date startDate;
    
    @JsonProperty("endDate")
    private Date endDate;
    
    @JsonProperty("contractedServices")
    private Map<String, String> contractedServices;
    
    @JsonProperty("subscriptionPlans")
    private Map<String, String> subscriptionPlans;
    
    @JsonProperty("subscriptionAddOns")
    private Map<String, Map<String, Integer>> subscriptionAddOns;

    public ContractHistoryEntry() {
    }

    // Getters and Setters
    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

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
