package io.github.dev2pew.notenoughhints.hud;

import java.util.Objects;

public sealed interface HintDescription {
    record Default() implements HintDescription {}

    record Literal(String text) implements HintDescription {
        public Literal {
            Objects.requireNonNull(text, "text");
        }
    }

    record Translation(String key) implements HintDescription {
        public Translation {
            Objects.requireNonNull(key, "key");
            if (key.isBlank()) {
                throw new IllegalArgumentException("Translation key must not be blank");
            }
        }
    }
}
