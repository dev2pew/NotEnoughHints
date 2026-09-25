# Reference Evidence

Inspection date: 2026-09-25.

This file records source material actually inspected for NEH. It separates observed implementation details from ideas NEH will implement independently.

## Supplied source bundle

### Binders

Repository: `Camawama/Binders`

Supplied commit: `adef07b331b9bfeba8c51ade48fc26e095688381`

Branch: `main`

Target in supplied source: Minecraft Forge 1.20.1.

Observed implementation:

- `BinderDefinition` stores a keybinding selector, label, item icon, context string, AND/OR mode, color, scale, press-count limit, regex mode, and dynamic-icon flag.
- `BinderManager.JsonBinder.shouldShow` parses comma-separated context strings while evaluating visibility.
- Supported conditions include player movement/state, held items, armor, block/entity targeting, dimension, biome, health, and hunger.
- Regex patterns are compiled inside several evaluation paths.
- `KeybindOverlay.render` iterates the binder registry, calls `shouldShow`, updates fade state, resolves icons, and renders entries every frame.
- `getKeyMapping` linearly searches Minecraft's key-mapping array when a specific mapping was not pre-bound.

NEH use:

- retain the idea of declarative context-aware hints;
- replace free-form comma-separated context strings with a validated typed condition tree;
- compile selectors and regex only at config-load time;
- evaluate context outside the render callback and render a precomputed state.

License note:

`gradle.properties` declares `mod_license=MIT`, but the repository's `LICENSE.txt` is Forge LGPL 2.1 boilerplate rather than an MIT license grant for Binders. Treat Binders code as reference-only until the repository's licensing is made internally consistent. Concepts can be independently implemented.

### Controllable

Repository: `MrCrayfish/Controllable`

Supplied commit: `7333428d29464db914750eac2a039c22102e3e65`

Supplied branch: `multiloader/26.2`.

The supplied snapshot is newer than NEH's initial Minecraft 1.21.8 target.

Observed implementation:

- `ActionHintOverlay` separates a tick-time action-map rebuild from the draw methods.
- The console-style path draws one 13-by-13 controller-button texture followed by a translated action description.
- The side-hint path stacks the same button/description unit from screen edges.
- Action selection already depends on screen type, carried stack, held item use animation, hit result, riding state, and other client state.
- the supplied Fabric HUD mixin moves the hotbar by changing the render transform when console-hotbar mode is enabled.
- `PaperDollPlayerOverlay` is a separate overlay rather than part of the action-hint renderer.

NEH use:

- adopt the visual grammar of compact glyph plus description;
- keep state evaluation separate from rendering;
- keep the paper-doll and hotbar-shift ideas outside the core hint system;
- do not copy Controllable textures or controller-specific input logic.

License in supplied repository: MIT for Controllable itself, with separate notices for bundled SDL/controller database material.

### ModKeys

Repository: `Cehira123/modkeys`

Supplied commit: `7cc5ca079b0dfa966e439cb8576d1b5b2bf246dc`

Branch: `main`

Target: Fabric 1.20.1.

Observed implementation:

- `KeyBindingScanner.scan` enumerates `MinecraftClient.options.allKeys` and uses each binding translation key as its stable ID.
- favorites are persisted as translation-key IDs.
- action labels and current bound-key labels are resolved separately.
- `ModIdResolver` guesses a mod ID from translation-key/category naming conventions.
- config has an explicit schema version, normalization, migration logic, and unit tests.
- `HudRenderer.render` calls `scanner.favorites(config)`; that path rescans all keybindings before rendering favorites.

NEH use:

- keep translation keys as one stable keybinding selector where the target version exposes them consistently;
- keep current bound-key text separate from the selector;
- keep schema-versioned config migration and unit tests;
- do not rescan every keybinding from the render callback;
- do not treat translation-key prefix heuristics as authoritative mod ownership.

License in supplied repository: MIT.

### Traveler's Backpack

Repository: `Tiviacz1337/Travelers-Backpack`

Supplied commit: `f6696f9f813048fdeeaae3e29d0b8d903e99ce66`

Supplied branch: `1.20.1`

Supplied target: Forge 1.20.1.

The supplied snapshot is useful for historical structure but is not the target integration source for NEH 1.21.8.

## Target-version GitHub references

### Controllable 1.21.8

Branch inspected through GitHub connector: `multiloader/1.21.8`.

`gradle.properties` blob `6e7ae17652d444c8bb2bc1424ede3e7bf3923f3d` declares Minecraft 1.21.8, Java 21, Fabric API 0.129.0+1.21.8, and Controllable 0.25.3.

Relevant file blobs:

- `ActionHintOverlay.java`: `505e5297b0b49fa4a3e1532a745f4070ee86d7b7`.
- `FabricGuiMixin.java`: `af6da99c9f0576561cdd05e615f6cfd5141b8dbc`.

The 1.21.8 action overlay retains the same core visual model: a 13-by-13 controller glyph next to an action label. The Fabric hotbar offset is implemented by a mixin around `Gui.renderHotbarAndDecorations`.

License file blob `13ef09e227188a747c71766c250bbbbd653aecaa`: MIT.

### Traveler's Backpack 1.21.8 Fabric

Branch inspected through GitHub connector: `1.21.8-fabric`.

`gradle.properties` blob `b265ded900555aaf4b8ab77c10698712034314b2` declares Minecraft 1.21.8, Traveler's Backpack 10.8.4, Fabric Loader 0.17.2, and Fabric API 0.132.0+1.21.8.

Relevant file blobs:

- `BackpackContainerContents.java`: `acab44ef3c11a176692ffd192710493563a38cea`.
- `ModDataComponents.java`: `496f26eb4eacce87608a4a23756f591e02fe6c0b`.

Observed target-version storage model:

- storage, upgrades, and tools use dedicated data components;
- `BACKPACK_CONTAINER`, `UPGRADES`, and `TOOLS_CONTAINER` hold `BackpackContainerContents`;
- `BackpackContainerContents` exposes the contained `ItemStack` list;
- the component codec explicitly bounds the list.

NEH integration direction:

A Traveler's Backpack adapter should prefer reading the target mod's public data component from the backpack `ItemStack` rather than constructing a full `BackpackWrapper` merely to answer a read-only `inventory_contains` predicate.

Traveler's Backpack's Fabric project is published under LGPLv3. The adapter should remain optional and isolated behind a mod-presence check.

## External API evidence

Fabric documentation confirms:

- key mappings are client-side;
- Fabric provides `HudElementRegistry` for layered HUD elements;
- HUD matrix handling changed starting with Minecraft 1.21.8;
- Fabric Loader JUnit and client GameTests are available for automated testing;
- persistent `ItemStack` state uses the data-component system from Minecraft 1.20.5 onward.

These facts constrain NEH's initial 1.21.8 implementation and test plan.

## Copying policy

No reference implementation is copied by default.

Before adapting source code, record:

- repository;
- exact commit or file blob;
- source path;
- applicable license;
- copied/adapted lines or independently reproduced behavior.

When licensing is ambiguous, use behavior and architecture as research input only.
