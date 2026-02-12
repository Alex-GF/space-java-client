package io.github.pgmarc.space;

import io.github.pgmarc.space.deserializers.ErrorDeserializer;
import io.github.pgmarc.space.deserializers.FeatureEvaluationDeserializer;
import io.github.pgmarc.space.exceptions.SpaceApiException;
import io.github.pgmarc.space.features.Revert;
import io.github.pgmarc.space.features.UsageLimitConsumption;
import io.github.pgmarc.space.features.FeatureEvaluationResult;
import io.github.pgmarc.space.serializers.ConsumptionSerializer;
import okhttp3.*;
import org.json.JSONObject;

import java.io.IOException;

public final class FeaturesEndpoint {

    private static final String ENDPOINT = "features";
    private static final MediaType JSON = MediaType.get("application/json");
    private static final String STATUS_CODE = "statusCode";


    private final OkHttpClient client;
    private final HttpUrl baseUrl;
    private final Headers requiredHeaders;
    private final ConsumptionSerializer consumptionSerializer;
    private final ErrorDeserializer errorDeserializer;

    FeaturesEndpoint(OkHttpClient client, HttpUrl baseUrl, String apiKey) {
        this.client = client;
        this.baseUrl = baseUrl.newBuilder().addPathSegment(ENDPOINT).build();
        this.requiredHeaders = new Headers.Builder().add("Accept", JSON.toString())
            .add("x-api-key", apiKey).build();
        this.consumptionSerializer = new ConsumptionSerializer();
        this.errorDeserializer = new ErrorDeserializer();
    }

    private static String formatFeatureId(String service, String feature) {
        return service.toLowerCase() + "-" + feature;
    }

    public FeatureEvaluationResult evaluate(String userId, String service, String feature) throws IOException {
        HttpUrl url = this.baseUrl.newBuilder().addEncodedPathSegment(userId)
            .addEncodedPathSegment(formatFeatureId(service, feature)).build();
        Request request = new Request(url, requiredHeaders ,"POST" , RequestBody.EMPTY);

        FeatureEvaluationResult res = null;
        try (Response response = client.newCall(request).execute()) {
            JSONObject jsonResponse = new JSONObject(response.body().string());
            if (!response.isSuccessful()) {
                jsonResponse.put(STATUS_CODE, response.code());
                throw new SpaceApiException(errorDeserializer.fromJson(jsonResponse));
            }
            FeatureEvaluationDeserializer deserializer = new FeatureEvaluationDeserializer(service.length());
            res = deserializer.fromJson(jsonResponse);
        }

        return res;
    }

    public FeatureEvaluationResult evaluateOptimistically(String userId, String service, String featureId, UsageLimitConsumption usageLimitConsumption)
        throws IOException {
        HttpUrl url = this.baseUrl.newBuilder().addEncodedPathSegment(userId)
            .addEncodedPathSegment(formatFeatureId(service, featureId)).build();
        RequestBody body = RequestBody.create(consumptionSerializer.toJson(usageLimitConsumption).toString(), JSON);
        Request request = new Request(url, requiredHeaders ,"POST" , body);

        FeatureEvaluationResult res = null;
        try (Response response = client.newCall(request).execute()) {
            JSONObject jsonResponse = new JSONObject(response.body().string());
            if (!response.isSuccessful()) {
                jsonResponse.put(STATUS_CODE, response.code());
                throw new SpaceApiException(errorDeserializer.fromJson(jsonResponse));
            }
            FeatureEvaluationDeserializer deserializer = new FeatureEvaluationDeserializer(service.length());
            res = deserializer.fromJson(jsonResponse);
        }

        return res;
    }


    public boolean revert(String userId, String service, String feature, Revert revert) throws IOException {
        HttpUrl url = this.baseUrl.newBuilder().addEncodedPathSegment(userId)
            .addEncodedPathSegment(formatFeatureId(service, feature))
            .addQueryParameter("revert", String.valueOf(true))
            .addQueryParameter("latest", String.valueOf(revert.isLatest())).build();

        Request request = new Request(url, requiredHeaders ,"POST" , RequestBody.EMPTY);

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                JSONObject jsonResponse = new JSONObject(response.body().string());
                jsonResponse.put(STATUS_CODE, response.code());
                throw new SpaceApiException(errorDeserializer.fromJson(jsonResponse));
            }

            return response.code() == 204;
        }

    }

    public String generatePricingTokenForUser(String userId) throws IOException {

        HttpUrl url = this.baseUrl.newBuilder().addEncodedPathSegment(userId)
            .addPathSegment("pricing-token").build();
        Request request = new Request(url, requiredHeaders ,"POST" , RequestBody.EMPTY);

        try (Response response = client.newCall(request).execute()) {
            JSONObject jsonResponse = new JSONObject(response.body().string());
            if (!response.isSuccessful()) {
                jsonResponse.put(STATUS_CODE, response.code());
                throw new SpaceApiException(errorDeserializer.fromJson(jsonResponse));
            }
            return jsonResponse.getString("pricingToken");
        }
    }
}
