package io.github.pgmarc.space.deserializers;

import io.github.pgmarc.space.exceptions.FeatureEvaluationError;
import io.github.pgmarc.space.exceptions.SpaceApiException;
import io.github.pgmarc.space.features.FeatureEvaluationResult;
import org.json.JSONObject;

import java.util.*;

public class FeatureEvaluationDeserializer implements JsonDeserializable<FeatureEvaluationResult> {

    private final int serviceLength;

    public FeatureEvaluationDeserializer(int serviceLength) {
        this.serviceLength = serviceLength;
    }

    private enum Keys {
        EVAL("eval"),
        ERROR("error"),
        CODE("code"),
        MESSAGE("message"),
        USED("used"),
        LIMIT("limit");

        private final String name;

        Keys(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
           return name;
        }
    }

    @Override
    public FeatureEvaluationResult fromJson(JSONObject json) {

        JSONObject error = json.optJSONObject(Keys.ERROR.toString());
        if (error != null) {
            FeatureEvaluationError evalError = FeatureEvaluationError.of(error.getString(Keys.CODE.toString()),
                    error.getString(Keys.MESSAGE.toString()));
            throw new SpaceApiException(evalError.toString());
        }

        boolean available = json.getBoolean(Keys.EVAL.toString());
        Map<String, FeatureEvaluationResult.Usage> quotas = featureQuotasFromJson(json, serviceLength);

        return FeatureEvaluationResult.of(available, quotas);
    }

    private static  Map<String, FeatureEvaluationResult.Usage> featureQuotasFromJson(JSONObject json, int serviceNameLength) {
        Map<String, FeatureEvaluationResult.Usage> res = new HashMap<>();

        if (json.isNull(Keys.USED.toString())) {
            return res;
        }

        for (String usageLimitId : json.getJSONObject(Keys.USED.toString()).keySet()) {
            String usedJsonPointer = "/" + Keys.USED + "/" + usageLimitId;
            String limitJsonPointer = "/" + Keys.LIMIT + "/" + usageLimitId;
            String usageLimit = usageLimitId.substring(serviceNameLength + 1);
            Number used = (Number) json.query(usedJsonPointer);
            Number limit = (Number) json.query(limitJsonPointer);
            res.put(usageLimit, FeatureEvaluationResult.Usage.of(used, limit));
        }

        return res;
    }
}
