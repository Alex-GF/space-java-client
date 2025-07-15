package io.github.isagroup.space.client.services;

import java.util.Map;

public record Service(String _id, String name, boolean disabled, Map<String, Map<String, Object>> activePricings,
        Map<String, Map<String, Object>> archivedPricings) {
}
