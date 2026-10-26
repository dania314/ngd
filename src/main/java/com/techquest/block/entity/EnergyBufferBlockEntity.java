package com.techquest.block.entity;

import com.techquest.energy.EnergyUtil;
import com.techquest.energy.MachineEnergyStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.IEnergyStorage;

/** Основа для кабелей и батарей: принимает и отдаёт энергию. */
public abstract class EnergyBufferBlockEntity extends BlockEntity {
    protected final MachineEnergyStorage energy;
    protected final int transfer;
    private int lastSavedEnergy = -1;

    protected EnergyBufferBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state,
                                      int capacity, int transfer) {
        super(type, pos, state);
        this.energy = new MachineEnergyStorage(capacity, transfer, transfer);
        this.transfer = transfer;
    }

    public IEnergyStorage getEnergyStorage() {
        return energy;
    }

    public double fillFraction() {
        return (double) energy.getEnergyStored() / energy.getMaxEnergyStored();
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, EnergyBufferBlockEntity be) {
        EnergyUtil.push(level, pos, be.energy, be.transfer, true);
        if (level.getGameTime() % 20 == 0 && be.lastSavedEnergy != be.energy.getEnergyStored()) {
            be.lastSavedEnergy = be.energy.getEnergyStored();
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
