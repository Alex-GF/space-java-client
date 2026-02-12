package io.github.pgmarc.space.deserializers;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import io.github.pgmarc.space.contracts.Subscription;
import io.github.pgmarc.space.contracts.Subscription.Snapshot;

class SnapshotsDeserializer implements JsonDeserializable<List<Snapshot>> {

    private final ServicesDeserializer servicesDeserializer;

    SnapshotsDeserializer(ServicesDeserializer servicesDeserializer) {
        this.servicesDeserializer = servicesDeserializer;
    }

    @Override
    public List<Snapshot> fromJson(JSONObject json) {
        JSONArray history = json.getJSONArray(Subscription.Keys.HISTORY.toString());
        List<Snapshot> res = new ArrayList<>();
        for (int i = 0; i < history.length(); i++) {
            JSONObject snapshot = history.getJSONObject(i);
            ZonedDateTime startUtc = ZonedDateTime
                    .parse(snapshot.getString("startDate"));
            ZonedDateTime endUtc = ZonedDateTime
                    .parse(snapshot.getString("endDate"));
            res.add(Snapshot.of(startUtc, endUtc, servicesDeserializer.fromJson(snapshot)));
        }
        return res;
    }
}
