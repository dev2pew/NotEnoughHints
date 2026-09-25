package io.github.dev2pew.notenoughhints.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.StringReader;

import org.junit.jupiter.api.Test;

import io.github.dev2pew.notenoughhints.context.EquipmentSlotKey;
import io.github.dev2pew.notenoughhints.hud.HintDescription;
import io.github.dev2pew.notenoughhints.hud.HudAnchor;
import io.github.dev2pew.notenoughhints.rule.Condition;
import io.github.dev2pew.notenoughhints.rule.RuleAction;

class HintPackJsonParserTest {
    private final HintPackJsonParser parser = new HintPackJsonParser();

    @Test
    void parsesGroupsDescriptionsConditionsAndActions() {
        String json =
                """
                {
                  "schema_version": 1,
                  "groups": [
                    {
                      "id": "main",
                      "anchor": "bottom_left",
                      "flow": "horizontal",
                      "hints": [
                        {
                          "id": "inventory",
                          "binding": "key.inventory",
                          "description": {"translate": "key.inventory"},
                          "show_binding": true,
                          "visible_by_default": true
                        }
                      ]
                    }
                  ],
                  "rules": [
                    {
                      "id": "armor",
                      "priority": 10,
                      "when": {
                        "type": "all",
                        "conditions": [
                          {"type": "dimension", "id": "minecraft:the_nether"},
                          {
                            "type": "equipment_slot_item",
                            "slot": "head",
                            "item": "minecraft:diamond_helmet"
                          }
                        ]
                      },
                      "actions": [
                        {"type": "show_hint", "hint": "inventory"}
                      ]
                    }
                  ]
                }
                """;

        HintPack pack = parser.parse(new StringReader(json));

        assertEquals(1, pack.groups().size());
        assertEquals(HudAnchor.BOTTOM_LEFT, pack.groups().getFirst().anchor());
        assertInstanceOf(
                HintDescription.Translation.class,
                pack.groups().getFirst().hints().getFirst().description());

        Condition.All condition = assertInstanceOf(Condition.All.class, pack.rules().getFirst().condition());
        Condition.EquipmentSlotItem equipment =
                assertInstanceOf(
                        Condition.EquipmentSlotItem.class, condition.conditions().get(1));
        assertEquals(EquipmentSlotKey.HEAD, equipment.slot());
        assertInstanceOf(RuleAction.ShowHint.class, pack.rules().getFirst().actions().getFirst());
    }

    @Test
    void rejectsUnknownConditionTypes() {
        String json =
                """
                {
                  "schema_version": 1,
                  "rules": [
                    {
                      "id": "bad",
                      "when": {"type": "does_not_exist"},
                      "actions": []
                    }
                  ]
                }
                """;

        assertThrows(
                IllegalArgumentException.class,
                () -> parser.parse(new StringReader(json)));
    }

    @Test
    void rejectsAmbiguousDescriptionObject() {
        String json =
                """
                {
                  "schema_version": 1,
                  "groups": [
                    {
                      "id": "main",
                      "hints": [
                        {
                          "id": "inventory",
                          "binding": "key.inventory",
                          "description": {
                            "literal": "Inventory",
                            "translate": "key.inventory"
                          }
                        }
                      ]
                    }
                  ]
                }
                """;

        assertThrows(
                IllegalArgumentException.class,
                () -> parser.parse(new StringReader(json)));
    }

    @Test
    void rejectsDuplicateHintIdsAcrossGroups() {
        String json =
                """
                {
                  "schema_version": 1,
                  "groups": [
                    {"id": "a", "hints": [{"id": "same", "binding": "key.inventory"}]},
                    {"id": "b", "hints": [{"id": "same", "binding": "key.jump"}]}
                  ]
                }
                """;

        assertThrows(
                IllegalArgumentException.class,
                () -> parser.parse(new StringReader(json)));
    }
}
