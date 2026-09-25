# CI/CD Plan

NEH uses two release paths: automatic nightly prereleases from `dev`, and manual stable releases from `main`.

## Branches

- `dev`: primary integration branch and source of automatic nightly prereleases.
- `main`: stable-release branch.
- feature or compatibility branches: temporary or task-specific; CI runs before they are merged back into `dev`.

## Supported build matrix

The current shared source tree is tested against:

| Minecraft | Fabric API |
| --- | --- |
| 1.21.8 | 0.136.1+1.21.8 |
| 1.21.7 | 0.129.0+1.21.7 |

YACL `3.8.1+1.21.6-fabric`, Mod Menu `15.0.2`, and the Traveler's Backpack `10.8.4` compile-only adapter target are shared across these two builds.

A target is not considered supported unless both the normal build/unit-test job and the production client GameTest pass for that target.

## CI

Every push and pull request runs:

1. repository checkout;
2. Temurin Java 21;
3. Gradle 9.5.1;
4. Spotless formatting checks;
5. compilation, remapping, and JUnit tests for each supported Minecraft target;
6. production client GameTests for each supported target;
7. short-retention JAR and GameTest screenshot artifacts.

CI does not publish a GitHub release.

## Nightly releases

Pushes to `dev` build and production-test both supported Minecraft targets before replacing the moving `nightly` prerelease.

The release contains one target-specific JAR for Minecraft 1.21.8 and one for Minecraft 1.21.7. The `nightly` tag points at the exact commit that produced those artifacts. A failed build or production client test prevents publication.

## Stable releases

Stable releases use `workflow_dispatch` and remain restricted to `main`.

The workflow checks:

- the selected ref is `main`;
- the input version uses `X.Y.Z`;
- the input version matches `mod_version`;
- the stable tag does not already exist;
- both supported Minecraft builds pass compilation, JUnit, remapping, and production client GameTests;
- one target-specific release JAR is staged for each supported Minecraft version.

The GitHub release is created only after those checks pass.
