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
        return matchingRules(context, rules, Set.of());
    }

    public List<Rule> matchingRules(
            ClientContext context, List<Rule> rules, Set<String> disabledRuleIds) {
        return rules.stream()
                .filter(Rule::enabled)
                .filter(rule -> !disabledRuleIds.contains(rule.id()))
                .filter(rule -> rule.condition().test(context))
                .sorted(ORDER)
                .toList();
    }

    public boolean requiresInventory(List<Rule> rules) {
        return requiresInventory(rules, Set.of());
    }

    public boolean requiresInventory(List<Rule> rules, Set<String> disabledRuleIds) {
        return rules.stream()
                .filter(Rule::enabled)
                .filter(rule -> !disabledRuleIds.contains(rule.id()))
                .map(Rule::condition)
                .anyMatch(Condition::requiresInventory);
    }

    public boolean requiresNestedInventory(List<Rule> rules) {
        return requiresNestedInventory(rules, Set.of());
    }

    public boolean requiresNestedInventory(List<Rule> rules, Set<String> disabledRuleIds) {
        return rules.stream()
                .filter(Rule::enabled)
                .filter(rule -> !disabledRuleIds.contains(rule.id()))
                .map(Rule::condition)
                .anyMatch(Condition::requiresNestedInventory);
    }

    public RuleEvaluationResult evaluate(
            ClientContext context, List<Rule> rules, Set<String> defaultVisibleHintIds) {
        return evaluate(context, rules, defaultVisibleHintIds, Set.of());
    }

    public RuleEvaluationResult evaluate(
            ClientContext context,
            List<Rule> rules,
            Set<String> defaultVisibleHintIds,
            Set<String> disabledRuleIds) {
        LinkedHashSet<String> visible = new LinkedHashSet<>(defaultVisibleHintIds);
        List<Rule> matchedRules = new ArrayList<>();
        int evaluatedRuleCount = 0;
        int inventorySelectorCount = 0;

        for (Rule rule : rules) {
            if (!rule.enabled() || disabledRuleIds.contains(rule.id())) {
                continue;
            }

            evaluatedRuleCount++;
            inventorySelectorCount += rule.condition().inventorySelectorCount();
            if (rule.condition().test(context)) {
                matchedRules.add(rule);
            }
        }

        matchedRules.sort(ORDER);
        List<String> matchedRuleIds = new ArrayList<>(matchedRules.size());
        for (Rule rule : matchedRules) {
            matchedRuleIds.add(rule.id());

            for (RuleAction action : rule.actions()) {
                switch (action) {
                    case RuleAction.ShowHint show -> visible.add(show.hintId());
                    case RuleAction.HideHint hide -> visible.remove(hide.hintId());
                }
            }
        }

        return new RuleEvaluationResult(
                visible, matchedRuleIds, evaluatedRuleCount, inventorySelectorCount);
    }
}
