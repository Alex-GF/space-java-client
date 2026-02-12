package io.github.pgmarc.space.deserializers;

import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import io.github.pgmarc.space.contracts.UsageLevel;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import io.github.pgmarc.space.contracts.Subscription;

import static org.assertj.core.api.Assertions.*;


class SubscriptionSerializerTest {

    private final SubscriptionDeserializer serializer = new SubscriptionDeserializer();

    @Test
    void givenSubscriptionAsJsonShouldCreateSubscription() {

        String maxVisits = "maxVisits";
        double visitsConsumed = 5.0;
        String maxVisitsResetUtc = "2025-07-31T00:00:00Z";

        JSONObject input = new JSONObject(Map.of(
                "id", "68050bd09890322c57842f6f",
                "userContact", Map.of(
                        "userId", "01c36d29-0d6a-4b41-83e9-8c6d9310c508",
                        "username", "johndoe",
                        "fistName", "John",
                        "lastName", "Doe",
                        "email", "john.doe@my-domain.com",
                        "phone", "+34 666 666 666"),
                "billingPeriod", Map.of(
                        "startDate", "2025-12-31T00:00:00Z",
                        "endDate", "2025-12-31T00:00:00Z",
                        "autoRenew", true,
                        "renewalDays", 365),
                "usageLevels", Map.of(
                        "zoom", Map.of(
                                "maxSeats", Map.of("consumed", 10)),
                        "petclinic", Map.of(
                                "maxPets", Map.of("consumed", 2),
                                "maxVisits", Map.of(
                                        "consumed", visitsConsumed,
                                        "resetTimeStamp",  maxVisitsResetUtc))),
                "contractedServices", Map.of(
                        "zoom", "2025",
                        "petclinic", "2024"),
                "subscriptionPlans", Map.of(
                        "zoom", "ENTERPRISE",
                        "petclinic", "GOLD"),
                "subscriptionAddOns", Map.of(
                        "zoom", Map.of(
                                "extraSeats", 2,
                                "hugeMeetings", 1),
                        "petclinic", Map.of(
                                "petsAdoptionCentre", 1)),
                "history", List.of(
                        Map.of(
                                "startDate", "2025-12-31T00:00:00Z",
                                "endDate", "2025-12-31T00:00:00Z",
                                "contractedServices", Map.of(
                                        "zoom", "2025",
                                        "petclinic", "2024"),
                                "subscriptionPlans", Map.of(
                                        "zoom", "ENTERPRISE",
                                        "petclinic", "GOLD"),
                                "subscriptionAddOns", Map.of(
                                        "zoom", Map.of(
                                                "extraSeats", 2,
                                                "hugeMeetings", 1),
                                        "petclinic", Map.of(
                                                "petsAdoptionCentre", 1))))));

        Subscription actual = serializer.fromJson(input);

        assertAll(
                () -> assertThat(actual.getHistory()).hasSize(1),
                () -> assertThat(actual.getUsageLevels()).hasSize(2),
                () -> assertThat(actual.getServicesMap()).hasSize(2));

        ZonedDateTime maxVisitsExpiration = ZonedDateTime.parse(maxVisitsResetUtc);

        UsageLevel actualLevel = actual.getUsageLevels().get("petclinic").get(maxVisits);
        assertThat(actualLevel).isEqualTo(UsageLevel.of(maxVisits,visitsConsumed, maxVisitsExpiration));

    }

}
