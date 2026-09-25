package io.github.dev2pew.notenoughhints.client.config;

import java.util.HashSet;
import java.util.Set;

import dev.isxander.yacl3.api.ButtonOption;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.LabelOption;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import dev.isxander.yacl3.api.controller.FloatSliderControllerBuilder;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import io.github.dev2pew.notenoughhints.client.NotEnoughHintsClient;
import io.github.dev2pew.notenoughhints.config.HintPack;
import io.github.dev2pew.notenoughhints.config.NehConfig;
import io.github.dev2pew.notenoughhints.rule.Rule;

public final class NehConfigScreen {
    private NehConfigScreen() {}

    public static Screen create(Screen parent, NehConfigManager configManager) {
        Draft draft = new Draft(configManager.current());
        NehConfig defaults = NehConfig.defaults();

        return YetAnotherConfigLib.createBuilder()
                .title(Component.translatable("screen.notenoughhints.config"))
                .category(
                        ConfigCategory.createBuilder()
                                .name(Component.translatable("category.notenoughhints.general"))
                                .option(
                                        Option.<Boolean>createBuilder()
                                                .name(Component.translatable("option.notenoughhints.enabled"))
                                                .description(
                                                        OptionDescription.of(
                                                                Component.translatable(
                                                                        "option.notenoughhints.enabled.description")))
                                                .binding(
                                                        defaults.enabled(),
                                                        () -> draft.enabled,
                                                        value -> draft.enabled = value)
                                                .controller(BooleanControllerBuilder::create)
                                                .build())
                                .option(
                                        Option.<Boolean>createBuilder()
                                                .name(Component.translatable("option.notenoughhints.debug"))
                                                .description(
                                                        OptionDescription.of(
                                                                Component.translatable(
                                                                        "option.notenoughhints.debug.description")))
                                                .binding(
                                                        defaults.debug(),
                                                        () -> draft.debug,
                                                        value -> draft.debug = value)
                                                .controller(BooleanControllerBuilder::create)
                                                .build())
                                .option(
                                        Option.<Boolean>createBuilder()
                                                .name(
                                                        Component.translatable(
                                                                "option.notenoughhints.show_binding_labels"))
                                                .description(
                                                        OptionDescription.of(
                                                                Component.translatable(
                                                                        "option.notenoughhints.show_binding_labels.description")))
                                                .binding(
                                                        defaults.showBindingLabels(),
                                                        () -> draft.showBindingLabels,
                                                        value -> draft.showBindingLabels = value)
                                                .controller(BooleanControllerBuilder::create)
                                                .build())
                                .option(
                                        Option.<Float>createBuilder()
                                                .name(Component.translatable("option.notenoughhints.scale"))
                                                .description(
                                                        OptionDescription.of(
                                                                Component.translatable(
                                                                        "option.notenoughhints.scale.description")))
                                                .binding(
                                                        defaults.scale(),
                                                        () -> draft.scale,
                                                        value -> draft.scale = value)
                                                .controller(
                                                        option ->
                                                                FloatSliderControllerBuilder.create(option)
                                                                        .range(0.5F, 2.0F)
                                                                        .step(0.05F))
                                                .build())
                                .option(
                                        Option.<Float>createBuilder()
                                                .name(Component.translatable("option.notenoughhints.opacity"))
                                                .description(
                                                        OptionDescription.of(
                                                                Component.translatable(
                                                                        "option.notenoughhints.opacity.description")))
                                                .binding(
                                                        defaults.opacity(),
                                                        () -> draft.opacity,
                                                        value -> draft.opacity = value)
                                                .controller(
                                                        option ->
                                                                FloatSliderControllerBuilder.create(option)
                                                                        .range(0.1F, 1.0F)
                                                                        .step(0.05F))
                                                .build())
                                .build())
                .category(buildHintPackCategory())
                .category(buildRulesCategory(draft))
                .save(
                        () -> {
                            if (configManager.save(draft.toConfig())) {
                                NotEnoughHintsClient.refreshObservationRequirements();
                            }
                        })
                .build()
                .generateScreen(parent);
    }

    private static ConfigCategory buildHintPackCategory() {
        HintPackManager manager = NotEnoughHintsClient.hintPackManager();
        HintPack pack = manager.current();
        String summary =
                manager.issues().isEmpty()
                        ? "No hint-pack load issues"
                        : manager.issues().size() + " hint-pack load issue(s); see latest.log";

        return ConfigCategory.createBuilder()
                .name(Component.translatable("category.notenoughhints.hint_packs"))
                .option(
                        LabelOption.create(
                                Component.literal(
                                        "Loaded "
                                                + pack.groups().size()
                                                + " group(s), "
                                                + pack.rules().size()
                                                + " rule(s)")))
                .option(LabelOption.create(Component.literal(summary)))
                .option(
                        ButtonOption.createBuilder()
                                .name(Component.translatable("option.notenoughhints.reload_hint_packs"))
                                .description(
                                        OptionDescription.of(
                                                Component.translatable(
                                                        "option.notenoughhints.reload_hint_packs.description")))
                                .action(
                                        (screen, option) ->
                                                NotEnoughHintsClient.reloadHintPacks())
                                .build())
                .build();
    }

    private static ConfigCategory buildRulesCategory(Draft draft) {
        HintPack pack = NotEnoughHintsClient.hintPackManager().current();
        var builder =
                ConfigCategory.createBuilder()
                        .name(Component.translatable("category.notenoughhints.rules"));

        if (pack.rules().isEmpty()) {
            builder.option(LabelOption.create(Component.literal("No rules are currently loaded")));
        }

        for (Rule rule : pack.rules()) {
            builder.option(
                    Option.<Boolean>createBuilder()
                            .name(Component.literal(rule.id()))
                            .description(
                                    OptionDescription.of(
                                            Component.literal(
                                                    "Priority "
                                                            + rule.priority()
                                                            + (rule.enabled()
                                                                    ? ""
                                                                    : "; disabled by hint pack"))))
                            .binding(
                                    rule.enabled(),
                                    () ->
                                            rule.enabled()
                                                    && !draft.disabledRuleIds.contains(rule.id()),
                                    enabled -> {
                                        if (!rule.enabled()) {
                                            return;
                                        }
                                        if (enabled) {
                                            draft.disabledRuleIds.remove(rule.id());
                                        } else {
                                            draft.disabledRuleIds.add(rule.id());
                                        }
                                    })
                            .available(rule.enabled())
                            .controller(BooleanControllerBuilder::create)
                            .build());
        }

        return builder.build();
    }

    private static final class Draft {
        private boolean enabled;
        private boolean debug;
        private float scale;
        private float opacity;
        private boolean showBindingLabels;
        private final Set<String> disabledRuleIds;

        private Draft(NehConfig config) {
            enabled = config.enabled();
            debug = config.debug();
            scale = config.scale();
            opacity = config.opacity();
            showBindingLabels = config.showBindingLabels();
            disabledRuleIds = new HashSet<>(config.disabledRuleIds());
        }

        private NehConfig toConfig() {
            return new NehConfig(
                    NehConfig.CURRENT_SCHEMA_VERSION,
                    enabled,
                    debug,
                    scale,
                    opacity,
                    showBindingLabels,
                    Set.copyOf(disabledRuleIds));
        }
    }
}
