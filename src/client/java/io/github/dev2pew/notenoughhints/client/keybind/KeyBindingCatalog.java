package io.github.dev2pew.notenoughhints.client.keybind;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;

public final class KeyBindingCatalog {
    private volatile Map<String, KeyBindingDescriptor> byId = Map.of();

    public void rebuild(Minecraft client) {
        Map<String, KeyBindingDescriptor> discovered = new LinkedHashMap<>();

        for (KeyMapping mapping : client.options.keyMappings) {
            String id = mapping.getName();
            discovered.putIfAbsent(
                    id,
                    new KeyBindingDescriptor(id, mapping.getCategory(), mapping));
        }

        byId = Collections.unmodifiableMap(discovered);
    }

    public Optional<KeyBindingDescriptor> find(String id) {
        return Optional.ofNullable(byId.get(id));
    }

    public int size() {
        return byId.size();
    }

    public Map<String, KeyBindingDescriptor> snapshot() {
        return byId;
    }
}
