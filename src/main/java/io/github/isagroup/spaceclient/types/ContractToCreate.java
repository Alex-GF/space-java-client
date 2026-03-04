package io.github.isagroup.spaceclient.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

/**
 * Contract creation request
 */
public class ContractToCreate {
    
    @JsonProperty("userContact")
    private UserContact userContact;
    
    @JsonProperty("billingPeriod")
    private BillingPeriodToCreate billingPeriod;
    
    @JsonProperty("contractedServices")
    private Map<String, String> contractedServices;
    
    @JsonProperty("subscriptionPlans")
    private Map<String, String> subscriptionPlans;
    
    @JsonProperty("subscriptionAddOns")
    private Map<String, Map<String, Integer>> subscriptionAddOns;

    public ContractToCreate() {
    }

    public ContractToCreate(UserContact userContact, BillingPeriodToCreate billingPeriod, Map<String, String> contractedServices,
                           Map<String, String> subscriptionPlans, Map<String, Map<String, Integer>> subscriptionAddOns) {
        this.userContact = userContact;
        this.billingPeriod = billingPeriod;
        this.contractedServices = contractedServices;
        this.subscriptionPlans = subscriptionPlans;
        this.subscriptionAddOns = subscriptionAddOns;
    }

    // Getters and Setters
    public UserContact getUserContact() {
        return userContact;
    }

    public void setUserContact(UserContact userContact) {
        this.userContact = userContact;
    }

    public BillingPeriodToCreate getBillingPeriod() {
        return billingPeriod;
    }

    public void setBillingPeriod(BillingPeriodToCreate billingPeriod) {
        this.billingPeriod = billingPeriod;
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

    /**
     * Billing period creation options
     */
    public static class BillingPeriodToCreate {
        @JsonProperty("autoRenew")
        private Boolean autoRenew;
        
        @JsonProperty("renewalDays")
        private Integer renewalDays;

        public BillingPeriodToCreate() {
        }

        public BillingPeriodToCreate(Boolean autoRenew, Integer renewalDays) {
            this.autoRenew = autoRenew;
            this.renewalDays = renewalDays;
        }

        public Boolean getAutoRenew() {
            return autoRenew;
        }

        public void setAutoRenew(Boolean autoRenew) {
            this.autoRenew = autoRenew;
        }

        public Integer getRenewalDays() {
            return renewalDays;
        }

        public void setRenewalDays(Integer renewalDays) {
            this.renewalDays = renewalDays;
        }
    }
}
