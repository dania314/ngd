package com.techquest.quest;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/** Один квест. Лежит в data/techquest/techquest/quest/*.json. */
public record Quest(Component title, Component description, Item icon, int x, int y,
                    List<ResourceLocation> dependencies, QuestTask task, List<ItemStack> rewards) {

    public static final Codec<Quest> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            ComponentSerialization.CODEC.fieldOf("title").forGetter(Quest::title),
            ComponentSerialization.CODEC.fieldOf("description").forGetter(Quest::description),
            BuiltInRegistries.ITEM.byNameCodec().fieldOf("icon").forGetter(Quest::icon),
            Codec.INT.optionalFieldOf("x", 0).forGetter(Quest::x),
            Codec.INT.optionalFieldOf("y", 0).forGetter(Quest::y),
            ResourceLocation.CODEC.listOf().optionalFieldOf("dependencies", List.of()).forGetter(Quest::dependencies),
            QuestTask.CODEC.fieldOf("task").forGetter(Quest::task),
            ItemStack.CODEC.listOf().optionalFieldOf("rewards", List.of()).forGetter(Quest::rewards)
    ).apply(inst, Quest::new));
}
