package io.github.dev2pew.notenoughhints.config;

import java.util.Objects;

import io.github.dev2pew.notenoughhints.hud.HudAnchor;

public record GroupOverride(HudAnchor anchor, int offsetX, int offsetY, boolean visible) {
    public GroupOverride {
        Objects.requireNonNull(anchor, "anchor");
    }
}
