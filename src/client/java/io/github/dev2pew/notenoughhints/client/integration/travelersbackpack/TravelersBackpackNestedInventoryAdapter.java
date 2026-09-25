package io.github.dev2pew.notenoughhints.client.integration.travelersbackpack;

import java.util.List;

import com.tiviacz.travelersbackpack.components.BackpackContainerContents;
import com.tiviacz.travelersbackpack.init.ModDataComponents;

import net.minecraft.world.item.ItemStack;

import io.github.dev2pew.notenoughhints.client.integration.NestedInventoryAdapter;

public final class TravelersBackpackNestedInventoryAdapter implements NestedInventoryAdapter {
    public static final String ID = "travelersbackpack:backpack_container";
    public static final String TARGET_MOD_ID = "travelersbackpack";

    @Override
    public String id() {
        return ID;
    }

    @Override
    public String targetModId() {
        return TARGET_MOD_ID;
    }

    @Override
    public boolean supports(ItemStack stack) {
        return !stack.isEmpty() && stack.has(ModDataComponents.BACKPACK_CONTAINER);
    }

    @Override
    public Iterable<ItemStack> contents(ItemStack stack) {
        BackpackContainerContents contents = stack.get(ModDataComponents.BACKPACK_CONTAINER);
        return contents == null ? List.of() : contents.getItems();
    }
}
