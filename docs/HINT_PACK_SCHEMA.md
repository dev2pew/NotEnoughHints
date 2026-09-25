# Hint pack JSON schema

Status: development schema, version 1.

NEH reads creator-defined hint packs from:

`config/not-enough-hints/hints/*.json`

On first launch, when that directory contains no JSON files, NEH creates `starter.json` with one inventory-key hint. Files are loaded in filename order. A malformed file is ignored and logged; other valid files continue loading.

IDs for groups, hints, and rules must be unique across all loaded files.

## Root object

```json
{
  "schema_version": 1,
  "groups": [],
  "rules": []
}
```

`schema_version` is required. `groups` and `rules` may be omitted and default to empty arrays.

## Hint groups

```json
{
  "id": "movement",
  "anchor": "bottom_left",
  "offset_x": 0,
  "offset_y": 0,
  "flow": "horizontal",
  "entry_gap": 6,
  "line_gap": 4,
  "max_width": 0,
  "hints": []
}
```

Supported anchors:

- `top_left`
- `top_center`
- `top_right`
- `center_left`
- `center`
- `center_right`
- `bottom_left`
- `bottom_center`
- `bottom_right`

Supported flow values are `horizontal` and `vertical`.

`entry_gap` controls spacing between entries in the same row, or between entries in a vertical group. `line_gap` controls spacing between rows created by horizontal wrapping.

`max_width` is the group wrapping width in GUI pixels before NEH scale is applied. A value of `0` uses the available safe-screen width. Horizontal groups wrap whole hint entries onto additional rows rather than shrinking key glyphs or descriptions. Group placement is clamped to the configured screen margin after anchor offsets are applied.

## Hints

```json
{
  "id": "inventory",
  "binding": "key.inventory",
  "show_binding": true,
  "visible_by_default": true
}
```

`binding` is the stable Minecraft keybinding translation ID, not the currently assigned physical key.

The description is optional. When omitted, NEH uses the keybinding's translated description.

A literal override:

```json
"description": {
  "literal": "Open equipment"
}
```

A translated override:

```json
"description": {
  "translate": "examplemod.key.open_equipment"
}
```

Exactly one of `literal` or `translate` is allowed when `description` is present.

## Rules

```json
{
  "id": "show-nether-tool",
  "enabled": true,
  "priority": 20,
  "when": {
    "type": "dimension",
    "id": "minecraft:the_nether"
  },
  "actions": [
    {
      "type": "show_hint",
      "hint": "nether-tool"
    }
  ]
}
```

Rules evaluate in ascending priority order. Rules with the same priority are ordered by ID. Later explicit visibility actions therefore override earlier actions targeting the same hint.

A pack's `enabled: false` is creator-owned and cannot be overridden by the player. Player rule-disable preferences are stored separately in `neh.json` as stable rule IDs, so using the settings screen does not rewrite creator hint packs.

Supported actions:

- `show_hint`
- `hide_hint`

## Logical conditions

All conditions:

```json
{
  "type": "all",
  "conditions": [
    {"type": "dimension", "id": "minecraft:the_nether"},
    {"type": "off_hand_item", "item": "minecraft:shield"}
  ]
}
```

Any condition:

```json
{
  "type": "any",
  "conditions": [
    {"type": "main_hand_item", "item": "minecraft:blaze_rod"},
    {"type": "main_hand_item", "item": "minecraft:breeze_rod"}
  ]
}
```

Negation:

```json
{
  "type": "not",
  "condition": {
    "type": "dimension",
    "id": "minecraft:the_end"
  }
}
```

## State conditions implemented in schema version 1

World availability:

```json
{"type": "world_present"}
```

Dimension:

```json
{"type": "dimension", "id": "minecraft:the_nether"}
```

Exact screen class:

```json
{"type": "screen_class", "class": "com.example.client.SomeScreen"}
```

The selector is a binary class name. Mod-owned screen classes use their normal binary names. Minecraft classes must use the stable Fabric intermediary name because Minecraft 1.21.8 class names differ between the development namespace and production. For example, the 1.21.8 options screen is:

```json
{"type": "screen_class", "class": "net.minecraft.class_429"}
```

The debug HUD reports the canonical screen selector, so pack authors do not need to infer an intermediary class ID from the development class name.

Registered handled-menu ID:

```json
{"type": "handled_menu", "id": "minecraft:generic_9x3"}
```

Main hand:

```json
{"type": "main_hand_item", "item": "minecraft:blaze_rod"}
```

Off hand:

```json
{"type": "off_hand_item", "item": "minecraft:shield"}
```

Either hand:

```json
{"type": "held_item_any_hand", "item": "minecraft:breeze_rod"}
```

Exact equipment slot:

```json
{
  "type": "equipment_slot_item",
  "slot": "head",
  "item": "minecraft:diamond_helmet"
}
```

Supported equipment slots are:

- `main_hand`
- `off_hand`
- `head`
- `chest`
- `legs`
- `feet`

Loaded mod:

```json
{"type": "mod_loaded", "mod": "fabric-api"}
```

Available keybinding:

```json
{"type": "keybinding_exists", "binding": "key.inventory"}
```

Inventory contents:

```json
{
  "type": "inventory_contains",
  "scope": "player_inventory",
  "item": "minecraft:ender_pearl",
  "min_count": 4
}
```

Supported inventory scopes:

- `hotbar`: slots (0) through (8);
- `main_inventory`: the remaining ordinary inventory slots, excluding the hotbar;
- `armor`: head, chest, legs, and feet;
- `offhand`: the off-hand equipment slot;
- `player_inventory`: hotbar, main inventory, armor, and off-hand combined.

`scope` defaults to `player_inventory`. `min_count` defaults to (1) and must be at least (1).

Set `"include_nested": true` to include immediate nested contents. Vanilla component-backed containers include shulker-style `container` contents and bundles. Supported mod-owned containers are added through optional NEH adapters; the first adapter reads Traveler's Backpack storage from its `travelersbackpack:backpack_container` data component.

Nested lookup is intentionally one level deep. A container inside another container is not recursively opened, including a container returned by a compatibility adapter.

Direct and nested-inclusive counts are stored separately. A rule without `include_nested` therefore retains ordinary player-inventory semantics even when another rule enables nested scanning.

NEH builds the inventory count index only when an enabled rule contains an `inventory_contains` condition. Nested component inspection is enabled only when an enabled rule sets `include_nested` to `true`. Multiple inventory conditions reuse those indexes during the context refresh.

## Combined example

```json
{
  "schema_version": 1,
  "groups": [
    {
      "id": "contextual",
      "anchor": "bottom_left",
      "flow": "horizontal",
      "hints": [
        {
          "id": "inventory",
          "binding": "key.inventory",
          "visible_by_default": true
        },
        {
          "id": "drop-nether-item",
          "binding": "key.drop",
          "description": {"literal": "Drop held item"},
          "visible_by_default": false
        }
      ]
    }
  ],
  "rules": [
    {
      "id": "nether-blaze-rod",
      "priority": 10,
      "when": {
        "type": "all",
        "conditions": [
          {"type": "dimension", "id": "minecraft:the_nether"},
          {"type": "main_hand_item", "item": "minecraft:blaze_rod"},
          {"type": "off_hand_item", "item": "minecraft:shield"},
          {
            "type": "equipment_slot_item",
            "slot": "head",
            "item": "minecraft:diamond_helmet"
          }
        ]
      },
      "actions": [
        {"type": "show_hint", "hint": "drop-nether-item"}
      ]
    }
  ]
}
```

Traveler's Backpack nested storage is supported through an optional adapter when the target mod is loaded. Other mod-owned container adapters, custom mod integration state, sprite composition, and runtime reload controls remain later development work.
