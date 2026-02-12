package io.github.pgmarc.space.serializers;

import io.github.pgmarc.space.contracts.Subscription;
import io.github.pgmarc.space.contracts.SubscriptionRequest;
import io.github.pgmarc.space.contracts.UserContact;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SubscriptionRequestSerializerTest {

    @Test
    void givenSubscriptionRequestShouldSerialize() {

        UserContact userContact = UserContact
            .builder("01c36d29-0d6a-4b41-83e9-8c6d9310c508", "johndoe")
            .firstName("John")
            .lastName("Doe")
            .email("john.doe@my-domain.com")
            .phone("+34 666 666 666")
            .build();

        String petclinic = "petclinic";
        String petsAdoptionCentre = "petsAdoptionCentre";
        String petclinicVersion = "2024";
        String petclinicPlan = "GOLD";
        String zoom = "zoom";
        String zoomVersion = "2025";
        String zoomPlan = "ENTERPRISE";
        String hugeMeetings = "hugeMeetings";
        String extraSeats = "extraSeats";
        int renewalDays = 365;
        SubscriptionRequest subReq = SubscriptionRequest.builder(userContact)
            .renewInDays(renewalDays)
            .startService(zoom, zoomVersion)
                .plan(zoomPlan)
                .addOn(extraSeats, 2)
                .addOn(hugeMeetings, 1)
            .endService()
            .startService(petclinic, petclinicVersion)
                .plan(petclinicPlan)
                .addOn(petsAdoptionCentre, 1)
            .endService()
            .build();

        SubscriptionRequestSerializer serializer = new SubscriptionRequestSerializer();
        JSONObject actual = serializer.toJson(subReq);

        JSONObject contractedServices = actual.getJSONObject(Subscription.Keys.CONTRACTED_SERVICES.toString());
        JSONObject subscriptionPlans = actual.getJSONObject(Subscription.Keys.SUBSCRIPTION_PLANS.toString());
        JSONObject subscriptionAddOns = actual.getJSONObject(Subscription.Keys.SUBSCRIPTION_ADDONS.toString());
        int actualRenewal = actual.getJSONObject(Subscription.Keys.BILLING_PERIOD.toString())
                .getInt("renewalDays");
        boolean actualAutoRenew = actual.getJSONObject(Subscription.Keys.BILLING_PERIOD.toString()).getBoolean("autoRenew");

        assertThat(actualRenewal).isEqualTo(renewalDays);
        assertThat(actualAutoRenew).isTrue();
        assertThat(contractedServices.keySet()).contains(zoom, petclinic);
        assertThat(subscriptionPlans.getString(zoom)).isEqualTo(zoomPlan);
        assertThat(subscriptionPlans.getString(petclinic)).isEqualTo(petclinicPlan);
        assertThat(subscriptionAddOns.getJSONObject(zoom).keySet()).contains(hugeMeetings, extraSeats);
        assertThat(subscriptionAddOns.getJSONObject(petclinic).keySet()).contains(petsAdoptionCentre);
    }

}
