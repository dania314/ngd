package com.techquest.block.entity;

import com.techquest.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class BatteryBoxBlockEntity extends EnergyBufferBlockEntity {
    public static final int CAPACITY = 100_000;
    public static final int TRANSFER = 200;

    public BatteryBoxBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BATTERY_BOX.get(), pos, state, CAPACITY, TRANSFER);
    }

    public int getStored() {
        return energy.getEnergyStored();
    }
}
