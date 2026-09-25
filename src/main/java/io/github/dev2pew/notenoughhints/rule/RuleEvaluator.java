package io.github.dev2pew.notenoughhints.rule;

import java.util.Comparator;
import java.util.List;

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
}
