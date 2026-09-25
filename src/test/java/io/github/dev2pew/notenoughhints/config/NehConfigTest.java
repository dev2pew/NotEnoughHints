package io.github.dev2pew.notenoughhints.config;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import io.github.dev2pew.notenoughhints.hud.HudAnchor;

class NehConfigTest {
    @Test
    void defaultsAreValid() {
        assertTrue(NehConfig.defaults().validate().isEmpty());
    }

    @Test
    void rejectsUnsupportedSchema() {
        NehConfig config =
                new NehConfig(99, true, false, 1.0F, 1.0F, true, Set.of(), Map.of());
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
                        Set.of(),
                        Map.of());
        assertFalse(config.validate().isEmpty());
    }

    @Test
    void copiesRuleAndGroupOverrides() {
        Set<String> ids = new java.util.HashSet<>();
        ids.add("rule-a");
        Map<String, GroupOverride> overrides = new HashMap<>();
        overrides.put("group-a", new GroupOverride(HudAnchor.TOP_LEFT, 3, 4, true));

        NehConfig config =
                new NehConfig(
                        NehConfig.CURRENT_SCHEMA_VERSION,
                        true,
                        false,
                        1.0F,
                        1.0F,
                        true,
                        ids,
                        overrides);
        ids.add("rule-b");
        overrides.put("group-b", new GroupOverride(HudAnchor.BOTTOM_RIGHT, 0, 0, false));

        assertTrue(config.disabledRuleIds().contains("rule-a"));
        assertFalse(config.disabledRuleIds().contains("rule-b"));
        assertTrue(config.groupOverrides().containsKey("group-a"));
        assertFalse(config.groupOverrides().containsKey("group-b"));
    }
}
