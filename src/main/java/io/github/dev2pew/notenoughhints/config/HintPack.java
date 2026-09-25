package io.github.dev2pew.notenoughhints.config;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.github.dev2pew.notenoughhints.hud.HintGroupDefinition;
import io.github.dev2pew.notenoughhints.rule.Rule;

public record HintPack(int schemaVersion, List<HintGroupDefinition> groups, List<Rule> rules) {
    public static final int CURRENT_SCHEMA_VERSION = 1;

    public HintPack {
        groups = List.copyOf(groups);
        rules = List.copyOf(rules);

        if (schemaVersion != CURRENT_SCHEMA_VERSION) {
            throw new IllegalArgumentException(
                    "Unsupported hint-pack schema_version: " + schemaVersion);
        }

        validateUniqueIds(groups, rules);
    }

    public static HintPack empty() {
        return new HintPack(CURRENT_SCHEMA_VERSION, List.of(), List.of());
    }

    private static void validateUniqueIds(List<HintGroupDefinition> groups, List<Rule> rules) {
        Set<String> groupIds = new HashSet<>();
        Set<String> hintIds = new HashSet<>();
        Set<String> ruleIds = new HashSet<>();

        for (HintGroupDefinition group : groups) {
            if (!groupIds.add(group.id())) {
                throw new IllegalArgumentException("Duplicate hint group id: " + group.id());
            }
            group.hints().forEach(
                    hint -> {
                        if (!hintIds.add(hint.id())) {
                            throw new IllegalArgumentException("Duplicate hint id: " + hint.id());
                        }
                    });
        }

        for (Rule rule : rules) {
            if (!ruleIds.add(rule.id())) {
                throw new IllegalArgumentException("Duplicate rule id: " + rule.id());
            }
        }
    }
}
