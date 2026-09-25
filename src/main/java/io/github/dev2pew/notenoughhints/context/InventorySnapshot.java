package io.github.dev2pew.notenoughhints.context;

import java.util.EnumMap;
import java.util.Map;

public record InventorySnapshot(
        Map<InventoryScope, Map<String, Integer>> directCounts,
        Map<InventoryScope, Map<String, Integer>> nestedInclusiveCounts) {
    public InventorySnapshot {
        directCounts = immutableCopy(directCounts);
        nestedInclusiveCounts = immutableCopy(nestedInclusiveCounts);
    }

    public static InventorySnapshot empty() {
        return new InventorySnapshot(Map.of(), Map.of());
    }

    public int count(InventoryScope scope, String itemId, boolean includeNested) {
        Map<InventoryScope, Map<String, Integer>> source =
                includeNested ? nestedInclusiveCounts : directCounts;
        return source.getOrDefault(scope, Map.of()).getOrDefault(itemId, 0);
    }

    private static Map<InventoryScope, Map<String, Integer>> immutableCopy(
            Map<InventoryScope, Map<String, Integer>> source) {
        EnumMap<InventoryScope, Map<String, Integer>> copy =
                new EnumMap<>(InventoryScope.class);
        source.forEach((scope, values) -> copy.put(scope, Map.copyOf(values)));
        return Map.copyOf(copy);
    }
}
