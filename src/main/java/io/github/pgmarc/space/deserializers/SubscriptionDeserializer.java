package io.github.pgmarc.space.deserializers;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import io.github.pgmarc.space.contracts.UsageLevel;
import io.github.pgmarc.space.contracts.UserContact;
import io.github.pgmarc.space.contracts.Service;
import io.github.pgmarc.space.contracts.Subscription;
import io.github.pgmarc.space.contracts.Subscription.Snapshot;

public final class SubscriptionDeserializer implements JsonDeserializable<Subscription> {

    private final UserContactDeserializer userContactDeserializer = new UserContactDeserializer();
    private final UsageLevelDeserializer usageLevelDeserializer = new UsageLevelDeserializer();
    private final ServicesDeserializer servicesDeserializer = new ServicesDeserializer();
    private final SnapshotsDeserializer historyDeserializer = new SnapshotsDeserializer(servicesDeserializer);

    @Override
    public Subscription fromJson(JSONObject json) {
        UserContact userContact = userContactDeserializer.fromJson(json.getJSONObject(Subscription.Keys.USER_CONTACT.toString()));
        Map<String, Map<String, UsageLevel>> usageLevels = usageLevelDeserializer.fromJson(
                json.getJSONObject(Subscription.Keys.USAGE_LEVELS.toString()));
        Map<String, Service> services = servicesDeserializer.fromJson(json);

        JSONObject billingPeriod = json.getJSONObject(Subscription.Keys.BILLING_PERIOD.toString());
        ZonedDateTime start = ZonedDateTime.parse(billingPeriod.getString("startDate"));
        ZonedDateTime end = ZonedDateTime.parse(billingPeriod.getString("endDate"));

        List<Snapshot> history = historyDeserializer.fromJson(json);
        Subscription.Builder builder =  Subscription.builder(userContact, start, end, services.values())
            .addUsageLevels(usageLevels)
            .addSnapshots(history);

        int renewalDays = billingPeriod.optInt("renewalDays", -1);
        if (renewalDays > 0) {
            builder.renewInDays(renewalDays);
        }

        return builder.build();
    }

}
