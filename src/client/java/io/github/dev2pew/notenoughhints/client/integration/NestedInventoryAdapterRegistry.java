package io.github.dev2pew.notenoughhints.client.integration;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.world.item.ItemStack;

import io.github.dev2pew.notenoughhints.NotEnoughHints;

public final class NestedInventoryAdapterRegistry {
    private static final Logger LOGGER =
            LoggerFactory.getLogger(NotEnoughHints.MOD_ID + "/NestedInventoryAdapters");

    private final Map<String, Entry> entries = new LinkedHashMap<>();

    public void register(NestedInventoryAdapter adapter) {
        Objects.requireNonNull(adapter, "adapter");
        String id = requireIdentifier(adapter.id(), "adapter id");
        requireIdentifier(adapter.targetModId(), "target mod id");

        if (entries.putIfAbsent(id, new Entry(adapter)) != null) {
            throw new IllegalArgumentException("Duplicate nested inventory adapter id: " + id);
        }

        LOGGER.info(
                "Registered nested inventory adapter {} for mod {}",
                adapter.id(),
                adapter.targetModId());
    }

    public void visitContents(ItemStack stack, Consumer<ItemStack> consumer) {
        Objects.requireNonNull(stack, "stack");
        Objects.requireNonNull(consumer, "consumer");

        for (String id : entries.keySet()) {
            runGuarded(
                    id,
                    () -> {
                        Entry entry = entries.get(id);
                        if (!entry.adapter.supports(stack)) {
                            return;
                        }

                        for (ItemStack nestedStack : entry.adapter.contents(stack)) {
                            if (nestedStack != null) {
                                consumer.accept(nestedStack);
                            }
                        }
                    });
        }
    }

    void runGuarded(String id, Runnable operation) {
        Objects.requireNonNull(operation, "operation");
        Entry entry = entries.get(id);
        if (entry == null || !entry.enabled) {
            return;
        }

        try {
            operation.run();
        } catch (RuntimeException | LinkageError exception) {
            entry.enabled = false;
            LOGGER.error(
                    "Disabled nested inventory adapter {} after an exception",
                    entry.adapter.id(),
                    exception);
        }
    }

    public List<String> activeAdapterIds() {
        List<String> active = new ArrayList<>();
        entries.forEach(
                (id, entry) -> {
                    if (entry.enabled) {
                        active.add(id);
                    }
                });
        return List.copyOf(active);
    }

    public List<String> disabledAdapterIds() {
        List<String> disabled = new ArrayList<>();
        entries.forEach(
                (id, entry) -> {
                    if (!entry.enabled) {
                        disabled.add(id);
                    }
                });
        return List.copyOf(disabled);
    }

    private static String requireIdentifier(String value, String label) {
        Objects.requireNonNull(value, label);
        if (value.isBlank()) {
            throw new IllegalArgumentException(label + " must not be blank");
        }
        return value;
    }

    private static final class Entry {
        private final NestedInventoryAdapter adapter;
        private boolean enabled = true;

        private Entry(NestedInventoryAdapter adapter) {
            this.adapter = adapter;
        }
    }
}
