package io.github.dev2pew.notenoughhints.client.debug;

public record ClientTickMetrics(long contextRefreshNanos, long nestedAdapterCalls) {
    public ClientTickMetrics {
        if (contextRefreshNanos < 0 || nestedAdapterCalls < 0) {
            throw new IllegalArgumentException("Metric values must not be negative");
        }
    }

    public static ClientTickMetrics empty() {
        return new ClientTickMetrics(0L, 0L);
    }
}
