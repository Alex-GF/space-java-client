package io.github.pgmarc.space.contracts;

import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertAll;


class UsageLevelTest {

    @Test
    void givenNonRenewableUsageLimitShouldCreate() {

        String usageLimitName = "maxPets";
        double consumption = 0;
        UsageLevel usageLevel = UsageLevel.of(usageLimitName, consumption);

        assertAll(
                () -> assertThat(usageLevel.getName()).isEqualTo(usageLimitName),
                () -> assertThat(usageLevel.getConsumption()).isEqualTo(consumption),
                () -> assertThat(usageLevel.isRenewableUsageLimit()).isFalse());
    }

    @Test
    void givenNullUsageLimitNameShouldThrow() {

        assertThatExceptionOfType(NullPointerException.class)
            .isThrownBy(() -> UsageLevel.of(null, 5))
                .withMessage("usage limit name must not be null");
    }

    @Test
    void givenNegativeConsumptionShouldThrow() {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> UsageLevel.of("maxPets", -1))
            .withMessage("usage level consumption must be positive");
    }

    @Test
    void givenRenewableUsageLimitShouldCreate() {

        ZonedDateTime resetTimestamp = ZonedDateTime.parse("2025-08-19T00:00:00Z");
        UsageLevel usageLevel = UsageLevel.of("maxTokens", 300, resetTimestamp);

        assertThat(usageLevel.getResetTimestamp()).hasValue(resetTimestamp.toLocalDateTime());
        assertThat(usageLevel.isRenewableUsageLimit()).isTrue();
    }

}
