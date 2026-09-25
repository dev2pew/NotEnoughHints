package io.github.dev2pew.notenoughhints.config;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.SerializedName;

public record NehConfig(
        @SerializedName("schema_version") int schemaVersion,
        boolean enabled,
        boolean debug,
        float scale,
        float opacity,
        @SerializedName("show_binding_labels") boolean showBindingLabels) {
    public static final int CURRENT_SCHEMA_VERSION = 1;

    public static NehConfig defaults() {
        return new NehConfig(CURRENT_SCHEMA_VERSION, true, false, 1.0F, 1.0F, true);
    }

    public List<String> validate() {
        List<String> errors = new ArrayList<>();

        if (schemaVersion != CURRENT_SCHEMA_VERSION) {
            errors.add("Unsupported schema_version: " + schemaVersion);
        }
        if (!Float.isFinite(scale) || scale <= 0.0F) {
            errors.add("scale must be finite and greater than 0");
        }
        if (!Float.isFinite(opacity) || opacity < 0.0F || opacity > 1.0F) {
            errors.add("opacity must be finite and between 0 and 1");
        }

        return List.copyOf(errors);
    }
}
