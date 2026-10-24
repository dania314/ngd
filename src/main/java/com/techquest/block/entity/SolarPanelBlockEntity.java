package com.techquest.block.entity;

import com.techquest.energy.EnergyUtil;
import com.techquest.energy.MachineEnergyStorage;
import com.techquest.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.IEnergyStorage;

/** Солнечная панель: 4 FE/т днём под открытым небом без дождя. */
public class SolarPanelBlockEntity extends BlockEntity {
    public static final int CAPACITY = 2_000;
    public static final int GENERATION = 4;
    public static final int OUTPUT = 40;

    private final MachineEnergyStorage energy = new MachineEnergyStorage(CAPACITY, 0, OUTPUT);

    public SolarPanelBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SOLAR_PANEL.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, SolarPanelBlockEntity be) {
        if (level.isDay() && !level.isRaining() && level.canSeeSky(pos.above())) {
            be.energy.addInternal(GENERATION);
        }
        EnergyUtil.push(level, pos, be.energy, OUTPUT, false);
        if (level.getGameTime() % 20 == 0) {
            be.setChanged();
        }
    }

    public IEnergyStorage getEnergyStorage() {
        return energy;
    }

    public int getStored() {
        return energy.getEnergyStored();
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
