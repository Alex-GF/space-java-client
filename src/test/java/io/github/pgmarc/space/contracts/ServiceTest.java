package io.github.pgmarc.space.contracts;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ServiceTest {


    private final Service.Builder baseBuilder = Service.builder("test", "alfa");

    @Test
    void givenServiceWithPlanShouldCreateService() {

        String name = "petclinic";
        String version = "v1";
        String plan = "GOLD";
        String addOnName = "petsAdoptionCentre";

        Service service = Service.builder(name, version)
                .plan(plan).addOn(addOnName, 1).build();

        assertThat(service.getName()).isEqualTo(name);
        assertThat(service.getVersion()).isEqualTo(version);
        assertThat(service.getPlan()).isPresent().hasValue(plan);
        assertThat(service.getAddOn(addOnName)).isPresent().hasValue(new AddOn(addOnName, 1));

    }

    @Test
    void givenServiceWithBlankPlanShouldThrow() {


        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> baseBuilder.plan(""))
                .withMessage("plan must not be blank");
    }

    @Test
    void givenNoPlanOrAddOnShouldThrow() {

        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(baseBuilder::build)
                .withMessage("At least you have to be subscribed to a plan or add-on");
    }

    @Test
    void givenNullAsAddOnKeyShouldThrow() {

        assertThatExceptionOfType(NullPointerException.class)
                .isThrownBy(() -> baseBuilder.addOn(null, 1))
                .withMessage("add-on name must not be null");
    }

    @Test
    void givenAddOnWithZeroQuantityShouldThrow() {

        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> baseBuilder.addOn("zeroQuantity", 0))
                .withMessage("zeroQuantity quantity must be greater than 0");
    }
}
