package com.techquest.tier;

import com.techquest.energy.EnergyUtil;
import com.techquest.energy.MachineEnergyStorage;
import com.techquest.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.IEnergyStorage;

/**
 * Трансформатор: буфер FE, пушит соседям.
 * Shift+ПКМ — режим step-up / step-down (лимит отдачи).
 */
public class TransformerBlockEntity extends BlockEntity {
    public static final int CAPACITY = 100_000;
    private final MachineEnergyStorage energy = new MachineEnergyStorage(CAPACITY, 2048, 2048);
    /** 0=LV out 32, 1=MV 128, 2=HV 512, 3=EV 2048 */
    private int mode = 1;

    public TransformerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TRANSFORMER.get(), pos, state);
    }

    public IEnergyStorage getEnergyStorage() { return energy; }

    public void cycleMode() {
        mode = (mode + 1) % 4;
        setChanged();
    }

    public String getModeName() {
        return EnergyTier.values()[mode].name();
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, TransformerBlockEntity be) {
        int limit = EnergyTier.values()[be.mode].transfer();
        EnergyUtil.push(level, pos, be.energy, limit, false);
    }

    @Override protected void saveAdditional(CompoundTag tag, HolderLookup.Provider r) {
        super.saveAdditional(tag, r);
        tag.putInt("Energy", energy.getEnergyStored());
        tag.putInt("Mode", mode);
    }
    @Override protected void loadAdditional(CompoundTag tag, HolderLookup.Provider r) {
        super.loadAdditional(tag, r);
        energy.setEnergy(tag.getInt("Energy"));
        mode = tag.getInt("Mode");
    }
}
