package io.github.isagroup.space.springboot.client.contracts;

import java.util.Date;
import java.util.Objects;

public record BillingPeriod(Date startDate, Date endDate, boolean autoRenew, Integer renewalDays) {
    public BillingPeriod {
        Objects.requireNonNull(startDate);
        Objects.requireNonNull(endDate);
        if (autoRenew && Objects.isNull(renewalDays)) {
            throw new IllegalArgumentException("if autoRenew is enabled you must define renewalDays");
        }

    }
}
