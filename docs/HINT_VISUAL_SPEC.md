# Hint Visual Specification

Status: design contract for the first NEH HUD implementation.

This document refines the visual model from `TECHNICAL_PLAN.md`. It is based on the supplied Controllable screenshot, the inspected Controllable source, and the requirement that NEH remain readable without taking over the screen.

## 1. Hint entry model

A visible hint entry has two required parts and one optional part:

- a required glyph frame;
- a required description;
- an optional binding label.

The glyph frame is always present. The binding label may be hidden. The description may not be hidden.

The default presentation is:

`[glyph containing binding] Description`

This matches the compact structure of Controllable's action hints while allowing keyboard and mouse bindings instead of controller-button textures.

A hint entry is data, not a pre-rendered image. The renderer composes the glyph frame, optional semantic icon, binding label, and description at runtime.

## 2. Layout presets

Version 1 supports named presets instead of an unrestricted per-subcomponent layout editor.

### COMPACT

`[glyph + centered binding] Description`

This is the default. It is the closest match to the supplied console-style example.

### CLASSIC

`[glyph/icon] [binding capsule] Description`

Use this when the semantic icon matters more than the physical key. The binding capsule remains visually separate and can grow for longer key names.

### STACKED

Top row: `[glyph/icon] Description`

Second row below the glyph: centered binding label.

This is available for creators who want the binding visually subordinate to the action description. It is not the default because it consumes more vertical space.

The binding label can be disabled in all presets. The glyph remains.

## 3. Glyph frame

The term glyph frame replaces “sprite” inside the implementation because the result may be composed from several texture pieces and text layers.

Initial built-in frame shapes:

- keycap;
- rounded square;
- square;
- circle;
- pill.

The frame implementation should use atlas-backed, Minecraft-style textures or sliced textures rather than rasterizing arbitrary vector geometry every frame.

A frame style contains:

- shape;
- fill;
- border;
- optional shadow;
- opacity;
- padding;
- optional semantic icon;
- semantic icon tint;
- binding text color;
- optional accent.

Creators can select included icons. A later extension may allow a resource-location texture supplied by a resource pack, but arbitrary external image loading is outside version 1.

## 4. Binding labels

Binding text is resolved from the player's current mapping. Configuration stores a stable keybinding selector, not the currently assigned physical key.

Standard Minecraft key mappings produce one binding token. Optional integrations may produce multiple tokens for custom chords.

Examples:

`E`

`F12`

`Mouse 4`

`Ctrl + G`

`Ctrl + Shift + G`

A chord is rendered as a glyph cluster. Each token receives its own keycap-like segment, with separators supplied by the layout. This avoids forcing a long chord into a fixed square sprite.

The glyph frame grows to the measured text width. The renderer must not truncate a functional key combination merely to keep a fixed icon width.

If a key is unbound, the hint remains valid. When binding visibility is enabled, the glyph displays an explicit unbound state and diagnostics identify the unresolved or unbound selector.

## 5. Description resolution

Description precedence:

1. creator-supplied literal or translatable override;
2. the keybinding's game/mod translation;
3. the fallback text `Unnamed action`.

The raw translation key or stable selector is shown in diagnostics, not as ordinary player-facing fallback text.

The fallback must never be `Sample` in a normal game session because that looks like unfinished configuration rather than an unavailable translation.

Descriptions can use Minecraft translatable components so resource packs and the active game language continue to work.

## 6. Readability

Binding text drawn inside a glyph frame must stay readable over every built-in fill style.

The renderer should provide:

- automatic foreground selection for built-in themes;
- an outline or shadow option;
- validation warnings for creator-selected foreground/background combinations that make the text difficult to read;
- a safe built-in fallback style if a custom style cannot be resolved.

User-selected colors may override the warning. NEH should not silently rewrite a pack author's configuration after validation.

The description background is optional. The default console-style preset uses a restrained translucent backing behind the description, matching the visual role of Controllable's hint background without copying its assets.

## 7. Sizing

Controllable's inspected action overlay uses a 13-by-13 GUI-pixel controller button cell. NEH may use that as the base single-glyph height for the first visual prototype because it fits Minecraft's normal HUD typography and reproduces the supplied screenshot's visual weight.

Width is dynamic.

A single-character key can remain close to square. Labels such as `F12`, `Mouse 4`, or chord groups expand horizontally according to the measured font width plus frame padding.

Scaling is applied to the composed hint after layout measurement so the key label and description keep consistent proportions.

## 8. Hint groups

Hints belong to named groups. A group controls:

- screen anchor;
- x/y offset;
- row, column, or wrapping flow;
- entry gap;
- line gap;
- group scale;
- group opacity;
- element order;
- maximum wrapping width;
- visibility policy.

The default console-style group is bottom-left and horizontal, matching the supplied screenshot.

A creator can instead use a vertical group for larger cheat sheets.

The group renderer lays out only the final visible entries produced by the rule engine.

## 9. HUD collision policy

NEH does not assume ownership of the player's HUD.

Version 1 will account for known vanilla UI occupancy where reliable client state exists, including chat and other major vanilla overlays. Unknown third-party HUDs cannot be discovered generically, so creators and players retain anchor and offset controls.

The default preset must not move the vanilla hotbar.

Controllable's optional console-hotbar mode modifies the hotbar render transform. NEH treats that behavior as a separate future feature because changing vanilla HUD coordinates increases compatibility risk with unrelated HUD mods.

## 10. Animation

No animation is required for the first playable build.

If animation is added later, layout state and visibility state remain independent. Fading an entry must not cause its condition to be re-evaluated from the render method.

Animations may interpolate already-computed render state only.

## 11. Creator and player controls

Creator-owned hint definitions specify:

- description source;
- keybinding selector;
- glyph shape/icon/style;
- binding visibility default;
- layout preset;
- rule conditions;
- group;
- priority.

Player preferences may override presentation without modifying the shipped hint definitions:

- global enable/disable;
- global scale;
- global opacity;
- optional binding-label visibility override;
- debug mode;
- per-group visibility where allowed by the pack;
- accessibility/readability preset.

This separation lets a modpack ship useful defaults without trapping the player in the author's HUD preferences.

## 12. Validation cases

Visual tests must cover at least these content classes:

- one-character keyboard key;
- function key;
- mouse button;
- long localized key name;
- custom chord with multiple tokens;
- unbound key;
- missing translation;
- icon-only glyph with binding hidden;
- several hints in one horizontal group;
- enough hints to trigger wrapping;
- bottom-left placement with chat visible;
- multiple GUI scales.

Client GameTests should retain screenshots for representative visual cases so regressions in spacing and clipping can be inspected in CI.
