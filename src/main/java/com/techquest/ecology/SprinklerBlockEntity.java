package com.techquest.ecology;

import com.techquest.conveyor.ConveyorBlock;
import com.techquest.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Спринклер: при редстоуне тушит огонь 5×5×3 и снимает burning с конвейеров. */
public class SprinklerBlockEntity extends BlockEntity {
    public SprinklerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SPRINKLER.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, SprinklerBlockEntity be) {
        if (!state.getValue(SprinklerBlock.POWERED)) return;
        if (level.getGameTime() % 10 != 0) return;

        if (level instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.FALLING_WATER,
                    pos.getX() + 0.5, pos.getY() - 0.1, pos.getZ() + 0.5,
                    8, 1.5, 0.2, 1.5, 0.05);
        }

        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = -2; dy <= 1; dy++) {
                for (int dz = -2; dz <= 2; dz++) {
                    BlockPos p = pos.offset(dx, dy, dz);
                    BlockState st = level.getBlockState(p);
                    if (st.is(Blocks.FIRE) || st.is(Blocks.SOUL_FIRE)) {
                        level.removeBlock(p, false);
                    }
                    if (st.getBlock() instanceof ConveyorBlock && st.hasProperty(ConveyorBlock.BURNING)
                            && st.getValue(ConveyorBlock.BURNING)) {
                        level.setBlock(p, st.setValue(ConveyorBlock.BURNING, false), 3);
                    }
                }
            }
        }
    }
}
