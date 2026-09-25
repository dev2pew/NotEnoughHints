package io.github.dev2pew.notenoughhints.client.hud;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import net.minecraft.network.chat.Component;

import io.github.dev2pew.notenoughhints.client.config.NehConfigManager;
import io.github.dev2pew.notenoughhints.client.keybind.KeyBindingCatalog;
import io.github.dev2pew.notenoughhints.client.keybind.KeyBindingDescriptor;
import io.github.dev2pew.notenoughhints.config.NehConfig;
import io.github.dev2pew.notenoughhints.context.ClientContext;
import io.github.dev2pew.notenoughhints.hud.HintDefinition;
import io.github.dev2pew.notenoughhints.hud.HintGroupDefinition;

public final class HintController {
    private final NehConfigManager configManager;
    private final KeyBindingCatalog keyBindingCatalog;
    private final HintGroupDefinition groupDefinition;
    private final AtomicReference<HintGroupRenderState> renderState =
            new AtomicReference<>(HintGroupRenderState.hidden());

    public HintController(
            NehConfigManager configManager,
            KeyBindingCatalog keyBindingCatalog,
            HintGroupDefinition groupDefinition) {
        this.configManager = configManager;
        this.keyBindingCatalog = keyBindingCatalog;
        this.groupDefinition = groupDefinition;
    }

    public void update(ClientContext context) {
        NehConfig config = configManager.current();
        if (!config.enabled() || !context.worldPresent()) {
            renderState.set(HintGroupRenderState.hidden());
            return;
        }

        List<ResolvedHint> resolved = new ArrayList<>();
        for (HintDefinition definition : groupDefinition.hints()) {
            resolve(definition, config).ifPresent(resolved::add);
        }

        renderState.set(
                new HintGroupRenderState(
                        !resolved.isEmpty(),
                        groupDefinition.anchor(),
                        groupDefinition.offsetX(),
                        groupDefinition.offsetY(),
                        groupDefinition.flow(),
                        groupDefinition.entryGap(),
                        config.scale(),
                        config.opacity(),
                        resolved));
    }

    public HintGroupRenderState renderState() {
        return renderState.get();
    }

    private java.util.Optional<ResolvedHint> resolve(HintDefinition definition, NehConfig config) {
        KeyBindingDescriptor binding = keyBindingCatalog.find(definition.bindingId()).orElse(null);
        if (binding == null) {
            return java.util.Optional.empty();
        }

        boolean showBinding = config.showBindingLabels() && definition.showBinding();
        String bindingText = showBinding ? binding.boundKeyText() : "";
        String description =
                definition.descriptionTranslationKey().isBlank()
                        ? binding.descriptionText()
                        : Component.translatable(definition.descriptionTranslationKey()).getString();

        if (description.equals(definition.descriptionTranslationKey())
                && !definition.descriptionTranslationKey().isBlank()) {
            description = binding.descriptionText();
        }
        if (description.isBlank() || description.equals(definition.bindingId())) {
            description = Component.translatable("text.notenoughhints.unnamed_action").getString();
        }

        return java.util.Optional.of(new ResolvedHint(bindingText, description));
    }
}
