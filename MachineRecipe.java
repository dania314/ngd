package com.techquest.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.techquest.registry.ModRecipes;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;

import java.util.Optional;

/**
 * Универсальный рецепт «один предмет → один результат» для наших машин.
 * Поле machine выбирает станок ("macerator", "compressor"), time — длительность в тиках.
 */
public record MachineRecipe(String machine, Ingredient ingredient, ItemStack result, int time)
        implements Recipe<SingleRecipeInput> {

    public static Optional<MachineRecipe> find(Level level, String machine, ItemStack input) {
        SingleRecipeInput in = new SingleRecipeInput(input);
        for (RecipeHolder<MachineRecipe> holder : level.getRecipeManager().getAllRecipesFor(ModRecipes.MACHINE_TYPE.get())) {
            MachineRecipe recipe = holder.value();
            if (recipe.machine().equals(machine) && recipe.matches(in, level)) {
                return Optional.of(recipe);
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean matches(SingleRecipeInput input, Level level) {
        return ingredient.test(input.item());
    }

    @Override
    public ItemStack assemble(SingleRecipeInput input, HolderLookup.Provider registries) {
        return result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return result;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> list = NonNullList.create();
        list.add(ingredient);
        return list;
    }

    /** Не показываем в книге рецептов верстака. */
    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.MACHINE_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.MACHINE_TYPE.get();
    }

    public static class Serializer implements RecipeSerializer<MachineRecipe> {
        public static final MapCodec<MachineRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                Codec.STRING.fieldOf("machine").forGetter(MachineRecipe::machine),
                Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter(MachineRecipe::ingredient),
                ItemStack.CODEC.fieldOf("result").forGetter(MachineRecipe::result),
                Codec.INT.optionalFieldOf("time", 100).forGetter(MachineRecipe::time)
        ).apply(inst, MachineRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, MachineRecipe> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8, MachineRecipe::machine,
                Ingredient.CONTENTS_STREAM_CODEC, MachineRecipe::ingredient,
                ItemStack.STREAM_CODEC, MachineRecipe::result,
                ByteBufCodecs.VAR_INT, MachineRecipe::time,
                MachineRecipe::new);

        @Override
        public MapCodec<MachineRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, MachineRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
