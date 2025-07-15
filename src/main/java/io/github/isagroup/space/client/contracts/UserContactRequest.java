package io.github.isagroup.space.client.contracts;

import java.util.Objects;

public record UserContactRequest(String userId, String username, String firstName, String lastName, String email,
        String phone) {
    public UserContactRequest {
        Objects.requireNonNull(userId);
        Objects.requireNonNull(username);
    }
}
