# Integration adapters

Status: development contract for Tier 2 compatibility.

NEH uses adapters only for mod-owned state that cannot be read through ordinary Minecraft or Fabric APIs. Core rule evaluation does not load target-mod classes.

## Nested inventory adapter contract

A nested inventory adapter implements `NestedInventoryAdapter` and provides:

- a stable NEH adapter ID;
- the target mod ID;
- a fast `supports(ItemStack)` check;
- the immediate contained `ItemStack` values.

Adapters are called only when an enabled `inventory_contains` rule sets `include_nested` to `true`. Adapter contents are one level deep. NEH does not recursively inspect containers returned by an adapter.

Adapter failures are isolated. If `supports` or `contents` throws a runtime exception or linkage error, NEH disables that adapter for the rest of the client session and continues evaluating independent rules.

## Traveler's Backpack

Adapter ID: `travelersbackpack:backpack_container`.

Target mod ID: `travelersbackpack`.

Initial tested target: Traveler's Backpack Fabric `10.8.4` for Minecraft `1.21.8`.

The adapter reads `ModDataComponents.BACKPACK_CONTAINER` directly from the backpack `ItemStack` and iterates the component's `BackpackContainerContents#getItems()` result. It does not construct `BackpackWrapper`, open a menu, or inspect private fields.

The Traveler's Backpack dependency is compile-only. NEH starts normally when the mod is absent. Registration is guarded by Fabric Loader's mod-presence check before the adapter class is instantiated.

## Adding another adapter

A new adapter should:

1. use a public API or stable registered component owned by the target mod;
2. avoid reflection over arbitrary mod internals;
3. avoid recursive nested-container traversal;
4. avoid mutating the target `ItemStack` or container state;
5. document the tested target mod version;
6. remain behind a target-mod presence gate;
7. fail independently from other adapters.

A compatibility change that requires private bytecode patches belongs outside Tier 2 and should not be added to the default adapter set.
