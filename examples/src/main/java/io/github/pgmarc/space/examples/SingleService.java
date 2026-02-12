package io.github.pgmarc.space.examples;

import io.github.pgmarc.space.SpaceClient;
import io.github.pgmarc.space.contracts.Subscription;
import io.github.pgmarc.space.contracts.SubscriptionRequest;
import io.github.pgmarc.space.contracts.SubscriptionUpdateRequest;
import io.github.pgmarc.space.contracts.UserContact;

import java.io.IOException;
import java.util.Objects;

/**
 * In this example you will be subscribing to a service with only a single
 * plan available. After that you will update your subscription to the
 * lastest available version of the service pricing.
 *
 * <p>
 * You have the corresponding <code>Pricing2Yaml</code> files in
 * <code>src/main/resources/single</code> folder.
 * </p>
 */
final class SingleService {

    /**
     * Before executing the code snippets you have to do the following:
     * <ul>
     *     <li>Start a SPACE instance</li>
     *     <li>Get a <code>MANAGER</code> or <code>ADMIN</code> api key</li>
     * </ul>
     * <p>
     * You are free to experiment with the API and adjust the code to your needs.
     * Happy Hacking :)
     *
     * @see <a href="https://github.com/isa-group/Pricing4SaaS-docs"></a>
     *
     */
    public static void main(String[] args) throws IOException {

        String apiKey = Objects.requireNonNull(System.getenv("SPACE_API_KEY"),
            "You need to set SPACE_API_KEY env variable");

        SpaceClient client = SpaceClient.builder("localhost", apiKey).build();

        String userId = "4427d118-073d-4da2-a145-f77a75b52595";

        addContract(client, userId);
        getUserContract(client, userId);
        //updateContract(client, userId);

    }

    /**
     * To create a <code>SubscriptionRequest</code>, i.e, create a Subscription in SPACE
     * API you need the following parameters:
     * <ul>
     *     <li>An <code>UserContact</code></li>
     *     <li>A <code>Service</code></li>
     * </ul>
     *
     * <p>
     * An <code>UserContact</code> needs al least the following parameters:
     * <ul>
     *     <li><code>userId</code>: is the id of the user of <b>YOUR</b> application</li>
     *     <li><code>username</code>: alias that identifies the user of <b>YOUR</b> application</li>
     * </ul>
     * <p>
     * You can pass optional parameters using the <code>UserContact.Builder</code> methods
     *
     * <p>
     * <pre><code>
     * UserContact.builder("you_user_id", "janedoe")
     *     .firstName("Jane")
     *     .lastName("Doe")
     *     .email("janedoe@example.com")
     *     .phone("280-689-4646")
     *     .build();
     * </code></pre>
     * </p>
     *
     * <p>
     * A subscription al least contains one contracted service. A service
     * can optionally have a plan or multiple add-ons, but at least you
     * have to be subscribed to one of them. For example the following combinations
     * are possible:
     *
     * <table>
     *   <thead>
     *     <tr>
     *       <th>Plan</th>
     *       <th>Add-on</th>
     *       <th>Posible</th>
     *     </tr>
     *   </thead>
     * <tbody>
     *   <tr>
     *     <td>0</td>
     *     <td>0</td>
     *     <td>No</td>
     *   </tr>
     *   <tr>
     *     <td>0</td>
     *     <td>1</td>
     *     <td>Yes</td>
     *   </tr>
     *   <tr>
     *     <td>0</td>
     *     <td>N</td>
     *     <td>Yes</td>
     *   </tr>
     *   <tr>
     *     <td>1</td>
     *     <td>0</td>
     *     <td>Yes</td>
     *   </tr>
     *   <tr>
     *     <td>1</td>
     *     <td>1</td>
     *     <td>Yes</td>
     *   </tr>
     *   <tr>
     *     <td>1</td>
     *     <td>N</td>
     *     <td>Yes</td>
     *   </tr>
     * </tbody>
     * </table>
     * </p>
     */
    private static void addContract(SpaceClient client, String userId) throws IOException {
        // A subscription at least requires a user id and a username
        String username = "alex";
        UserContact contact1 = UserContact.builder(userId, username).build();

        // But, you can provide more user contact information if you want
        UserContact contact2 = UserContact
            .builder("3f5f934c-951b-4a2d-ad10-b1679ac9b7ba", "janedoe")
            .firstName("Jane")
            .lastName("Doe")
            .email("janedoe@example.com")
            .phone("280-689-4646").build();

        SubscriptionRequest subReq = SubscriptionRequest.builder(contact1)
            .startService("WireMock", "2024")
            .plan("Enterprise")
            .endService()
            .build();

        Subscription newSubscription = client.contracts().addContract(subReq);
        System.out.println(newSubscription);
    }

    private static void getUserContract(SpaceClient client, String userId) throws IOException {
        Subscription subscription = client.contracts().getContractByUserId(userId);
        System.out.println(subscription);
    }

    /**
     * Updating (in legal terms, novating) the subscription of WireMock
     * version 2024 to WireMock version 2025
     */
    private static void updateContract(SpaceClient client, String userId) throws IOException {
        SubscriptionUpdateRequest upReq = SubscriptionUpdateRequest.builder()
            .startService("WireMock", "2025")
            .plan("Enterprise")
            .endService()
            .build();

        Subscription updatedSubscription = client.contracts().updateContractByUserId(userId, upReq);
        System.out.println(updatedSubscription);
    }
}
