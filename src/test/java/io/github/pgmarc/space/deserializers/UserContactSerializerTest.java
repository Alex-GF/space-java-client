package io.github.pgmarc.space.deserializers;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import io.github.pgmarc.space.contracts.UserContact;

class UserContactSerializerTest {

    private final UserContactDeserializer deserializer = new UserContactDeserializer();

    @Test
    void givenUserContactJsonShouldParse() {

        String userId = "123456789";
        String username = "alex";
        String firtName = "Alex";
        String lastName = "Doe";
        String email = "alexdoe@example.com";
        String phone = "(+34) 666 666 666";
        UserContact expected = UserContact.builder(userId, username)
                .firstName(firtName)
                .lastName(lastName)
                .email(email)
                .phone(phone)
                .build();

        JSONObject input = new JSONObject().put("userId", userId)
                .put("username", username)
                .put("firstName", firtName)
                .put("lastName", lastName)
                .put("email", email)
                .put("phone", phone);
        assertEquals(expected, deserializer.fromJson(input));
    }

    @Test
    void givenMinimunUserContactOptionalsShouldNotBeParsed() {

        JSONObject input = new JSONObject()
                .put("userId", "123456789")
                .put("username", "test");

        UserContact actual = deserializer.fromJson(input);
        assertAll(() -> assertTrue(actual.getFirstName().isEmpty()),
                () -> assertTrue(actual.getLastName().isEmpty()),
                () -> assertTrue(actual.getEmail().isEmpty()),
                () -> assertTrue(actual.getPhone().isEmpty()));
    }

    @Test
    void givenNullJsonShouldThrow() {

        Exception ex = assertThrows(NullPointerException.class, () -> deserializer.fromJson(null));
        assertEquals("user contact json must not be null", ex.getMessage());
    }
}
