# Configuration UI

Status: first M6 settings slice.

NEH uses YetAnotherConfigLib (YACL) for the settings screen while keeping NEH's existing JSON serializer as the source of truth for `neh.json`.

Target dependency:

- YACL `3.8.1+1.21.6-fabric`, which supports Minecraft 1.21.6 through 1.21.8;
- Mod Menu `15.0.2` as an optional entry point.

YACL is a required client dependency because the NEH config-screen implementation references its API directly. Mod Menu remains optional and is declared through the `modmenu` entry point plus Fabric's `suggests` metadata.

NEH also registers an unbound `Open NEH settings` key mapping. Players who do not install Mod Menu can assign that mapping in Minecraft's Controls screen and open the same YACL screen directly.

## First settings screen

The current screen edits:

- global enable or disable;
- debug HUD;
- binding-label visibility;
- NEH HUD scale;
- NEH HUD opacity;
- per-rule player enable or disable overrides;
- per-group visibility, anchor, horizontal offset, and vertical offset.

It also shows the currently loaded hint-group/rule counts and provides a manual hint-pack reload action. Reload recomputes the rule engine's inventory dependencies before the next context snapshot, so adding or removing an `inventory_contains` rule does not leave stale observation requirements behind. Invalid hint-pack files are isolated, recorded as load issues, and do not prevent independent valid files from loading.

`neh.json` schema version 3 stores player-owned `disabled_rule_ids` and `group_overrides`. Schema version 1 files migrate through version 2 before version 3; version 2 files add an empty `group_overrides` object. Migrated files are written back through the same validated save path.

Saving the screen writes a complete schema-versioned `NehConfig` through `NehConfigManager`. The manager validates the replacement first, writes a sibling temporary file, and then replaces `neh.json`. The in-memory configuration changes only after the file replacement succeeds.

Because runtime HUD controllers read `NehConfigManager.current()`, saved presentation changes apply on subsequent client ticks without reparsing files from the render callback.

## Remaining M6 work

The first screen does not complete M6. Remaining work includes:

- detailed validation and unresolved-reference reporting;
- import workflow;
- localized strings beyond English.

A full visual rule builder remains outside the first-release requirement.
