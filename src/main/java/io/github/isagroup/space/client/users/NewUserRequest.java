package io.github.isagroup.space.client.users;

import java.util.Objects;

public record NewUserRequest(String username, String password, Role role) {
    public NewUserRequest {
        Objects.requireNonNull(username);
        Objects.requireNonNull(password);
        if (password.isBlank()) {
            throw new IllegalArgumentException("password is blank");
        }
        if (password.length() < 5) {
            throw new IllegalArgumentException("Password is too short, length must be greater than 5 characters");
        }
    }
}
