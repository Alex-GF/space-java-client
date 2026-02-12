package io.github.pgmarc.space.contracts;

import static org.assertj.core.api.Assertions.*;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class UserContactTest {

    private static final String TEST_USER_ID = "123456789";
    private static final String TEST_USERNAME = "alex";

    @Test
    void givenIdAndUsernameShouldCreateUserContact() {

        UserContact contact = UserContact.builder(TEST_USER_ID, TEST_USERNAME).build();
        assertThat(contact.getUserId()).isEqualTo(TEST_USER_ID);
        assertThat(contact.getUsername()).isEqualTo(TEST_USERNAME);
    }

    @Test
    void givenNullUserIdShouldThrow() {

        assertThatExceptionOfType(NullPointerException.class)
                .isThrownBy(() -> UserContact.builder(null, TEST_USERNAME))
                .withMessage("userId must not be null");
    }

    @Test
    void givenNullUsernameShouldThrow() {

        assertThatExceptionOfType(NullPointerException.class)
                .isThrownBy(() -> UserContact.builder(TEST_USER_ID, null))
                .withMessage("username must not be null");
    }

    @ParameterizedTest
    @ValueSource(strings = { "", "ab", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" })
    void givenInvalidUsernamesShouldThrow(String username) {

        UserContact.Builder builder = UserContact.builder(TEST_USER_ID, username);

        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(builder::build);

    }

    // Using pairwise testing
    @ParameterizedTest
    @CsvSource(value = {
            "NIL,NIL,NIL,NIL",
            "Alex,Doe,NIL,666666666",
            "NIL,Doe,alexdoe@example.com,666666666",
            "Alex,NIL,alexdoe@example.com,666666666"
    }, nullValues = "NIL")
    void givenOptionalParametersExpecttoBeDefined(String firstName, String lastName, String email, String phone) {

        UserContact contact = UserContact.builder(TEST_USER_ID, TEST_USERNAME)
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .phone(phone).build();
        assertThat(contact.getFirstName()).isEqualTo(Optional.ofNullable(firstName));
        assertThat(contact.getLastName()).isEqualTo(Optional.ofNullable(lastName));
        assertThat(contact.getEmail()).isEqualTo(Optional.ofNullable(email));
        assertThat(contact.getPhone()).isEqualTo(Optional.ofNullable(phone));
    }
}
