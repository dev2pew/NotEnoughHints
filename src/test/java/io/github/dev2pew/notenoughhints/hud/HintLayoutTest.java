package io.github.dev2pew.notenoughhints.hud;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class HintLayoutTest {
    @Test
    void glyphNeverShrinksBelowBaseHeight() {
        assertEquals(HintLayout.GLYPH_HEIGHT, HintLayout.glyphWidth(1));
    }

    @Test
    void glyphExpandsForLongLabels() {
        int textWidth = 40;
        assertEquals(
                textWidth + HintLayout.GLYPH_HORIZONTAL_PADDING * 2,
                HintLayout.glyphWidth(textWidth));
    }

    @Test
    void centersTextInsideGlyph() {
        assertEquals(12, HintLayout.centeredTextX(5, 20, 6));
    }

    @Test
    void anchorsContentAtAllHorizontalPositions() {
        assertEquals(5, HintLayout.anchoredX(HudAnchor.TOP_LEFT, 200, 40, 5, 0));
        assertEquals(80, HintLayout.anchoredX(HudAnchor.TOP_CENTER, 200, 40, 5, 0));
        assertEquals(155, HintLayout.anchoredX(HudAnchor.TOP_RIGHT, 200, 40, 5, 0));
    }

    @Test
    void anchorsContentAtAllVerticalPositions() {
        assertEquals(5, HintLayout.anchoredY(HudAnchor.TOP_LEFT, 120, 20, 5, 0));
        assertEquals(50, HintLayout.anchoredY(HudAnchor.CENTER, 120, 20, 5, 0));
        assertEquals(95, HintLayout.anchoredY(HudAnchor.BOTTOM_RIGHT, 120, 20, 5, 0));
    }

    @Test
    void appliesOffsetsAfterAnchoring() {
        assertEquals(12, HintLayout.anchoredX(HudAnchor.TOP_LEFT, 200, 40, 5, 7));
        assertEquals(92, HintLayout.anchoredY(HudAnchor.BOTTOM_LEFT, 120, 20, 5, -3));
    }

    @Test
    void rejectsNegativeWidths() {
        assertThrows(IllegalArgumentException.class, () -> HintLayout.glyphWidth(-1));
        assertThrows(
                IllegalArgumentException.class,
                () -> HintLayout.anchoredX(HudAnchor.TOP_LEFT, -1, 10, 0, 0));
    }
}
