package io.github.dev2pew.notenoughhints.hud;

import java.util.List;
import java.util.Objects;

public record HintGroupDefinition(
        String id,
        HudAnchor anchor,
        int offsetX,
        int offsetY,
        HintFlow flow,
        int entryGap,
        List<HintDefinition> hints) {
    public HintGroupDefinition {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(anchor, "anchor");
        Objects.requireNonNull(flow, "flow");
        hints = List.copyOf(hints);

        if (id.isBlank()) {
            throw new IllegalArgumentException("Group id must not be blank");
        }
        if (entryGap < 0) {
            throw new IllegalArgumentException("entryGap must not be negative");
        }
    }
}
