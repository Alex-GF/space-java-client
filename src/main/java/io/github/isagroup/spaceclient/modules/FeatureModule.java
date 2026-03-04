package io.github.isagroup.spaceclient.modules;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.isagroup.spaceclient.SpaceClient;
import io.github.isagroup.spaceclient.types.FeatureEvaluationResult;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Module for evaluating features and managing pricing tokens
 */
public class FeatureModule {
  private static final Logger logger = LoggerFactory.getLogger(FeatureModule.class);
  private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

  private final SpaceClient spaceClient;
  private final OkHttpClient httpClient;
  private final ObjectMapper objectMapper;

  /**
   * Creates an instance of the FeatureModule class
   */
  public FeatureModule(SpaceClient spaceClient, OkHttpClient httpClient, ObjectMapper objectMapper) {
    this.spaceClient = spaceClient;
    this.httpClient = httpClient;
    this.objectMapper = objectMapper;
  }

  /**
   * Evaluates a feature for a specific user
   * Results are cached only for read-only evaluations (when expectedConsumption
   * is empty)
   *
   * @param userId              The ID of the user for whom the feature is being
   *                            evaluated
   * @param featureId           The ID of the feature to be evaluated (i.e.
   *                            ${serviceName}-${featureName})
   * @param expectedConsumption Expected consumption values for the feature
   * @param details             Whether to include details in the response
   * @param server              Whether to use server-side evaluation
   * @return The evaluation result
   * @throws IOException If the operation fails
   */
  public FeatureEvaluationResult evaluate(String userId, String featureId,
      Map<String, Number> expectedConsumption,
      boolean details, boolean server) {
    try {
      if (expectedConsumption == null) {
        expectedConsumption = new HashMap<>();
      }

      CacheModule cache = spaceClient.cache;
      boolean isReadOnlyEvaluation = expectedConsumption.isEmpty();
      String cacheKey = cache.getFeatureKey(userId, featureId);

      // Only use cache for read-only evaluations (no consumption)
      if (isReadOnlyEvaluation && cache.isEnabled()) {
        FeatureEvaluationResult cachedResult = cache.get(cacheKey, FeatureEvaluationResult.class);
        if (cachedResult != null) {
          return cachedResult;
        }
      }

      // Build query parameters
      StringBuilder queryParams = new StringBuilder();
      if (details) {
        queryParams.append("details=true");
      }
      if (server) {
        if (queryParams.length() > 0)
          queryParams.append("&");
        queryParams.append("server=true");
      }
      String queryString = queryParams.length() > 0 ? "?" + queryParams.toString() : "";

      String json = objectMapper.writeValueAsString(expectedConsumption);
      RequestBody body = RequestBody.create(json, JSON);

      Request request = new Request.Builder()
          .url(spaceClient.getHttpUrl() + "/features/" + userId + "/" + featureId + queryString)
          .header("x-api-key", spaceClient.getApiKey())
          .post(body)
          .build();

      try (Response response = httpClient.newCall(request).execute()) {
        if (!response.isSuccessful()) {
          String errorBody = response.body() != null ? response.body().string() : "Unknown error";
          logger.error("Error evaluating feature: {}", errorBody);
          throw new IOException("Unexpected response code: " + response.code() + " - " + errorBody);
        }

        String responseBody = response.body().string();
        FeatureEvaluationResult result = objectMapper.readValue(responseBody, FeatureEvaluationResult.class);

        // Cache the result only for read-only evaluations with shorter TTL
        if (isReadOnlyEvaluation && cache.isEnabled()) {
          // Use shorter TTL for feature evaluations (60 seconds)
          cache.set(cacheKey, result, 60);
        } else if (cache.isEnabled()) {
          // For write operations, invalidate related cache entries
          cache.delete(cacheKey);
          // Also invalidate contract cache as usage might have changed
          cache.delete(cache.getContractKey(userId));
          // Invalidate pricing token as user's consumption has changed
          cache.delete(cache.getPricingTokenKey(userId));
        }

        return result;
      }
    } catch (IOException e) {
      logger.error("IO error during feature evaluation", e);
      Map<String, Object> used = new HashMap<>();
      Map<String, Object> limit = new HashMap<>();
      return new FeatureEvaluationResult(false, used, limit,
          new FeatureEvaluationResult.EvaluationError("IO_ERROR", e.getMessage()));
    }
  }

  /**
   * Evaluates a feature for a specific user with default options
   *
   * @param userId    The ID of the user for whom the feature is being evaluated
   * @param featureId The ID of the feature to be evaluated
   * @return The evaluation result
   * @throws IOException If the operation fails
   */
  public FeatureEvaluationResult evaluate(String userId, String featureId) {
    return evaluate(userId, featureId, new HashMap<>(), false, false);
  }

  /**
   * Evaluates a feature for a specific user with expected consumption
   *
   * @param userId              The ID of the user for whom the feature is being
   *                            evaluated
   * @param featureId           The ID of the feature to be evaluated
   * @param expectedConsumption Expected consumption values for the feature
   * @return The evaluation result
   * @throws IOException If the operation fails
   */
  public FeatureEvaluationResult evaluate(String userId, String featureId, Map<String, Number> expectedConsumption) {
    return evaluate(userId, featureId, expectedConsumption, false, false);
  }

  /**
   * Reverts the optimistic usage level update performed during a previous
   * evaluation
   * This method also invalidates cached data for the user
   *
   * @param userId         The ID of the user for whom the feature is being
   *                       evaluated
   * @param featureId      The ID of the feature to be evaluated
   * @param revertToLatest Whether to reset to the latest stored value (true =>
   *                       newest | false => oldest)
   * @return true if the revert operation was successful
   * @throws IOException If the operation fails
   */
  public boolean revertEvaluation(String userId, String featureId, boolean revertToLatest) {
    try {
      RequestBody body = RequestBody.create("{}", JSON);

      Request request = new Request.Builder()
          .url(spaceClient.getHttpUrl() + "/features/" + userId + "?revert=true&latest=" + revertToLatest)
          .header("x-api-key", spaceClient.getApiKey())
          .post(body)
          .build();

      try (Response response = httpClient.newCall(request).execute()) {
        if (!response.isSuccessful()) {
          String errorBody = response.body() != null ? response.body().string() : "Unknown error";
          logger.error("Error reverting usage level for evaluation: {}", errorBody);
          throw new IOException("Unexpected response code: " + response.code() + " - " + errorBody);
        }

        CacheModule cache = spaceClient.cache;
        // Invalidate related cache entries after reverting
        if (cache.isEnabled()) {
          cache.delete(cache.getFeatureKey(userId, featureId));
          cache.delete(cache.getContractKey(userId));
          // Invalidate pricing token as user's consumption has changed
          cache.delete(cache.getPricingTokenKey(userId));
        }

        return true;
      }
    } catch (IOException e) {
      logger.error("IO error during revert evaluation", e);
      return false;
    }
  }

  /**
   * Reverts the optimistic usage level update with default (latest) behavior
   *
   * @param userId    The ID of the user for whom the feature is being evaluated
   * @param featureId The ID of the feature to be evaluated
   * @return true if the revert operation was successful
   * @throws IOException If the operation fails
   */
  public boolean revertEvaluation(String userId, String featureId) {
    return revertEvaluation(userId, featureId, true);
  }

  /**
   * Generates a pricing token for a user
   * The token is cached to improve performance
   *
   * @param userId The ID of the user for whom the pricing token is being
   *               generated
   * @return The generated pricing token
   * @throws IOException If the operation fails
   */
  public String generateUserPricingToken(String userId) {
    try {
      CacheModule cache = spaceClient.cache;
      String cacheKey = cache.getPricingTokenKey(userId);

      // Try to get from cache first
      if (cache.isEnabled()) {
        String cachedToken = cache.get(cacheKey, String.class);
        if (cachedToken != null) {
          return cachedToken;
        }
      }

      RequestBody body = RequestBody.create("{}", JSON);

      Request request = new Request.Builder()
          .url(spaceClient.getHttpUrl() + "/features/" + userId + "/pricing-token")
          .header("x-api-key", spaceClient.getApiKey())
          .post(body)
          .build();

      try (Response response = httpClient.newCall(request).execute()) {
        if (!response.isSuccessful()) {
          String errorBody = response.body() != null ? response.body().string() : "Unknown error";
          logger.error("Error generating user pricing token: {}", errorBody);
          throw new IOException("Unexpected response code: " + response.code() + " - " + errorBody);
        }

        String responseBody = response.body().string();
        Map<String, Object> responseMap = objectMapper.readValue(responseBody, Map.class);
        String token = (String) responseMap.get("pricingToken");

        // Cache the token if caching is enabled (with longer TTL as tokens are more
        // stable)
        if (cache.isEnabled() && token != null) {
          // Use longer TTL for pricing tokens (15 minutes)
          cache.set(cacheKey, token, 900);
        }

        return token;
      }
    } catch (IOException e) {
      logger.error("IO error during pricing token generation", e);
      return null;
    }
  }
}
