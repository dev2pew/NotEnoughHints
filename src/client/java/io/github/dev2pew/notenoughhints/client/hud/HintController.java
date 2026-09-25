package io.github.dev2pew.notenoughhints.client.hud;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import net.minecraft.network.chat.Component;

import io.github.dev2pew.notenoughhints.client.config.NehConfigManager;
import io.github.dev2pew.notenoughhints.client.keybind.KeyBindingCatalog;
import io.github.dev2pew.notenoughhints.client.keybind.KeyBindingDescriptor;
import io.github.dev2pew.notenoughhints.config.NehConfig;
import io.github.dev2pew.notenoughhints.context.ClientContext;
import io.github.dev2pew.notenoughhints.hud.HintDefinition;
import io.github.dev2pew.notenoughhints.hud.HintDescription;
import io.github.dev2pew.notenoughhints.hud.HintGroupDefinition;
import io.github.dev2pew.notenoughhints.rule.Rule;
import io.github.dev2pew.notenoughhints.rule.RuleEvaluationResult;
import io.github.dev2pew.notenoughhints.rule.RuleEvaluator;

public final class HintController {
    private final NehConfigManager configManager;
    private final KeyBindingCatalog keyBindingCatalog;
    private volatile DefinitionState definitions;
    private final RuleEvaluator ruleEvaluator = new RuleEvaluator();
    private final AtomicReference<List<HintGroupRenderState>> renderStates =
            new AtomicReference<>(List.of());

    public HintController(
            NehConfigManager configManager,
            KeyBindingCatalog keyBindingCatalog,
            List<HintGroupDefinition> groups,
            List<Rule> rules) {
        this.configManager = configManager;
        this.keyBindingCatalog = keyBindingCatalog;
        replaceDefinitions(groups, rules);
    }

    public void replaceDefinitions(
            List<HintGroupDefinition> groups, List<Rule> rules) {
        List<HintGroupDefinition> groupCopy = List.copyOf(groups);
        List<Rule> ruleCopy = List.copyOf(rules);
        validateUniqueIds(groupCopy);
        Set<String> defaultVisibleHintIds =
                groupCopy.stream()
                        .flatMap(group -> group.hints().stream())
                        .filter(HintDefinition::visibleByDefault)
                        .map(HintDefinition::id)
                        .collect(Collectors.toUnmodifiableSet());
        definitions = new DefinitionState(groupCopy, ruleCopy, defaultVisibleHintIds);
        renderStates.set(List.of());
    }

    public void update(ClientContext context) {
        NehConfig config = configManager.current();
        if (!config.enabled() || !context.worldPresent()) {
            renderStates.set(List.of());
            return;
        }

        DefinitionState currentDefinitions = definitions;
        RuleEvaluationResult evaluation =
                ruleEvaluator.evaluate(
                        context,
                        currentDefinitions.rules(),
                        currentDefinitions.defaultVisibleHintIds());

        List<HintGroupRenderState> nextStates =
                new ArrayList<>(currentDefinitions.groups().size());
        for (HintGroupDefinition group : currentDefinitions.groups()) {
            List<ResolvedHint> resolved = new ArrayList<>();
            for (HintDefinition definition : group.hints()) {
                if (!evaluation.visibleHintIds().contains(definition.id())) {
                    continue;
                }
                resolve(definition, config).ifPresent(resolved::add);
            }

            if (!resolved.isEmpty()) {
                nextStates.add(
                        new HintGroupRenderState(
                                true,
                                group.anchor(),
                                group.offsetX(),
                                group.offsetY(),
                                group.flow(),
                                group.entryGap(),
                                config.scale(),
                                config.opacity(),
                                resolved));
            }
        }

        renderStates.set(List.copyOf(nextStates));
    }

    public List<HintGroupRenderState> renderStates() {
        return renderStates.get();
    }

    private java.util.Optional<ResolvedHint> resolve(HintDefinition definition, NehConfig config) {
        KeyBindingDescriptor binding = keyBindingCatalog.find(definition.bindingId()).orElse(null);
        if (binding == null) {
            return java.util.Optional.empty();
        }

        boolean showBinding = config.showBindingLabels() && definition.showBinding();
        String bindingText = showBinding ? binding.boundKeyText() : "";
        String description =
                switch (definition.description()) {
                    case HintDescription.Default ignored -> binding.descriptionText();
                    case HintDescription.Literal literal -> literal.text();
                    case HintDescription.Translation translation -> {
                        String translated = Component.translatable(translation.key()).getString();
                        yield translated.equals(translation.key())
                                ? binding.descriptionText()
                                : translated;
                    }
                };

        if (description.isBlank() || description.equals(definition.bindingId())) {
            description = Component.translatable("text.notenoughhints.unnamed_action").getString();
        }

        return java.util.Optional.of(new ResolvedHint(bindingText, description));
    }

    private record DefinitionState(
            List<HintGroupDefinition> groups,
            List<Rule> rules,
            Set<String> defaultVisibleHintIds) {}

    private static void validateUniqueIds(List<HintGroupDefinition> groups) {
        Set<String> groupIds = new HashSet<>();
        Set<String> hintIds = new HashSet<>();

        for (HintGroupDefinition group : groups) {
            if (!groupIds.add(group.id())) {
                throw new IllegalArgumentException("Duplicate hint group id: " + group.id());
            }
            for (HintDefinition hint : group.hints()) {
                if (!hintIds.add(hint.id())) {
                    throw new IllegalArgumentException("Duplicate hint id: " + hint.id());
                }
            }
        }
    }
}
