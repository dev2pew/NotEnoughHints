package io.github.dev2pew.notenoughhints.client.hud;

import java.util.List;
import java.util.Set;

public record HintDiagnostics(
        List<String> activeRuleIds,
        Set<String> unresolvedBindingIds,
        int visibleHintCount) {
    public HintDiagnostics {
        activeRuleIds = List.copyOf(activeRuleIds);
        unresolvedBindingIds = Set.copyOf(unresolvedBindingIds);
    }

    public static HintDiagnostics empty() {
        return new HintDiagnostics(List.of(), Set.of(), 0);
    }
}
