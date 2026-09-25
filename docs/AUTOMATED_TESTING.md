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

The production client suite also installs a temporary hint pack and verifies runtime behavior against the real client:

- keybinding rebinding updates the cached hint output without rebuilding the catalog;
- player-owned group anchor, offset, and scale changes reach render state while the world is running;
- off-hand and each armor slot activate and deactivate independent rules;
- an inventory rule added by runtime hint-pack reload observes newly acquired items;
- an overworld-to-Nether-to-overworld transition updates the dimension rule;
- test-owned config and hint-pack files are restored or removed before shutdown.

Screen transitions remain pending because the current `screen_class` condition records a runtime binary class name. A stable namespace contract for remapped vanilla screen classes must be defined before a production test can assert creator-facing screen selectors.

Representative Minecraft GUI-scale screenshots also remain pending.
