package io.github.dev2pew.notenoughhints.client.debug;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import io.github.dev2pew.notenoughhints.client.config.NehConfigManager;
import io.github.dev2pew.notenoughhints.client.integration.NestedInventoryAdapterRegistry;
import io.github.dev2pew.notenoughhints.context.ClientContext;
import io.github.dev2pew.notenoughhints.context.EquipmentSlotKey;

public final class DiagnosticsController {
    private final NehConfigManager configManager;
    private final NestedInventoryAdapterRegistry nestedInventoryAdapters;
    private final AtomicReference<List<String>> lines = new AtomicReference<>(List.of());

    public DiagnosticsController(
            NehConfigManager configManager,
            NestedInventoryAdapterRegistry nestedInventoryAdapters) {
        this.configManager = configManager;
        this.nestedInventoryAdapters = nestedInventoryAdapters;
    }

    public void update(ClientContext context) {
        if (!configManager.current().debug()) {
            lines.set(List.of());
            return;
        }

        lines.set(
                List.of(
                        "NEH debug",
                        "dimension: " + display(context.dimensionId()),
                        "screen: " + display(context.screenClassName()),
                        "menu: " + display(context.handledMenuId()),
                        "main_hand: " + display(context.mainHandItemId()),
                        "off_hand: " + display(context.offHandItemId()),
                        "head: " + display(context.equipmentItem(EquipmentSlotKey.HEAD)),
                        "chest: " + display(context.equipmentItem(EquipmentSlotKey.CHEST)),
                        "legs: " + display(context.equipmentItem(EquipmentSlotKey.LEGS)),
                        "feet: " + display(context.equipmentItem(EquipmentSlotKey.FEET)),
                        "keybindings: " + context.keyBindingIds().size(),
                        "mods: " + context.loadedModIds().size(),
                        "nested_adapters: " + nestedInventoryAdapters.activeAdapterIds(),
                        "disabled_adapters: " + nestedInventoryAdapters.disabledAdapterIds()));
    }

    public List<String> lines() {
        return lines.get();
    }

    private static String display(String value) {
        return value.isEmpty() ? "<none>" : value;
    }
}
