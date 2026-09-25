package io.github.dev2pew.notenoughhints.client.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

import io.github.dev2pew.notenoughhints.client.NotEnoughHintsClient;

public final class NehModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> NehConfigScreen.create(parent, NotEnoughHintsClient.configManager());
    }
}
