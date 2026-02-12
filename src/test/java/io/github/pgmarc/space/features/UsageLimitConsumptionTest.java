package io.github.pgmarc.space.features;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class UsageLimitConsumptionTest {

    @Test
    void givenConsumptionServiceNameMustBeLowerCase() {

        String service = "Petclinic";
        UsageLimitConsumption usageLimitConsumption = UsageLimitConsumption.builder(service)
            .addInt("intLimit", Integer.MAX_VALUE)
            .addLong("longLimit", Long.MAX_VALUE)
            .addFloat("floatLimit", Float.MAX_VALUE)
            .addDouble("doubleLimit", Double.MAX_VALUE)
            .build();
        assertThat(usageLimitConsumption.getService()).isLowerCase();
        assertThat(usageLimitConsumption.getConsumption()).isNotEmpty();
    }

    @Test
    void givenNullServiceNameShouldThrow() {

        assertThatExceptionOfType(NullPointerException.class)
            .isThrownBy(() -> UsageLimitConsumption.builder(null))
            .withMessage("service name must not be null");
    }

    @Test
    void givenEmptyServiceNameShouldThrow() {

        UsageLimitConsumption.Builder builder = UsageLimitConsumption.builder("");

        assertThatExceptionOfType(IllegalStateException.class)
            .isThrownBy(builder::build)
            .withMessage("service name must not be blank");
    }

    @Test
    void givenEmptyUsageLimitConsumptionShouldThrow() {

        UsageLimitConsumption.Builder builder = UsageLimitConsumption.builder("Petclinic");

        assertThatExceptionOfType(IllegalStateException.class)
            .isThrownBy(builder::build)
            .withMessage("usage limits consumption must not be empty");
    }



}
