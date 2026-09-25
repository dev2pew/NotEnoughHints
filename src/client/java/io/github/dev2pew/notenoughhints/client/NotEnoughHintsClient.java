package io.github.dev2pew.notenoughhints.client;

import java.util.List;

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
import io.github.dev2pew.notenoughhints.client.debug.ClientTickMetrics;
import io.github.dev2pew.notenoughhints.client.debug.DiagnosticsController;
import io.github.dev2pew.notenoughhints.client.debug.DiagnosticsRenderer;
import io.github.dev2pew.notenoughhints.client.hud.HintController;
import io.github.dev2pew.notenoughhints.client.hud.HintDiagnostics;
import io.github.dev2pew.notenoughhints.client.hud.HintGroupRenderState;
import io.github.dev2pew.notenoughhints.client.hud.HintRenderer;
import io.github.dev2pew.notenoughhints.client.integration.IntegrationBootstrap;
import io.github.dev2pew.notenoughhints.client.integration.NestedInventoryAdapterRegistry;
import io.github.dev2pew.notenoughhints.client.input.NehKeyBindings;
import io.github.dev2pew.notenoughhints.client.keybind.KeyBindingCatalog;
import io.github.dev2pew.notenoughhints.config.HintPack;
import io.github.dev2pew.notenoughhints.context.ClientContext;
import io.github.dev2pew.notenoughhints.rule.RuleEvaluator;

public final class NotEnoughHintsClient implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger(NotEnoughHints.MOD_ID);

    private static NehConfigManager configManager;
    private static HintPackManager hintPackManager;
    private static ClientContextCollector contextCollector;
    private static HintController hintController;
    private static RuleEvaluator ruleEvaluator;

    public static NehConfigManager configManager() {
        requireInitialized();
        return configManager;
    }

    public static HintPackManager hintPackManager() {
        requireInitialized();
        return hintPackManager;
    }

    public static HintDiagnostics hintDiagnostics() {
        requireInitialized();
        return hintController.diagnostics();
    }

    public static List<HintGroupRenderState> hintRenderStates() {
        requireInitialized();
        return hintController.renderStates();
    }

    public static List<String> reloadHintPacks() {
        requireInitialized();

        hintPackManager.load();
        HintPack hintPack = hintPackManager.current();
        hintController.replaceDefinitions(hintPack.groups(), hintPack.rules());
        refreshObservationRequirements();

        boolean collectInventory =
                ruleEvaluator.requiresInventory(
                        hintPack.rules(), configManager.current().disabledRuleIds());
        boolean collectNestedInventory =
                ruleEvaluator.requiresNestedInventory(
                        hintPack.rules(), configManager.current().disabledRuleIds());

        LOGGER.info(
                "Reloaded NEH hint packs; groups: {}, rules: {}, inventory indexing: {}, nested indexing: {}, issues: {}",
                hintPack.groups().size(),
                hintPack.rules().size(),
                collectInventory,
                collectNestedInventory,
                hintPackManager.issues().size());
        return hintPackManager.issues();
    }

    public static void refreshObservationRequirements() {
        requireInitialized();
        HintPack hintPack = hintPackManager.current();
        boolean collectInventory =
                ruleEvaluator.requiresInventory(
                        hintPack.rules(), configManager.current().disabledRuleIds());
        boolean collectNestedInventory =
                ruleEvaluator.requiresNestedInventory(
                        hintPack.rules(), configManager.current().disabledRuleIds());
        contextCollector.setInventoryRequirements(collectInventory, collectNestedInventory);
    }

    @Override
    public void onInitializeClient() {
        configManager = new NehConfigManager();
        configManager.load();

        hintPackManager = new HintPackManager();
        hintPackManager.load();
        HintPack hintPack = hintPackManager.current();

        NehKeyBindings.register();

        KeyBindingCatalog keyBindingCatalog = new KeyBindingCatalog();
        NestedInventoryAdapterRegistry nestedInventoryAdapters =
                new NestedInventoryAdapterRegistry();
        IntegrationBootstrap.registerAvailableAdapters(nestedInventoryAdapters);

        ruleEvaluator = new RuleEvaluator();
        boolean collectInventory =
                ruleEvaluator.requiresInventory(
                        hintPack.rules(), configManager.current().disabledRuleIds());
        boolean collectNestedInventory =
                ruleEvaluator.requiresNestedInventory(
                        hintPack.rules(), configManager.current().disabledRuleIds());
        contextCollector =
                new ClientContextCollector(
                        keyBindingCatalog,
                        collectInventory,
                        collectNestedInventory,
                        nestedInventoryAdapters);

        hintController =
                new HintController(
                        configManager,
                        keyBindingCatalog,
                        hintPack.groups(),
                        hintPack.rules());
        HintRenderer hintRenderer = new HintRenderer(hintController);

        DiagnosticsController diagnosticsController =
                new DiagnosticsController(
                        configManager, nestedInventoryAdapters, hintController);
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
                    NehKeyBindings.handle(client);

                    long adapterCallsBefore = nestedInventoryAdapters.adapterCallCount();
                    long contextStarted = System.nanoTime();
                    ClientContext context = contextCollector.collect(client);
                    long contextRefreshNanos = System.nanoTime() - contextStarted;
                    long nestedAdapterCalls =
                            nestedInventoryAdapters.adapterCallCount() - adapterCallsBefore;

                    hintController.update(context);
                    diagnosticsController.update(
                            context,
                            new ClientTickMetrics(
                                    contextRefreshNanos, nestedAdapterCalls));
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

    private static void requireInitialized() {
        if (configManager == null
                || hintPackManager == null
                || contextCollector == null
                || hintController == null
                || ruleEvaluator == null) {
            throw new IllegalStateException("NEH client has not initialized yet");
        }
    }
}
