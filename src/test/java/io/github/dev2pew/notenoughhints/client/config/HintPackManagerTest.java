package io.github.dev2pew.notenoughhints.client.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class HintPackManagerTest {
    @TempDir Path temporaryDirectory;

    @Test
    void reportsRuleActionsThatReferenceMissingHints() throws IOException {
        Files.writeString(
                temporaryDirectory.resolve("missing-reference.json"),
                """
                {
                  "schema_version": 1,
                  "rules": [
                    {
                      "id": "broken-reference",
                      "when": {"type": "world_present"},
                      "actions": [
                        {"type": "show_hint", "hint": "does-not-exist"}
                      ]
                    }
                  ]
                }
                """);

        HintPackManager manager = new HintPackManager(temporaryDirectory);
        manager.load();

        assertEquals(1, manager.current().rules().size());
        assertEquals(
                List.of(
                        "rule 'broken-reference' references missing hint 'does-not-exist'"),
                manager.issues());
    }

    @Test
    void keepsValidPacksWhenAnotherFileIsInvalid() throws IOException {
        Files.writeString(
                temporaryDirectory.resolve("a-valid.json"),
                """
                {
                  "schema_version": 1,
                  "groups": [
                    {
                      "id": "valid",
                      "hints": [
                        {
                          "id": "inventory",
                          "binding": "key.inventory",
                          "visible_by_default": true
                        }
                      ]
                    }
                  ]
                }
                """);
        Files.writeString(
                temporaryDirectory.resolve("b-invalid.json"),
                """
                {
                  "schema_version": 1,
                  "rules": [
                    {
                      "id": "broken",
                      "when": {"type": "unknown_condition"},
                      "actions": []
                    }
                  ]
                }
                """);

        HintPackManager manager = new HintPackManager(temporaryDirectory);
        manager.load();

        assertEquals(1, manager.current().groups().size());
        assertEquals("valid", manager.current().groups().getFirst().id());
        assertFalse(manager.issues().isEmpty());
    }
}
