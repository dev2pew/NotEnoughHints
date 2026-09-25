package io.github.dev2pew.notenoughhints.client.config;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
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

        try (Reader reader = Files.newBufferedReader(configFile, StandardCharsets.UTF_8)) {
            NehConfig loaded = GSON.fromJson(reader, NehConfig.class);
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
        } catch (IOException | JsonParseException exception) {
            LOGGER.error(
                    "Failed to read NEH config {}; defaults will be used in memory",
                    configFile,
                    exception);
            current = NehConfig.defaults();
        }
    }

    private void writeDefaults() {
        try {
            Files.createDirectories(configFile.getParent());
            try (Writer writer = Files.newBufferedWriter(configFile, StandardCharsets.UTF_8)) {
                GSON.toJson(current, writer);
            }
        } catch (IOException exception) {
            LOGGER.error("Failed to create default NEH config {}", configFile, exception);
        }
    }
}
