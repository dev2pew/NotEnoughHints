package io.github.dev2pew.notenoughhints.client.context;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.item.component.ItemContainerContents;

import net.fabricmc.loader.api.FabricLoader;

import io.github.dev2pew.notenoughhints.client.integration.NestedInventoryAdapterRegistry;
import io.github.dev2pew.notenoughhints.client.keybind.KeyBindingCatalog;
import io.github.dev2pew.notenoughhints.context.ClientContext;
import io.github.dev2pew.notenoughhints.context.EquipmentSlotKey;
import io.github.dev2pew.notenoughhints.context.InventoryScope;
import io.github.dev2pew.notenoughhints.context.InventorySnapshot;

public final class ClientContextCollector {
    private final KeyBindingCatalog keyBindingCatalog;
    private final Set<String> loadedModIds;
    private volatile boolean collectInventory;
    private volatile boolean collectNestedInventory;
    private final NestedInventoryAdapterRegistry nestedInventoryAdapters;

    public ClientContextCollector(
            KeyBindingCatalog keyBindingCatalog,
            boolean collectInventory,
            boolean collectNestedInventory,
            NestedInventoryAdapterRegistry nestedInventoryAdapters) {
        this.keyBindingCatalog = keyBindingCatalog;
        this.collectInventory = collectInventory;
        this.collectNestedInventory = collectNestedInventory;
        this.nestedInventoryAdapters = nestedInventoryAdapters;
        this.loadedModIds =
                FabricLoader.getInstance().getAllMods().stream()
                        .map(container -> container.getMetadata().getId())
                        .collect(Collectors.toUnmodifiableSet());
    }

    public void setInventoryRequirements(
            boolean collectInventory, boolean collectNestedInventory) {
        this.collectInventory = collectInventory;
        this.collectNestedInventory = collectNestedInventory;
    }

    public ClientContext collect(Minecraft client) {
        boolean worldPresent = client.level != null;
        String dimensionId =
                client.level == null ? "" : client.level.dimension().location().toString();
        String screenClassName =
                client.screen == null
                        ? ""
                        : ScreenClassSelector.canonicalize(client.screen.getClass());
        String handledMenuId = handledMenuId(client);

        if (client.player == null) {
            return new ClientContext(
                    worldPresent,
                    dimensionId,
                    screenClassName,
                    handledMenuId,
                    "",
                    "",
                    -1,
                    Map.of(),
                    InventorySnapshot.empty(),
                    loadedModIds,
                    keyBindingCatalog.snapshot().keySet());
        }

        EnumMap<EquipmentSlotKey, String> equipment = new EnumMap<>(EquipmentSlotKey.class);
        equipment.put(EquipmentSlotKey.MAIN_HAND, itemId(client.player.getMainHandItem()));
        equipment.put(EquipmentSlotKey.OFF_HAND, itemId(client.player.getOffhandItem()));
        equipment.put(
                EquipmentSlotKey.HEAD, itemId(client.player.getItemBySlot(EquipmentSlot.HEAD)));
        equipment.put(
                EquipmentSlotKey.CHEST, itemId(client.player.getItemBySlot(EquipmentSlot.CHEST)));
        equipment.put(
                EquipmentSlotKey.LEGS, itemId(client.player.getItemBySlot(EquipmentSlot.LEGS)));
        equipment.put(
                EquipmentSlotKey.FEET, itemId(client.player.getItemBySlot(EquipmentSlot.FEET)));

        InventorySnapshot inventory =
                collectInventory
                        ? collectInventory(client, collectNestedInventory)
                        : InventorySnapshot.empty();

        return new ClientContext(
                worldPresent,
                dimensionId,
                screenClassName,
                handledMenuId,
                equipment.get(EquipmentSlotKey.MAIN_HAND),
                equipment.get(EquipmentSlotKey.OFF_HAND),
                client.player.getInventory().getSelectedSlot(),
                equipment,
                inventory,
                loadedModIds,
                keyBindingCatalog.snapshot().keySet());
    }

    private InventorySnapshot collectInventory(Minecraft client, boolean includeNested) {
        Inventory inventory = client.player.getInventory();
        EnumMap<InventoryScope, Map<String, Integer>> direct =
                new EnumMap<>(InventoryScope.class);
        EnumMap<InventoryScope, Map<String, Integer>> nested =
                new EnumMap<>(InventoryScope.class);
        for (InventoryScope scope : InventoryScope.values()) {
            direct.put(scope, new HashMap<>());
            nested.put(scope, new HashMap<>());
        }

        for (int slot = 0; slot < Inventory.INVENTORY_SIZE; slot++) {
            ItemStack stack = inventory.getItem(slot);
            InventoryScope scope =
                    slot < Inventory.SELECTION_SIZE
                            ? InventoryScope.HOTBAR
                            : InventoryScope.MAIN_INVENTORY;
            addDirectAndNested(
                    direct,
                    nested,
                    stack,
                    includeNested,
                    scope,
                    InventoryScope.PLAYER_INVENTORY);
        }

        for (EquipmentSlot slot :
                new EquipmentSlot[] {
                    EquipmentSlot.HEAD,
                    EquipmentSlot.CHEST,
                    EquipmentSlot.LEGS,
                    EquipmentSlot.FEET
                }) {
            ItemStack stack = client.player.getItemBySlot(slot);
            addDirectAndNested(
                    direct,
                    nested,
                    stack,
                    includeNested,
                    InventoryScope.ARMOR,
                    InventoryScope.PLAYER_INVENTORY);
        }

        ItemStack offhand = client.player.getOffhandItem();
        addDirectAndNested(
                direct,
                nested,
                offhand,
                includeNested,
                InventoryScope.OFFHAND,
                InventoryScope.PLAYER_INVENTORY);

        return new InventorySnapshot(direct, nested);
    }

    private void addDirectAndNested(
            Map<InventoryScope, Map<String, Integer>> direct,
            Map<InventoryScope, Map<String, Integer>> nested,
            ItemStack stack,
            boolean includeNested,
            InventoryScope... scopes) {
        for (InventoryScope scope : scopes) {
            add(direct.get(scope), stack);
            add(nested.get(scope), stack);
        }

        if (!includeNested || stack.isEmpty()) {
            return;
        }

        ItemContainerContents container = stack.get(DataComponents.CONTAINER);
        if (container != null) {
            for (ItemStack nestedStack : container.nonEmptyItems()) {
                addToScopes(nested, nestedStack, scopes);
            }
        }

        BundleContents bundle = stack.get(DataComponents.BUNDLE_CONTENTS);
        if (bundle != null) {
            for (ItemStack nestedStack : bundle.items()) {
                addToScopes(nested, nestedStack, scopes);
            }
        }

        nestedInventoryAdapters.visitContents(
                stack, nestedStack -> addToScopes(nested, nestedStack, scopes));
    }

    private static void addToScopes(
            Map<InventoryScope, Map<String, Integer>> counts,
            ItemStack stack,
            InventoryScope... scopes) {
        for (InventoryScope scope : scopes) {
            add(counts.get(scope), stack);
        }
    }

    private static void add(Map<String, Integer> counts, ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        counts.merge(itemId(stack), stack.getCount(), Integer::sum);
    }

    private static String handledMenuId(Minecraft client) {
        if (!(client.screen instanceof AbstractContainerScreen<?> containerScreen)) {
            return "";
        }

        return BuiltInRegistries.MENU.getKey(containerScreen.getMenu().getType()).toString();
    }

    private static String itemId(ItemStack stack) {
        if (stack.isEmpty()) {
            return "";
        }

        return BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
    }
}
