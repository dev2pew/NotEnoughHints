package io.github.dev2pew.notenoughhints.context;

import java.util.Map;
import java.util.Set;

public record ClientContext(
        boolean worldPresent,
        String dimensionId,
        String screenClassName,
        String handledMenuId,
        String mainHandItemId,
        String offHandItemId,
        Map<EquipmentSlotKey, String> equipmentItems,
        Set<String> loadedModIds,
        Set<String> keyBindingIds) {

    public ClientContext {
        dimensionId = nonNull(dimensionId);
        screenClassName = nonNull(screenClassName);
        handledMenuId = nonNull(handledMenuId);
        mainHandItemId = nonNull(mainHandItemId);
        offHandItemId = nonNull(offHandItemId);
        equipmentItems = Map.copyOf(equipmentItems);
        loadedModIds = Set.copyOf(loadedModIds);
        keyBindingIds = Set.copyOf(keyBindingIds);
    }

    public String equipmentItem(EquipmentSlotKey slot) {
        return equipmentItems.getOrDefault(slot, "");
    }

    private static String nonNull(String value) {
        return value == null ? "" : value;
    }
}
