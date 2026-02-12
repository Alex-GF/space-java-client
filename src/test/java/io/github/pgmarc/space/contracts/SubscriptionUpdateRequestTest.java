package io.github.pgmarc.space.contracts;

import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class SubscriptionUpdateRequestTest {

    @Test
    void givenParametersShouldCreateSubscriptionUpdateRequest() {

        String serviceName = "Petclinic";
        String version = "v3";
        String plan = "PLATINUM";
        String addOn = "petLover";
        int quantity = 1;
        Service petclinic = Service.builder(serviceName, version)
            .plan(plan)
            .addOn(addOn, quantity)
            .build();

        SubscriptionUpdateRequest upReq = SubscriptionUpdateRequest.builder()
            .startService(serviceName, version)
                .plan(plan)
                .addOn(addOn, quantity)
            .endService()
            .build();

        assertThat(upReq.getServices()).contains(petclinic);
    }


    @Test
    void givenMultipleServicesInSubscriptionShouldCreate() {

        String service1Name = "Petclinic";
        String service2Name = "Petclinic Labs";

        Service service1 = Service.builder(service1Name, "v1").plan("GOLD").build();
        Service service2 = Service.builder(service2Name, "v2").plan("PLATINUM").build();

        SubscriptionUpdateRequest sub = SubscriptionUpdateRequest.builder()
            .subscribe(service1)
            .subscribe(service2)
            .build();

        assertThat(sub.getServices()).contains(service1, service2);
    }

    @Test
    void givenServiceCollectionShouldCreate() {

        String service1Name = "Petclinic";
        String service2Name = "Petclinic Labs";

        Service service1 = Service.builder(service1Name, "v1").plan("GOLD").build();
        Service service2 = Service.builder(service2Name, "v2").plan("PLATINUM").build();
        Set<Service> services = Set.of(service1, service2);

        SubscriptionUpdateRequest sub = SubscriptionUpdateRequest.builder()
            .subscribeAll(services)
            .build();

        assertThat(sub.getServices()).contains(service1, service2);
    }

    @Test
    void givenEmptyCollectionOfServicesShouldThrow() {

        Collection<Service> emptyServices = Set.of();
        SubscriptionUpdateRequest.Builder builder = SubscriptionUpdateRequest
            .builder();
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> builder.subscribeAll(emptyServices))
            .withMessage("services must not be empty");
    }
}
