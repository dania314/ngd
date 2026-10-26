package com.techquest.ecology;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/** Предохранительный клапан: рядом с котлом сбрасывает давление (пар). */
public class SafetyValveBlock extends Block {
    public static final MapCodec<SafetyValveBlock> CODEC = simpleCodec(SafetyValveBlock::new);
    public SafetyValveBlock(Properties p) { super(p); }
    @Override protected MapCodec<? extends Block> codec() { return CODEC; }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        // визуальный пар если рядом горячий котёл
        for (Direction d : Direction.values()) {
            if (level.getBlockEntity(pos.relative(d)) instanceof BoilerBlockEntity boiler) {
                if (boiler.getPressurePublic() > 200) {
                    level.sendParticles(ParticleTypes.CLOUD,
                            pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
                            3, 0.1, 0.15, 0.1, 0.02);
                }
            }
        }
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) { return true; }
}
