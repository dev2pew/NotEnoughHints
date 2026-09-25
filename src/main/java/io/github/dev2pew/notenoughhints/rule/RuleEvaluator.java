package io.github.dev2pew.notenoughhints.rule;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import io.github.dev2pew.notenoughhints.context.ClientContext;

public final class RuleEvaluator {
    private static final Comparator<Rule> ORDER =
            Comparator.comparingInt(Rule::priority).thenComparing(Rule::id);

    public List<Rule> matchingRules(ClientContext context, List<Rule> rules) {
        return rules.stream()
                .filter(Rule::enabled)
                .filter(rule -> rule.condition().test(context))
                .sorted(ORDER)
                .toList();
    }

    public boolean requiresInventory(List<Rule> rules) {
        return rules.stream()
                .filter(Rule::enabled)
                .map(Rule::condition)
                .anyMatch(Condition::requiresInventory);
    }

    public boolean requiresNestedInventory(List<Rule> rules) {
        return rules.stream()
                .filter(Rule::enabled)
                .map(Rule::condition)
                .anyMatch(Condition::requiresNestedInventory);
    }

    public RuleEvaluationResult evaluate(
            ClientContext context, List<Rule> rules, Set<String> defaultVisibleHintIds) {
        LinkedHashSet<String> visible = new LinkedHashSet<>(defaultVisibleHintIds);
        List<String> matchedRuleIds = new ArrayList<>();

        for (Rule rule : matchingRules(context, rules)) {
            matchedRuleIds.add(rule.id());

            for (RuleAction action : rule.actions()) {
                switch (action) {
                    case RuleAction.ShowHint show -> visible.add(show.hintId());
                    case RuleAction.HideHint hide -> visible.remove(hide.hintId());
                }
            }
        }

        return new RuleEvaluationResult(visible, matchedRuleIds);
    }
}
