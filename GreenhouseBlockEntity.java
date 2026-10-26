package com.techquest.farm;

import com.techquest.energy.MachineEnergyStorage;
import com.techquest.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.IEnergyStorage;

/** Теплица: раз в N тиков боунмил-эффект растениям в радиусе 2, тратит FE. */
public class GreenhouseBlockEntity extends BlockEntity {
    private final MachineEnergyStorage energy = new MachineEnergyStorage(20_000, 100, 0);
    private int timer;

    public GreenhouseBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.GREENHOUSE.get(), pos, state);
    }

    public IEnergyStorage getEnergyStorage() { return energy; }

    public static void serverTick(Level level, BlockPos pos, BlockState state, GreenhouseBlockEntity be) {
        if (!(level instanceof ServerLevel server)) return;
        be.timer++;
        if (be.timer < 80) return;
        be.timer = 0;
        if (be.energy.getEnergyStored() < 50) return;
        be.energy.removeInternal(50);
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                BlockPos p = pos.offset(dx, 0, dz);
                BlockState st = level.getBlockState(p);
                if (st.getBlock() instanceof BonemealableBlock growable) {
                    if (growable.isValidBonemealTarget(level, p, st) && level.random.nextInt(3) == 0) {
                        growable.performBonemeal(server, level.random, p, st);
                    }
                }
            }
        }
        be.setChanged();
    }

    @Override protected void saveAdditional(CompoundTag tag, HolderLookup.Provider r) {
        super.saveAdditional(tag, r);
        tag.putInt("Energy", energy.getEnergyStored());
    }
    @Override protected void loadAdditional(CompoundTag tag, HolderLookup.Provider r) {
        super.loadAdditional(tag, r);
        energy.setEnergy(tag.getInt("Energy"));
    }
}
