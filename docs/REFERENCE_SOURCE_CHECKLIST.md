# Reference Source Checklist

Acquire or provide source snapshots before implementation work that depends on them.

Prefer source archives or repository links pinned to an exact tag/commit. Do not rely on an unversioned working tree when the purpose is to study a particular Minecraft release.

## Priority A

### Controllable

Repository: https://github.com/MrCrayfish/Controllable

Current source branch matching the target: `multiloader/1.21.8`.

Published repository license: MIT.

Purpose:

- study controller-oriented HUD composition;
- study console-like placement decisions;
- inspect how it interacts with the vanilla HUD and hotbar;
- identify whether any public abstraction is reusable without invasive patches.

Need:

- source snapshot from `multiloader/1.21.8` pinned to an exact commit;
- license file from that snapshot;
- build metadata;
- relevant HUD/render classes;
- any dependency/library choices used by that version.

### Traveler's Backpack

Repository: https://github.com/Tiviacz1337/Travelers-Backpack

Current source branch matching the target: `1.21.8-fabric`.

Published Fabric project license: LGPL-3.0.

Purpose:

- first real test of a mod-owned portable inventory/container;
- determine how backpack contents are represented on 1.21.8;
- design the NEH inventory-adapter boundary;
- test behavior when the integration mod is absent or changes.

Need:

- source snapshot from `1.21.8-fabric` pinned to an exact commit;
- license;
- item/storage component classes;
- inventory/container interfaces;
- screen-handler/screen classes;
- API classes intended for other mods.

### ModKeys / ModKyes

Repository: https://github.com/Cehira123/modkeys

Current source branch: `main`. The repository describes the project as Fabric 1.20.1 and includes source plus an MIT LICENSE file.

Purpose:

- study keybinding enumeration;
- study favorites/config persistence;
- study compact HUD presentation;
- identify failure modes caused by removed/changed bindings.

Need:

- repository snapshot pinned to an exact commit;
- license file from the same commit;
- config schema;
- keybinding discovery code;
- HUD renderer;
- config UI.

Previous distribution metadata did not consistently match the repository license. The repository snapshot's license must be preserved in the evidence log before any code is adapted.

## Priority B

### Binders

Project page: https://www.curseforge.com/minecraft/mc-mods/binders

The published project targets Forge 1.20.1 and is listed under the MIT License.

Purpose:

- study declarative condition-driven HUD entries;
- compare its selector and context model with NEH's rule engine;
- inspect its context-aware visibility model and dynamic response to key remapping.

Need:

- source repository linked by the project page;
- exact commit/tag;
- license from the source tree;
- config examples;
- condition evaluation code;
- rendering/layout code.

### VisualKeys or another current 1.21.8 keybinding visualizer

Purpose:

- compare current-version keybinding enumeration and 1.21.8 rendering;
- identify API changes that older reference projects do not show.

Source is useful only if its license permits the intended type of reuse.

## What to send in the next turn

Best next set:

1. Controllable `multiloader/1.21.8` source snapshot or exact commit.
2. Traveler's Backpack `1.21.8-fabric` source snapshot or exact commit.
3. ModKeys `main` source snapshot or exact commit.
4. Binders source snapshot or exact source repository/tag.

A repository URL plus exact commit is sufficient. Uploading ZIP archives is also fine.

If the intended test modpack already has exact versions of these mods, include those version numbers as well. A reference source should preferably match the binary that will be tested.

## Evidence log to create during implementation

For every borrowed or adapted implementation idea, record:

- project;
- repository URL;
- commit/tag;
- file/path;
- license;
- what NEH used from it;
- whether code was copied, adapted, or independently reimplemented.

This log should live in `docs/REFERENCE_EVIDENCE.md` once implementation starts.
