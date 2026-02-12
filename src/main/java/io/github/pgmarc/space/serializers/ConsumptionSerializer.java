package io.github.pgmarc.space.serializers;

import io.github.pgmarc.space.features.UsageLimitConsumption;
import org.json.JSONObject;

public final class ConsumptionSerializer implements JsonSerializable<UsageLimitConsumption> {

    private static String formatUsageLimitConsumptionKey(String serviceName, String usageLimitName) {
        return serviceName + "-" + usageLimitName;
    }

    @Override
    public JSONObject toJson(UsageLimitConsumption usageLimitConsumption) {

        JSONObject jsonObject = new JSONObject();
        for (UsageLimitConsumption.Item<? extends  Number> item: usageLimitConsumption.getConsumption()) {
            jsonObject.put(formatUsageLimitConsumptionKey(usageLimitConsumption.getService(), item.getUsageLimit()), item.getQuantity());
        }

        return jsonObject;
    }
}
