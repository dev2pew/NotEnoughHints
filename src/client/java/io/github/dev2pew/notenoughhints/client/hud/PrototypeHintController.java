package io.github.dev2pew.notenoughhints.client.hud;

import java.util.concurrent.atomic.AtomicReference;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import io.github.dev2pew.notenoughhints.client.config.NehConfigManager;
import io.github.dev2pew.notenoughhints.config.NehConfig;

public final class PrototypeHintController {
    private final NehConfigManager configManager;
    private final AtomicReference<PrototypeHintRenderState> renderState =
            new AtomicReference<>(PrototypeHintRenderState.hidden());

    public PrototypeHintController(NehConfigManager configManager) {
        this.configManager = configManager;
    }

    public void tick(Minecraft client) {
        NehConfig config = configManager.current();
        if (!config.enabled() || client.player == null) {
            renderState.set(PrototypeHintRenderState.hidden());
            return;
        }

        String bindingText =
                config.showBindingLabels()
                        ? client.options.keyInventory.getTranslatedKeyMessage().getString()
                        : "";
        String description = Component.translatable("key.inventory").getString();

        renderState.set(
                new PrototypeHintRenderState(
                        true, bindingText, description, config.scale(), config.opacity()));
    }

    public PrototypeHintRenderState renderState() {
        return renderState.get();
    }
}
