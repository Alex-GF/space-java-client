package io.github.pgmarc.space.features;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class FeatureEvaluationResult {

    private final boolean available;
    private final Map<String,Usage> quotas;

    private FeatureEvaluationResult(boolean available, Map<String,Usage> quotas) {
        this.available = available;
        this.quotas = quotas;
    }

    public boolean isAvailable() {
        return available;
    }

    public Map<String,Usage> getQuotas() {
        return Collections.unmodifiableMap(quotas);
    }

    public Optional<Number> getConsumed(String usageLimit) {
        Objects.requireNonNull(usageLimit, "usage limit must not be null");
        return quotas.containsKey(usageLimit) ?
            Optional.of(quotas.get(usageLimit).getUsed()) : Optional.empty();
    }

    public Optional<Number> getLimit(String usageLimit) {
        return quotas.containsKey(usageLimit) ?
            Optional.of(quotas.get(usageLimit).getLimit()) : Optional.empty();
    }


    public static FeatureEvaluationResult of(boolean available, Map<String,Usage> quotas) {
        return new FeatureEvaluationResult(available, quotas);
    }

    @Override
    public String toString() {
        return "FeatureEvaluationResult{" +
            "available=" + available +
            ", quotas=" + quotas +
            '}';
    }

    public static final class Usage {

        private final Number used;
        private final Number limit;

        private Usage(Number used, Number limit) {
            this.used = used;
            this.limit = limit;
        }

        public Number getUsed() {
            return used;
        }

        public Number getLimit() {
            return limit;
        }

        public static Usage of(Number used, Number limit) {
            return new Usage(used, limit);
        }

        @Override
        public String toString() {
            return "Usage{" +
                "used=" + used +
                ", limit=" + limit +
                '}';
        }
    }

}
