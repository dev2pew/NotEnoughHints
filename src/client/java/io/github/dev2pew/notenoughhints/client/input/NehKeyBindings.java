package io.github.dev2pew.notenoughhints.client.input;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;

import io.github.dev2pew.notenoughhints.client.NotEnoughHintsClient;
import io.github.dev2pew.notenoughhints.client.config.NehConfigScreen;

public final class NehKeyBindings {
    private static final String CATEGORY = "key.categories.notenoughhints";
    private static KeyMapping openConfig;

    private NehKeyBindings() {}

    public static void register() {
        openConfig =
                KeyBindingHelper.registerKeyBinding(
                        new KeyMapping(
                                "key.notenoughhints.open_config",
                                InputConstants.Type.KEYSYM,
                                InputConstants.UNKNOWN.getValue(),
                                CATEGORY));
    }

    public static void handle(Minecraft client) {
        if (openConfig == null) {
            return;
        }

        while (openConfig.consumeClick()) {
            client.setScreen(
                    NehConfigScreen.create(client.screen, NotEnoughHintsClient.configManager()));
        }
    }
}
