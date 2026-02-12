package io.github.pgmarc.space.contracts;

import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

public final class Subscription {

    private final UserContact userContact;
    private final Map<String, Service> services;
    private final ZonedDateTime startDate;
    private final ZonedDateTime endDate;
    private final Period renewalPeriod;
    private final List<Snapshot> history;
    private final Map<String, Map<String, UsageLevel>> usageLevels;

    private Subscription(Builder builder) {
        this.userContact = builder.userContact;
        this.services = builder.services;
        this.startDate = builder.startDate;
        this.endDate = builder.endDate;
        this.renewalPeriod = builder.renewalPeriod;
        this.history = builder.history;
        this.usageLevels = builder.usageLevels;
    }

    public static Builder builder(UserContact userContact, ZonedDateTime startDate, ZonedDateTime endDate,
            Service service) {
        return new Builder(userContact, startDate, endDate).subscribe(service);
    }

    public static Builder builder(UserContact userContact, ZonedDateTime startDate, ZonedDateTime endDate, Collection<Service> services) {
        return new Builder(userContact, startDate, endDate).subscribeAll(services);
    }

    public String getUserId() {
        return userContact.getUserId();
    }

    public String getUsername() {
        return userContact.getUsername();
    }

    public LocalDateTime getStartDate() {
        return startDate.toLocalDateTime();
    }

    public LocalDateTime getEndDate() {
        return endDate.toLocalDateTime();
    }

    public Optional<Period> getRenewalPeriod() {
        return Optional.of(renewalPeriod);
    }

    public boolean isAutoRenewable() {
        return renewalPeriod != null;
    }

    public Optional<LocalDateTime> getRenewalDate() {
        return Optional.ofNullable(renewalPeriod)
            .map( renewalPeriod -> endDate.plus(renewalPeriod).toLocalDateTime());
    }

    /**
     * Checks whether a <code>date</code> is within the subscription interval.
     * A subscription is active if <code>date</code> is between subscription
     * start date and end date (both boundaries are inclusive).
     */
    public boolean isActive(LocalDateTime date) {
        Objects.requireNonNull(date, "date must not be null");
        ZonedDateTime utcDate = ZonedDateTime.of(date, ZoneId.of("UTC"));

        return  (startDate.isEqual(utcDate) || startDate.isBefore(utcDate)) &&
            (endDate.isAfter(utcDate) || endDate.isEqual(utcDate));
    }

    /**
     * A subscription has expired if given <code>date</code> is after
     * subscription interval.
     */
    public boolean isExpired(LocalDateTime date) {
        Objects.requireNonNull(date, "date must not be null");
        ZonedDateTime utcDate = ZonedDateTime.of(date, ZoneId.of("UTC"));
        return startDate.isBefore(utcDate) && endDate.isBefore(utcDate);
    }

    public Map<String, Service> getServicesMap() {
        return Collections.unmodifiableMap(services);
    }

    public Collection<Service> getServices() {
        return Collections.unmodifiableCollection(services.values());
    }

    public Optional<Service> getService(String serviceName) {
        return Optional.ofNullable(this.services.get(serviceName));
    }

    public List<Snapshot> getHistory() {
        return Collections.unmodifiableList(history);
    }

    public Map<String, Map<String, UsageLevel>> getUsageLevels() {
        return Collections.unmodifiableMap(usageLevels);
    }

    public Optional<Map<String, UsageLevel>> getServiceUsageLevels(String service) {
        Objects.requireNonNull(service, "service name must not be null");
        return Optional.ofNullable(usageLevels.get(service));
    }

    public Optional<UsageLevel> getUsageLevel(String service, String usageLimit) {
        Objects.requireNonNull(service, "service name must not be null");
        Objects.requireNonNull(usageLimit, "usage limit name must not be null");
        if (!usageLevels.containsKey(service)) {
            return Optional.empty();
        }
        return Optional.ofNullable(usageLevels.get(service).get(usageLimit));

    }

    @Override
    public String toString() {
        return "Subscription{" +
            "userContact=" + userContact +
            ", services=" + services +
            ", startDate=" + startDate +
            ", endDate=" + endDate +
            ", renewalPeriod=" + renewalPeriod +
            ", history=" + history +
            ", usageLevels=" + usageLevels +
            '}';
    }

    public enum Keys {
        USER_CONTACT("userContact"),
        BILLING_PERIOD("billingPeriod"),
        CONTRACTED_SERVICES("contractedServices"),
        SUBSCRIPTION_PLANS("subscriptionPlans"),
        SUBSCRIPTION_ADDONS("subscriptionAddOns"),
        USAGE_LEVELS("usageLevels"),
        HISTORY("history");

        private final String name;

        Keys(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public static final class Builder {

        private final ZonedDateTime startDate;
        private final ZonedDateTime endDate;
        private final UserContact userContact;
        private final Map<String, Service> services = new HashMap<>();
        private final List<Snapshot> history = new ArrayList<>();
        private final Map<String, Map<String, UsageLevel>> usageLevels = new HashMap<>();
        private Period renewalPeriod;

        private Builder(UserContact userContact, ZonedDateTime startDate, ZonedDateTime endDate) {
            this.startDate = startDate;
            this.endDate = endDate;
            this.userContact = userContact;
        }

        public Builder renewIn(Period renewalPeriod) {
            this.renewalPeriod = renewalPeriod;
            return this;
        }

        public Builder renewInDays(int days) {
            this.renewalPeriod = Period.ofDays(days);
            return this;
        }

        public Builder renewInMonths(int months) {
            this.renewalPeriod = Period.ofMonths(months);
            return this;
        }

        public Builder renewInYears(int years) {
            this.renewalPeriod = Period.ofYears(years);
            return this;
        }

        public Builder subscribe(Service service) {
            this.services.put(service.getName(), Objects.requireNonNull(service, "service must not be null"));
            return this;
        }

        private Map<String, Service> collectionToServiceMap(Collection<Service> services) {
            return services.stream()
                    .collect(Collectors.toUnmodifiableMap(Service::getName, service -> service));
        }

        public Builder subscribeAll(Collection<Service> services) {
            Objects.requireNonNull(services, "services must not be null");
            this.services.putAll(collectionToServiceMap(services));
            return this;
        }

        public Builder addSnapshots(Collection<Snapshot> snapshots) {
            Objects.requireNonNull(snapshots, "snapshots must not be null");
            this.history.addAll(snapshots);
            return this;
        }

        public Builder addUsageLevels(Map<String, Map<String, UsageLevel>> usageLevels) {
            this.usageLevels.putAll(usageLevels);
            return this;
        }

        private void validateSubscriptionInterval() {
            Objects.requireNonNull(startDate, "start date must not be null");
            Objects.requireNonNull(endDate, "end date must not be null");
            if (startDate.isAfter(endDate)) {
                throw new IllegalStateException("startDate is after endDate");
            }
        }

        private void validateRenewalPeriod() {
            if (renewalPeriod != null && renewalPeriod.isZero()) {
                throw new IllegalArgumentException("your renewal period must not be zero");
            }

            if (renewalPeriod != null && renewalPeriod.isNegative()) {
                throw new IllegalStateException("renewal period must not be negative");
            }
        }

        public Subscription build() {
            Objects.requireNonNull(userContact, "userContact must not be null");
            validateSubscriptionInterval();
            validateRenewalPeriod();
            return new Subscription(this);
        }
    }

    public static final class Snapshot {

        private final ZonedDateTime starDateTime;
        private final ZonedDateTime endDateTime;
        private final Map<String, Service> services;

        private Snapshot(ZonedDateTime startDateTime, ZonedDateTime endDateTime,
                Map<String, Service> services) {
            this.starDateTime = startDateTime;
            this.endDateTime = endDateTime;
            this.services = new HashMap<>(services);
        }

        public LocalDateTime getStartDate() {
            return starDateTime.toLocalDateTime();
        }

        public LocalDateTime getEndDate() {
            return endDateTime.toLocalDateTime();
        }

        public Map<String, Service> getServices() {
            return Collections.unmodifiableMap(services);
        }

        public Optional<Service> getService(String name) {
            return Optional.ofNullable(services.get(name));
        }

        public static Snapshot of(ZonedDateTime startDateTime, ZonedDateTime endDateTime,
                                  Map<String, Service> services) {
            return new Snapshot(startDateTime, endDateTime, services);
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            Snapshot snapshot = (Snapshot) o;
            return Objects.equals(starDateTime, snapshot.starDateTime) && Objects.equals(endDateTime, snapshot.endDateTime) && Objects.equals(services, snapshot.services);
        }

        @Override
        public int hashCode() {
            return Objects.hash(starDateTime, endDateTime, services);
        }

        @Override
        public String toString() {
            return "Snapshot{" +
                "starDateTime=" + starDateTime +
                ", endDateTime=" + endDateTime +
                ", services=" + services +
                '}';
        }
    }
}
