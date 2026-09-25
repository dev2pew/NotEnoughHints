package io.github.dev2pew.notenoughhints.client.config;

import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import dev.isxander.yacl3.api.controller.FloatSliderControllerBuilder;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import io.github.dev2pew.notenoughhints.config.NehConfig;

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
                .save(() -> configManager.save(draft.toConfig()))
                .build()
                .generateScreen(parent);
    }

    private static final class Draft {
        private boolean enabled;
        private boolean debug;
        private float scale;
        private float opacity;
        private boolean showBindingLabels;

        private Draft(NehConfig config) {
            enabled = config.enabled();
            debug = config.debug();
            scale = config.scale();
            opacity = config.opacity();
            showBindingLabels = config.showBindingLabels();
        }

        private NehConfig toConfig() {
            return new NehConfig(
                    NehConfig.CURRENT_SCHEMA_VERSION,
                    enabled,
                    debug,
                    scale,
                    opacity,
                    showBindingLabels);
        }
    }
}
