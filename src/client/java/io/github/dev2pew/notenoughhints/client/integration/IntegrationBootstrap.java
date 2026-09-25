package io.github.dev2pew.notenoughhints.client.integration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.loader.api.FabricLoader;

import io.github.dev2pew.notenoughhints.NotEnoughHints;
import io.github.dev2pew.notenoughhints.client.integration.travelersbackpack.TravelersBackpackNestedInventoryAdapter;

public final class IntegrationBootstrap {
    private static final Logger LOGGER =
            LoggerFactory.getLogger(NotEnoughHints.MOD_ID + "/Integrations");

    private IntegrationBootstrap() {}

    public static void registerAvailableAdapters(NestedInventoryAdapterRegistry registry) {
        if (!FabricLoader.getInstance().isModLoaded("travelersbackpack")) {
            return;
        }

        try {
            registry.register(new TravelersBackpackNestedInventoryAdapter());
        } catch (RuntimeException | LinkageError exception) {
            LOGGER.error(
                    "Traveler's Backpack is loaded, but the NEH integration could not initialize",
                    exception);
        }
    }
}
