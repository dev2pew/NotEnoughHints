# Configuration UI

Status: first M6 settings slice.

NEH uses YetAnotherConfigLib (YACL) for the settings screen while keeping NEH's existing JSON serializer as the source of truth for `neh.json`.

Target dependency:

- YACL `3.8.1+1.21.6-fabric`, which supports Minecraft 1.21.6 through 1.21.8;
- Mod Menu `15.0.2` as an optional entry point.

YACL is a required client dependency because the NEH config-screen implementation references its API directly. Mod Menu remains optional and is declared through the `modmenu` entry point plus Fabric's `suggests` metadata.

## First settings screen

The current screen edits:

- global enable or disable;
- debug HUD;
- binding-label visibility;
- NEH HUD scale;
- NEH HUD opacity.

Saving the screen writes a complete schema-versioned `NehConfig` through `NehConfigManager`. The manager validates the replacement first, writes a sibling temporary file, and then replaces `neh.json`. The in-memory configuration changes only after the file replacement succeeds.

Because runtime HUD controllers read `NehConfigManager.current()`, saved presentation changes apply on subsequent client ticks without reparsing files from the render callback.

## Remaining M6 work

The first screen does not complete M6. Remaining work includes:

- hint-group placement editing;
- per-rule enable or disable controls;
- validation and unresolved-reference reporting;
- hint-pack reload controls;
- import or reload workflow;
- a direct NEH access path when Mod Menu is absent, if required by the final UX;
- localized strings beyond English.

A full visual rule builder remains outside the first-release requirement.
