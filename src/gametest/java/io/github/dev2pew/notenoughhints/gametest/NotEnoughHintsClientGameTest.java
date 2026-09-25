package io.github.dev2pew.notenoughhints.gametest;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;

import net.minecraft.client.KeyMapping;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.fabricmc.loader.api.FabricLoader;

import io.github.dev2pew.notenoughhints.client.NotEnoughHintsClient;
import io.github.dev2pew.notenoughhints.client.hud.HintGroupRenderState;
import io.github.dev2pew.notenoughhints.client.hud.ResolvedHint;
import io.github.dev2pew.notenoughhints.config.GroupOverride;
import io.github.dev2pew.notenoughhints.config.NehConfig;
import io.github.dev2pew.notenoughhints.hud.HudAnchor;

@SuppressWarnings("UnstableApiUsage")
public final class NotEnoughHintsClientGameTest implements FabricClientGameTest {
    private static final String INVENTORY_DESCRIPTION = "Inventory probe";
    private static final String CONTEXT_DESCRIPTION = "Context probe";
    private static final String TEST_PACK =
            """
            {
              "schema_version": 1,
              "groups": [
                {
                  "id": "gametest-context",
                  "anchor": "bottom_right",
                  "offset_x": 0,
                  "offset_y": 0,
                  "flow": "horizontal",
                  "entry_gap": 6,
                  "hints": [
                    {
                      "id": "gametest-inventory-hint",
                      "binding": "key.inventory",
                      "description": {"literal": "Inventory probe"},
                      "show_binding": true,
                      "visible_by_default": true
                    },
                    {
                      "id": "gametest-context-hint",
                      "binding": "key.drop",
                      "description": {"literal": "Context probe"},
                      "show_binding": true,
                      "visible_by_default": false
                    }
                  ]
                }
              ],
              "rules": [
                {
                  "id": "gametest-nether",
                  "priority": 10,
                  "when": {"type": "dimension", "id": "minecraft:the_nether"},
                  "actions": [{"type": "show_hint", "hint": "gametest-context-hint"}]
                },
                {
                  "id": "gametest-offhand",
                  "priority": 20,
                  "when": {"type": "off_hand_item", "item": "minecraft:shield"},
                  "actions": [{"type": "show_hint", "hint": "gametest-context-hint"}]
                },
                {
                  "id": "gametest-head",
                  "priority": 30,
                  "when": {
                    "type": "equipment_slot_item",
                    "slot": "head",
                    "item": "minecraft:iron_helmet"
                  },
                  "actions": [{"type": "show_hint", "hint": "gametest-context-hint"}]
                },
                {
                  "id": "gametest-chest",
                  "priority": 40,
                  "when": {
                    "type": "equipment_slot_item",
                    "slot": "chest",
                    "item": "minecraft:iron_chestplate"
                  },
                  "actions": [{"type": "show_hint", "hint": "gametest-context-hint"}]
                },
                {
                  "id": "gametest-legs",
                  "priority": 50,
                  "when": {
                    "type": "equipment_slot_item",
                    "slot": "legs",
                    "item": "minecraft:iron_leggings"
                  },
                  "actions": [{"type": "show_hint", "hint": "gametest-context-hint"}]
                },
                {
                  "id": "gametest-feet",
                  "priority": 60,
                  "when": {
                    "type": "equipment_slot_item",
                    "slot": "feet",
                    "item": "minecraft:iron_boots"
                  },
                  "actions": [{"type": "show_hint", "hint": "gametest-context-hint"}]
                },
                {
                  "id": "gametest-inventory-rule",
                  "priority": 70,
                  "when": {
                    "type": "inventory_contains",
                    "scope": "player_inventory",
                    "item": "minecraft:diamond",
                    "min_count": 1
                  },
                  "actions": [{"type": "show_hint", "hint": "gametest-context-hint"}]
                }
              ]
            }
            """;

    @Override
    public void runTest(ClientGameTestContext context) {
        FabricLoader loader = FabricLoader.getInstance();
        requireLoaded(loader, "notenoughhints");
        requireLoaded(loader, "yet_another_config_lib_v3");
        requireAbsent(loader, "modmenu");
        requireAbsent(loader, "travelersbackpack");

        Path testPack = installTestPack(loader);
        NehConfig originalConfig = NotEnoughHintsClient.configManager().current();

        try {
            List<String> issues = NotEnoughHintsClient.reloadHintPacks();
            if (!issues.isEmpty()) {
                throw new AssertionError("Hint-pack reload reported issues: " + issues);
            }

            try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
                singleplayer.getClientWorld().waitForChunksRender();
                context.waitFor(client -> client.level != null && client.player != null);
                context.waitFor(client -> findHint(INVENTORY_DESCRIPTION) != null);

                verifyLiveKeyRebinding(context);
                verifyGroupPlacementOverride(context, originalConfig);
                verifyItemContextRules(singleplayer, context);
                verifyDimensionRule(singleplayer, context);

                context.takeScreenshot("neh-context-state-smoke");
            }

            context.waitFor(client -> client.level == null);
        } finally {
            restoreConfig(originalConfig);
            deleteTestPack(testPack);
            NotEnoughHintsClient.reloadHintPacks();
        }
    }

    private static void verifyLiveKeyRebinding(ClientGameTestContext context) {
        ResolvedHint before = requireHint(INVENTORY_DESCRIPTION);
        String originalText = before.bindingText();

        KeyMapping inventory =
                context.computeOnClient(
                        client ->
                                Arrays.stream(client.options.keyMappings)
                                        .filter(mapping -> mapping.getName().equals("key.inventory"))
                                        .findFirst()
                                        .orElseThrow(
                                                () ->
                                                        new AssertionError(
                                                                "key.inventory was not discovered")));

        context.runOnClient(
                client -> {
                    inventory.setKey(
                            InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_B));
                    KeyMapping.resetMapping();
                });

        context.waitFor(
                client -> {
                    ResolvedHint hint = findHint(INVENTORY_DESCRIPTION);
                    return hint != null && !hint.bindingText().equals(originalText);
                });

        context.runOnClient(
                client -> {
                    inventory.setKey(inventory.getDefaultKey());
                    KeyMapping.resetMapping();
                });

        context.waitFor(
                client -> {
                    ResolvedHint hint = findHint(INVENTORY_DESCRIPTION);
                    return hint != null && hint.bindingText().equals(originalText);
                });
    }

    private static void verifyGroupPlacementOverride(
            ClientGameTestContext context, NehConfig originalConfig) {
        Map<String, GroupOverride> groupOverrides =
                new HashMap<>(originalConfig.groupOverrides());
        groupOverrides.put(
                "gametest-context",
                new GroupOverride(HudAnchor.TOP_RIGHT, 12, 18, true));

        NehConfig adjusted =
                new NehConfig(
                        NehConfig.CURRENT_SCHEMA_VERSION,
                        originalConfig.enabled(),
                        originalConfig.debug(),
                        1.25F,
                        originalConfig.opacity(),
                        originalConfig.showBindingLabels(),
                        originalConfig.disabledRuleIds(),
                        groupOverrides);

        if (!NotEnoughHintsClient.configManager().save(adjusted)) {
            throw new AssertionError("Failed to save game-test config");
        }

        context.waitFor(
                client -> {
                    HintGroupRenderState state = findState(INVENTORY_DESCRIPTION);
                    return state != null
                            && state.anchor() == HudAnchor.TOP_RIGHT
                            && state.offsetX() == 12
                            && state.offsetY() == 18
                            && Float.compare(state.scale(), 1.25F) == 0;
                });
    }

    private static void verifyItemContextRules(
            TestSingleplayerContext singleplayer, ClientGameTestContext context) {
        singleplayer.getServer().runCommand("gamemode creative @p");

        verifyRuleForCommand(
                singleplayer,
                context,
                "gametest-offhand",
                "item replace entity @p weapon.offhand with minecraft:shield",
                "item replace entity @p weapon.offhand with minecraft:air");
        verifyRuleForCommand(
                singleplayer,
                context,
                "gametest-head",
                "item replace entity @p armor.head with minecraft:iron_helmet",
                "item replace entity @p armor.head with minecraft:air");
        verifyRuleForCommand(
                singleplayer,
                context,
                "gametest-chest",
                "item replace entity @p armor.chest with minecraft:iron_chestplate",
                "item replace entity @p armor.chest with minecraft:air");
        verifyRuleForCommand(
                singleplayer,
                context,
                "gametest-legs",
                "item replace entity @p armor.legs with minecraft:iron_leggings",
                "item replace entity @p armor.legs with minecraft:air");
        verifyRuleForCommand(
                singleplayer,
                context,
                "gametest-feet",
                "item replace entity @p armor.feet with minecraft:iron_boots",
                "item replace entity @p armor.feet with minecraft:air");
        verifyRuleForCommand(
                singleplayer,
                context,
                "gametest-inventory-rule",
                "give @p minecraft:diamond 1",
                "clear @p minecraft:diamond");

        context.waitFor(client -> findHint(CONTEXT_DESCRIPTION) == null);
    }

    private static void verifyDimensionRule(
            TestSingleplayerContext singleplayer, ClientGameTestContext context) {
        singleplayer
                .getServer()
                .runCommand(
                        "execute as @p in minecraft:the_nether run tp @s 0 80 0");
        context.waitFor(
                client ->
                        client.level != null
                                && client.level
                                        .dimension()
                                        .location()
                                        .toString()
                                        .equals("minecraft:the_nether"));
        waitForRule(context, "gametest-nether", true);

        singleplayer
                .getServer()
                .runCommand(
                        "execute as @p in minecraft:overworld run tp @s 0 80 0");
        context.waitFor(
                client ->
                        client.level != null
                                && client.level
                                        .dimension()
                                        .location()
                                        .toString()
                                        .equals("minecraft:overworld"));
        waitForRule(context, "gametest-nether", false);
    }

    private static void verifyRuleForCommand(
            TestSingleplayerContext singleplayer,
            ClientGameTestContext context,
            String ruleId,
            String applyCommand,
            String clearCommand) {
        singleplayer.getServer().runCommand(applyCommand);
        waitForRule(context, ruleId, true);

        singleplayer.getServer().runCommand(clearCommand);
        waitForRule(context, ruleId, false);
    }

    private static void waitForRule(
            ClientGameTestContext context, String ruleId, boolean active) {
        context.waitFor(
                client ->
                        NotEnoughHintsClient.hintDiagnostics()
                                        .activeRuleIds()
                                        .contains(ruleId)
                                == active);
    }

    private static HintGroupRenderState findState(String description) {
        return NotEnoughHintsClient.hintRenderStates().stream()
                .filter(
                        state ->
                                state.hints().stream()
                                        .anyMatch(hint -> hint.description().equals(description)))
                .findFirst()
                .orElse(null);
    }

    private static ResolvedHint findHint(String description) {
        HintGroupRenderState state = findState(description);
        if (state == null) {
            return null;
        }

        return state.hints().stream()
                .filter(hint -> hint.description().equals(description))
                .findFirst()
                .orElse(null);
    }

    private static ResolvedHint requireHint(String description) {
        ResolvedHint hint = findHint(description);
        if (hint == null) {
            throw new AssertionError("Expected visible hint: " + description);
        }
        return hint;
    }

    private static Path installTestPack(FabricLoader loader) {
        Path file =
                loader.getConfigDir()
                        .resolve("not-enough-hints")
                        .resolve("hints")
                        .resolve("zz-gametest.json");
        try {
            Files.createDirectories(file.getParent());
            Files.writeString(file, TEST_PACK);
            return file;
        } catch (IOException exception) {
            throw new UncheckedIOException("Failed to create client game-test hint pack", exception);
        }
    }

    private static void deleteTestPack(Path file) {
        try {
            Files.deleteIfExists(file);
        } catch (IOException exception) {
            throw new UncheckedIOException("Failed to remove client game-test hint pack", exception);
        }
    }

    private static void restoreConfig(NehConfig config) {
        if (!NotEnoughHintsClient.configManager().save(config)) {
            throw new AssertionError("Failed to restore NEH config after client game test");
        }
        NotEnoughHintsClient.refreshObservationRequirements();
    }

    private static void requireLoaded(FabricLoader loader, String modId) {
        if (!loader.isModLoaded(modId)) {
            throw new AssertionError("Expected loaded mod: " + modId);
        }
    }

    private static void requireAbsent(FabricLoader loader, String modId) {
        if (loader.isModLoaded(modId)) {
            throw new AssertionError("Expected optional mod to be absent: " + modId);
        }
    }
}
