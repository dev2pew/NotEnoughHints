package io.github.dev2pew.notenoughhints.client.hud;

import java.util.List;

import io.github.dev2pew.notenoughhints.hud.HintFlow;
import io.github.dev2pew.notenoughhints.hud.HudAnchor;

public record HintGroupRenderState(
        boolean visible,
        HudAnchor anchor,
        int offsetX,
        int offsetY,
        HintFlow flow,
        int entryGap,
        int lineGap,
        int maxWidth,
        float scale,
        float opacity,
        List<ResolvedHint> hints) {
    public HintGroupRenderState {
        hints = List.copyOf(hints);
    }

    public static HintGroupRenderState hidden() {
        return new HintGroupRenderState(
                false,
                HudAnchor.BOTTOM_LEFT,
                0,
                0,
                HintFlow.HORIZONTAL,
                0,
                0,
                0,
                1.0F,
                1.0F,
                List.of());
    }
}
