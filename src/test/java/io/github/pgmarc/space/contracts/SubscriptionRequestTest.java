package io.github.pgmarc.space.contracts;

import static org.assertj.core.api.Assertions.*;


import org.junit.jupiter.api.Test;

import java.util.Set;

class SubscriptionRequestTest {

    private static final UserContact TEST_USER_CONTACT = UserContact.builder("123456789", "alexdoe")
            .build();

    @Test
    void givenMultipleServicesInSubscriptionShouldCreate() {

        int renewalDays = 30;
        String service1Name = "Petclinic";
        String service2Name = "Petclinic Labs";

        Service service1 = Service.builder(service1Name, "v1").plan("GOLD").build();
        Service service2 = Service.builder(service2Name, "v2").plan("PLATINUM").build();

        SubscriptionRequest sub = SubscriptionRequest
                .builder(TEST_USER_CONTACT)
                .subscribe(service1)
                .subscribe(service2)
                .renewInDays(renewalDays)
                .build();

        assertThat(sub.getServices()).contains(service1, service2);
    }

    @Test
    void givenNegativeRenewalDaysShouldThrow() {
        SubscriptionRequest.Builder builder = SubscriptionRequest
            .builder(TEST_USER_CONTACT);
        Set<Service> emptyServices = Set.of();
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> builder.renewInDays(0))
            .withMessage("renewal period given in days must be positive");

    }

    @Test
    void givenEmptyCollectionOfAddOnsShouldThrow() {
        SubscriptionRequest.Builder builder = SubscriptionRequest
            .builder(TEST_USER_CONTACT);
        Set<Service> emptyServices = Set.of();
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> builder.subscribeAll(emptyServices))
            .withMessage("services must not be empty");
    }

    @Test
    void givenConsecutiveServiceCreationShouldThrow() {

        SubscriptionRequest.Builder builder = SubscriptionRequest
                .builder(TEST_USER_CONTACT)
                .startService("test", "v1");

        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> builder.startService("incorrect", "v1"))
                .withMessage("you must build a service before creating another");
    }

    @Test
    void givenPlanCallBeforeCallingCreationServiceShouldThrow() {

        SubscriptionRequest.Builder builder = SubscriptionRequest
                .builder(TEST_USER_CONTACT);

        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> builder.plan("foo"))
                .withMessage("you must call 'newService' before setting a plan: foo");
    }

    @Test
    void givenAddOnCallBeforeCallingCreationServiceShouldThrow() {

        SubscriptionRequest.Builder builder = SubscriptionRequest
                .builder(TEST_USER_CONTACT);

        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> builder.addOn("foo", 1))
                .withMessage("you must call 'newService' before setting an add-on: foo");

    }

    @Test
    void givenServiceBuildCallBeforeCreationServiceShouldThrow() {

        SubscriptionRequest.Builder builder = SubscriptionRequest.builder(TEST_USER_CONTACT);

        assertThatExceptionOfType(IllegalStateException.class).isThrownBy(builder::endService)
                .withMessage("you must call 'newService' before adding a service");
    }

    @Test
    void whenNoRequiredParametersInputShouldThrow() {

        SubscriptionRequest.Builder builder = SubscriptionRequest.builder(null);

        assertThatExceptionOfType(NullPointerException.class)
                .isThrownBy(builder::build)
                .withMessage("userContact must not be null");
    }

    @Test
    void givenOptionalRenewalDaysShouldNotThrow() {

        SubscriptionRequest subReq = SubscriptionRequest.builder(TEST_USER_CONTACT)
                .startService("foo", "bar").plan("baz")
                .endService().build();

        assertThat(subReq.getRenewalPeriod()).isNull();

    }

    @Test
    void givenNoEndServiceShouldThrow() {

        SubscriptionRequest.Builder builder = SubscriptionRequest.builder(TEST_USER_CONTACT)
                .startService("foo", "bar").plan("baz");

        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(builder::build)
                .withMessage("finish the creation of your service by calling endService");
    }

    @Test
    void givenEmptySubscriptionRequestShouldThrow() {

        SubscriptionRequest.Builder builder = SubscriptionRequest.builder(TEST_USER_CONTACT);

        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(builder::build)
                .withMessage("you have to be subscribed al least to one service");
    }

}
