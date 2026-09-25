package io.github.dev2pew.notenoughhints.context;

import java.util.EnumMap;
import java.util.Map;

public record InventorySnapshot(Map<InventoryScope, Map<String, Integer>> counts) {
    public InventorySnapshot {
        EnumMap<InventoryScope, Map<String, Integer>> copy =
                new EnumMap<>(InventoryScope.class);
        counts.forEach((scope, values) -> copy.put(scope, Map.copyOf(values)));
        counts = Map.copyOf(copy);
    }

    public static InventorySnapshot empty() {
        return new InventorySnapshot(Map.of());
    }

    public int count(InventoryScope scope, String itemId) {
        return counts.getOrDefault(scope, Map.of()).getOrDefault(itemId, 0);
    }
}
