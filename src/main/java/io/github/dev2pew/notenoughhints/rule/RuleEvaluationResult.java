package io.github.dev2pew.notenoughhints.rule;

import java.util.List;
import java.util.Set;

public record RuleEvaluationResult(Set<String> visibleHintIds, List<String> matchedRuleIds) {
    public RuleEvaluationResult {
        visibleHintIds = Set.copyOf(visibleHintIds);
        matchedRuleIds = List.copyOf(matchedRuleIds);
    }
}
