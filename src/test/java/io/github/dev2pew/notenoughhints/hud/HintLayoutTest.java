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
    void rejectsNegativeWidths() {
        assertThrows(IllegalArgumentException.class, () -> HintLayout.glyphWidth(-1));
    }
}
