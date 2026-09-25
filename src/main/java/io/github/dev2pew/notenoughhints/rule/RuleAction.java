package io.github.dev2pew.notenoughhints.rule;

import java.util.Objects;

public sealed interface RuleAction {
    String hintId();

    record ShowHint(String hintId) implements RuleAction {
        public ShowHint {
            requireHintId(hintId);
        }
    }

    record HideHint(String hintId) implements RuleAction {
        public HideHint {
            requireHintId(hintId);
        }
    }

    private static void requireHintId(String hintId) {
        Objects.requireNonNull(hintId, "hintId");
        if (hintId.isBlank()) {
            throw new IllegalArgumentException("hintId must not be blank");
        }
    }
}
