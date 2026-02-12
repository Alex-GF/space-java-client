package io.github.pgmarc.space.examples;

import io.github.pgmarc.space.SpaceClient;
import io.github.pgmarc.space.features.FeatureEvaluationResult;
import io.github.pgmarc.space.features.Revert;
import io.github.pgmarc.space.features.UsageLimitConsumption;

import java.io.IOException;
import java.util.Objects;

final class FeatureEvaluation {


    public static void main(String[] args) throws IOException {

        String apiKey = System.getenv("SPACE_API_KEY");
        Objects.requireNonNull(apiKey, "you must set SPACE_API_KEY env variable");
        SpaceClient client = SpaceClient.builder("localhost", apiKey).build();

        String userId = "4427d118-073d-4da2-a145-f77a75b52595";
        String service = "WireMock";
        String feature = "mockAPI";
        evaluateFeature(client, userId, service, feature);
        evaluateWithConsumption(client, userId, service, feature);
        revertFeatureOptimisticEvaluation(client, userId, service, feature);
        getPricingToken(client, userId);
    }

    private static void evaluateFeature(SpaceClient client, String userId, String service, String feature) throws IOException {
        FeatureEvaluationResult eval = client.features().evaluate(userId, service, feature);

        if (eval.isAvailable()) {
            System.out.println("User with id '" + userId + "' is able to use " + service + " " + feature);
        } else {
            System.out.println("User with id '" + userId + "' is not able to use " + service + " " + feature);
        }
    }

    private static void evaluateWithConsumption(SpaceClient client, String userId, String service, String feature) throws IOException {
        String usageLimit = "mockAPICallsLimit";
        UsageLimitConsumption consumption = UsageLimitConsumption.builder(service)
            .addInt(usageLimit, 1)
            .build();
        FeatureEvaluationResult eval = client.features().evaluateOptimistically(userId, service, feature, consumption);

        if (eval.isAvailable()) {
            System.out.println("User with id '" + userId + "' is able to use " + service + " " + feature);
            Number consumed = eval.getConsumed(usageLimit).orElse(null);
            Number limit = eval.getLimit(usageLimit).orElse(null);
            if (consumed != null) {
                System.out.println("Consumed " + consumed + " API calls out of " + limit);
            }
        } else {
            System.out.println("User with id '" + userId + "' is not able to use " + service + " " + feature);
        }
    }

    private static void revertFeatureOptimisticEvaluation(SpaceClient client, String userId, String service, String feature) throws IOException {
        boolean success = client.features().revert(userId, service, feature, Revert.NEWEST_VALUE);
        System.out.println("Revert operation has " + (success ? "succeeded" : "failed"));
    }

    private static void getPricingToken(SpaceClient client, String userId) throws IOException {

        String pricingJwtToken = client.features().generatePricingTokenForUser(userId);
        System.out.println(pricingJwtToken);
    }
}
