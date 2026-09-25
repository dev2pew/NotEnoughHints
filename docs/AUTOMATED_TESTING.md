# Automated client testing

NEH uses Fabric API's client GameTest support for tests that require a real Minecraft client.

The first production smoke test runs through Loom's `ClientProductionRunTask`, not the development client. It verifies:

- the remapped NEH mod starts;
- required YACL is present;
- optional Mod Menu is absent;
- optional Traveler's Backpack is absent;
- a singleplayer world can be created and rendered;
- the client returns to the no-world state after the test world closes;
- a screenshot is retained as a CI artifact.

Run locally with:

```text
gradle runProductionClientGameTest
```

The production task enables Fabric's client GameTest mode and disables the network synchronizer because Fabric documents an intermittent GitHub Actions failure in that synchronizer.

This smoke test is not the full M7 test matrix. Later client tests still need focused coverage for keybinding rebinding, equipment changes, screen transitions, runtime configuration reload, and representative HUD scales.
