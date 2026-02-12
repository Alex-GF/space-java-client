package io.github.pgmarc.space.contracts;

import java.util.*;

public final class SubscriptionUpdateRequest {

    private final Set<Service> services;

    private SubscriptionUpdateRequest(Builder builder) {
        services = builder.services;
    }

    public Set<Service> getServices() {
        return Collections.unmodifiableSet(services);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private final Set<Service> services = new HashSet<>();
        private Service.Builder serviceBuilder;

        public Builder subscribe(Service service) {
            this.services.add(Objects.requireNonNull(service, "service must not be null"));
            return this;
        }

        public Builder subscribeAll(Collection<Service> services) {
            Objects.requireNonNull(services, "services must not be null");
            if (services.isEmpty()) {
                throw new IllegalArgumentException("services must not be empty");
            }
            this.services.addAll(services);
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

        public SubscriptionUpdateRequest build() {
            if (isServiceBuilderAlive()) {
                throw new IllegalStateException("finish the creation of your service by calling endService");
            }
            if (services.isEmpty()) {
                throw new IllegalStateException("you have to be subscribed al least to one service");
            }
            return new SubscriptionUpdateRequest(this);
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
