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
}
