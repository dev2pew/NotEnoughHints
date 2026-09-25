# Reference Source Checklist

Acquire or provide source snapshots before implementation work that depends on them.

Prefer source archives or repository links pinned to an exact tag/commit. Do not rely on an unversioned working tree when the purpose is to study a particular Minecraft release.

## Priority A

### Controllable

Purpose:

- study controller-oriented HUD composition;
- study console-like placement decisions;
- inspect how it interacts with the vanilla HUD and hotbar;
- identify whether any public abstraction is reusable without invasive patches.

Need:

- exact source version closest to Minecraft 1.21.8;
- license file;
- build metadata;
- relevant HUD/render classes;
- any dependency/library choices used by that version.

Do not copy rendering code until its license and version are verified.

### Traveler's Backpack

Purpose:

- first real test of a mod-owned portable inventory/container;
- determine how backpack contents are represented on 1.21.8;
- design the NEH inventory-adapter boundary;
- test behavior when the integration mod is absent or changes.

Need:

- Fabric source for the exact 1.21.8-compatible release if available;
- license;
- item/storage component classes;
- inventory/container interfaces;
- screen-handler/screen classes;
- API classes intended for other mods.

### ModKeys / ModKyes

Purpose:

- study keybinding enumeration;
- study favorites/config persistence;
- study compact HUD presentation;
- identify failure modes caused by removed/changed bindings.

Need:

- repository snapshot used for the 1.20.1 Fabric build;
- license file from the same commit;
- config schema;
- keybinding discovery code;
- HUD renderer;
- config UI.

Note: previous public metadata showed a license inconsistency between repository and distribution listing. Treat the source as reference-only until the exact commit's licensing is verified.

## Priority B

### Binders

Purpose:

- study declarative condition-driven HUD entries;
- compare its selector and context model with NEH's rule engine.

Need:

- source repository if available;
- exact license;
- config examples;
- condition evaluation code;
- rendering/layout code.

### VisualKeys or another current 1.21.8 keybinding visualizer

Purpose:

- compare current-version keybinding enumeration and 1.21.8 rendering;
- identify API changes that older reference projects do not show.

Source is useful only if the license permits inspection/reuse.

## What to send in the next turn

For each project, provide one of:

1. a source ZIP/tar archive;
2. a repository URL plus exact tag or commit;
3. the built JAR only when source is unavailable and the goal is behavior/resource inspection rather than source reuse.

Best next set:

- Controllable source;
- Traveler's Backpack source;
- ModKeys source;
- Binders source if available.

Also include the exact Minecraft/mod versions currently used in the intended test modpack when known.

## Evidence log to create during implementation

For every borrowed or adapted implementation idea, record:

- project;
- repository URL;
- commit/tag;
- file/path;
- license;
- what NEH used from it;
- whether code was copied, adapted, or independently reimplemented.

This log should live in docs/REFERENCE_EVIDENCE.md once implementation starts.
