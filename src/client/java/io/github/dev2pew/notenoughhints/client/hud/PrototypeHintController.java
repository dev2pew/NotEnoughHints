package io.github.dev2pew.notenoughhints.client.hud;

import java.util.concurrent.atomic.AtomicReference;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import io.github.dev2pew.notenoughhints.client.config.NehConfigManager;
import io.github.dev2pew.notenoughhints.client.keybind.KeyBindingCatalog;
import io.github.dev2pew.notenoughhints.client.keybind.KeyBindingDescriptor;
import io.github.dev2pew.notenoughhints.config.NehConfig;

public final class PrototypeHintController {
    private static final String PROTOTYPE_BINDING_ID = "key.inventory";

    private final NehConfigManager configManager;
    private final KeyBindingCatalog keyBindingCatalog;
    private final AtomicReference<PrototypeHintRenderState> renderState =
            new AtomicReference<>(PrototypeHintRenderState.hidden());

    public PrototypeHintController(
            NehConfigManager configManager, KeyBindingCatalog keyBindingCatalog) {
        this.configManager = configManager;
        this.keyBindingCatalog = keyBindingCatalog;
    }

    public void tick(Minecraft client) {
        NehConfig config = configManager.current();
        if (!config.enabled() || client.player == null) {
            renderState.set(PrototypeHintRenderState.hidden());
            return;
        }

        KeyBindingDescriptor binding = keyBindingCatalog.find(PROTOTYPE_BINDING_ID).orElse(null);
        if (binding == null) {
            renderState.set(
                    new PrototypeHintRenderState(
                            true,
                            "",
                            Component.translatable("text.notenoughhints.unnamed_action").getString(),
                            config.scale(),
                            config.opacity()));
            return;
        }

        String bindingText = config.showBindingLabels() ? binding.boundKeyText() : "";
        renderState.set(
                new PrototypeHintRenderState(
                        true,
                        bindingText,
                        binding.descriptionText(),
                        config.scale(),
                        config.opacity()));
    }

    public PrototypeHintRenderState renderState() {
        return renderState.get();
    }
}
