package io.github.dev2pew.notenoughhints;

import net.minecraft.resources.ResourceLocation;

public final class NotEnoughHints {
    public static final String MOD_ID = "notenoughhints";

    private NotEnoughHints() {}

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
