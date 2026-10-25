package com.techquest.ecology;

import com.techquest.quarry.QuarryBlockEntity;
import com.techquest.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Гейгер: измеряет радиацию, даёт редстоун 0–15.
 * При rad > 40 — сбрасывает давление соседних котлов и выключает карьеры в радиусе 6.
 */
public class GeigerBlockEntity extends BlockEntity {
    private int redstoneLevel;
    private int lastMax;

    public GeigerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.GEIGER.get(), pos, state);
    }

    public int getRedstoneLevel() { return redstoneLevel; }

    public static void serverTick(Level level, BlockPos pos, BlockState state, GeigerBlockEntity be) {
        if (level.getGameTime() % 20 != 0) return;

        int max = 0;
        for (int dx = -4; dx <= 4; dx++)
            for (int dy = -2; dy <= 2; dy++)
                for (int dz = -4; dz <= 4; dz++)
                    max = Math.max(max, PollutionData.getRadiation(level, pos.offset(dx, dy, dz)));

        be.lastMax = max;
        int signal = Math.min(15, max * 15 / 100);
        if (signal != be.redstoneLevel) {
            be.redstoneLevel = signal;
            level.updateNeighborsAt(pos, state.getBlock());
            be.setChanged();
        }

        // auto emergency
        if (max > 40) {
            for (int dx = -6; dx <= 6; dx++) {
                for (int dy = -3; dy <= 3; dy++) {
                    for (int dz = -6; dz <= 6; dz++) {
                        BlockPos p = pos.offset(dx, dy, dz);
                        BlockEntity other = level.getBlockEntity(p);
                        if (other instanceof BoilerBlockEntity boiler) {
                            boiler.ventPressure(50);
                        }
                        if (other instanceof QuarryBlockEntity quarry && quarry.isActive()) {
                            quarry.toggle();
                        }
                    }
                }
            }
        }
    }
}
