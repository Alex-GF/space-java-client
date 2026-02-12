package io.github.pgmarc.space.serializers;

import org.json.JSONObject;

import io.github.pgmarc.space.contracts.Subscription;
import io.github.pgmarc.space.contracts.SubscriptionUpdateRequest;

public class SubscriptionUpdateRequestSerializer implements JsonSerializable<SubscriptionUpdateRequest> {

    @Override
    public JSONObject toJson(SubscriptionUpdateRequest subscription) {

        return new JSONObject()
                .put(Subscription.Keys.CONTRACTED_SERVICES.toString(),
                        SubscriptionRequestSerializer.contractedServices(subscription.getServices()))
                .put(Subscription.Keys.SUBSCRIPTION_PLANS.toString(),
                        SubscriptionRequestSerializer.subscriptionPlans(subscription.getServices()))
                .put(Subscription.Keys.SUBSCRIPTION_ADDONS.toString(),
                        SubscriptionRequestSerializer.subscriptionAddOns(subscription.getServices()));
    }

}
