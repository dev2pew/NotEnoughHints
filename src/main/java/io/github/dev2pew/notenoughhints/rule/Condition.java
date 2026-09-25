package io.github.dev2pew.notenoughhints.rule;

import java.util.List;
import java.util.Objects;

import io.github.dev2pew.notenoughhints.context.ClientContext;
import io.github.dev2pew.notenoughhints.context.EquipmentSlotKey;

public sealed interface Condition {
    boolean test(ClientContext context);

    record All(List<Condition> conditions) implements Condition {
        public All {
            conditions = List.copyOf(conditions);
        }

        @Override
        public boolean test(ClientContext context) {
            return conditions.stream().allMatch(condition -> condition.test(context));
        }
    }

    record Any(List<Condition> conditions) implements Condition {
        public Any {
            conditions = List.copyOf(conditions);
        }

        @Override
        public boolean test(ClientContext context) {
            return conditions.stream().anyMatch(condition -> condition.test(context));
        }
    }

    record Not(Condition condition) implements Condition {
        public Not {
            Objects.requireNonNull(condition, "condition");
        }

        @Override
        public boolean test(ClientContext context) {
            return !condition.test(context);
        }
    }

    record WorldPresent() implements Condition {
        @Override
        public boolean test(ClientContext context) {
            return context.worldPresent();
        }
    }

    record Dimension(String id) implements Condition {
        public Dimension {
            Objects.requireNonNull(id, "id");
        }

        @Override
        public boolean test(ClientContext context) {
            return id.equals(context.dimensionId());
        }
    }

    record ScreenClass(String className) implements Condition {
        public ScreenClass {
            Objects.requireNonNull(className, "className");
        }

        @Override
        public boolean test(ClientContext context) {
            return className.equals(context.screenClassName());
        }
    }

    record HandledMenu(String id) implements Condition {
        public HandledMenu {
            Objects.requireNonNull(id, "id");
        }

        @Override
        public boolean test(ClientContext context) {
            return id.equals(context.handledMenuId());
        }
    }

    record MainHandItem(String itemId) implements Condition {
        public MainHandItem {
            Objects.requireNonNull(itemId, "itemId");
        }

        @Override
        public boolean test(ClientContext context) {
            return itemId.equals(context.mainHandItemId());
        }
    }

    record OffHandItem(String itemId) implements Condition {
        public OffHandItem {
            Objects.requireNonNull(itemId, "itemId");
        }

        @Override
        public boolean test(ClientContext context) {
            return itemId.equals(context.offHandItemId());
        }
    }

    record HeldItemAnyHand(String itemId) implements Condition {
        public HeldItemAnyHand {
            Objects.requireNonNull(itemId, "itemId");
        }

        @Override
        public boolean test(ClientContext context) {
            return itemId.equals(context.mainHandItemId()) || itemId.equals(context.offHandItemId());
        }
    }

    record EquipmentSlotItem(EquipmentSlotKey slot, String itemId) implements Condition {
        public EquipmentSlotItem {
            Objects.requireNonNull(slot, "slot");
            Objects.requireNonNull(itemId, "itemId");
        }

        @Override
        public boolean test(ClientContext context) {
            return itemId.equals(context.equipmentItem(slot));
        }
    }

    record ModLoaded(String modId) implements Condition {
        public ModLoaded {
            Objects.requireNonNull(modId, "modId");
        }

        @Override
        public boolean test(ClientContext context) {
            return context.loadedModIds().contains(modId);
        }
    }

    record KeyBindingExists(String bindingId) implements Condition {
        public KeyBindingExists {
            Objects.requireNonNull(bindingId, "bindingId");
        }

        @Override
        public boolean test(ClientContext context) {
            return context.keyBindingIds().contains(bindingId);
        }
    }
}
