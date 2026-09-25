package io.github.dev2pew.notenoughhints.client.hud;

import java.util.List;
import java.util.Set;

public record HintDiagnostics(
        List<String> activeRuleIds,
        Set<String> unresolvedBindingIds,
        Set<String> unboundBindingIds,
        int visibleHintCount,
        long ruleEvaluationNanos,
        int evaluatedRuleCount,
        int inventorySelectorCount) {
    public HintDiagnostics {
        activeRuleIds = List.copyOf(activeRuleIds);
        unresolvedBindingIds = Set.copyOf(unresolvedBindingIds);
        unboundBindingIds = Set.copyOf(unboundBindingIds);

        if (visibleHintCount < 0
                || ruleEvaluationNanos < 0
                || evaluatedRuleCount < 0
                || inventorySelectorCount < 0) {
            throw new IllegalArgumentException("Diagnostic values must not be negative");
        }
    }

    public static HintDiagnostics empty() {
        return new HintDiagnostics(List.of(), Set.of(), Set.of(), 0, 0L, 0, 0);
    }
}
