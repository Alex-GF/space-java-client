package io.github.pgmarc.space.contracts;

import java.util.Objects;
import java.util.Optional;

public final class UserContact {

    private final String userId;
    private final String username;
    private final String firstName;
    private final String lastName;
    private final String email;
    private final String phone;

    private UserContact(Builder builder) {
        this.userId = builder.userId;
        this.username = builder.username;
        this.firstName = builder.firstName;
        this.lastName = builder.lastName;
        this.email = builder.email;
        this.phone = builder.phone;
    }

    public String getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public Optional<String> getFirstName() {
        return Optional.ofNullable(firstName);
    }

    public Optional<String> getLastName() {
        return Optional.ofNullable(lastName);
    }

    public Optional<String> getEmail() {
        return Optional.ofNullable(email);
    }

    public Optional<String> getPhone() {
        return Optional.ofNullable(phone);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        UserContact that = (UserContact) o;
        return Objects.equals(userId, that.userId) && Objects.equals(username, that.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, username);
    }

    @Override
    public String toString() {
        return "UserContact{" +
            "userId='" + userId + '\'' +
            ", username='" + username + '\'' +
            ", firstName='" + firstName + '\'' +
            ", lastName='" + lastName + '\'' +
            ", email='" + email + '\'' +
            ", phone='" + phone + '\'' +
            '}';
    }

    public static Builder builder(String userId, String username) {
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(username, "username must not be null");
        return new Builder(userId, username);
    }

    public static class Builder {

        private final String userId;
        private final String username;
        private String firstName;
        private String lastName;
        private String email;
        private String phone;

        private Builder(String userId, String username) {
            this.userId = userId;
            this.username = username;
        }

        public Builder firstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public Builder lastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder phone(String phone) {
            this.phone = phone;
            return this;
        }

        public UserContact build() {
            if (username.isBlank()) {
                throw new IllegalArgumentException("username is blank");
            }
            validateUserNameLength();
            return new UserContact(this);
        }

        private void validateUserNameLength() {
            int length = this.username.length();
            boolean isValidLength = length >= 3 && length <= 30;
            if (!isValidLength) {
                throw new IllegalArgumentException("username must be between 3 and 30 characters. Current " + username
                        + " has " + length + "characters");
            }
        }
    }

    public enum Keys {

        USER_ID("userId"),
        USERNAME("username"),
        FIRST_NAME("firstName"),
        LAST_NAME("lastName"),
        EMAIL("email"),
        PHONE("phone");

        private final String name;

        Keys(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
