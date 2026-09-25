package io.github.dev2pew.notenoughhints.gametest;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.fabricmc.loader.api.FabricLoader;

@SuppressWarnings("UnstableApiUsage")
public final class NotEnoughHintsClientGameTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        FabricLoader loader = FabricLoader.getInstance();
        requireLoaded(loader, "notenoughhints");
        requireLoaded(loader, "yet_another_config_lib_v3");
        requireAbsent(loader, "modmenu");
        requireAbsent(loader, "travelersbackpack");

        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getClientWorld().waitForChunksRender();
            context.waitFor(client -> client.level != null && client.player != null);
            context.takeScreenshot("neh-singleplayer-smoke");
        }

        context.waitFor(client -> client.level == null);
    }

    private static void requireLoaded(FabricLoader loader, String modId) {
        if (!loader.isModLoaded(modId)) {
            throw new AssertionError("Expected loaded mod: " + modId);
        }
    }

    private static void requireAbsent(FabricLoader loader, String modId) {
        if (loader.isModLoaded(modId)) {
            throw new AssertionError("Expected optional mod to be absent: " + modId);
        }
    }
}
