# Not Enough Hints (NEH)

Context-aware, low-profile HUD hints for Minecraft Java Edition on Fabric.

NEH is intended for heavily customized modpacks where players need temporary, contextual reminders for keybindings, screens, items, dimensions, and other client-visible state. The mod should behave like an unobtrusive console-style hint layer: useful while learning a pack, easy to disable when no longer needed, and conservative about occupying screen space.

## Status

Development is active on the `dev` branch. Automated development builds are prereleases, not stable releases.

Tested Fabric targets: Minecraft Java Edition 1.21.7 and 1.21.8. Both targets must pass the normal build/test job and the production client GameTest before publication.

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

`main` is the canonical branch. The `dev` branch carries integration work and automatic prereleases.

Development releases are built from the exact `dev` commit after both supported Minecraft targets pass their production client tests. Stable GitHub releases remain a manual workflow from `main`.

No license has been selected for NEH yet. Reference-source licenses must be checked before code is copied or adapted.
