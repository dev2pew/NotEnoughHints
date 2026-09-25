# Multi-version feasibility

Status: M8 feasibility result.

## Selected second target

Minecraft Java Edition 1.21.7 is the second target evaluated beside the initial 1.21.8 build.

The comparison is intentionally narrow. NEH does not claim support for earlier 1.21.x releases from dependency metadata alone.

## Compatibility spike

Branch: `compat/1.21.7-spike`.

Baseline: the 1.21.8 `dev` source at commit `34faba3425a44d92ac8ff21f6696b3db169dd15e`.

Spike commit: `159c634a8353e1998bfcaebd20256bc63f23054b`.

The spike changed only build and mod metadata:

- `minecraft_version`: 1.21.8 to 1.21.7;
- `fabric_api_version`: 0.136.1+1.21.8 to 0.129.0+1.21.7;
- `fabric.mod.json` Minecraft dependency: `~1.21.8` to `~1.21.7`;
- `fabric.mod.json` Fabric API minimum: 0.136.1 to 0.129.0.

No Java source file, hint-pack schema, configuration schema, resource file, or test source required a version-specific edit.

GitHub Actions CI run `36115825719` completed successfully on the spike. The workflow compiled, remapped, ran unit tests, launched the production client GameTest, created a singleplayer world, and exercised the same runtime NEH state tests used by 1.21.8.

## Dependency compatibility

The tested target set is:

| Dependency | Minecraft 1.21.8 | Minecraft 1.21.7 |
| --- | --- | --- |
| Fabric Loader | 0.19.5 | 0.19.5 |
| Fabric API | 0.136.1+1.21.8 | 0.129.0+1.21.7 |
| YACL | 3.8.1+1.21.6-fabric | 3.8.1+1.21.6-fabric |
| Mod Menu | 15.0.2 | 15.0.2 |
| Traveler's Backpack compile-only adapter target | 10.8.4 Fabric | 10.8.4 Fabric |

YACL's 3.8.1 compatibility table maps both Minecraft 1.21.7 and 1.21.8 to its Fabric 1.21.6 target. Mod Menu 15.0.2 declares Minecraft 1.21.6 through 1.21.8 compatibility. Traveler's Backpack 10.8.4 Fabric is published for Minecraft 1.21.7 through 1.21.8.

## Rendering boundary

Fabric documents a Minecraft 1.21.8 GUI/HUD rendering change: HUD matrices moved from PoseStack to Matrix3x2fStack and GUI rendering joined the render-state extraction model.

This is a real version boundary, but the NEH source used by the compatibility spike does not require a source substitution for 1.21.7. The Fabric HUD registration interface and the methods NEH currently calls compile and run on both tested targets.

This result applies to the current implementation. Future rendering work must keep both production client tests green before it can be considered shared code.

## Build-system decision

Stonecutter is not adopted at this stage.

There are currently zero required source substitutions between 1.21.7 and 1.21.8. Adding version preprocessing now would introduce conditional-source machinery without removing any duplicated Java code.

NEH will instead keep one source tree and pass target-specific Minecraft/Fabric metadata to Gradle. CI builds and production client tests both supported targets. If a future target requires actual source substitutions, the Stonecutter decision should be reopened using the concrete diff at that time.

## Support rule

A Minecraft version is supported only when both of these pass for that target:

1. the normal build/unit-test job;
2. the production client GameTest job.

Adding a version to dependency metadata without both gates does not make it a supported NEH target.
