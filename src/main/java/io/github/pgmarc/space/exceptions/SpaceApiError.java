package io.github.pgmarc.space.exceptions;

import java.util.Collections;
import java.util.Set;

public class SpaceApiError {

    private final Set<String> messages;

    private final int statusCode;

    public SpaceApiError(int code, Set<String> messages) {
        this.statusCode = code;
        this.messages = Collections.unmodifiableSet(messages);
    }

    int getStatusCode() {
        return statusCode;
    }

    @Override
    public String toString() {
        return String.join("\n", messages);
    }

}
