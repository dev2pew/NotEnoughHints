package io.github.dev2pew.notenoughhints.rule;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import io.github.dev2pew.notenoughhints.context.ClientContext;
import io.github.dev2pew.notenoughhints.context.EquipmentSlotKey;
import io.github.dev2pew.notenoughhints.context.InventoryScope;
import io.github.dev2pew.notenoughhints.context.InventorySnapshot;

class RuleEvaluatorTest {
    private final ClientContext context =
            new ClientContext(
                    true,
                    "minecraft:the_nether",
                    "example.InventoryScreen",
                    "minecraft:generic_9x3",
                    "minecraft:blaze_rod",
                    "minecraft:shield",
                    Map.of(
                            EquipmentSlotKey.MAIN_HAND, "minecraft:blaze_rod",
                            EquipmentSlotKey.OFF_HAND, "minecraft:shield",
                            EquipmentSlotKey.HEAD, "minecraft:diamond_helmet",
                            EquipmentSlotKey.CHEST, "minecraft:diamond_chestplate",
                            EquipmentSlotKey.LEGS, "minecraft:diamond_leggings",
                            EquipmentSlotKey.FEET, "minecraft:diamond_boots"),
                    new InventorySnapshot(
                            Map.of(
                                    InventoryScope.HOTBAR,
                                    Map.of("minecraft:blaze_rod", 3),
                                    InventoryScope.PLAYER_INVENTORY,
                                    Map.of("minecraft:blaze_rod", 3, "minecraft:shield", 1))),
                    Set.of("fabricloader", "notenoughhints"),
                    Set.of("key.inventory", "key.jump"));

    @Test
    void combinesEquipmentDimensionAndOffhandConditions() {
        Condition condition =
                new Condition.All(
                        List.of(
                                new Condition.Dimension("minecraft:the_nether"),
                                new Condition.OffHandItem("minecraft:shield"),
                                new Condition.EquipmentSlotItem(
                                        EquipmentSlotKey.HEAD, "minecraft:diamond_helmet")));

        assertTrue(condition.test(context));
    }

    @Test
    void carriedArmorDoesNotSubstituteForEquippedSlot() {
        Condition condition =
                new Condition.EquipmentSlotItem(
                        EquipmentSlotKey.HEAD, "minecraft:diamond_chestplate");

        assertFalse(condition.test(context));
    }

    @Test
    void evaluatesAnyAndNotWithoutSideEffects() {
        Condition condition =
                new Condition.All(
                        List.of(
                                new Condition.Any(
                                        List.of(
                                                new Condition.MainHandItem("minecraft:stick"),
                                                new Condition.MainHandItem("minecraft:blaze_rod"))),
                                new Condition.Not(new Condition.Dimension("minecraft:the_end"))));

        assertTrue(condition.test(context));
    }

    @Test
    void returnsOnlyEnabledMatchesInPriorityOrder() {
        List<Rule> rules =
                List.of(
                        new Rule("later", true, 20, new Condition.WorldPresent(), List.of()),
                        new Rule("disabled", false, 0, new Condition.WorldPresent(), List.of()),
                        new Rule(
                                "first",
                                true,
                                10,
                                new Condition.KeyBindingExists("key.inventory"),
                                List.of()),
                        new Rule(
                                "miss",
                                true,
                                5,
                                new Condition.Dimension("minecraft:the_end"),
                                List.of()));

        List<String> ids =
                new RuleEvaluator().matchingRules(context, rules).stream().map(Rule::id).toList();

        assertEquals(List.of("first", "later"), ids);
    }

    @Test
    void laterMatchingRuleCanExplicitlyOverrideHintVisibility() {
        List<Rule> rules =
                List.of(
                        new Rule(
                                "show",
                                true,
                                10,
                                new Condition.WorldPresent(),
                                List.of(new RuleAction.ShowHint("contextual"))),
                        new Rule(
                                "hide-in-nether",
                                true,
                                20,
                                new Condition.Dimension("minecraft:the_nether"),
                                List.of(new RuleAction.HideHint("contextual"))));

        RuleEvaluationResult result =
                new RuleEvaluator().evaluate(context, rules, Set.of("always"));

        assertEquals(Set.of("always"), result.visibleHintIds());
        assertEquals(List.of("show", "hide-in-nether"), result.matchedRuleIds());
    }

    @Test
    void inventoryConditionUsesRequestedScopeAndMinimumCount() {
        Condition enough =
                new Condition.InventoryContains(
                        InventoryScope.HOTBAR, "minecraft:blaze_rod", 3);
        Condition tooMany =
                new Condition.InventoryContains(
                        InventoryScope.HOTBAR, "minecraft:blaze_rod", 4);

        assertTrue(enough.test(context));
        assertFalse(tooMany.test(context));
        assertTrue(enough.requiresInventory());
    }

    @Test
    void showActionCanEnableHiddenByDefaultHint() {
        Rule rule =
                new Rule(
                        "show-shield",
                        true,
                        10,
                        new Condition.OffHandItem("minecraft:shield"),
                        List.of(new RuleAction.ShowHint("shield")));

        RuleEvaluationResult result =
                new RuleEvaluator().evaluate(context, List.of(rule), Set.of());

        assertEquals(Set.of("shield"), result.visibleHintIds());
    }
}
