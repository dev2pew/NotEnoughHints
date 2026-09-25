package io.github.dev2pew.notenoughhints.hud;

import java.util.Objects;

public record HintDefinition(
        String id,
        String bindingId,
        HintDescription description,
        boolean showBinding,
        boolean visibleByDefault) {
    public HintDefinition {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(bindingId, "bindingId");
        Objects.requireNonNull(description, "description");

        if (id.isBlank()) {
            throw new IllegalArgumentException("Hint id must not be blank");
        }
        if (bindingId.isBlank()) {
            throw new IllegalArgumentException("Binding id must not be blank");
        }
    }
}
