package io.github.isagroup.space.client.contracts;

import java.util.Map;
import java.util.Objects;

public record CreateContractRequest(UserContactRequest userContact,
        Map<String, String> subscriptionPlans,
        Map<String, Map<String, Integer>> subscriptionAddOns) {
    public CreateContractRequest {
        Objects.requireNonNull(userContact);
        Objects.requireNonNull(subscriptionAddOns);

    }
}
