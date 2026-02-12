package io.github.pgmarc.space.serializers;

import org.json.JSONObject;

public interface JsonSerializable<T> {
    JSONObject toJson(T object);
}
