package com.techquest.quest;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

/**
 * Задание квеста.
 * target: id предмета/блока ("techquest:tin_ore") или тег с решёткой ("#c:ores/tin").
 */
public record QuestTask(Type type, String target, int count, boolean consume, Optional<Component> label) {

    public enum Type implements StringRepresentable {
        CHECKMARK("checkmark"),   // выполняется сразу, как квест открылся
        HAS_ITEM("has_item"),     // иметь предметы в инвентаре (consume = сдать при получении награды)
        MINE("mine"),             // сломать блоки
        CRAFT("craft"),           // скрафтить предметы
        PLACE("place"),           // поставить блоки
        SMELT("smelt");           // достать результат из ванильной печи

        public static final Codec<Type> CODEC = StringRepresentable.fromEnum(Type::values);

        private final String name;

        Type(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }

    public static final Codec<QuestTask> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Type.CODEC.fieldOf("type").forGetter(QuestTask::type),
            Codec.STRING.optionalFieldOf("target", "").forGetter(QuestTask::target),
            Codec.intRange(1, 1_000_000).optionalFieldOf("count", 1).forGetter(QuestTask::count),
            Codec.BOOL.optionalFieldOf("consume", false).forGetter(QuestTask::consume),
            ComponentSerialization.CODEC.optionalFieldOf("label").forGetter(QuestTask::label)
    ).apply(inst, QuestTask::new));

    public static boolean matchesItem(String target, ItemStack stack) {
        if (target.isEmpty() || stack.isEmpty()) {
            return false;
        }
        if (target.startsWith("#")) {
            return stack.is(TagKey.create(Registries.ITEM, ResourceLocation.parse(target.substring(1))));
        }
        return BuiltInRegistries.ITEM.getKey(stack.getItem()).equals(ResourceLocation.parse(target));
    }

    public static boolean matchesBlock(String target, BlockState state) {
        if (target.isEmpty()) {
            return false;
        }
        if (target.startsWith("#")) {
            return state.is(TagKey.create(Registries.BLOCK, ResourceLocation.parse(target.substring(1))));
        }
        return BuiltInRegistries.BLOCK.getKey(state.getBlock()).equals(ResourceLocation.parse(target));
    }

    /** Название цели для показа игроку. */
    public Component displayName() {
        if (label.isPresent()) {
            return label.get();
        }
        if (target.isEmpty() || target.startsWith("#")) {
            return Component.literal(target);
        }
        ResourceLocation id = ResourceLocation.parse(target);
        return switch (type) {
            case MINE, PLACE -> BuiltInRegistries.BLOCK.get(id).getName();
            default -> BuiltInRegistries.ITEM.get(id).getDescription();
        };
    }
}
