# NEH Technical Plan

Status: implementation contract for the first playable development line.

Target: Minecraft Java Edition 1.21.8, Fabric Loader, client-side mod.

## 1. Product boundary

Not Enough Hints (NEH) is a context-aware HUD instruction system for players using complex modpacks.

The first release is not a tutorial engine, quest system, scripting runtime, or universal mod introspection framework. It reads client-visible state, evaluates declarative conditions, and renders low-profile HUD elements. It must remain useful when other mods are installed, removed, updated, or partially incompatible.

Primary use cases:

1. Always show one or more keybinding hints.
2. Change one or more hints while a specific item is held.
3. Show several hints while another item is held.
4. Change HUD content by dimension.
5. Change HUD content while an inventory or other screen is open.
6. Combine conditions such as screen, dimension, held item, inventory contents, and mod presence.
7. Disable the hint system globally once the player no longer needs it.

## 2. Non-goals for the first release

The following are explicitly outside the initial implementation unless a later design decision promotes them:

- Server-authoritative gameplay rules.
- Packet inspection for undocumented mod state.
- Reflection-based scraping of arbitrary mod internals.
- Executing commands or arbitrary code from configuration.
- Automatic discovery of every possible Screen subclass before it has been opened.
- Deep recursive scanning of every possible mod container on every tick.
- Replacing Minecraft's Controls screen.
- Automatic remapping of conflicting keys.
- Editing other mods' configuration.
- Moving vanilla HUD elements by invasive mixins solely for visual style.
- Compatibility hacks that require broad bytecode patches when a public API or adapter can solve the same problem.

## 3. Architecture

NEH is divided into six layers.

### 3.1 Discovery layer

Build immutable or rarely changing catalogs for:

- loaded mods;
- registered key mappings visible through the client options/keybinding system;
- vanilla and registered item identifiers;
- dimension identifiers observed through the active client world;
- registered handled-menu identifiers where obtainable from the screen-handler registry;
- screens observed at runtime;
- optional integration providers supplied by NEH or third-party mods.

Discovery must not run in a render callback.

Catalog refresh triggers:

- client initialization;
- resource reload only when translation/display metadata requires it;
- config reload;
- explicit debug refresh;
- world join where world-dependent identifiers become available;
- integration provider registration.

Loaded-mod and registry catalogs are not rebuilt every tick.

### 3.2 Observation layer

Collect a compact ClientContext snapshot containing only state used by active rules.

Candidate fields:

- world present;
- player present;
- current dimension key;
- active Screen class name;
- active handled-menu type identifier when known;
- main-hand ItemStack identifier;
- off-hand ItemStack identifier;
- selected hotbar slot;
- inventory item summary;
- player state flags required by configured predicates;
- loaded mod IDs;
- current resolved key mappings referenced by configured elements.

Observation is demand-driven. If no active rule asks about inventory contents, NEH does not scan inventory contents.

### 3.3 Rule engine

Rules are declarative and side-effect free.

Each rule contains:

- stable rule ID;
- enabled flag;
- priority;
- condition tree;
- one or more actions affecting NEH HUD state.

Condition nodes:

- all: logical AND;
- any: logical OR;
- not;
- mod_loaded;
- world_present;
- dimension;
- screen_class;
- handled_menu;
- main_hand_item;
- off_hand_item;
- held_item_any_hand;
- inventory_contains;
- keybinding_exists;
- integration_state.

No arbitrary script expressions in version 1.

Rules evaluate against one ClientContext snapshot. A condition cannot mutate game state.

Conflict resolution:

1. Disabled rules do not evaluate.
2. Rules evaluate in ascending priority order.
3. Actions target named HUD elements.
4. Later matching rules may override properties only when the action explicitly declares override behavior.
5. Hide is explicit; a missing or invalid condition does not silently hide unrelated elements.
6. Debug output records which rule produced the final state.

### 3.4 HUD model

Initial element types:

- text;
- keybinding hint;
- item icon plus text;
- compact box/group.

Every element has:

- stable ID;
- enabled flag;
- anchor;
- x/y offset;
- horizontal text alignment where relevant;
- z/layer ordering controlled through the supported HUD API;
- scale;
- opacity;
- padding;
- optional background;
- optional border;
- visibility default;
- compact spacing.

Initial anchors:

- top-left;
- top-center;
- top-right;
- center-left;
- center;
- center-right;
- bottom-left;
- bottom-center;
- bottom-right.

A keybinding hint references a Minecraft keybinding translation key or another stable selector. The displayed key is resolved from the player's current mapping at render-state preparation time. Config files do not hard-code the physical key unless the creator intentionally uses plain text.

The default visual preset must be low-profile:

- small text;
- restrained padding;
- translucent or absent background;
- no animation required;
- edge-aligned placement;
- no full-screen overlays;
- no automatic displacement of unrelated mod HUDs.

Console-style placement is a visual reference, not a requirement to clone another mod's rendering code.

### 3.5 Integration layer

Adapters are the only supported mechanism for mod-owned state that cannot be obtained safely from vanilla/Fabric APIs.

An integration provider may expose:

- additional condition types;
- nested inventory lookup;
- stable identifiers for custom UI state;
- mod-specific key chord metadata when not represented by normal Minecraft key mappings.

Providers must be optional. Missing target mods must not crash class loading.

Preferred pattern:

- common integration interface in NEH;
- one adapter package per supported mod;
- loader/mod-presence gate before adapter initialization;
- no direct references to absent mod classes from always-loaded classes.

### 3.6 Diagnostics layer

Debug mode is a product feature, not temporary logging.

Initial diagnostics:

- current dimension ID;
- current Screen class;
- current handled-menu ID when available;
- main/off-hand item IDs;
- selected slot;
- referenced inventory query results;
- loaded mod IDs;
- discovered keybinding translation keys, categories, and resolved keys;
- active rule IDs;
- failed/missing references;
- per-rule evaluation result;
- frame-safe performance counters for context refresh and rule evaluation.

Debug views must redact nothing that is already local game/mod metadata, but should avoid dumping large NBT/component payloads by default.

## 4. Keybinding discovery

Minecraft/Fabric key mappings are client-side. NEH should enumerate the key mappings already present in the client's options/control system instead of maintaining its own duplicate list.

Store a compact descriptor:

- translation key;
- category identifier/name where exposed;
- current bound input;
- source mod ID when it can be derived reliably;
- display translation resolved separately from the stable selector.

Do not use translated display text as the persistent identity.

Custom multi-key/chord systems are not guaranteed to be represented as normal Minecraft key mappings. NEH must support an extension provider for mods that implement their own chord logic.

If a referenced keybinding disappears after a mod update:

- keep the user's configuration;
- mark the reference unresolved;
- do not crash;
- do not substitute a different binding with a similar translated name;
- expose the unresolved selector in diagnostics and config UI.

## 5. Screen and GUI identification

There is no assumption that every possible client Screen is globally registered and enumerable.

NEH therefore uses two sources:

1. Registered handled-menu/screen-handler identifiers where the game exposes a registry-backed type.
2. Runtime observation of the active Screen class.

For an active screen, diagnostics should expose:

- fully qualified Screen class name;
- simple class name;
- handled-menu type ID if the screen is a handled/container screen and the type can be resolved;
- owning mod guess only when it can be derived from stable registration or package metadata.

Runtime-observed screen classes may be cached for diagnostics. They are not treated as proof that the screen still exists after a mod is removed.

Configuration may match:

- exact class name;
- exact handled-menu ID;
- built-in semantic aliases such as any_screen or any_handled_screen.

Regex matching is deferred until there is a demonstrated need because it complicates validation and accidental broad matches.

## 6. Player state

### 6.1 World and dimension

Dimension checks use the active client's world registry key/identifier.

Rules referencing a missing dimension remain valid configuration but evaluate false while unresolved.

### 6.2 Held items

Held-item predicates compare stable item identifiers against main hand, off hand, or either hand.

Optional future predicates may inspect components, but version 1 should not expose arbitrary component expressions.

### 6.3 Player inventory

Vanilla player inventory lookup is indexed once per context refresh only when required.

An inventory query specifies:

- item ID or tag selector;
- minimum count;
- search scope.

Initial search scopes:

- hotbar;
- main inventory;
- armor;
- offhand;
- player_inventory.

The same inventory must not be rescanned separately for every rule. Build one count/index structure for the selectors required by active rules.

### 6.4 Nested containers

Nested-container scanning is opt-in and bounded.

Vanilla shulker-like storage should use supported data-component/container APIs for the target Minecraft version rather than legacy assumptions about raw NBT.

Mod-owned containers, including Traveler's Backpack, use adapters.

Safety limits:

- no unbounded recursion;
- no scanning arbitrary unknown item payloads;
- cache adapter results for the current inventory revision/context window;
- one broken adapter is isolated and disabled without breaking NEH.

The exact cache invalidation mechanism will be chosen during implementation after profiling the available client inventory-change signals. No fixed tick interval is mandated by this plan.

## 7. Performance rules

Hard architectural rules:

- no registry enumeration in render callbacks;
- no config file parsing in render callbacks;
- no translation discovery in render callbacks;
- no reflection scan across loaded classes;
- no deep nested-container traversal per frame;
- no allocation-heavy reconstruction of the full rule graph per frame.

Use event-driven invalidation where public callbacks exist. Where polling is unavoidable, poll compact primitive/state fingerprints and recompute only dependent predicates.

Maintain dependency metadata from condition types to context fields. Example: a dimension-only ruleset does not request inventory indexing.

The render path consumes a precomputed RenderState containing final visible elements.

Performance diagnostics should measure:

- context refresh duration;
- rule evaluation duration;
- number of evaluated rules;
- number of inventory selectors;
- nested adapter calls;
- number of visible HUD elements.

No performance target in milliseconds is fixed until a working prototype can be profiled on a real modpack.

## 8. Configuration

Use a versioned, human-readable configuration format.

Separate:

- user preferences;
- hint-pack/rule definitions;
- runtime caches, if any.

Caches must be disposable and reconstructable. Persistent configuration must not depend on cache contents.

Proposed logical files:

- neh.json or neh.json5: user/global preferences;
- hints/*.json or *.json5: rule and HUD definitions;
- cache/: optional disposable discovery cache.

Before implementation, choose JSON, JSON5, or another format based on the selected config library and comment-preservation requirements.

Each config document includes schema_version.

Unknown fields should be preserved where the parser permits it. Unknown condition/action types must produce a validation error for that rule without crashing the client.

## 9. Configuration UI

YACL is the preferred candidate for standard settings because a Fabric 3.8.1 build declares support for Minecraft 1.21.6 through 1.21.8.

Mod Menu integration is optional and should only provide an entry point to the NEH config screen.

The first config UI should cover:

- global enable/disable;
- preset/style controls;
- debug enable/disable;
- element placement editor;
- rule enable/disable;
- validation errors;
- unresolved references;
- import/reload.

A complete visual rule-builder is not required for the first development milestone. Hand-editable validated config plus a practical settings UI is acceptable.

## 10. Rendering

For Minecraft 1.21.8, use the Fabric-supported HUD element API and the rendering primitives appropriate to 1.21.8.

Do not port old PoseStack assumptions blindly. The 1.21.8 GUI/HUD rendering changes are treated as a version boundary.

Rendering responsibilities:

- layout only final visible elements;
- clip or skip content that would leave the safe screen region;
- account for GUI scale;
- respect configured anchor and alignment;
- avoid changing vanilla render state globally;
- avoid mixins when Fabric's HUD APIs are sufficient.

Vanilla HUD displacement, including raising the hotbar, is deferred. It may be implemented later as an explicit compatibility-tested option, not as a prerequisite for NEH's hint system.

## 11. Missing and stale data

All external references are soft dependencies unless explicitly documented otherwise.

Cases:

- mod removed;
- mod renamed;
- keybinding removed or translation key changed;
- item removed;
- dimension removed;
- screen class renamed;
- handled-menu ID changed;
- adapter target version unsupported;
- malformed user configuration;
- duplicate element/rule IDs.

Required behavior:

- preserve recoverable user configuration;
- disable or false-evaluate only the affected rule/reference;
- produce one bounded diagnostic message instead of log spam;
- never substitute by fuzzy name matching automatically;
- expose repair information in diagnostics/config UI;
- continue rendering independent valid hints.

Config migrations are explicit and keyed by schema_version.

## 12. Abuse and fault resistance

NEH must tolerate:

- rapidly opening and closing screens;
- fast dimension changes;
- repeated config reload requests;
- hundreds of rules;
- missing translations;
- keys being rebound while a world is open;
- malformed hint packs;
- cyclic logical structures if a serialized format can express references;
- adapter exceptions;
- empty worlds/client title screen;
- resource reloads while configuration is open.

Parser and validator limits should cap structure depth and collection sizes if external hint packs become shareable.

Debug logging requires rate limiting for recurring failures.

## 13. Compatibility policy

Core NEH must depend only on Minecraft, Fabric Loader, Fabric API, and the selected config/UI library.

Compatibility levels:

- Tier 0: vanilla/Fabric behavior through public APIs.
- Tier 1: generic compatibility through registries, key mappings, screens, and standard inventories.
- Tier 2: explicit adapters for named mods such as Traveler's Backpack.
- Tier 3: unsupported private internals. NEH does not patch these by default.

Adapters should declare the mod IDs and version range they were tested against.

## 14. Multi-version strategy

Start with Minecraft 1.21.8 only.

Do not introduce a multi-version build system until:

1. the 1.21.8 architecture is stable;
2. a second target version is selected;
3. the source differences are measured;
4. shared-code percentage is high enough to justify one repository.

Stonecutter is the preferred candidate if the second target needs isolated source substitutions. It supports multiple Minecraft versions in one codebase using version-specific source preprocessing.

The 1.21.8 HUD rendering change means earlier 1.21.x versions must not be declared compatible based only on dependency metadata.

Version adapters should isolate Minecraft-version differences from the rule engine and config model.

## 15. Dependencies

Preferred:

- Fabric API: required runtime API.
- YACL: candidate config UI/library.
- Mod Menu: optional integration only.

Avoid adding a library for functionality already small and stable in NEH.

A dependency must provide one of:

- difficult UI/config work that would otherwise be duplicated;
- maintained cross-version abstraction;
- testing/build functionality that materially reduces maintenance.

Do not add a general UI framework solely for drawing NEH's small HUD elements.

## 16. Testing and quality gates

Required before the first stable release:

### Unit tests

Use Fabric Loader JUnit where Minecraft classes/runtime transformation are required.

Test:

- condition tree evaluation;
- rule priority and override behavior;
- unresolved reference handling;
- config validation;
- config migration;
- anchor and alignment math;
- inventory query indexing;
- adapter exception isolation.

### Game/client tests

Use Fabric/Minecraft GameTest where an actual client is needed.

Test:

- world join/leave;
- dimension change;
- opening/closing screens;
- keybinding rebinding reflected in hint output;
- HUD rendering on representative GUI scales;
- config reload while running;
- missing optional mod behavior.

Client screenshots may be retained as CI artifacts for rendering failures.

### Static checks

Adopt one deterministic formatter and one static analysis/lint path. The exact Java formatter is selected when the Gradle scaffold is created.

CI must fail on:

- formatting violations;
- compilation errors;
- unit-test failure;
- configured GameTest failure;
- invalid generated metadata.

### Production smoke test

Use Loom production run tasks for at least one automated client smoke path once the mod is runnable.

## 17. Branch and release policy

- main: canonical, reviewable state; stable releases are cut manually from here.
- dev: integration branch; successful pushes may produce development/nightly artifacts.
- feature branches: optional, merge into dev or main according to project maturity.

Stable release:

- manually dispatched;
- version must match project metadata;
- tag must not already exist;
- clean build and tests required;
- GitHub release created only after checks pass.

Development/nightly release:

- generated from dev;
- clearly marked prerelease;
- unique identifier derived from project version and commit;
- may replace or accumulate according to workflow retention policy;
- must never use the stable version tag.

The workflow structure should be adapted from dev2pew/TreasureMapFix, but NEH's build matrix and test gates will be added after the Gradle scaffold exists.

## 18. Reference-source policy

Reference projects are used for architecture study, interoperability research, and behavior comparison.

Before copying any source:

- verify repository license;
- verify the exact file is covered by that license;
- record source repository and commit/tag;
- preserve required notices;
- prefer independent implementation from documented behavior where licensing is unclear.

Visual behavior may be reproduced independently from observation. Source code from all-rights-reserved projects must not be copied.

## 19. Implementation milestones

M0: repository and design contract.
- README.
- technical plan.
- source checklist.
- dev branch.

M1: minimal Fabric 1.21.8 scaffold.
- client initializer.
- config loading.
- CI build.
- formatter.
- unit-test harness.

M2: keybinding catalog and static HUD.
- enumerate standard key mappings.
- stable keybinding selectors.
- text/key hint rendering.
- anchors, offsets, alignment.
- global enable/disable.
- diagnostics.

M3: rule engine and core context.
- held items.
- dimension.
- active screen.
- handled-menu ID where available.
- logical condition trees.
- rule priority/override behavior.

M4: inventory predicates.
- player inventory query index.
- vanilla nested-container support where public APIs permit.
- cache/invalidation profiling.

M5: compatibility adapters.
- Traveler's Backpack adapter as first reference integration.
- adapter API documentation.
- failure isolation.

M6: configuration UX.
- YACL settings.
- optional Mod Menu entry.
- element placement editor.
- unresolved-reference diagnostics.

M7: automated client tests and development releases.
- GameTests.
- production smoke task.
- dev prerelease workflow.

M8: second-version feasibility study.
- choose one additional Minecraft version.
- diff API/rendering requirements.
- adopt Stonecutter only if maintenance cost is lower than separate branches.

## 20. Change-control rule

Any new feature proposed during implementation must be classified as one of:

- required by an existing section of this plan;
- defect fix;
- compatibility adapter;
- deferred feature.

A deferred feature does not enter the active milestone unless it blocks correctness, compatibility, or testing.

Update this document before implementing a change that alters the rule model, configuration schema, dependency policy, compatibility tiers, branch policy, or performance model.
