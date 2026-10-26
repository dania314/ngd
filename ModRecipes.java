package com.techquest.registry;

import com.techquest.TechQuest;
import com.techquest.recipe.MachineRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModRecipes {
    public static final DeferredRegister<RecipeType<?>> TYPES =
            DeferredRegister.create(Registries.RECIPE_TYPE, TechQuest.MODID);
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, TechQuest.MODID);

    public static final DeferredHolder<RecipeType<?>, RecipeType<MachineRecipe>> MACHINE_TYPE =
            TYPES.register("machine",
                    () -> RecipeType.simple(ResourceLocation.fromNamespaceAndPath(TechQuest.MODID, "machine")));

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<MachineRecipe>> MACHINE_SERIALIZER =
            SERIALIZERS.register("machine", MachineRecipe.Serializer::new);

    private ModRecipes() {}
}
