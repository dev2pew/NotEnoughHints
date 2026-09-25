package io.github.dev2pew.notenoughhints.client.hud;

public record PrototypeHintRenderState(
        boolean visible, String bindingText, String description, float scale, float opacity) {
    public static PrototypeHintRenderState hidden() {
        return new PrototypeHintRenderState(false, "", "", 1.0F, 1.0F);
    }
}
