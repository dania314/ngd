package com.techquest.block.entity;

import com.techquest.registry.ModBlockEntities;
import com.techquest.registry.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

/** Пресс: слитки → пластины. */
public class CompressorBlockEntity extends ProcessingBlockEntity {
    public CompressorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.COMPRESSOR.get(), pos, state, 30);
    }

    @Override
    protected Optional<Result> findRecipe(Level level, ItemStack input) {
        return findMachineRecipe(level, "compressor", input);
    }

    @Override
    protected MenuType<?> menuType() {
        return ModMenus.COMPRESSOR.get();
    }
}
