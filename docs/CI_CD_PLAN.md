# CI/CD Plan

NEH will reuse the release discipline from `dev2pew/TreasureMapFix` without copying assumptions that only fit that project.

## Branches

- `main`: canonical branch and only source of stable releases.
- `dev`: integration branch and source of automatic development builds.
- feature branches: optional; must pass CI before merge.

## CI

Once the Gradle scaffold exists, every push and pull request will run:

1. checkout;
2. Temurin Java 21 setup for Minecraft 1.21.8;
3. Gradle setup;
4. formatter/checkstyle or equivalent deterministic style check;
5. `./gradlew build --no-daemon --stacktrace`;
6. unit tests;
7. configured GameTests/client tests when available;
8. upload non-sources/non-javadoc development JARs as short-retention artifacts.

The workflow must not publish a GitHub Release for ordinary `main` pushes.

## Development releases

Pushes to `dev` may publish a GitHub prerelease after the project becomes buildable.

Required properties:

- prerelease flag enabled;
- unique tag containing the base mod version and commit/build identifier;
- artifact built from the exact commit being tagged;
- failed tests prevent publication;
- development tags never overlap stable semantic-version tags;
- retention/cleanup policy prevents unbounded nightly release accumulation.

A single moving `dev` prerelease is acceptable if GitHub's release/tag handling remains deterministic. Otherwise use immutable prerelease tags and periodically prune them.

## Stable releases

Stable releases remain manual through `workflow_dispatch`, following the working pattern in TreasureMapFix.

Required checks:

- workflow must run from `main`;
- input version must use the chosen semantic-version format;
- input version must match the project metadata;
- target tag must not already exist;
- clean build and all configured tests must pass;
- exactly the expected release JAR set must be selected;
- GitHub Release is created only after successful validation;
- release is marked latest only for stable versions.

## Why workflows are not committed yet

The repository currently contains planning documents only. Adding a build workflow before `gradlew`, Gradle metadata, and source sets exist would produce guaranteed CI failures.

The first implementation commit will add the Fabric 1.21.8 scaffold and CI together. The stable release workflow follows after artifact naming and the test task graph are fixed.
