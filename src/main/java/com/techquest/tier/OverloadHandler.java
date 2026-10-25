package com.techquest.tier;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;

/** Если в LV-машину за тик пришло больше safeTransfer — шанс сгорания. */
public final class OverloadHandler {
    public static void check(Level level, BlockPos pos, int receivedThisTick, EnergyTier machineTier) {
        if (level.isClientSide || receivedThisTick <= 0) return;
        int safe = machineTier.transfer();
        if (receivedThisTick <= safe * 2) return;
        // Перегруз: 5% шанс сломать блок при сильном превышении
        if (level.random.nextInt(20) == 0 && level instanceof ServerLevel) {
            level.destroyBlock(pos, true);
            level.setBlock(pos, Blocks.FIRE.defaultBlockState(), 3);
        }
    }

    private OverloadHandler() {}
}
