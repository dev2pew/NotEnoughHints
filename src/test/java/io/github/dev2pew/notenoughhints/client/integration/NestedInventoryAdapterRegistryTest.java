package io.github.dev2pew.notenoughhints.client.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import net.minecraft.world.item.ItemStack;

class NestedInventoryAdapterRegistryTest {
    @Test
    void disablesBrokenAdapterAfterFirstFailure() {
        NestedInventoryAdapterRegistry registry = new NestedInventoryAdapterRegistry();
        AtomicInteger calls = new AtomicInteger();

        registry.register(emptyAdapter("test:broken"));

        registry.runGuarded(
                "test:broken",
                () -> {
                    calls.incrementAndGet();
                    throw new IllegalStateException("boom");
                });
        registry.runGuarded("test:broken", calls::incrementAndGet);

        assertEquals(1, calls.get());
        assertEquals(List.of(), registry.activeAdapterIds());
        assertEquals(List.of("test:broken"), registry.disabledAdapterIds());
    }

    @Test
    void rejectsDuplicateAdapterIds() {
        NestedInventoryAdapterRegistry registry = new NestedInventoryAdapterRegistry();
        NestedInventoryAdapter adapter = emptyAdapter("test:same");

        registry.register(adapter);

        assertThrows(IllegalArgumentException.class, () -> registry.register(adapter));
    }

    private static NestedInventoryAdapter emptyAdapter(String id) {
        return new NestedInventoryAdapter() {
            @Override
            public String id() {
                return id;
            }

            @Override
            public String targetModId() {
                return "testmod";
            }

            @Override
            public boolean supports(ItemStack stack) {
                return false;
            }

            @Override
            public Iterable<ItemStack> contents(ItemStack stack) {
                return List.of();
            }
        };
    }
}
