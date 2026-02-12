package io.github.pgmarc.space;

import java.io.IOException;
import java.util.Objects;

import org.json.JSONObject;

import io.github.pgmarc.space.contracts.Subscription;
import io.github.pgmarc.space.contracts.SubscriptionRequest;
import io.github.pgmarc.space.contracts.SubscriptionUpdateRequest;
import io.github.pgmarc.space.deserializers.ErrorDeserializer;
import io.github.pgmarc.space.deserializers.SubscriptionDeserializer;
import io.github.pgmarc.space.exceptions.SpaceApiException;
import io.github.pgmarc.space.serializers.SubscriptionRequestSerializer;
import io.github.pgmarc.space.serializers.SubscriptionUpdateRequestSerializer;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public final class ContractsEndpoint {

    private static final MediaType JSON = MediaType.get("application/json");
    private static final String ENDPOINT = "contracts";
    private static final String STATUS_CODE = "statusCode";

    private final OkHttpClient client;
    private final HttpUrl baseUrl;
    private final SubscriptionDeserializer subscriptionDeserializer = new SubscriptionDeserializer();
    private final SubscriptionRequestSerializer subscriptionRequestSerializer = new SubscriptionRequestSerializer();
    private final SubscriptionUpdateRequestSerializer subscriptionUpdateRequestSerializer = new SubscriptionUpdateRequestSerializer();
    private final ErrorDeserializer errorDeserializer = new ErrorDeserializer();
    private final Headers requiredHeaders;

    ContractsEndpoint(OkHttpClient client, HttpUrl baseUrl, String apiKey) {
        this.client = client;
        this.baseUrl = baseUrl;
        this.requiredHeaders = new Headers.Builder().add("Accept", JSON.toString())
                .add("x-api-key", apiKey).build();
    }

    public Subscription addContract(SubscriptionRequest subscriptionReq) throws IOException {
        Objects.requireNonNull(subscriptionReq, "subscription request must not be null");

        HttpUrl url = this.baseUrl.newBuilder().addPathSegment(ENDPOINT).build();
        Subscription res = null;
        Request request = new Request.Builder().url(url)
                .post(RequestBody.create(subscriptionRequestSerializer.toJson(subscriptionReq).toString(), JSON))
                .headers(requiredHeaders).build();
        try (Response response = client.newCall(request).execute()) {
            ResponseBody responseBody = response.body();
            JSONObject jsonResponse = new JSONObject(responseBody.string());
            if (!response.isSuccessful()) {
                jsonResponse.put(STATUS_CODE, response.code());
                throw new SpaceApiException(errorDeserializer.fromJson(jsonResponse));
            }

            res = subscriptionDeserializer.fromJson(jsonResponse);
        }

        return res;
    }

    public Subscription getContractByUserId(String userId) throws IOException {

        HttpUrl url = this.baseUrl.newBuilder().addPathSegment(ENDPOINT).addEncodedPathSegment(userId).build();
        Subscription res = null;
        Request request = new Request.Builder().url(url).headers(requiredHeaders).build();
        try (Response response = client.newCall(request).execute()) {
            JSONObject jsonResponse = new JSONObject(response.body().string());
            if (!response.isSuccessful()) {
                jsonResponse.put(STATUS_CODE, response.code());
                throw new SpaceApiException(errorDeserializer.fromJson(jsonResponse));
            }
            res = subscriptionDeserializer.fromJson(jsonResponse);
        }

        return res;
    }

    public Subscription updateContractByUserId(String userId, SubscriptionUpdateRequest subscription)
            throws IOException {
        HttpUrl url = this.baseUrl.newBuilder().addPathSegment(ENDPOINT).addEncodedPathSegment(userId).build();
        Subscription res = null;
        Request request = new Request.Builder().url(url)
                .put(RequestBody.create(subscriptionUpdateRequestSerializer.toJson(subscription).toString(), JSON))
                .headers(requiredHeaders)
                .build();
        try (Response response = client.newCall(request).execute()) {
            ResponseBody responseBody = response.body();
            JSONObject jsonResponse = new JSONObject(responseBody.string());
            if (!response.isSuccessful()) {
                jsonResponse.put(STATUS_CODE, response.code());
                throw new SpaceApiException(errorDeserializer.fromJson(jsonResponse));
            }
            res = subscriptionDeserializer.fromJson(jsonResponse);
        }

        return res;
    }

}
