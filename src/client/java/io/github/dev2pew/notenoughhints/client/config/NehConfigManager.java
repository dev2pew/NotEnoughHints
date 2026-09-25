package io.github.dev2pew.notenoughhints.client.config;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.loader.api.FabricLoader;

import io.github.dev2pew.notenoughhints.NotEnoughHints;
import io.github.dev2pew.notenoughhints.config.NehConfig;

public final class NehConfigManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(NotEnoughHints.MOD_ID + "/Config");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final Path configFile;
    private volatile NehConfig current = NehConfig.defaults();

    public NehConfigManager() {
        this(FabricLoader.getInstance().getConfigDir().resolve("not-enough-hints").resolve("neh.json"));
    }

    NehConfigManager(Path configFile) {
        this.configFile = configFile;
    }

    public NehConfig current() {
        return current;
    }

    public void load() {
        if (Files.notExists(configFile)) {
            current = NehConfig.defaults();
            writeDefaults();
            return;
        }

        try {
            JsonObject root;
            try (Reader reader = Files.newBufferedReader(configFile, StandardCharsets.UTF_8)) {
                root = JsonParser.parseReader(reader).getAsJsonObject();
            }

            boolean migrated = migrate(root);
            NehConfig loaded = GSON.fromJson(root, NehConfig.class);
            if (loaded == null) {
                LOGGER.error("NEH config is empty: {}", configFile);
                current = NehConfig.defaults();
                return;
            }

            List<String> errors = loaded.validate();
            if (!errors.isEmpty()) {
                LOGGER.error("NEH config validation failed: {}", String.join("; ", errors));
                current = NehConfig.defaults();
                return;
            }

            current = loaded;
            if (migrated && !save(loaded)) {
                LOGGER.error("NEH config migrated in memory but could not be written to {}", configFile);
            }
        } catch (IOException | RuntimeException exception) {
            LOGGER.error(
                    "Failed to read NEH config {}; defaults will be used in memory",
                    configFile,
                    exception);
            current = NehConfig.defaults();
        }
    }

    private static boolean migrate(JsonObject root) {
        int schemaVersion =
                root.has("schema_version") ? root.get("schema_version").getAsInt() : 0;
        if (schemaVersion != 1) {
            return false;
        }

        root.addProperty("schema_version", NehConfig.CURRENT_SCHEMA_VERSION);
        if (!root.has("disabled_rule_ids")) {
            root.add("disabled_rule_ids", new JsonArray());
        }
        LOGGER.info("Migrated NEH config schema from 1 to {}", NehConfig.CURRENT_SCHEMA_VERSION);
        return true;
    }

    public boolean save(NehConfig next) {
        List<String> errors = next.validate();
        if (!errors.isEmpty()) {
            LOGGER.error("Refusing to save invalid NEH config: {}", String.join("; ", errors));
            return false;
        }

        Path temporaryFile = configFile.resolveSibling(configFile.getFileName() + ".tmp");
        try {
            Files.createDirectories(configFile.getParent());
            try (Writer writer = Files.newBufferedWriter(temporaryFile, StandardCharsets.UTF_8)) {
                GSON.toJson(next, writer);
            }

            try {
                Files.move(
                        temporaryFile,
                        configFile,
                        StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException exception) {
                Files.move(temporaryFile, configFile, StandardCopyOption.REPLACE_EXISTING);
            }

            current = next;
            return true;
        } catch (IOException exception) {
            LOGGER.error("Failed to save NEH config {}", configFile, exception);
            try {
                Files.deleteIfExists(temporaryFile);
            } catch (IOException cleanupException) {
                LOGGER.debug("Failed to remove temporary NEH config {}", temporaryFile, cleanupException);
            }
            return false;
        }
    }

    private void writeDefaults() {
        if (!save(current)) {
            LOGGER.error("Failed to create default NEH config {}", configFile);
        }
    }
}
