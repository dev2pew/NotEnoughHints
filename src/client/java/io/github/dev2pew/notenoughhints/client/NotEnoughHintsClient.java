package io.github.dev2pew.notenoughhints.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;

import io.github.dev2pew.notenoughhints.NotEnoughHints;
import io.github.dev2pew.notenoughhints.client.config.HintPackManager;
import io.github.dev2pew.notenoughhints.client.config.NehConfigManager;
import io.github.dev2pew.notenoughhints.client.context.ClientContextCollector;
import io.github.dev2pew.notenoughhints.client.debug.DiagnosticsController;
import io.github.dev2pew.notenoughhints.client.debug.DiagnosticsRenderer;
import io.github.dev2pew.notenoughhints.client.hud.HintController;
import io.github.dev2pew.notenoughhints.client.hud.HintRenderer;
import io.github.dev2pew.notenoughhints.client.integration.IntegrationBootstrap;
import io.github.dev2pew.notenoughhints.client.integration.NestedInventoryAdapterRegistry;
import io.github.dev2pew.notenoughhints.client.keybind.KeyBindingCatalog;
import io.github.dev2pew.notenoughhints.config.HintPack;
import io.github.dev2pew.notenoughhints.context.ClientContext;
import io.github.dev2pew.notenoughhints.rule.RuleEvaluator;

public final class NotEnoughHintsClient implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger(NotEnoughHints.MOD_ID);

    @Override
    public void onInitializeClient() {
        NehConfigManager configManager = new NehConfigManager();
        configManager.load();

        HintPackManager hintPackManager = new HintPackManager();
        hintPackManager.load();
        HintPack hintPack = hintPackManager.current();

        KeyBindingCatalog keyBindingCatalog = new KeyBindingCatalog();
        NestedInventoryAdapterRegistry nestedInventoryAdapters =
                new NestedInventoryAdapterRegistry();
        IntegrationBootstrap.registerAvailableAdapters(nestedInventoryAdapters);

        RuleEvaluator ruleEvaluator = new RuleEvaluator();
        boolean collectInventory = ruleEvaluator.requiresInventory(hintPack.rules());
        boolean collectNestedInventory = ruleEvaluator.requiresNestedInventory(hintPack.rules());
        ClientContextCollector contextCollector =
                new ClientContextCollector(
                        keyBindingCatalog,
                        collectInventory,
                        collectNestedInventory,
                        nestedInventoryAdapters);

        HintController hintController =
                new HintController(
                        configManager,
                        keyBindingCatalog,
                        hintPack.groups(),
                        hintPack.rules());
        HintRenderer hintRenderer = new HintRenderer(hintController);

        DiagnosticsController diagnosticsController =
                new DiagnosticsController(configManager, nestedInventoryAdapters);
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

        LOGGER.info(
                "Not Enough Hints development build initialized; inventory indexing: {}, nested indexing: {}, adapters: {}",
                collectInventory,
                collectNestedInventory,
                nestedInventoryAdapters.activeAdapterIds());
    }
}
