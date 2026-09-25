package io.github.dev2pew.notenoughhints.client.context;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.MappingResolver;

public final class ScreenClassSelector {
    private static final String INTERMEDIARY_NAMESPACE = "intermediary";

    private ScreenClassSelector() {}

    public static String canonicalize(Class<?> screenClass) {
        if (screenClass == null) {
            return "";
        }

        String runtimeName = screenClass.getName();
        MappingResolver resolver = FabricLoader.getInstance().getMappingResolver();
        if (!resolver.getNamespaces().contains(INTERMEDIARY_NAMESPACE)) {
            return runtimeName;
        }

        return resolver.unmapClassName(INTERMEDIARY_NAMESPACE, runtimeName);
    }
}
