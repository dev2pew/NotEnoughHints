package io.github.dev2pew.notenoughhints.client.context;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import net.fabricmc.loader.api.FabricLoader;

import io.github.dev2pew.notenoughhints.client.keybind.KeyBindingCatalog;
import io.github.dev2pew.notenoughhints.context.ClientContext;
import io.github.dev2pew.notenoughhints.context.EquipmentSlotKey;
import io.github.dev2pew.notenoughhints.context.InventoryScope;
import io.github.dev2pew.notenoughhints.context.InventorySnapshot;

public final class ClientContextCollector {
    private final KeyBindingCatalog keyBindingCatalog;
    private final Set<String> loadedModIds;
    private final boolean collectInventory;

    public ClientContextCollector(KeyBindingCatalog keyBindingCatalog, boolean collectInventory) {
        this.keyBindingCatalog = keyBindingCatalog;
        this.collectInventory = collectInventory;
        this.loadedModIds =
                FabricLoader.getInstance().getAllMods().stream()
                        .map(container -> container.getMetadata().getId())
                        .collect(Collectors.toUnmodifiableSet());
    }

    public ClientContext collect(Minecraft client) {
        boolean worldPresent = client.level != null;
        String dimensionId =
                client.level == null ? "" : client.level.dimension().location().toString();
        String screenClassName = client.screen == null ? "" : client.screen.getClass().getName();
        String handledMenuId = handledMenuId(client);

        if (client.player == null) {
            return new ClientContext(
                    worldPresent,
                    dimensionId,
                    screenClassName,
                    handledMenuId,
                    "",
                    "",
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
                collectInventory ? collectInventory(client) : InventorySnapshot.empty();

        return new ClientContext(
                worldPresent,
                dimensionId,
                screenClassName,
                handledMenuId,
                equipment.get(EquipmentSlotKey.MAIN_HAND),
                equipment.get(EquipmentSlotKey.OFF_HAND),
                equipment,
                inventory,
                loadedModIds,
                keyBindingCatalog.snapshot().keySet());
    }

    private static InventorySnapshot collectInventory(Minecraft client) {
        Inventory inventory = client.player.getInventory();
        EnumMap<InventoryScope, Map<String, Integer>> counts =
                new EnumMap<>(InventoryScope.class);
        for (InventoryScope scope : InventoryScope.values()) {
            counts.put(scope, new HashMap<>());
        }

        for (int slot = 0; slot < Inventory.INVENTORY_SIZE; slot++) {
            ItemStack stack = inventory.getItem(slot);
            InventoryScope scope =
                    slot < Inventory.SELECTION_SIZE
                            ? InventoryScope.HOTBAR
                            : InventoryScope.MAIN_INVENTORY;
            add(counts.get(scope), stack);
            add(counts.get(InventoryScope.PLAYER_INVENTORY), stack);
        }

        for (EquipmentSlot slot :
                new EquipmentSlot[] {
                    EquipmentSlot.HEAD,
                    EquipmentSlot.CHEST,
                    EquipmentSlot.LEGS,
                    EquipmentSlot.FEET
                }) {
            ItemStack stack = client.player.getItemBySlot(slot);
            add(counts.get(InventoryScope.ARMOR), stack);
            add(counts.get(InventoryScope.PLAYER_INVENTORY), stack);
        }

        ItemStack offhand = client.player.getOffhandItem();
        add(counts.get(InventoryScope.OFFHAND), offhand);
        add(counts.get(InventoryScope.PLAYER_INVENTORY), offhand);

        return new InventorySnapshot(counts);
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
