# Architecture Decisions

This file records decisions that should not be casually reversed during implementation.

## ADR-001: Client-side first

NEH is a client-side hint system. The initial architecture does not require a server component.

## ADR-002: Declarative rules, no embedded scripting

Version 1 uses a typed condition tree and typed HUD actions. No JavaScript, Lua, expression-language execution, reflection expressions, or arbitrary commands are loaded from hint configuration.

Reason: validation, deterministic behavior, compatibility, and fault isolation matter more than unlimited expressiveness.

## ADR-003: Runtime screen observation

NEH will not claim to enumerate every possible GUI at startup. It records the active Screen class at runtime and resolves registry-backed handled-menu identifiers where available.

## ADR-004: Adapter boundary for mod-owned inventories

Vanilla player inventory is supported generically. Nested and mod-owned storage uses bounded built-in logic or explicit adapters.

Traveler's Backpack is the first planned adapter test.

## ADR-005: Demand-driven context

The active rules determine which state NEH collects. Inventory scanning is not performed when no active condition needs inventory data.

## ADR-006: Render precomputed state

Rule evaluation and expensive discovery do not occur inside the HUD render callback. Rendering consumes final visible element state.

## ADR-007: Minecraft 1.21.8 first

The first implementation targets Fabric 1.21.8 only. Multi-version tooling is introduced only after measuring a second target's source differences.

## ADR-008: YACL candidate, Mod Menu optional

YACL is the preferred settings/config UI candidate for 1.21.8. Mod Menu may expose the settings entry point but must not be required for NEH to load.

## ADR-009: Stable releases remain manual

Stable releases are manually dispatched from main. The dev branch may receive automatic prerelease artifacts after the project is buildable.

## ADR-010: Missing references fail locally

A removed mod, item, keybinding, screen, or dimension invalidates only the affected selector/rule. NEH preserves the configuration and reports the unresolved reference.


## ADR-011: Equipment state is first-class context

NEH treats player equipment as direct client context rather than as a generic inventory search.

Version 1 must expose the current main hand, off hand, head, chest, legs, and feet ItemStacks to the rule engine when configured rules require them. The typed `equipment_slot_item` predicate checks one exact slot.

Armor checks therefore distinguish equipped items from matching items merely carried in the player's inventory. Equipment-only rules must not trigger full inventory or nested-container scans.
