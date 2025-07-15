package io.github.isagroup.space.client.services;

public enum PricingAvailabilityStatus {
    ACTIVE, ARCHIVED;

    @Override
    public String toString() {
        return name().toLowerCase();
    }

}
