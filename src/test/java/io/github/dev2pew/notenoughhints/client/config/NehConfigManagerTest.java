package io.github.dev2pew.notenoughhints.client.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import io.github.dev2pew.notenoughhints.config.NehConfig;

class NehConfigManagerTest {
    @TempDir Path temporaryDirectory;

    @Test
    void savesAndReloadsValidatedConfig() {
        Path file = temporaryDirectory.resolve("neh.json");
        NehConfigManager manager = new NehConfigManager(file);
        NehConfig saved = new NehConfig(NehConfig.CURRENT_SCHEMA_VERSION, false, true, 1.25F, 0.7F, false);

        assertTrue(manager.save(saved));

        NehConfigManager reloaded = new NehConfigManager(file);
        reloaded.load();

        assertEquals(saved, reloaded.current());
    }

    @Test
    void rejectsInvalidConfigWithoutReplacingCurrentValue() {
        Path file = temporaryDirectory.resolve("neh.json");
        NehConfigManager manager = new NehConfigManager(file);
        NehConfig original = NehConfig.defaults();
        NehConfig invalid =
                new NehConfig(
                        NehConfig.CURRENT_SCHEMA_VERSION,
                        true,
                        false,
                        0.0F,
                        1.0F,
                        true);

        assertTrue(manager.save(original));
        assertFalse(manager.save(invalid));
        assertEquals(original, manager.current());
    }
}
