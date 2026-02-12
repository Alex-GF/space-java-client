package io.github.pgmarc.space.deserializers;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.json.JSONObject;

import io.github.pgmarc.space.contracts.UsageLevel;

final class UsageLevelDeserializer implements JsonDeserializable<Map<String, Map<String, UsageLevel>>> {

    private Map<String, UsageLevel> getServiceUsageLevels(JSONObject usageLevels) {
        Map<String, UsageLevel> res = new HashMap<>();
        for (String usageLimitName : usageLevels.keySet()) {
            JSONObject rawUsageLevel = usageLevels.getJSONObject(usageLimitName);
            ZonedDateTime resetTimestamp = null;
            if (rawUsageLevel.has(UsageLevel.Keys.RESET_TIMESTAMP.toString())) {
                resetTimestamp = ZonedDateTime
                        .parse(rawUsageLevel.getString(UsageLevel.Keys.RESET_TIMESTAMP.toString()));
            }
            UsageLevel ul = UsageLevel.of(usageLimitName, rawUsageLevel.getDouble(UsageLevel.Keys.CONSUMED.toString()),
                    resetTimestamp);
            res.put(usageLimitName, ul);
        }
        return Collections.unmodifiableMap(res);

    }

    @Override
    public Map<String, Map<String, UsageLevel>> fromJson(JSONObject usageLevel) {
        Objects.requireNonNull(usageLevel, "usage level must not be null");
        Map<String, Map<String, UsageLevel>> res = new HashMap<>();
        for (String serviceName : usageLevel.keySet()) {
            res.put(serviceName, getServiceUsageLevels(usageLevel.getJSONObject(serviceName)));
        }
        return Collections.unmodifiableMap(res);
    }
}
