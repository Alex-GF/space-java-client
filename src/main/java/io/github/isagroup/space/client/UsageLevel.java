package io.github.isagroup.space.client;

import java.util.Date;
import java.util.Objects;

public record UsageLevel(int consumed, Date resetTimestamp) {
    public UsageLevel {
        Objects.requireNonNull(resetTimestamp);
        if (consumed < 0) {
            throw new IllegalArgumentException("consumption must be a positive number larger than zero");
        }
    }
}
