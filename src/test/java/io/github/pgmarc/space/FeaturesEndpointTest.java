package io.github.pgmarc.space;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import io.github.pgmarc.space.features.FeatureEvaluationResult;
import io.github.pgmarc.space.features.Revert;
import io.github.pgmarc.space.features.UsageLimitConsumption;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

@WireMockTest
class FeaturesEndpointTest {

    protected static final String TEST_API_KEY = "prueba";
    protected static final OkHttpClient httpClient = new OkHttpClient.Builder().build();
    private static FeaturesEndpoint endpoint;

    @BeforeAll
    static void setup(WireMockRuntimeInfo wmRuntimeInfo) {
        HttpUrl url = new HttpUrl.Builder().scheme("http").host("localhost").port(wmRuntimeInfo.getHttpPort()).build();
        endpoint = new FeaturesEndpoint(httpClient, url, TEST_API_KEY);
    }

    @Test
    void givenSimpleFeatureIdShouldEvaluate() {

        String userId = "e8e053c5-fd2b-4e4c-85a0-f1a52f0da72e";
        String featureId = "petclinic-featureA";

        stubFor(post(urlPathTemplate("/features/{userId}/{featureId}"))
            .withHeader("x-api-key", equalTo("prueba"))
            .withPathParam("userId", equalTo(userId))
            .withPathParam("featureId", equalTo(featureId))
            .willReturn(
                ok()
                    .withHeader("Content-Type", "application/json")
                    .withBodyFile("boolean-feature-evaluation.json")));

        try {
            FeatureEvaluationResult res = endpoint.evaluate(userId, "Petclinic", "featureA");
            assertThat(res.isAvailable()).isTrue();
            assertThat(res.getQuotas()).isEmpty();
        } catch (IOException e) {
            fail();
        }

    }

    @Test
    void givenConsumptionShouldEvaluateOptimistically() {

        String userId = "e8e053c5-fd2b-4e4c-85a0-f1a52f0da72e";
        String featureId = "petclinic-featureA";

        stubFor(post(urlPathTemplate("/features/{userId}/{featureId}"))
            .withHeader("x-api-key", equalTo("prueba"))
            .withPathParam("userId", equalTo(userId))
            .withPathParam("featureId", equalTo(featureId))
            .willReturn(
                ok()
                    .withHeader("Content-Type", "application/json")
                    .withBodyFile("optimistic-evaluation-response.json")));

        String service = "Petclinic";
        String feature = "featureA";
        String usageLimit = "featureALimit";

        try {
            UsageLimitConsumption usageLimitConsumption = UsageLimitConsumption.builder(service).addInt(usageLimit, 100).build();
            FeatureEvaluationResult res = endpoint.evaluateOptimistically(userId, service, feature, usageLimitConsumption);
            assertThat(res.isAvailable()).isTrue();
            assertThat(res.getConsumed(usageLimit)).hasValue(100);
            assertThat(res.getLimit(usageLimit)).hasValue(500);
        } catch (IOException e) {
            fail();
        }
    }

    @Test
    void givenRevertNewestCallShouldCompleteSucessfully() {
        String userId = "e8e053c5-fd2b-4e4c-85a0-f1a52f0da72e";
        String featureId = "petclinic-featureA";

        stubFor(post(urlPathTemplate("/features/{userId}/{featureId}"))
            .withHeader("x-api-key", equalTo("prueba"))
                .withRequestBody(absent())
            .withPathParam("userId", equalTo(userId))
            .withPathParam("featureId", equalTo(featureId))
                .withQueryParam("revert", equalTo("true"))
                .withQueryParam("latest", equalTo("true"))
            .willReturn(
                noContent()));

        String service = "Petclinic";
        String feature = "featureA";

        try {
             assertThat(endpoint.revert(userId, service, feature, Revert.NEWEST_VALUE))
                 .isTrue();
        } catch (IOException e) {
            fail();
        }
    }

    @Test
    void givenRevertOldestCallShouldCompleteSucessfully() {
        String userId = "e8e053c5-fd2b-4e4c-85a0-f1a52f0da72e";
        String featureId = "petclinic-featureA";

        stubFor(post(urlPathTemplate("/features/{userId}/{featureId}"))
            .withHeader("x-api-key", equalTo("prueba"))
            .withRequestBody(absent())
            .withPathParam("userId", equalTo(userId))
            .withPathParam("featureId", equalTo(featureId))
            .withQueryParam("revert", equalTo("true"))
            .withQueryParam("latest", equalTo("false"))
            .willReturn(
                noContent()));

        String service = "Petclinic";
        String feature = "featureA";

        try {
            assertThat(endpoint.revert(userId, service, feature, Revert.OLDEST_VALUE))
                .isTrue();
        } catch (IOException e) {
            fail();
        }
    }

    @Test
    void getPricingTokenByUserId() {

        String userId = "e8e053c5-fd2b-4e4c-85a0-f1a52f0da72e";

        stubFor(post(urlPathTemplate("/features/{userId}/pricing-token"))
            .withHeader("x-api-key", equalTo("prueba"))
            .withRequestBody(absent())
            .withPathParam("userId", equalTo(userId))
            .willReturn(
                ok().withBodyFile("pricing-token-response.json")));

        try {
            assertThat(endpoint.generatePricingTokenForUser(userId)).hasSize(879);
        } catch (IOException e) {
            fail();
        }
    }
}
