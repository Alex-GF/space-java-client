package io.github.pgmarc.space;

import java.io.IOException;
import java.time.Duration;
import java.time.Period;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;

import io.github.pgmarc.space.contracts.Subscription;
import io.github.pgmarc.space.contracts.SubscriptionRequest;
import io.github.pgmarc.space.contracts.SubscriptionUpdateRequest;
import io.github.pgmarc.space.contracts.UserContact;
import io.github.pgmarc.space.exceptions.SpaceApiException;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

import static org.assertj.core.api.Assertions.*;

class ContractsEndpointTest {

    private static final String TEST_API_KEY = "prueba";
    private static final OkHttpClient httpClient = new OkHttpClient.Builder().build();
    private static ContractsEndpoint endpoint;

    @RegisterExtension
    static WireMockExtension wm = WireMockExtension.newInstance()
        .options(wireMockConfig().globalTemplating(true))
        .build();

    @BeforeAll
    static void setUp() {
        HttpUrl url = new HttpUrl.Builder().scheme("http").host("localhost").port(wm.getPort()).build();
        endpoint = new ContractsEndpoint(httpClient, url, TEST_API_KEY);
    }

    @Test
    void givenASubscriptionShouldBeCreated() {

        String userId = "01c36d29-0d6a-4b41-83e9-8c6d9310c508";
        int renewalDays = 45;

        wm.stubFor(post(urlEqualTo("/contracts"))
                .withHeader("x-api-key", equalTo(TEST_API_KEY))
                .withHeader("Content-Type", equalToIgnoreCase("application/json; charset=utf-8"))
                .withHeader("Accept", equalTo("application/json"))
                .withRequestBody(matchingJsonPath("$.userContact"))
                .withRequestBody(matchingJsonPath("$.billingPeriod"))
                .withRequestBody(matchingJsonPath("$.contractedServices"))
                .withRequestBody(matchingJsonPath("$.subscriptionPlans"))
                .withRequestBody(matchingJsonPath("$.subscriptionAddOns"))
            .willReturn(
                        created()
                                .withHeader("Content-Type", "application/json")
                                .withBodyFile("addContracts-response.hbs")));

        UserContact userContact = UserContact.builder("01c36d29-0d6a-4b41-83e9-8c6d9310c508", "johndoe")
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@my-domain.com")
                .phone("+34 666 666 666")
                .build();

        SubscriptionRequest subReq = SubscriptionRequest.builder(userContact)
                .renewInDays(renewalDays)
                .startService("zoom", "2025")
                    .plan("ENTERPRISE")
                    .addOn("extraSeats", 2)
                    .addOn("hugeMeetings", 1)
                .endService()
                .startService("petclinic", "2024")
                    .plan("GOLD")
                    .addOn("petsAdoptionCentre", 1)
                .endService()
                .build();

        assertThatNoException().isThrownBy(() -> endpoint.addContract(subReq));
        Subscription subscription;
        try {
            subscription = endpoint.addContract(subReq);
            assertThat(subscription.getServices()).containsAll(subReq.getServices());
            assertThat(subscription.getUserId()).isEqualTo(userId);
            assertThat(subscription.getRenewalPeriod()).hasValue(Period.ofDays(renewalDays));
            assertThat(subscription.getHistory()).isEmpty();
        } catch (IOException e) {
            fail();
        }

    }

    @Test
    void givenRequestWithNoApiKeyShouldThrow() {

        wm.stubFor(post(urlEqualTo("/contracts"))
                .willReturn(
                        unauthorized()
                                .withHeader("Content-Type", "application/json")
                                .withBody("{\r\n" + //
                                        "  \"error\": \"API Key not found. Please ensure to add an API Key as value of the \\\"x-api-key\\\" header.\"\r\n"
                                        + //
                                        "}")));

        UserContact userContact = UserContact.builder("error", "alex")
                .build();

        SubscriptionRequest subReq = SubscriptionRequest.builder(userContact)
                .startService("err", "v1")
                    .plan("Error")
                .endService()
                .build();

        assertThatExceptionOfType(SpaceApiException.class).isThrownBy(() -> endpoint.addContract(subReq))
                .withMessageContaining("API Key not found");

    }

    @Test
    void givenAnUserIdShouldReturnASubscription() {

        String userId = "01c36d29-0d6a-4b41-83e9-8c6d9310c508";

        wm.stubFor(get(urlPathTemplate("/contracts/{userId}"))
                .withPathParam("userId", equalTo(userId))
                .withHeader("x-api-key", equalTo(TEST_API_KEY))
                .withHeader("Accept", equalTo("application/json"))
                .willReturn(
                        created()
                                .withHeader("Content-Type", "application/json")
                                .withBodyFile("getContractById-response.json")));

        Subscription subscription;
        try {
            subscription = endpoint.getContractByUserId(userId);
            assertThat(subscription.getUserId()).isEqualTo(userId);
        } catch (IOException e) {
            fail();
        }

    }

    @Test
    void givenAnUserIdThatDoesNotExistShouldThrowError() {

        String userId = "non-existent";

        wm.stubFor(get(urlPathTemplate("/contracts/{userId}"))
                .withPathParam("userId", equalTo(userId))
                .withHeader("x-api-key", equalTo(TEST_API_KEY))
                .withHeader("Accept", equalTo("application/json"))
                .willReturn(
                        aResponse()
                                .withStatus(404)
                                .withHeader("Content-Type", "application/json; charset=utf-8")
                                .withBody("{\"error\":\"Contract with userId {{request.path.userId}} not found\"}")));

        assertThatExceptionOfType(SpaceApiException.class)
                .isThrownBy(() -> endpoint.getContractByUserId(userId))
                .withMessage("Contract with userId " + userId + " not found")
                .extracting(SpaceApiException::getCode).isEqualTo(404);

    }

    @Test
    void givenAnUserIdAndServicesShouldUpdateSubscription() {

        String userId = "01c36d29-0d6a-4b41-83e9-8c6d9310c508";

        wm.stubFor(put(urlPathTemplate("/contracts/{userId}"))
                .withPathParam("userId", equalTo(userId))
                .withHeader("x-api-key", equalTo(TEST_API_KEY))
                .withHeader("Accept", equalTo("application/json"))
                .withHeader("Content-Type", equalToIgnoreCase("application/json; charset=utf-8"))
                .withRequestBody(matchingJsonPath("$.contractedServices"))
                .withRequestBody(matchingJsonPath("$.subscriptionPlans"))
                .withRequestBody(matchingJsonPath("$.subscriptionAddOns"))
                .willReturn(
                        ok()
                                .withHeader("Content-Type", "application/json")
                                .withBodyFile("getContractById-response.json")));

        SubscriptionUpdateRequest subscription = SubscriptionUpdateRequest.builder()
                .startService("petclinic", "v1")
                    .plan("GOLD")
                .endService()
                .build();
        Subscription sub;
        try {
            sub = endpoint.updateContractByUserId(userId, subscription);
            assertThat(sub.getUserId()).isEqualTo(userId);
        } catch (IOException e) {
            fail();
        }

    }

}
