package io.github.dev2pew.notenoughhints.rule;

import java.util.Objects;

public record Rule(String id, boolean enabled, int priority, Condition condition) {
    public Rule {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(condition, "condition");

        if (id.isBlank()) {
            throw new IllegalArgumentException("Rule id must not be blank");
        }
    }
}
