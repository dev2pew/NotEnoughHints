package io.github.dev2pew.notenoughhints.config;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.Test;

class NehConfigTest {
    @Test
    void defaultsAreValid() {
        assertTrue(NehConfig.defaults().validate().isEmpty());
    }

    @Test
    void rejectsUnsupportedSchema() {
        NehConfig config = new NehConfig(99, true, false, 1.0F, 1.0F, true, Set.of());
        assertFalse(config.validate().isEmpty());
    }

    @Test
    void rejectsInvalidScaleAndOpacity() {
        NehConfig config =
                new NehConfig(
                        NehConfig.CURRENT_SCHEMA_VERSION,
                        true,
                        false,
                        0.0F,
                        1.5F,
                        true,
                        Set.of());
        assertFalse(config.validate().isEmpty());
    }

    @Test
    void copiesDisabledRuleIds() {
        Set<String> ids = new java.util.HashSet<>();
        ids.add("rule-a");

        NehConfig config =
                new NehConfig(
                        NehConfig.CURRENT_SCHEMA_VERSION,
                        true,
                        false,
                        1.0F,
                        1.0F,
                        true,
                        ids);
        ids.add("rule-b");

        assertTrue(config.disabledRuleIds().contains("rule-a"));
        assertFalse(config.disabledRuleIds().contains("rule-b"));
    }
}
