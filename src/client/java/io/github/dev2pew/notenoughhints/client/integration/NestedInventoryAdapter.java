package io.github.dev2pew.notenoughhints.client.integration;

import net.minecraft.world.item.ItemStack;

public interface NestedInventoryAdapter {
    String id();

    String targetModId();

    boolean supports(ItemStack stack);

    Iterable<ItemStack> contents(ItemStack stack);
}
