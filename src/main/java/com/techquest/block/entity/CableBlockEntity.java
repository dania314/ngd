package com.techquest.block.entity;

import com.techquest.block.CableBlock;
import com.techquest.energy.MachineEnergyStorage;
import com.techquest.registry.ModBlockEntities;
import com.techquest.tier.EnergyTier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;

public class CableBlockEntity extends BlockEntity {
    private final EnergyTier tier;
    private final MachineEnergyStorage energy;

    public CableBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TIN_CABLE.get(), pos, state);
        this.tier = state.getBlock() instanceof CableBlock cb ? cb.getTier() : EnergyTier.LV;
        int cap = Math.max(64, this.tier.transfer() * 4);
        this.energy = new MachineEnergyStorage(cap, this.tier.transfer(), this.tier.transfer());
    }

    public EnergyTier getTier() { return tier; }
    public IEnergyStorage getEnergyStorage() { return energy; }

    public static void serverTick(Level level, BlockPos pos, BlockState state, CableBlockEntity be) {
        if (be.energy.getEnergyStored() <= 0) return;
        int maxPer = be.tier.transfer();
        double lossRate = switch (be.tier) {
            case LV -> 0.02;
            case MV -> 0.01;
            case HV -> 0.005;
            case EV -> 0.0;
        };

        for (Direction dir : Direction.values()) {
            BlockPos n = pos.relative(dir);
            IEnergyStorage target = level.getCapability(Capabilities.EnergyStorage.BLOCK, n, dir.getOpposite());
            if (target == null || !target.canReceive()) continue;

            int offer = be.energy.extractEnergy(maxPer, true);
            if (offer <= 0) break;

            int net = offer - (int) Math.floor(offer * lossRate);
            if (net <= 0) continue;

            int accepted = target.receiveEnergy(net, false);
            if (accepted > 0) {
                int totalDrain = accepted + (int) Math.ceil(accepted * lossRate);
                be.energy.extractEnergy(Math.min(totalDrain, offer), false);
            }
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
