package io.github.dev2pew.notenoughhints.client.hud;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import io.github.dev2pew.notenoughhints.hud.HintLayout;

public final class PrototypeHintRenderer {
    private static final int GLYPH_BORDER_RGB = 0xD8D8D8;
    private static final int GLYPH_FILL_RGB = 0x4A4A4A;
    private static final int DESCRIPTION_FILL_RGB = 0x202020;
    private static final int TEXT_RGB = 0xFFFFFF;

    private final PrototypeHintController controller;

    public PrototypeHintRenderer(PrototypeHintController controller) {
        this.controller = controller;
    }

    public void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
        PrototypeHintRenderState state = controller.renderState();
        Minecraft client = Minecraft.getInstance();
        if (!state.visible() || client.options.hideGui || client.player == null) {
            return;
        }

        float scale = state.scale();
        int scaledHeight =
                Math.max(1, (int) Math.floor(client.getWindow().getGuiScaledHeight() / scale));
        int x = HintLayout.SCREEN_MARGIN;
        int y = scaledHeight - HintLayout.SCREEN_MARGIN - HintLayout.GLYPH_HEIGHT;

        Component binding = Component.literal(state.bindingText());
        Component description = Component.literal(state.description());
        int bindingWidth = state.bindingText().isEmpty() ? 0 : client.font.width(binding);
        int glyphWidth = HintLayout.glyphWidth(bindingWidth);
        int descriptionWidth = client.font.width(description);
        int descriptionBoxWidth =
                descriptionWidth + HintLayout.DESCRIPTION_HORIZONTAL_PADDING * 2;
        int descriptionX = x + glyphWidth + HintLayout.ENTRY_GAP;

        graphics.pose().pushMatrix();
        graphics.pose().scale(scale, scale);

        graphics.fill(
                x,
                y,
                x + glyphWidth,
                y + HintLayout.GLYPH_HEIGHT,
                argb(GLYPH_BORDER_RGB, state.opacity()));
        graphics.fill(
                x + 1,
                y + 1,
                x + glyphWidth - 1,
                y + HintLayout.GLYPH_HEIGHT - 1,
                argb(GLYPH_FILL_RGB, state.opacity()));

        if (!state.bindingText().isEmpty()) {
            int bindingX = HintLayout.centeredTextX(x, glyphWidth, bindingWidth);
            graphics.drawString(
                    client.font,
                    binding,
                    bindingX,
                    y + 3,
                    argb(TEXT_RGB, state.opacity()));
        }

        graphics.fill(
                descriptionX,
                y,
                descriptionX + descriptionBoxWidth,
                y + HintLayout.GLYPH_HEIGHT,
                argb(DESCRIPTION_FILL_RGB, state.opacity() * 0.75F));
        graphics.drawString(
                client.font,
                description,
                descriptionX + HintLayout.DESCRIPTION_HORIZONTAL_PADDING,
                y + 3,
                argb(TEXT_RGB, state.opacity()));

        graphics.pose().popMatrix();
    }

    private static int argb(int rgb, float opacity) {
        int alpha = Math.max(0, Math.min(255, Math.round(opacity * 255.0F)));
        return (alpha << 24) | (rgb & 0x00FFFFFF);
    }
}
