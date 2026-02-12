package io.github.pgmarc.space.contracts;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.Optional;

public final class UsageLevel {

    private final String name;
    private final double consumed;
    private final ZonedDateTime resetTimestamp;

    public enum Keys {
        CONSUMED("consumed"),
        RESET_TIMESTAMP("resetTimeStamp");

        private final String name;

        Keys(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private UsageLevel(String name, double consumed, ZonedDateTime resetTimestamp) {
        this.name = name;
        this.consumed = consumed;
        this.resetTimestamp = resetTimestamp;
    }

    public String getName() {
        return name;
    }

    public Optional<LocalDateTime> getResetTimestamp() {
        return Optional.ofNullable(resetTimestamp).map(ZonedDateTime::toLocalDateTime);
    }

    public boolean isRenewableUsageLimit() {
        return resetTimestamp != null;
    }

    public double getConsumption() {
        return consumed;
    }

    private static void validateUsageLevel(String name, double consumed) {
        Objects.requireNonNull(name, "usage limit name must not be null");
        if (consumed < 0) {
            throw new IllegalArgumentException("usage level consumption must be positive");
        }
    }

    public static UsageLevel of(String name, double consumed) {
        return of(name, consumed, null);
    }

    public static UsageLevel of(String name, double consumed, ZonedDateTime resetTimestamp) {
        validateUsageLevel(name, consumed);
        return new UsageLevel(name, consumed, resetTimestamp);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        UsageLevel that = (UsageLevel) o;
        return Double.compare(consumed, that.consumed) == 0 && Objects.equals(name, that.name) && Objects.equals(resetTimestamp, that.resetTimestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, consumed, resetTimestamp);
    }

    @Override
    public String toString() {
        return "UsageLevel{" +
            "name='" + name + '\'' +
            ", consumed=" + consumed +
            ", resetTimestamp=" + resetTimestamp +
            '}';
    }
}
