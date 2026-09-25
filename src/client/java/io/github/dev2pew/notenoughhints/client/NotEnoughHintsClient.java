package io.github.dev2pew.notenoughhints.client;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;

import io.github.dev2pew.notenoughhints.NotEnoughHints;
import io.github.dev2pew.notenoughhints.client.config.NehConfigManager;
import io.github.dev2pew.notenoughhints.client.context.ClientContextCollector;
import io.github.dev2pew.notenoughhints.client.debug.DiagnosticsController;
import io.github.dev2pew.notenoughhints.client.debug.DiagnosticsRenderer;
import io.github.dev2pew.notenoughhints.client.hud.HintController;
import io.github.dev2pew.notenoughhints.client.hud.HintRenderer;
import io.github.dev2pew.notenoughhints.client.keybind.KeyBindingCatalog;
import io.github.dev2pew.notenoughhints.context.ClientContext;
import io.github.dev2pew.notenoughhints.hud.HintDefinition;
import io.github.dev2pew.notenoughhints.hud.HintFlow;
import io.github.dev2pew.notenoughhints.hud.HintGroupDefinition;
import io.github.dev2pew.notenoughhints.hud.HudAnchor;

public final class NotEnoughHintsClient implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger(NotEnoughHints.MOD_ID);

    @Override
    public void onInitializeClient() {
        NehConfigManager configManager = new NehConfigManager();
        configManager.load();

        KeyBindingCatalog keyBindingCatalog = new KeyBindingCatalog();
        ClientContextCollector contextCollector = new ClientContextCollector(keyBindingCatalog);

        HintGroupDefinition developmentGroup =
                new HintGroupDefinition(
                        "development",
                        HudAnchor.BOTTOM_LEFT,
                        0,
                        0,
                        HintFlow.HORIZONTAL,
                        6,
                        List.of(
                                new HintDefinition(
                                        "inventory", "key.inventory", "", true, true)));

        HintController hintController =
                new HintController(
                        configManager, keyBindingCatalog, List.of(developmentGroup), List.of());
        HintRenderer hintRenderer = new HintRenderer(hintController);

        DiagnosticsController diagnosticsController = new DiagnosticsController(configManager);
        DiagnosticsRenderer diagnosticsRenderer = new DiagnosticsRenderer(diagnosticsController);

        ClientTickEvents.START_CLIENT_TICK.register(
                client -> {
                    if (keyBindingCatalog.size() == 0) {
                        keyBindingCatalog.rebuild(client);
                        LOGGER.info("Discovered {} key mappings", keyBindingCatalog.size());
                    }
                });
        ClientTickEvents.END_CLIENT_TICK.register(
                client -> {
                    ClientContext context = contextCollector.collect(client);
                    hintController.update(context);
                    diagnosticsController.update(context);
                });

        HudElementRegistry.attachElementBefore(
                VanillaHudElements.CHAT, NotEnoughHints.id("hints"), hintRenderer::render);
        HudElementRegistry.attachElementBefore(
                VanillaHudElements.CHAT,
                NotEnoughHints.id("diagnostics"),
                diagnosticsRenderer::render);

        LOGGER.info("Not Enough Hints development prototype initialized");
    }
}
