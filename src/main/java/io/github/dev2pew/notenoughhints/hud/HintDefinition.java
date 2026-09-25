package io.github.dev2pew.notenoughhints.hud;

import java.util.Objects;

public record HintDefinition(
        String id, String bindingId, String descriptionTranslationKey, boolean showBinding) {
    public HintDefinition {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(bindingId, "bindingId");
        Objects.requireNonNull(descriptionTranslationKey, "descriptionTranslationKey");

        if (id.isBlank()) {
            throw new IllegalArgumentException("Hint id must not be blank");
        }
        if (bindingId.isBlank()) {
            throw new IllegalArgumentException("Binding id must not be blank");
        }
    }
}
