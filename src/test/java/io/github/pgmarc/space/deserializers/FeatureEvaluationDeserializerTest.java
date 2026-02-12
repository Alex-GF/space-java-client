package io.github.pgmarc.space.deserializers;

import io.github.pgmarc.space.exceptions.SpaceApiException;
import io.github.pgmarc.space.features.FeatureEvaluationResult;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;

class FeatureEvaluationDeserializerTest {


    @Test
    void givenBooleanEvaluationResultShouldCreateObject() {

        String service = "Petclinic";
        JSONObject jsonObject = new JSONObject()
            .put("eval", true)
            .put("used", JSONObject.NULL)
            .put("limit", JSONObject.NULL)
            .put("error", JSONObject.NULL);

        FeatureEvaluationDeserializer deserializer = new FeatureEvaluationDeserializer(service.length());
        assertThatNoException().isThrownBy(() -> deserializer.fromJson(jsonObject));
    }

    @Test
    void givenJsonShouldCreateEvaluationResult() {

        String zoom = "Zoom";
        String zoomStorage = "zoom-storage";
        String zoomApiCalls = "zoom-apiCalls";
        String zoomBandwidth = "zoom-bandwidth";

        JSONObject jsonObject = new JSONObject()
            .put("eval", true)
            .put("used", Map.of(zoomStorage, 50, zoomApiCalls, 1, zoomBandwidth, 20))
            .put("limit", Map.of(zoomStorage, 500, zoomApiCalls, 1000, zoomBandwidth, 200))
            .put("error", JSONObject.NULL);

        FeatureEvaluationDeserializer deserializer = new FeatureEvaluationDeserializer(zoom.length());
        assertThatNoException().isThrownBy(() -> deserializer.fromJson(jsonObject));

        FeatureEvaluationResult res =  deserializer.fromJson(jsonObject);
        assertThat(res.isAvailable()).isTrue();
        assertThat(res.getConsumed("storage")).hasValue(50);
        assertThat(res.getLimit("storage")).hasValue(500);
    }

    @Test
    void givenJsonShouldNotBeEmptyNonExistentService() {

        String service = "Zoom";
        String zoomStorage = "zoom-storage";

        JSONObject jsonObject = new JSONObject()
            .put("eval", true)
            .put("used", Map.of(zoomStorage, 50))
            .put("limit", Map.of(zoomStorage, 500))
            .put("error", JSONObject.NULL);

        FeatureEvaluationDeserializer deserializer = new FeatureEvaluationDeserializer(service.length());
        assertThatNoException().isThrownBy(() -> deserializer.fromJson(jsonObject));

        FeatureEvaluationResult res =  deserializer.fromJson(jsonObject);
        assertThat(res.getQuotas()).isNotEmpty();
        assertThat(res.getConsumed("test")).isEmpty();
        assertThat(res.getLimit("tests")).isEmpty();
    }

    @Test
    void givenFeatureEvaluationErrorShouldThrow() {

        String service = "Petclinic";
        String code = "FLAG_NOT_FOUND";
        String message = "Feature pets not found in \"pricingContext\".";
        JSONObject jsonObject = new JSONObject()
            .put("eval", false)
            .put("used", JSONObject.NULL)
            .put("limit", JSONObject.NULL)
            .put("error", Map.of("code", code, "message", message));

        FeatureEvaluationDeserializer deserializer = new FeatureEvaluationDeserializer(service.length());
        assertThatExceptionOfType(SpaceApiException.class)
            .isThrownBy(() -> deserializer.fromJson(jsonObject))
            .withMessage("Feature pets not found in \"pricingContext\". Error code: FLAG_NOT_FOUND");
    }
}
