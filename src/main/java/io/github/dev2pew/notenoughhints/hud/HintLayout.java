package io.github.dev2pew.notenoughhints.hud;

public final class HintLayout {
    public static final int GLYPH_HEIGHT = 13;
    public static final int GLYPH_HORIZONTAL_PADDING = 3;
    public static final int DESCRIPTION_HORIZONTAL_PADDING = 4;
    public static final int ENTRY_GAP = 5;
    public static final int SCREEN_MARGIN = 5;

    private HintLayout() {}

    public static int glyphWidth(int textWidth) {
        if (textWidth < 0) {
            throw new IllegalArgumentException("textWidth must not be negative");
        }

        return Math.max(GLYPH_HEIGHT, textWidth + GLYPH_HORIZONTAL_PADDING * 2);
    }

    public static int centeredTextX(int x, int width, int textWidth) {
        if (width < 0 || textWidth < 0) {
            throw new IllegalArgumentException("widths must not be negative");
        }

        return x + Math.max(0, (width - textWidth) / 2);
    }

    public static int anchoredX(
            HudAnchor anchor, int viewportWidth, int contentWidth, int margin, int offsetX) {
        validateExtent(viewportWidth, "viewportWidth");
        validateExtent(contentWidth, "contentWidth");
        validateExtent(margin, "margin");

        int base =
                switch (anchor) {
                    case TOP_LEFT, CENTER_LEFT, BOTTOM_LEFT -> margin;
                    case TOP_CENTER, CENTER, BOTTOM_CENTER -> (viewportWidth - contentWidth) / 2;
                    case TOP_RIGHT, CENTER_RIGHT, BOTTOM_RIGHT ->
                            viewportWidth - margin - contentWidth;
                };

        return base + offsetX;
    }

    public static int anchoredY(
            HudAnchor anchor, int viewportHeight, int contentHeight, int margin, int offsetY) {
        validateExtent(viewportHeight, "viewportHeight");
        validateExtent(contentHeight, "contentHeight");
        validateExtent(margin, "margin");

        int base =
                switch (anchor) {
                    case TOP_LEFT, TOP_CENTER, TOP_RIGHT -> margin;
                    case CENTER_LEFT, CENTER, CENTER_RIGHT -> (viewportHeight - contentHeight) / 2;
                    case BOTTOM_LEFT, BOTTOM_CENTER, BOTTOM_RIGHT ->
                            viewportHeight - margin - contentHeight;
                };

        return base + offsetY;
    }

    public static int clampToSafeOrigin(
            int origin, int viewportExtent, int contentExtent, int margin) {
        validateExtent(viewportExtent, "viewportExtent");
        validateExtent(contentExtent, "contentExtent");
        validateExtent(margin, "margin");

        int minimum = margin;
        int maximum = viewportExtent - margin - contentExtent;
        if (maximum < minimum) {
            return minimum;
        }
        return Math.max(minimum, Math.min(maximum, origin));
    }

    private static void validateExtent(int value, String name) {
        if (value < 0) {
            throw new IllegalArgumentException(name + " must not be negative");
        }
    }
}
