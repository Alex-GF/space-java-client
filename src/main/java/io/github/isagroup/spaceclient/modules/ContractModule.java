package io.github.isagroup.spaceclient.modules;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.isagroup.spaceclient.SpaceClient;
import io.github.isagroup.spaceclient.types.Contract;
import io.github.isagroup.spaceclient.types.ContractToCreate;
import io.github.isagroup.spaceclient.types.Subscription;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Module for managing user contracts with the Space platform
 */
public class ContractModule {
    private static final Logger logger = LoggerFactory.getLogger(ContractModule.class);
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    
    private final SpaceClient spaceClient;
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;

    /**
     * Creates an instance of the ContractModule class
     */
    public ContractModule(SpaceClient spaceClient, OkHttpClient httpClient, ObjectMapper objectMapper) {
        this.spaceClient = spaceClient;
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }

    /**
     * Retrieves the contract for a specific user from the Space platform
     * This method first checks the cache before making an API request
     *
     * @param userId The ID of the user whose contract is to be retrieved
     * @return The user's contract data
     * @throws IOException If the operation fails
     */
    public Contract getContract(String userId) throws IOException {
        CacheModule cache = spaceClient.cache;
        String cacheKey = cache.getContractKey(userId);

        // Try to get from cache first
        if (cache.isEnabled()) {
            Contract cachedContract = cache.get(cacheKey, Contract.class);
            if (cachedContract != null) {
                return cachedContract;
            }
        }

        // If not in cache, fetch from API
        Request request = new Request.Builder()
                .url(spaceClient.getHttpUrl() + "/contracts/" + userId)
                .header("x-api-key", spaceClient.getApiKey())
                .get()
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                logger.error("Error fetching contract: {}", errorBody);
                throw new IOException("Unexpected response code: " + response.code() + " - " + errorBody);
            }

            String responseBody = response.body().string();
            Contract contract = objectMapper.readValue(responseBody, Contract.class);

            // Cache the result if caching is enabled
            if (cache.isEnabled()) {
                cache.set(cacheKey, contract);
            }

            return contract;
        }
    }

    /**
     * Adds a new contract to the Space platform
     * This method also invalidates any cached data for the user
     *
     * @param contractToCreate The contract details to be created
     * @return The created contract
     * @throws IOException If the operation fails
     */
    public Contract addContract(ContractToCreate contractToCreate) throws IOException {
        String json = objectMapper.writeValueAsString(contractToCreate);

        RequestBody body = RequestBody.create(json, JSON);

        Request request = new Request.Builder()
                .url(spaceClient.getHttpUrl() + "/contracts")
                .header("x-api-key", spaceClient.getApiKey())
                .post(body)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                logger.error("Error adding contract: {}", errorBody);
                throw new IOException("Unexpected response code: " + response.code() + " - " + errorBody);
            }

            String responseBody = response.body().string();
            Contract contract = objectMapper.readValue(responseBody, Contract.class);

            CacheModule cache = spaceClient.cache;
            // Invalidate cache for this user if caching is enabled
            if (cache.isEnabled() && contract.getUserId() != null) {
                cache.invalidateUser(contract.getUserId());
                // Cache the new contract
                cache.set(cache.getContractKey(contract.getUserId()), contract);
            }

            return contract;
        }
    }

    /**
     * Updates the subscription for a user in the Space platform
     * This method also invalidates the cached data for the user
     *
     * @param userId The ID of the user whose subscription is to be updated
     * @param newSubscription The new subscription details to be applied
     * @return The updated contract
     * @throws IOException If the operation fails
     */
    public Contract updateContractSubscription(String userId, Subscription newSubscription) throws IOException {
        String json = objectMapper.writeValueAsString(newSubscription);
        RequestBody body = RequestBody.create(json, JSON);

        Request request = new Request.Builder()
                .url(spaceClient.getHttpUrl() + "/contracts/" + userId)
                .header("x-api-key", spaceClient.getApiKey())
                .put(body)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                logger.error("Error updating contract subscription: {}", errorBody);
                throw new IOException("Unexpected response code: " + response.code() + " - " + errorBody);
            }

            String responseBody = response.body().string();
            Contract contract = objectMapper.readValue(responseBody, Contract.class);

            CacheModule cache = spaceClient.cache;
            // Invalidate and update cache for this user if caching is enabled
            if (cache.isEnabled()) {
                cache.invalidateUser(userId);
                cache.set(cache.getContractKey(userId), contract);
            }

            return contract;
        }
    }
}
