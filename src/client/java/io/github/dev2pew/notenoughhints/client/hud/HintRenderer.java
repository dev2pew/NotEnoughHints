package io.github.dev2pew.notenoughhints.client.hud;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import io.github.dev2pew.notenoughhints.hud.HintFlow;
import io.github.dev2pew.notenoughhints.hud.HintLayout;

public final class HintRenderer {
    private static final int GLYPH_BORDER_RGB = 0xD8D8D8;
    private static final int GLYPH_FILL_RGB = 0x4A4A4A;
    private static final int DESCRIPTION_FILL_RGB = 0x202020;
    private static final int TEXT_RGB = 0xFFFFFF;

    private final HintController controller;

    public HintRenderer(HintController controller) {
        this.controller = controller;
    }

    public void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
        Minecraft client = Minecraft.getInstance();
        if (client.options.hideGui || client.player == null) {
            return;
        }

        for (HintGroupRenderState state : controller.renderStates()) {
            renderGroup(graphics, client, state);
        }
    }

    private static void renderGroup(
            GuiGraphics graphics, Minecraft client, HintGroupRenderState state) {
        if (!state.visible()) {
            return;
        }

        List<EntryLayout> entries = layoutEntries(client, state);
        if (entries.isEmpty()) {
            return;
        }

        int contentWidth =
                state.flow() == HintFlow.HORIZONTAL
                        ? entries.stream().mapToInt(EntryLayout::width).sum()
                                + state.entryGap() * (entries.size() - 1)
                        : entries.stream().mapToInt(EntryLayout::width).max().orElse(0);
        int contentHeight =
                state.flow() == HintFlow.VERTICAL
                        ? entries.size() * HintLayout.GLYPH_HEIGHT
                                + state.entryGap() * (entries.size() - 1)
                        : HintLayout.GLYPH_HEIGHT;

        float scale = state.scale();
        int viewportWidth =
                Math.max(1, (int) Math.floor(client.getWindow().getGuiScaledWidth() / scale));
        int viewportHeight =
                Math.max(1, (int) Math.floor(client.getWindow().getGuiScaledHeight() / scale));

        int originX =
                HintLayout.anchoredX(
                        state.anchor(),
                        viewportWidth,
                        contentWidth,
                        HintLayout.SCREEN_MARGIN,
                        state.offsetX());
        int originY =
                HintLayout.anchoredY(
                        state.anchor(),
                        viewportHeight,
                        contentHeight,
                        HintLayout.SCREEN_MARGIN,
                        state.offsetY());

        graphics.pose().pushMatrix();
        graphics.pose().scale(scale, scale);

        int x = originX;
        int y = originY;
        for (EntryLayout entry : entries) {
            drawEntry(graphics, client, entry, x, y, state.opacity());

            if (state.flow() == HintFlow.HORIZONTAL) {
                x += entry.width() + state.entryGap();
            } else {
                y += HintLayout.GLYPH_HEIGHT + state.entryGap();
            }
        }

        graphics.pose().popMatrix();
    }

    private static List<EntryLayout> layoutEntries(
            Minecraft client, HintGroupRenderState state) {
        List<EntryLayout> entries = new ArrayList<>(state.hints().size());
        for (ResolvedHint hint : state.hints()) {
            Component binding = Component.literal(hint.bindingText());
            Component description = Component.literal(hint.description());
            int bindingWidth = hint.bindingText().isEmpty() ? 0 : client.font.width(binding);
            int glyphWidth = HintLayout.glyphWidth(bindingWidth);
            int descriptionWidth = client.font.width(description);
            int descriptionBoxWidth =
                    descriptionWidth + HintLayout.DESCRIPTION_HORIZONTAL_PADDING * 2;
            entries.add(
                    new EntryLayout(
                            binding,
                            description,
                            bindingWidth,
                            glyphWidth,
                            descriptionBoxWidth,
                            glyphWidth + HintLayout.ENTRY_GAP + descriptionBoxWidth));
        }
        return entries;
    }

    private static void drawEntry(
            GuiGraphics graphics,
            Minecraft client,
            EntryLayout entry,
            int x,
            int y,
            float opacity) {
        int descriptionX = x + entry.glyphWidth() + HintLayout.ENTRY_GAP;

        graphics.fill(
                x,
                y,
                x + entry.glyphWidth(),
                y + HintLayout.GLYPH_HEIGHT,
                argb(GLYPH_BORDER_RGB, opacity));
        graphics.fill(
                x + 1,
                y + 1,
                x + entry.glyphWidth() - 1,
                y + HintLayout.GLYPH_HEIGHT - 1,
                argb(GLYPH_FILL_RGB, opacity));

        if (entry.bindingWidth() > 0) {
            int bindingX =
                    HintLayout.centeredTextX(x, entry.glyphWidth(), entry.bindingWidth());
            graphics.drawString(
                    client.font, entry.binding(), bindingX, y + 3, argb(TEXT_RGB, opacity));
        }

        graphics.fill(
                descriptionX,
                y,
                descriptionX + entry.descriptionBoxWidth(),
                y + HintLayout.GLYPH_HEIGHT,
                argb(DESCRIPTION_FILL_RGB, opacity * 0.75F));
        graphics.drawString(
                client.font,
                entry.description(),
                descriptionX + HintLayout.DESCRIPTION_HORIZONTAL_PADDING,
                y + 3,
                argb(TEXT_RGB, opacity));
    }

    private static int argb(int rgb, float opacity) {
        int alpha = Math.max(0, Math.min(255, Math.round(opacity * 255.0F)));
        return (alpha << 24) | (rgb & 0x00FFFFFF);
    }

    private record EntryLayout(
            Component binding,
            Component description,
            int bindingWidth,
            int glyphWidth,
            int descriptionBoxWidth,
            int width) {}
}
