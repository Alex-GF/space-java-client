package io.github.pgmarc.space.contracts;

import java.time.Period;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public final class SubscriptionRequest {

    private final UserContact userContact;
    private final Set<Service> services;
    private final Period renewalPeriod;

    private SubscriptionRequest(Builder builder) {
        this.userContact = builder.userContact;
        this.renewalPeriod = builder.renewalPeriod;
        this.services = builder.services;
    }

    public static Builder builder(UserContact userContact) {
        return new Builder(userContact);
    }

    public UserContact getUserContact() {
        return userContact;
    }

    public Collection<Service> getServices() {
        return Collections.unmodifiableCollection(services);
    }

    public Period getRenewalPeriod() {
        return renewalPeriod;
    }

    public static final class Builder {

        private final UserContact userContact;
        private final Set<Service> services = new HashSet<>();
        private Service.Builder serviceBuilder;
        private Period renewalPeriod;

        private Builder(UserContact userContact) {
            this.userContact = userContact;
        }

        public Builder subscribe(Service service) {
            this.services.add(Objects.requireNonNull(service, "service must not be null"));
            return this;
        }

        public Builder subscribeAll(Collection<Service> services) {
            Objects.requireNonNull(services, "services must not be null");
            if (services.isEmpty()) {
                throw  new IllegalArgumentException("services must not be empty");
            }
            this.services.addAll(services);
            return this;
        }

        public Builder renewInDays(int days) {
            if (days < 1) {
                throw new IllegalArgumentException("renewal period given in days must be positive");
            }
            this.renewalPeriod = Period.ofDays(days);
            return this;
        }

        public Builder startService(String name, String version) {
            if (isServiceBuilderAlive()) {
                throw new IllegalStateException("you must build a service before creating another");
            }
            this.serviceBuilder = Service.builder(name, version);
            return this;
        }

        public Builder plan(String plan) {
            validateServiceBuilderCalled("you must call 'newService' before setting a plan: " + plan);
            serviceBuilder.plan(plan);
            return this;
        }

        public Builder addOn(String addOnName, long quantity) {
            validateServiceBuilderCalled("you must call 'newService' before setting an add-on: " + addOnName);
            serviceBuilder.addOn(addOnName, quantity);
            return this;
        }

        public Builder endService() {
            validateServiceBuilderCalled("you must call 'newService' before adding a service");
            services.add(serviceBuilder.build());
            destroyServiceBuilder();
            return this;
        }

        public SubscriptionRequest build() {
            if (isServiceBuilderAlive()) {
                throw new IllegalStateException("finish the creation of your service by calling endService");
            }
            Objects.requireNonNull(userContact, "userContact must not be null");
            if (services.isEmpty()) {
                throw new IllegalStateException("you have to be subscribed al least to one service");
            }
            return new SubscriptionRequest(this);
        }

        private boolean isServiceBuilderAlive() {
            return serviceBuilder != null;
        }

        private void validateServiceBuilderCalled(String message) {
            if (!isServiceBuilderAlive()) {
                throw new IllegalStateException(message);
            }
        }

        private void destroyServiceBuilder() {
            this.serviceBuilder = null;
        }
    }
}
