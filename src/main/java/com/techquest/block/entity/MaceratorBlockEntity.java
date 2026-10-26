package com.techquest.block.entity;

import com.techquest.registry.ModBlockEntities;
import com.techquest.registry.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

/** Дробилка: руда → пыль (в 2 раза больше металла). */
public class MaceratorBlockEntity extends ProcessingBlockEntity {
    public MaceratorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MACERATOR.get(), pos, state, 20);
    }

    @Override
    protected Optional<Result> findRecipe(Level level, ItemStack input) {
        return findMachineRecipe(level, "macerator", input);
    }

    @Override
    protected MenuType<?> menuType() {
        return ModMenus.MACERATOR.get();
    }
}
