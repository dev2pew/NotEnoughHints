package io.github.dev2pew.notenoughhints.config;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import io.github.dev2pew.notenoughhints.context.EquipmentSlotKey;
import io.github.dev2pew.notenoughhints.context.InventoryScope;
import io.github.dev2pew.notenoughhints.hud.HintDefinition;
import io.github.dev2pew.notenoughhints.hud.HintDescription;
import io.github.dev2pew.notenoughhints.hud.HintFlow;
import io.github.dev2pew.notenoughhints.hud.HintGroupDefinition;
import io.github.dev2pew.notenoughhints.hud.HudAnchor;
import io.github.dev2pew.notenoughhints.rule.Condition;
import io.github.dev2pew.notenoughhints.rule.Rule;
import io.github.dev2pew.notenoughhints.rule.RuleAction;

public final class HintPackJsonParser {
    public HintPack parse(Reader reader) {
        JsonObject root = requireObject(JsonParser.parseReader(reader), "$");
        int schemaVersion = requireInt(root, "schema_version", "$");

        List<HintGroupDefinition> groups = new ArrayList<>();
        JsonArray groupsArray = optionalArray(root, "groups");
        for (int i = 0; i < groupsArray.size(); i++) {
            groups.add(parseGroup(requireObject(groupsArray.get(i), "$.groups[" + i + "]"), i));
        }

        List<Rule> rules = new ArrayList<>();
        JsonArray rulesArray = optionalArray(root, "rules");
        for (int i = 0; i < rulesArray.size(); i++) {
            rules.add(parseRule(requireObject(rulesArray.get(i), "$.rules[" + i + "]"), i));
        }

        return new HintPack(schemaVersion, groups, rules);
    }

    private HintGroupDefinition parseGroup(JsonObject object, int index) {
        String path = "$.groups[" + index + "]";
        String id = requireString(object, "id", path);
        HudAnchor anchor = parseEnum(
                HudAnchor.class, optionalString(object, "anchor", "bottom_left"), path + ".anchor");
        int offsetX = optionalInt(object, "offset_x", 0);
        int offsetY = optionalInt(object, "offset_y", 0);
        HintFlow flow = parseEnum(
                HintFlow.class, optionalString(object, "flow", "horizontal"), path + ".flow");
        int entryGap = optionalInt(object, "entry_gap", 6);

        JsonArray hintsArray = optionalArray(object, "hints");
        List<HintDefinition> hints = new ArrayList<>(hintsArray.size());
        for (int i = 0; i < hintsArray.size(); i++) {
            hints.add(
                    parseHint(
                            requireObject(hintsArray.get(i), path + ".hints[" + i + "]"),
                            path + ".hints[" + i + "]"));
        }

        return new HintGroupDefinition(id, anchor, offsetX, offsetY, flow, entryGap, hints);
    }

    private HintDefinition parseHint(JsonObject object, String path) {
        String id = requireString(object, "id", path);
        String binding = requireString(object, "binding", path);
        HintDescription description = parseDescription(object.get("description"), path + ".description");
        boolean showBinding = optionalBoolean(object, "show_binding", true);
        boolean visibleByDefault = optionalBoolean(object, "visible_by_default", false);

        return new HintDefinition(id, binding, description, showBinding, visibleByDefault);
    }

    private HintDescription parseDescription(JsonElement element, String path) {
        if (element == null || element.isJsonNull()) {
            return new HintDescription.Default();
        }
        JsonObject object = requireObject(element, path);
        boolean hasLiteral = object.has("literal");
        boolean hasTranslation = object.has("translate");

        if (hasLiteral == hasTranslation) {
            throw error(path, "must contain exactly one of 'literal' or 'translate'");
        }
        if (hasLiteral) {
            return new HintDescription.Literal(requireString(object, "literal", path));
        }
        return new HintDescription.Translation(requireString(object, "translate", path));
    }

    private Rule parseRule(JsonObject object, int index) {
        String path = "$.rules[" + index + "]";
        String id = requireString(object, "id", path);
        boolean enabled = optionalBoolean(object, "enabled", true);
        int priority = optionalInt(object, "priority", 0);
        Condition condition = parseCondition(requireObject(require(object, "when", path), path + ".when"), path + ".when");

        JsonArray actionsArray = requireArray(object, "actions", path);
        List<RuleAction> actions = new ArrayList<>(actionsArray.size());
        for (int i = 0; i < actionsArray.size(); i++) {
            actions.add(
                    parseAction(
                            requireObject(actionsArray.get(i), path + ".actions[" + i + "]"),
                            path + ".actions[" + i + "]"));
        }

        return new Rule(id, enabled, priority, condition, actions);
    }

    private Condition parseCondition(JsonObject object, String path) {
        String type = requireString(object, "type", path);
        return switch (type) {
            case "all" -> new Condition.All(parseConditionArray(object, "conditions", path));
            case "any" -> new Condition.Any(parseConditionArray(object, "conditions", path));
            case "not" -> new Condition.Not(
                    parseCondition(
                            requireObject(require(object, "condition", path), path + ".condition"),
                            path + ".condition"));
            case "world_present" -> new Condition.WorldPresent();
            case "dimension" -> new Condition.Dimension(requireString(object, "id", path));
            case "screen_class" -> new Condition.ScreenClass(requireString(object, "class", path));
            case "handled_menu" -> new Condition.HandledMenu(requireString(object, "id", path));
            case "main_hand_item" -> new Condition.MainHandItem(requireString(object, "item", path));
            case "off_hand_item" -> new Condition.OffHandItem(requireString(object, "item", path));
            case "held_item_any_hand" ->
                    new Condition.HeldItemAnyHand(requireString(object, "item", path));
            case "equipment_slot_item" ->
                    new Condition.EquipmentSlotItem(
                            parseEnum(
                                    EquipmentSlotKey.class,
                                    requireString(object, "slot", path),
                                    path + ".slot"),
                            requireString(object, "item", path));
            case "inventory_contains" ->
                    new Condition.InventoryContains(
                            parseEnum(
                                    InventoryScope.class,
                                    optionalString(object, "scope", "player_inventory"),
                                    path + ".scope"),
                            requireString(object, "item", path),
                            optionalInt(object, "min_count", 1));
            case "mod_loaded" -> new Condition.ModLoaded(requireString(object, "mod", path));
            case "keybinding_exists" ->
                    new Condition.KeyBindingExists(requireString(object, "binding", path));
            default -> throw error(path + ".type", "unknown condition type '" + type + "'");
        };
    }

    private List<Condition> parseConditionArray(JsonObject object, String member, String path) {
        JsonArray array = requireArray(object, member, path);
        List<Condition> conditions = new ArrayList<>(array.size());
        for (int i = 0; i < array.size(); i++) {
            String childPath = path + "." + member + "[" + i + "]";
            conditions.add(parseCondition(requireObject(array.get(i), childPath), childPath));
        }
        return conditions;
    }

    private RuleAction parseAction(JsonObject object, String path) {
        String type = requireString(object, "type", path);
        String hintId = requireString(object, "hint", path);
        return switch (type) {
            case "show_hint" -> new RuleAction.ShowHint(hintId);
            case "hide_hint" -> new RuleAction.HideHint(hintId);
            default -> throw error(path + ".type", "unknown action type '" + type + "'");
        };
    }

    private static JsonElement require(JsonObject object, String member, String path) {
        JsonElement element = object.get(member);
        if (element == null || element.isJsonNull()) {
            throw error(path + "." + member, "is required");
        }
        return element;
    }

    private static JsonObject requireObject(JsonElement element, String path) {
        if (!element.isJsonObject()) {
            throw error(path, "must be an object");
        }
        return element.getAsJsonObject();
    }

    private static JsonArray requireArray(JsonObject object, String member, String path) {
        JsonElement element = require(object, member, path);
        if (!element.isJsonArray()) {
            throw error(path + "." + member, "must be an array");
        }
        return element.getAsJsonArray();
    }

    private static JsonArray optionalArray(JsonObject object, String member) {
        JsonElement element = object.get(member);
        if (element == null || element.isJsonNull()) {
            return new JsonArray();
        }
        if (!element.isJsonArray()) {
            throw error("$." + member, "must be an array");
        }
        return element.getAsJsonArray();
    }

    private static String requireString(JsonObject object, String member, String path) {
        JsonElement element = require(object, member, path);
        if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isString()) {
            throw error(path + "." + member, "must be a string");
        }
        return element.getAsString();
    }

    private static String optionalString(JsonObject object, String member, String fallback) {
        JsonElement element = object.get(member);
        if (element == null || element.isJsonNull()) {
            return fallback;
        }
        if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isString()) {
            throw error("$." + member, "must be a string");
        }
        return element.getAsString();
    }

    private static int requireInt(JsonObject object, String member, String path) {
        JsonElement element = require(object, member, path);
        try {
            return element.getAsInt();
        } catch (RuntimeException exception) {
            throw error(path + "." + member, "must be an integer");
        }
    }

    private static int optionalInt(JsonObject object, String member, int fallback) {
        JsonElement element = object.get(member);
        if (element == null || element.isJsonNull()) {
            return fallback;
        }
        try {
            return element.getAsInt();
        } catch (RuntimeException exception) {
            throw error("$." + member, "must be an integer");
        }
    }

    private static boolean optionalBoolean(JsonObject object, String member, boolean fallback) {
        JsonElement element = object.get(member);
        if (element == null || element.isJsonNull()) {
            return fallback;
        }
        if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isBoolean()) {
            throw error("$." + member, "must be a boolean");
        }
        return element.getAsBoolean();
    }

    private static <E extends Enum<E>> E parseEnum(
            Class<E> type, String value, String path) {
        try {
            return Enum.valueOf(type, value.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw error(path, "unknown value '" + value + "'");
        }
    }

    private static IllegalArgumentException error(String path, String message) {
        return new IllegalArgumentException(path + " " + message);
    }
}
