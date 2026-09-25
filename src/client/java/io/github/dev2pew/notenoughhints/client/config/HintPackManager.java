package io.github.dev2pew.notenoughhints.client.config;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.loader.api.FabricLoader;

import io.github.dev2pew.notenoughhints.NotEnoughHints;
import io.github.dev2pew.notenoughhints.config.HintPack;
import io.github.dev2pew.notenoughhints.config.HintPackJsonParser;
import io.github.dev2pew.notenoughhints.hud.HintGroupDefinition;
import io.github.dev2pew.notenoughhints.rule.Rule;

public final class HintPackManager {
    private static final Logger LOGGER =
            LoggerFactory.getLogger(NotEnoughHints.MOD_ID + "/HintPacks");
    private static final String STARTER_FILE_NAME = "starter.json";
    private static final String STARTER_PACK =
            """
            {
              "schema_version": 1,
              "groups": [
                {
                  "id": "starter",
                  "anchor": "bottom_left",
                  "offset_x": 0,
                  "offset_y": 0,
                  "flow": "horizontal",
                  "entry_gap": 6,
                  "hints": [
                    {
                      "id": "inventory",
                      "binding": "key.inventory",
                      "show_binding": true,
                      "visible_by_default": true
                    }
                  ]
                }
              ],
              "rules": []
            }
            """;

    private final Path hintsDirectory;
    private final HintPackJsonParser parser = new HintPackJsonParser();
    private volatile HintPack current = HintPack.empty();
    private volatile List<String> issues = List.of();

    public HintPackManager() {
        this(FabricLoader.getInstance().getConfigDir().resolve("not-enough-hints").resolve("hints"));
    }

    HintPackManager(Path hintsDirectory) {
        this.hintsDirectory = hintsDirectory;
    }

    public HintPack current() {
        return current;
    }

    public List<String> issues() {
        return issues;
    }

    public void load() {
        try {
            Files.createDirectories(hintsDirectory);
            ensureStarterPack();
            LoadResult result = loadDirectory();
            current = result.pack();
            issues = result.issues();
        } catch (IOException exception) {
            String issue = "Failed to load hint-pack directory: " + exception.getMessage();
            LOGGER.error("Failed to load NEH hint packs from {}", hintsDirectory, exception);
            current = HintPack.empty();
            issues = List.of(issue);
        }
    }

    private LoadResult loadDirectory() throws IOException {
        List<HintGroupDefinition> groups = new ArrayList<>();
        List<Rule> rules = new ArrayList<>();
        List<String> loadIssues = new ArrayList<>();
        Set<String> groupIds = new HashSet<>();
        Set<String> hintIds = new HashSet<>();
        Set<String> ruleIds = new HashSet<>();

        for (Path file : hintPackFiles()) {
            HintPack pack;
            try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                pack = parser.parse(reader);
            } catch (IOException | RuntimeException exception) {
                String issue =
                        file.getFileName() + ": " + exception.getClass().getSimpleName()
                                + ": " + String.valueOf(exception.getMessage());
                loadIssues.add(issue);
                LOGGER.error("Ignoring invalid NEH hint pack {}", file, exception);
                continue;
            }

            String conflict = findConflict(pack, groupIds, hintIds, ruleIds);
            if (conflict != null) {
                loadIssues.add(file.getFileName() + ": " + conflict);
                LOGGER.error("Ignoring NEH hint pack {} because {}", file, conflict);
                continue;
            }

            pack.groups().forEach(
                    group -> {
                        groupIds.add(group.id());
                        group.hints().forEach(hint -> hintIds.add(hint.id()));
                    });
            pack.rules().forEach(rule -> ruleIds.add(rule.id()));
            groups.addAll(pack.groups());
            rules.addAll(pack.rules());
        }

        LOGGER.info(
                "Loaded {} NEH hint groups and {} rules from {}",
                groups.size(),
                rules.size(),
                hintsDirectory);
        return new LoadResult(
                new HintPack(HintPack.CURRENT_SCHEMA_VERSION, groups, rules),
                List.copyOf(loadIssues));
    }

    private List<Path> hintPackFiles() throws IOException {
        try (Stream<Path> files = Files.list(hintsDirectory)) {
            return files.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".json"))
                    .sorted()
                    .toList();
        }
    }

    private void ensureStarterPack() throws IOException {
        if (!hintPackFiles().isEmpty()) {
            return;
        }

        Path starter = hintsDirectory.resolve(STARTER_FILE_NAME);
        Files.writeString(
                starter,
                STARTER_PACK,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE_NEW,
                StandardOpenOption.WRITE);
        LOGGER.info("Created starter NEH hint pack at {}", starter);
    }

    private record LoadResult(HintPack pack, List<String> issues) {}

    private static String findConflict(
            HintPack pack, Set<String> groupIds, Set<String> hintIds, Set<String> ruleIds) {
        for (HintGroupDefinition group : pack.groups()) {
            if (groupIds.contains(group.id())) {
                return "group id '" + group.id() + "' is already defined";
            }
            for (var hint : group.hints()) {
                if (hintIds.contains(hint.id())) {
                    return "hint id '" + hint.id() + "' is already defined";
                }
            }
        }

        for (Rule rule : pack.rules()) {
            if (ruleIds.contains(rule.id())) {
                return "rule id '" + rule.id() + "' is already defined";
            }
        }

        return null;
    }
}
