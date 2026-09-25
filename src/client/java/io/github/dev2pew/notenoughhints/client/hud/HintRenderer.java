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

        float scale = state.scale();
        int viewportWidth =
                Math.max(1, (int) Math.floor(client.getWindow().getGuiScaledWidth() / scale));
        int viewportHeight =
                Math.max(1, (int) Math.floor(client.getWindow().getGuiScaledHeight() / scale));
        int safeWidth = Math.max(1, viewportWidth - HintLayout.SCREEN_MARGIN * 2);
        int wrapWidth =
                state.maxWidth() > 0
                        ? Math.min(state.maxWidth(), safeWidth)
                        : safeWidth;

        List<RowLayout> rows = layoutRows(entries, state, wrapWidth);
        int contentWidth = rows.stream().mapToInt(RowLayout::width).max().orElse(0);
        int rowGap =
                state.flow() == HintFlow.VERTICAL ? state.entryGap() : state.lineGap();
        int contentHeight =
                rows.size() * HintLayout.GLYPH_HEIGHT
                        + Math.max(0, rows.size() - 1) * rowGap;

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
        originX =
                HintLayout.clampToSafeOrigin(
                        originX, viewportWidth, contentWidth, HintLayout.SCREEN_MARGIN);
        originY =
                HintLayout.clampToSafeOrigin(
                        originY, viewportHeight, contentHeight, HintLayout.SCREEN_MARGIN);

        graphics.pose().pushMatrix();
        graphics.pose().scale(scale, scale);

        int y = originY;
        for (RowLayout row : rows) {
            int x = originX;
            for (EntryLayout entry : row.entries()) {
                drawEntry(graphics, client, entry, x, y, state.opacity());
                x += entry.width() + state.entryGap();
            }
            y += HintLayout.GLYPH_HEIGHT + rowGap;
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

    private static List<RowLayout> layoutRows(
            List<EntryLayout> entries, HintGroupRenderState state, int wrapWidth) {
        if (state.flow() == HintFlow.VERTICAL) {
            return entries.stream()
                    .map(entry -> new RowLayout(List.of(entry), entry.width()))
                    .toList();
        }

        List<RowLayout> rows = new ArrayList<>();
        List<EntryLayout> currentEntries = new ArrayList<>();
        int currentWidth = 0;

        for (EntryLayout entry : entries) {
            int nextWidth =
                    currentEntries.isEmpty()
                            ? entry.width()
                            : currentWidth + state.entryGap() + entry.width();
            if (!currentEntries.isEmpty() && nextWidth > wrapWidth) {
                rows.add(new RowLayout(List.copyOf(currentEntries), currentWidth));
                currentEntries.clear();
                currentWidth = 0;
            }

            if (!currentEntries.isEmpty()) {
                currentWidth += state.entryGap();
            }
            currentEntries.add(entry);
            currentWidth += entry.width();
        }

        if (!currentEntries.isEmpty()) {
            rows.add(new RowLayout(List.copyOf(currentEntries), currentWidth));
        }
        return List.copyOf(rows);
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

    private record RowLayout(List<EntryLayout> entries, int width) {}
}
