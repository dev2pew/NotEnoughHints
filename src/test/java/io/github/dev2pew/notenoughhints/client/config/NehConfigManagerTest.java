package io.github.dev2pew.notenoughhints.client.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import io.github.dev2pew.notenoughhints.config.NehConfig;

class NehConfigManagerTest {
    @TempDir Path temporaryDirectory;

    @Test
    void savesAndReloadsValidatedConfig() {
        Path file = temporaryDirectory.resolve("neh.json");
        NehConfigManager manager = new NehConfigManager(file);
        NehConfig saved =
                new NehConfig(
                        NehConfig.CURRENT_SCHEMA_VERSION,
                        false,
                        true,
                        1.25F,
                        0.7F,
                        false,
                        Set.of("rule-a"));

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
                        true,
                        Set.of());

        assertTrue(manager.save(original));
        assertFalse(manager.save(invalid));
        assertEquals(original, manager.current());
    }

    @Test
    void migratesSchemaOneWithEmptyRuleOverrides() throws IOException {
        Path file = temporaryDirectory.resolve("neh.json");
        Files.writeString(
                file,
                """
                {
                  "schema_version": 1,
                  "enabled": false,
                  "debug": true,
                  "scale": 1.25,
                  "opacity": 0.75,
                  "show_binding_labels": false
                }
                """);

        NehConfigManager manager = new NehConfigManager(file);
        manager.load();

        assertEquals(NehConfig.CURRENT_SCHEMA_VERSION, manager.current().schemaVersion());
        assertEquals(Set.of(), manager.current().disabledRuleIds());
        assertFalse(manager.current().enabled());
        assertTrue(Files.readString(file).contains("\"schema_version\": 2"));
    }
}
