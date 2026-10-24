package com.techquest.block.entity;

import com.techquest.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class CableBlockEntity extends EnergyBufferBlockEntity {
    public static final int CAPACITY = 400;
    public static final int TRANSFER = 100;

    public CableBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TIN_CABLE.get(), pos, state, CAPACITY, TRANSFER);
    }
}
