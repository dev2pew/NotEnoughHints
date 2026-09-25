package io.github.dev2pew.notenoughhints.rule;

import java.util.List;
import java.util.Objects;

public record Rule(
        String id,
        boolean enabled,
        int priority,
        Condition condition,
        List<RuleAction> actions) {
    public Rule {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(condition, "condition");
        actions = List.copyOf(actions);

        if (id.isBlank()) {
            throw new IllegalArgumentException("Rule id must not be blank");
        }
    }
}
