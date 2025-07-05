package io.github.isagroup.space.springboot.client.services;

public enum PricingAvailabilityStatus {
    ACTIVE, ARCHIVED;

    @Override
    public String toString() {
        return name().toLowerCase();
    }

}
