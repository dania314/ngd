package com.techquest.block.entity;

import com.techquest.registry.ModBlockEntities;
import com.techquest.registry.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

/** Электропечь: ванильные рецепты плавки, но вдвое быстрее и без топлива. */
public class ElectricFurnaceBlockEntity extends ProcessingBlockEntity {
    private final RecipeManager.CachedCheck<SingleRecipeInput, SmeltingRecipe> check =
            RecipeManager.createCheck(RecipeType.SMELTING);

    public ElectricFurnaceBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ELECTRIC_FURNACE.get(), pos, state, 25);
    }

    @Override
    protected Optional<Result> findRecipe(Level level, ItemStack input) {
        SingleRecipeInput recipeInput = new SingleRecipeInput(input);
        return check.getRecipeFor(recipeInput, level).map(holder -> new Result(
                holder.value().assemble(recipeInput, level.registryAccess()),
                Math.max(20, holder.value().getCookingTime() / 2)));
    }

    @Override
    protected MenuType<?> menuType() {
        return ModMenus.ELECTRIC_FURNACE.get();
    }
}
