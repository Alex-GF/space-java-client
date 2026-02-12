package io.github.pgmarc.space.deserializers;

import java.util.HashSet;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

import io.github.pgmarc.space.exceptions.SpaceApiError;

public final class ErrorDeserializer implements JsonDeserializable<SpaceApiError> {

    private enum Keys {
        ERROR("error"),
        ERRORS("errors"),
        STATUS_CODE("statusCode"),
        MSG("msg");

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
    public SpaceApiError fromJson(JSONObject json) {

        Set<String> messages = new HashSet<>();
        if (json.has(Keys.ERROR.toString())) {
            messages.add(json.getString(Keys.ERROR.toString()));
        }

        if (json.has(Keys.ERRORS.toString())) {
            JSONArray jsonErrors = json.getJSONArray(Keys.ERRORS.toString());
            for (int i = 0; i < jsonErrors.length(); i++) {
                messages.add(jsonErrors.getJSONObject(i).getString(Keys.MSG.toString()));
            }
        }

        int statusCode = json.getInt(Keys.STATUS_CODE.toString());

        return new SpaceApiError(statusCode, messages);
    }

}
