package io.github.dev2pew.notenoughhints.rule;

import java.util.List;
import java.util.Set;

public record RuleEvaluationResult(
        Set<String> visibleHintIds,
        List<String> matchedRuleIds,
        int evaluatedRuleCount,
        int inventorySelectorCount) {
    public RuleEvaluationResult {
        visibleHintIds = Set.copyOf(visibleHintIds);
        matchedRuleIds = List.copyOf(matchedRuleIds);

        if (evaluatedRuleCount < 0 || inventorySelectorCount < 0) {
            throw new IllegalArgumentException("Diagnostic counts must not be negative");
        }
    }
}
