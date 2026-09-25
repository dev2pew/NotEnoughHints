package io.github.dev2pew.notenoughhints.context;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

public record ClientContext(
        boolean worldPresent,
        String dimensionId,
        String screenClassName,
        String handledMenuId,
        String mainHandItemId,
        String offHandItemId,
        int selectedHotbarSlot,
        Map<EquipmentSlotKey, String> equipmentItems,
        InventorySnapshot inventory,
        Set<String> loadedModIds,
        Set<String> keyBindingIds) {

    public ClientContext {
        dimensionId = nonNull(dimensionId);
        screenClassName = nonNull(screenClassName);
        handledMenuId = nonNull(handledMenuId);
        mainHandItemId = nonNull(mainHandItemId);
        offHandItemId = nonNull(offHandItemId);
        if (selectedHotbarSlot < -1 || selectedHotbarSlot > 8) {
            throw new IllegalArgumentException("selectedHotbarSlot must be -1 or between 0 and 8");
        }
        equipmentItems = Map.copyOf(equipmentItems);
        Objects.requireNonNull(inventory, "inventory");
        Objects.requireNonNull(loadedModIds, "loadedModIds");
        Objects.requireNonNull(keyBindingIds, "keyBindingIds");
    }

    public ClientContext(
            boolean worldPresent,
            String dimensionId,
            String screenClassName,
            String handledMenuId,
            String mainHandItemId,
            String offHandItemId,
            Map<EquipmentSlotKey, String> equipmentItems,
            InventorySnapshot inventory,
            Set<String> loadedModIds,
            Set<String> keyBindingIds) {
        this(
                worldPresent,
                dimensionId,
                screenClassName,
                handledMenuId,
                mainHandItemId,
                offHandItemId,
                -1,
                equipmentItems,
                inventory,
                loadedModIds,
                keyBindingIds);
    }

    public String equipmentItem(EquipmentSlotKey slot) {
        return equipmentItems.getOrDefault(slot, "");
    }

    private static String nonNull(String value) {
        return value == null ? "" : value;
    }
}
