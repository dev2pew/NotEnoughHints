package io.github.dev2pew.notenoughhints.client.keybind;

import net.minecraft.client.KeyMapping;

public record KeyBindingDescriptor(
        String id, String category, KeyMapping mapping) {
    public String boundKeyText() {
        return mapping.getTranslatedKeyMessage().getString();
    }

    public String descriptionText() {
        return net.minecraft.network.chat.Component.translatable(id).getString();
    }

    public boolean unbound() {
        return mapping.isUnbound();
    }
}
