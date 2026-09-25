package io.github.dev2pew.notenoughhints.client.context;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;

import net.fabricmc.loader.api.FabricLoader;

import io.github.dev2pew.notenoughhints.client.keybind.KeyBindingCatalog;
import io.github.dev2pew.notenoughhints.context.ClientContext;
import io.github.dev2pew.notenoughhints.context.EquipmentSlotKey;

public final class ClientContextCollector {
    private final KeyBindingCatalog keyBindingCatalog;
    private final Set<String> loadedModIds;

    public ClientContextCollector(KeyBindingCatalog keyBindingCatalog) {
        this.keyBindingCatalog = keyBindingCatalog;
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
                    loadedModIds,
                    keyBindingCatalog.snapshot().keySet());
        }

        EnumMap<EquipmentSlotKey, String> equipment = new EnumMap<>(EquipmentSlotKey.class);
        equipment.put(EquipmentSlotKey.MAIN_HAND, itemId(client.player.getMainHandItem()));
        equipment.put(EquipmentSlotKey.OFF_HAND, itemId(client.player.getOffhandItem()));
        equipment.put(
                EquipmentSlotKey.HEAD,
                itemId(client.player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.HEAD)));
        equipment.put(
                EquipmentSlotKey.CHEST,
                itemId(client.player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.CHEST)));
        equipment.put(
                EquipmentSlotKey.LEGS,
                itemId(client.player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.LEGS)));
        equipment.put(
                EquipmentSlotKey.FEET,
                itemId(client.player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.FEET)));

        return new ClientContext(
                worldPresent,
                dimensionId,
                screenClassName,
                handledMenuId,
                equipment.get(EquipmentSlotKey.MAIN_HAND),
                equipment.get(EquipmentSlotKey.OFF_HAND),
                equipment,
                loadedModIds,
                keyBindingCatalog.snapshot().keySet());
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
