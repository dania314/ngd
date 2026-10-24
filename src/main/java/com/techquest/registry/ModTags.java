package com.techquest.registry;

import com.techquest.TechQuest;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public final class ModTags {
    /** Топливо, которое принимает угольный генератор (уголь, древесный уголь, блок угля). */
    public static final TagKey<Item> COAL_GENERATOR_FUEL = TagKey.create(Registries.ITEM,
            ResourceLocation.fromNamespaceAndPath(TechQuest.MODID, "coal_generator_fuel"));

    private ModTags() {}
}
