package io.github.pgmarc.space.deserializers;

import java.util.Objects;

import org.json.JSONObject;

import io.github.pgmarc.space.contracts.UserContact;

final class UserContactDeserializer implements JsonDeserializable<UserContact> {

    @Override
    public UserContact fromJson(JSONObject json) {

        Objects.requireNonNull(json, "user contact json must not be null");
        return UserContact.builder(json.getString(UserContact.Keys.USER_ID.toString()),
                json.getString(UserContact.Keys.USERNAME.toString()))
                .firstName(json.optString(UserContact.Keys.FIRST_NAME.toString(), null))
                .lastName(json.optString(UserContact.Keys.LAST_NAME.toString(), null))
                .email(json.optString(UserContact.Keys.EMAIL.toString(), null))
                .phone(json.optString(UserContact.Keys.PHONE.toString(), null))
                .build();
    }

}
