package io.github.isagroup.spaceclient.examples;

import io.github.isagroup.spaceclient.SpaceClient;
import io.github.isagroup.spaceclient.SpaceClientFactory;
import io.github.isagroup.spaceclient.types.*;
import io.github.isagroup.spaceclient.types.ContractToCreate.BillingPeriodToCreate;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Basic usage examples for Space Java Client
 */
public class BasicExample {

    public static void main(String[] args) {
        // Create client
        SpaceClient client = SpaceClientFactory.connect(
            "http://localhost:3000",
            "your-api-key"
        );

        try {
            // Check connection
            if (client.isConnectedToSpace()) {
                System.out.println("Successfully connected to Space!");
            }

            // Create a contract
            UserContact userContact = new UserContact("user123", "john_doe");
            userContact.setEmail("john@example.com");

            BillingPeriodToCreate billingPeriod = new BillingPeriodToCreate(true, 30);

            Map<String, String> contractedServices = new HashMap<>();
            contractedServices.put("serviceA", "pricing/v1");

            Map<String, String> subscriptionPlans = new HashMap<>();
            subscriptionPlans.put("serviceA", "basic");

            ContractToCreate contractToCreate = new ContractToCreate(
                userContact,
                billingPeriod,
                contractedServices,
                subscriptionPlans,
                new HashMap<>()
            );

            Contract contract = client.contracts.addContract(contractToCreate);
            System.out.println("Contract created for user: " + contract.getUserId());

            // Evaluate a feature
            FeatureEvaluationResult result = client.features.evaluate(
                "user123",
                "serviceA-feature1"
            );

            if (result.getEval()) {
                System.out.println("Feature is enabled!");
                System.out.println("Used: " + result.getUsed());
                System.out.println("Limit: " + result.getLimit());
            } else {
                System.out.println("Feature is disabled");
                if (result.getError() != null) {
                    System.out.println("Error: " + result.getError().getMessage());
                }
            }

            // Generate pricing token
            String token = client.features.generateUserPricingToken("user123");
            System.out.println("Pricing token: " + token);

        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Always close the client
            client.close();
        }
    }
}
