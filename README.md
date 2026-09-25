# Not Enough Hints (NEH)

Context-aware, low-profile HUD hints for Minecraft Java Edition on Fabric.

NEH is intended for heavily customized modpacks where players need temporary, contextual reminders for keybindings, screens, items, dimensions, and other client-visible state. The mod should behave like an unobtrusive console-style hint layer: useful while learning a pack, easy to disable when no longer needed, and conservative about occupying screen space.

## Status

Planning only. No playable build exists yet.

Initial implementation target: Minecraft Java Edition 1.21.8 on Fabric.

## Project contract

Implementation work is governed by [docs/TECHNICAL_PLAN.md](docs/TECHNICAL_PLAN.md). That document fixes the first release scope, performance model, compatibility boundaries, failure behavior, configuration model, test gates, and version-support policy.

Source projects that should be supplied or inspected before implementation are tracked in [docs/REFERENCE_SOURCE_CHECKLIST.md](docs/REFERENCE_SOURCE_CHECKLIST.md).

## Intended capabilities

- Enumerate Minecraft keybindings registered by vanilla and installed mods.
- Display selected keybindings using the player's current bindings.
- Observe active client screens and identify registered handled-menu types.
- Evaluate player state such as dimension, main/off-hand items, equipped armor slots, and inventory contents.
- Support optional adapters for nested or mod-owned inventories.
- Combine conditions with deterministic AND, OR, and NOT logic.
- Render text, key hints, icons, and compact boxes through configurable HUD layouts.
- Provide diagnostics that expose identifiers and evaluated state without requiring a debugger.
- Fail closed when referenced mods, bindings, items, screens, or dimensions disappear.
- Avoid rescanning registries, files, or nested inventories during render frames.

## Repository policy

`main` is the canonical branch. A `dev` branch is used for integration builds once the Gradle project exists.

Development builds will be produced automatically from `dev`. Stable GitHub releases remain a manual workflow from `main`, following the release pattern already used in `dev2pew/TreasureMapFix`.

No license has been selected for NEH yet. Reference-source licenses must be checked before code is copied or adapted.
