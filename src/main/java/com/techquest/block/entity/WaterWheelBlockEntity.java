package com.techquest.block.entity;

import com.techquest.energy.EnergyUtil;
import com.techquest.energy.MachineEnergyStorage;
import com.techquest.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.energy.IEnergyStorage;

/** Водяное колесо: FE от соседней текущей воды. */
public class WaterWheelBlockEntity extends BlockEntity {
    public static final int CAPACITY = 5000;
    public static final int MAX_OUTPUT = 40;
    public static final int ENERGY_PER_FLOWING = 6; // за каждый соседний текущий блок воды
    public static final int ENERGY_PER_SOURCE = 2;  // за источник

    private final MachineEnergyStorage energy = new MachineEnergyStorage(CAPACITY, 0, MAX_OUTPUT);

    public WaterWheelBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.WATER_WHEEL.get(), pos, state);
    }

    public IEnergyStorage getEnergyStorage() {
        return energy;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, WaterWheelBlockEntity be) {
        int flowing = 0;
        int sources = 0;
        for (Direction dir : Direction.values()) {
            if (dir == Direction.UP) continue;
            BlockPos n = pos.relative(dir);
            FluidState fluid = level.getFluidState(n);
            if (fluid.is(net.minecraft.world.level.material.Fluids.WATER) || fluid.is(net.minecraft.world.level.material.Fluids.FLOWING_WATER)) {
                if (fluid.getAmount() == 8 && !fluid.isSource()) {
                    // flowing-ish
                }
                if (fluid.isSource()) {
                    sources++;
                } else if (!fluid.isEmpty()) {
                    flowing++;
                }
            }
            // Also check vanilla water blocks
            BlockState bs = level.getBlockState(n);
            if (bs.is(Blocks.WATER)) {
                if (bs.getFluidState().isSource()) sources++;
                else flowing++;
            }
        }
        int gen = flowing * ENERGY_PER_FLOWING + sources * ENERGY_PER_SOURCE;
        if (gen > 0) {
            be.energy.addInternal(Math.min(gen, 30)); // cap
        }
        EnergyUtil.push(level, pos, be.energy, MAX_OUTPUT, false);
        if (level.getGameTime() % 40 == 0) {
            be.setChanged();
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("Energy", energy.getEnergyStored());
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        energy.setEnergy(tag.getInt("Energy"));
    }
}
