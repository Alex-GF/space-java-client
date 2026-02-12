package io.github.pgmarc.space.deserializers;

import org.json.JSONObject;

public interface JsonDeserializable<U> {

    U fromJson(JSONObject json);
}
