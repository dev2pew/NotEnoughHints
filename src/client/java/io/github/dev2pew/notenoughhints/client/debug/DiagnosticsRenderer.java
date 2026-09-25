package io.github.dev2pew.notenoughhints.client.debug;

import java.util.List;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public final class DiagnosticsRenderer {
    private static final int X = 5;
    private static final int Y = 5;
    private static final int PADDING = 3;
    private static final int LINE_GAP = 1;
    private static final int BACKGROUND = 0xB0000000;
    private static final int TEXT = 0xFFFFFFFF;

    private final DiagnosticsController controller;

    public DiagnosticsRenderer(DiagnosticsController controller) {
        this.controller = controller;
    }

    public void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
        Minecraft client = Minecraft.getInstance();
        List<String> lines = controller.lines();
        if (lines.isEmpty() || client.options.hideGui) {
            return;
        }

        int lineHeight = client.font.lineHeight + LINE_GAP;
        int textWidth = lines.stream().mapToInt(client.font::width).max().orElse(0);
        int width = textWidth + PADDING * 2;
        int height = lines.size() * lineHeight - LINE_GAP + PADDING * 2;

        graphics.fill(X, Y, X + width, Y + height, BACKGROUND);

        int textY = Y + PADDING;
        for (String line : lines) {
            graphics.drawString(client.font, line, X + PADDING, textY, TEXT);
            textY += lineHeight;
        }
    }
}
