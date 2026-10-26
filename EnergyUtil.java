package com.techquest.energy;

import com.techquest.block.entity.EnergyBufferBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;

public final class EnergyUtil {

    /**
     * Раздаёт энергию соседям, которые умеют принимать FE.
     *
     * @param bufferRule true для кабелей и батарей: между буферами энергия течёт только
     *                   «вниз» (туда, где заполнение меньше), чтобы она не бегала туда-сюда.
     */
    public static void push(Level level, BlockPos pos, MachineEnergyStorage source, int maxPerTarget,
                            boolean bufferRule) {
        if (source.getEnergyStored() <= 0 || source.getMaxEnergyStored() <= 0) {
            return;
        }
        double ownFill = (double) source.getEnergyStored() / source.getMaxEnergyStored();
        for (Direction dir : Direction.values()) {
            BlockPos neighbor = pos.relative(dir);
            IEnergyStorage target = level.getCapability(Capabilities.EnergyStorage.BLOCK, neighbor, dir.getOpposite());
            if (target == null || !target.canReceive()) {
                continue;
            }
            if (bufferRule && level.getBlockEntity(neighbor) instanceof EnergyBufferBlockEntity other
                    && other.fillFraction() >= ownFill) {
                continue;
            }
            int offer = source.extractEnergy(maxPerTarget, true);
            if (offer <= 0) {
                return;
            }
            int accepted = target.receiveEnergy(offer, false);
            if (accepted > 0) {
                source.extractEnergy(accepted, false);
            }
        }
    }

    private EnergyUtil() {}
}
