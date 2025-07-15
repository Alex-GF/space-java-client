package io.github.isagroup.space.client.services;

import java.util.Map;

/**
 * @param subscriptionPlan plan name of the selected pricing
 * @param additionalAddOns keys of the map are add-ons names and values defined
 *                         the number of times the add-on is contracted
 */
public record FallBackSubscription(String subscriptionPlan, Map<String, Integer> additionalAddOns) {

}
