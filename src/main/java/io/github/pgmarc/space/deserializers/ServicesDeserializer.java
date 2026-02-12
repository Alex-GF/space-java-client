package io.github.pgmarc.space.deserializers;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import io.github.pgmarc.space.contracts.Service;
import io.github.pgmarc.space.contracts.Subscription;

final class ServicesDeserializer implements JsonDeserializable<Map<String, Service>> {

    @Override
    public Map<String, Service> fromJson(JSONObject json) {

        JSONObject contractedServices = json.getJSONObject(Subscription.Keys.CONTRACTED_SERVICES.toString());
        JSONObject subscriptionPlans = json.getJSONObject(Subscription.Keys.SUBSCRIPTION_PLANS.toString());
        JSONObject subscriptionAddOns = json.getJSONObject(Subscription.Keys.SUBSCRIPTION_ADDONS.toString());
        Map<String, Service> services = new HashMap<>();

        for (String serviceName : contractedServices.keySet()) {
            Service.Builder serviceBuilder = Service.builder(serviceName, contractedServices.getString(serviceName))
                    .plan(subscriptionPlans.getString(serviceName));

            for (String addOnName : subscriptionAddOns.getJSONObject(serviceName).keySet()) {
                serviceBuilder.addOn(addOnName, subscriptionAddOns.getJSONObject(serviceName).getLong(addOnName));
            }
            services.put(serviceName, serviceBuilder.build());
        }
        return services;
    }
}
