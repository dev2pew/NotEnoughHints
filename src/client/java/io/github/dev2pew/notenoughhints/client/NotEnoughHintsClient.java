package io.github.dev2pew.notenoughhints.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;

import io.github.dev2pew.notenoughhints.NotEnoughHints;
import io.github.dev2pew.notenoughhints.client.config.NehConfigManager;
import io.github.dev2pew.notenoughhints.client.hud.PrototypeHintController;
import io.github.dev2pew.notenoughhints.client.hud.PrototypeHintRenderer;
import io.github.dev2pew.notenoughhints.client.keybind.KeyBindingCatalog;

public final class NotEnoughHintsClient implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger(NotEnoughHints.MOD_ID);

    @Override
    public void onInitializeClient() {
        NehConfigManager configManager = new NehConfigManager();
        configManager.load();

        KeyBindingCatalog keyBindingCatalog = new KeyBindingCatalog();

        PrototypeHintController prototypeController =
                new PrototypeHintController(configManager, keyBindingCatalog);
        PrototypeHintRenderer prototypeRenderer = new PrototypeHintRenderer(prototypeController);

        ClientTickEvents.START_CLIENT_TICK.register(
                client -> {
                    if (keyBindingCatalog.size() == 0) {
                        keyBindingCatalog.rebuild(client);
                        LOGGER.info("Discovered {} key mappings", keyBindingCatalog.size());
                    }
                });
        ClientTickEvents.END_CLIENT_TICK.register(prototypeController::tick);

        HudElementRegistry.attachElementBefore(
                VanillaHudElements.CHAT, NotEnoughHints.id("hints"), prototypeRenderer::render);

        LOGGER.info("Not Enough Hints development prototype initialized");
    }
}
