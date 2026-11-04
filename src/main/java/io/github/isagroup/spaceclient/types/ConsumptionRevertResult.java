package io.github.isagroup.spaceclient.types;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/** Refund outcome. A 204 has no per-limit amounts; a 200 includes reverted/skipped. */
public final class ConsumptionRevertResult {
    private final String consumptionId;
    private final boolean found;
    private final Map<String, Number> reverted;
    private final List<String> skipped;

    public ConsumptionRevertResult(String consumptionId, boolean found,
                                   Map<String, Number> reverted, List<String> skipped) {
        this.consumptionId = consumptionId;
        this.found = found;
        this.reverted = reverted == null ? null : Collections.unmodifiableMap(new java.util.LinkedHashMap<>(reverted));
        this.skipped = Collections.unmodifiableList(new java.util.ArrayList<>(skipped));
    }
    public String getConsumptionId() { return consumptionId; }
    public boolean isFound() { return found; }
    /** The amounts reported on 200, or null on 204/404. */
    public Map<String, Number> getReverted() { return reverted; }
    public List<String> getSkipped() { return skipped; }
}
