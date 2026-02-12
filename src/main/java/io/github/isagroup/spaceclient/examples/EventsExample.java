package io.github.isagroup.spaceclient.examples;

import io.github.isagroup.spaceclient.SpaceClient;
import io.github.isagroup.spaceclient.SpaceClientFactory;

/**
 * Examples of using WebSocket events with Space Java Client
 */
public class EventsExample {

    public static void main(String[] args) throws InterruptedException {
        SpaceClient client = SpaceClientFactory.connect(
            "http://localhost:3000",
            "your-api-key"
        );

        // Register event handlers
        client.on("synchronized", data -> {
            System.out.println("✓ Connected to Space WebSocket");
        });

        client.on("pricing_created", data -> {
            System.out.println("📦 New pricing created: " + data);
        });

        client.on("pricing_archived", data -> {
            System.out.println("📁 Pricing archived: " + data);
        });

        client.on("pricing_actived", data -> {
            System.out.println("✨ Pricing activated: " + data);
        });

        client.on("service_disabled", data -> {
            System.out.println("🚫 Service disabled: " + data);
        });

        client.on("error", error -> {
            System.err.println("❌ WebSocket error: " + error);
        });

        // Connect to WebSocket
        client.connect();

        // Keep the application running to receive events
        System.out.println("Listening for events... Press Ctrl+C to exit");
        
        // Wait for events (in a real application, this would be your main logic)
        Thread.sleep(60000); // Wait for 60 seconds

        // Cleanup
        client.disconnect();
        client.close();
    }
}
