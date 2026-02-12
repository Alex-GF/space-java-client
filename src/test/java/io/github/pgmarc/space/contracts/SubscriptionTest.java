package io.github.pgmarc.space.contracts;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Set;

class SubscriptionTest {

    private static final UserContact TEST_CONTACT = UserContact.builder("123456789", "alex").build();
    private static final ZonedDateTime START = ZonedDateTime.parse("2025-08-15T00:00:00Z");
    private static final ZonedDateTime END = START.plusDays(30);
    private static final Service TEST_SERVICE = Service.builder("petclinic", "2025").plan("GOLD").build();

    @Test
    void givenSubscriptionShould() {

        String userId = "123456789";
        String username = "alex";
        UserContact contact = UserContact.builder(userId, username).build();


        String serviceName = "petclinic";
        Service service = Service.builder(serviceName, "2025").plan("GOLD").build();

        ZonedDateTime snapshotStart = ZonedDateTime.parse("2024-01-01T00:00:00Z");
        ZonedDateTime snapshotEnd = ZonedDateTime.parse("2025-01-01T00:00:00Z");
        Service snapshotService = Service.builder(serviceName, "2024").plan("FREE").build();
        Subscription.Snapshot snapshot1 = Subscription.Snapshot.of(snapshotStart,
            snapshotEnd, Map.of("petclinic", snapshotService));
        Set<Subscription.Snapshot> history = Set.of(snapshot1);

        ZonedDateTime start = ZonedDateTime.parse("2025-08-28T00:00:00Z");
        ZonedDateTime end = ZonedDateTime.parse("2025-08-29T00:00:00Z");
        int renewalDays = 45;
        Period renewalPeriod = Period.ofDays(renewalDays);

        Subscription subscription = Subscription.builder(contact, start, end, service)
            .renewInDays(renewalDays)
            .addSnapshots(history)
            .build();

        assertThat(subscription.getUsername()).isEqualTo(username);
        assertThat(subscription.getStartDate()).isEqualTo(start.toLocalDateTime());
        assertThat(subscription.getEndDate()).isEqualTo(end.toLocalDateTime());
        assertThat(subscription.isAutoRenewable()).isTrue();
        assertThat(subscription.getRenewalPeriod()).hasValue(renewalPeriod);
        assertThat(subscription.getRenewalDate()).hasValue(end.plusDays(renewalDays).toLocalDateTime());
        assertThat(subscription.getService(serviceName)).hasValue(service);
        assertThat(subscription.getHistory()).contains(snapshot1);
        assertThat(subscription.getHistory().get(0).getStartDate()).isEqualTo(snapshotStart.toLocalDateTime());
        assertThat(subscription.getHistory().get(0).getEndDate()).isEqualTo(snapshotEnd.toLocalDateTime());
        assertThat(subscription.getHistory().get(0).getServices()).isNotEmpty();
        assertThat(subscription.getHistory().get(0).getService(serviceName)).hasValue(snapshotService);
    }

    @Test
    void givenUsageLevelsShouldCreateSubscription() {

        String petclinic = "petclinic";
        String maxPets = "maxPets";
        double registeredPets = 3;
        UsageLevel ulMaxPets = UsageLevel.of(maxPets, registeredPets);

        String petclinicAI = "petclinic-ai";
        String maxTokens = "maxTokens";
        ZonedDateTime reset = ZonedDateTime.parse("2025-02-01T00:00:00Z");
        UsageLevel ulMaxTokens =  UsageLevel.of(maxTokens, 20, reset);
        Map<String, Map<String, UsageLevel>> usageLevels =
            Map.of(petclinic, Map.of(maxPets, ulMaxPets),
                petclinicAI, Map.of(maxTokens,ulMaxTokens));
        Subscription subscription = Subscription.builder(TEST_CONTACT, START, END, TEST_SERVICE)
            .addUsageLevels(usageLevels)
            .build();

        assertThat(subscription.getServiceUsageLevels(petclinic)).isNotEmpty();
        assertThat(subscription.getUsageLevel(petclinic, maxPets)).hasValue(ulMaxPets);
    }

    @Test
    void givenNullServiceShouldBeEmptyServiceUsageLevels() {

        String petclinic = "petclinic";
        String maxPets = "maxPets";
        double registeredPets = 3;
        UsageLevel ulMaxPets = UsageLevel.of(maxPets, registeredPets);

        Map<String, Map<String, UsageLevel>> usageLevels = Map.of(petclinic, Map.of(maxPets, ulMaxPets));
        Subscription subscription = Subscription.builder(TEST_CONTACT, START, END, TEST_SERVICE)
            .addUsageLevels(usageLevels)
            .build();

        assertThatExceptionOfType(NullPointerException.class)
            .isThrownBy(() -> subscription.getServiceUsageLevels(null))
            .withMessage("service name must not be null");
    }

    @Test
    void givenNullUsageLimitShouldBeEmptyUsageLevel() {

        String petclinic = "petclinic";
        String maxPets = "maxPets";
        double registeredPets = 3;
        UsageLevel ulMaxPets = UsageLevel.of(maxPets, registeredPets);

        Map<String, Map<String, UsageLevel>> usageLevels = Map.of(petclinic, Map.of(maxPets, ulMaxPets));
        Subscription subscription = Subscription.builder(TEST_CONTACT, START, END, TEST_SERVICE)
            .addUsageLevels(usageLevels)
            .build();

        assertThatExceptionOfType(NullPointerException.class)
            .isThrownBy(() -> subscription.getUsageLevel(null, maxPets))
            .withMessage("service name must not be null");

        assertThatExceptionOfType(NullPointerException.class)
            .isThrownBy(() -> subscription.getUsageLevel(petclinic, null))
            .withMessage("usage limit name must not be null");
    }

    @Test
    void givenRenewalPeriodInDaysShouldCreate() {

        int renewalDays = 45;
        Subscription subscription = Subscription.builder(TEST_CONTACT, START, END, TEST_SERVICE)
            .renewInDays(renewalDays)
            .build();

        assertThat(subscription.getRenewalPeriod()).hasValue(Period.ofDays(renewalDays));

    }

    @Test
    void givenYearlySubscriptionShouldCreate() {
        int years = 1;
        Subscription subscription = Subscription.builder(TEST_CONTACT, START, END, TEST_SERVICE)
            .renewInYears(years)
            .build();

        assertThat(subscription.getRenewalPeriod()).hasValue(Period.ofYears(years));
    }

    @Test
    void givenDateAfterSubscriptionShouldBeExpired() {

        ZonedDateTime start = ZonedDateTime.parse("2025-01-01T00:00:00Z");
        ZonedDateTime end =  ZonedDateTime.parse("2025-02-01T00:00:00Z");
        Subscription subscription = Subscription.builder(TEST_CONTACT, start, end, TEST_SERVICE).build();

        LocalDateTime dateToCheck = ZonedDateTime.parse("2025-02-01T00:00:01Z").toLocalDateTime();
        assertThat(subscription.isExpired(dateToCheck)).isTrue();
        assertThat(subscription.isActive(dateToCheck)).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings =
        {
            "2025-01-01T00:00:00Z",
            "2025-01-15T00:00:00Z",
            "2025-02-01T00:00:00Z"
        })
    void givenDateInsideSubscriptionShouldBeActive(String utcDate) {

        ZonedDateTime start = ZonedDateTime.parse("2025-01-01T00:00:00Z");
        ZonedDateTime end =  ZonedDateTime.parse("2025-02-01T00:00:00Z");
        Subscription subscription = Subscription.builder(TEST_CONTACT, start, end, TEST_SERVICE).build();

        LocalDateTime dateToCheck = ZonedDateTime.parse(utcDate).toLocalDateTime();
        assertThat(subscription.isExpired(dateToCheck)).isFalse();
        assertThat(subscription.isActive(dateToCheck)).isTrue();
    }


    @Test
    void givenNegativePeriodShouldThrow() {

        Subscription.Builder builder = Subscription.builder(TEST_CONTACT, START, END, TEST_SERVICE)
            .renewIn(Period.ofDays(-1));

        assertThatExceptionOfType(IllegalStateException.class)
            .isThrownBy(builder::build)
            .withMessage("renewal period must not be negative");
    }

    @Test
    void givenStartDateAfterEndDateShouldThrow() {

        ZonedDateTime endDate = START.minusDays(1);
        Subscription.Builder builder = Subscription.builder(TEST_CONTACT, START, endDate, TEST_SERVICE);

        assertThatExceptionOfType(IllegalStateException.class)
            .isThrownBy(builder::build)
            .withMessage("startDate is after endDate");
    }

    @Test
    void givenRenewablePeriodSubscriptionShouldBeRenowable() {

        int months = 1;
        Period period = Period.ofMonths(months);

        Subscription subscription = Subscription.builder(TEST_CONTACT, START, END, TEST_SERVICE)
            .renewInMonths(months)
            .build();

        assertThat(subscription.isAutoRenewable()).isTrue();
        assertThat(subscription.getRenewalPeriod()).hasValue(period);
        assertThat(subscription.getRenewalDate()).hasValue(END.plus(period).toLocalDateTime());
    }

    @Test
    void givenNullStartDateShouldThrow() {

        Subscription.Builder builder = Subscription.builder(TEST_CONTACT, null, END, TEST_SERVICE);

        assertThatExceptionOfType(NullPointerException.class)
            .isThrownBy(builder::build)
            .withMessage("start date must not be null");
    }

    @Test
    void givenNullEmdDateShouldThrow() {

        Subscription.Builder builder = Subscription.builder(TEST_CONTACT, START, null, TEST_SERVICE);

        assertThatExceptionOfType(NullPointerException.class)
            .isThrownBy(builder::build)
            .withMessage("end date must not be null");
    }
}
