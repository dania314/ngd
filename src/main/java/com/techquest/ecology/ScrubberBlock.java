package com.techquest.ecology;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/** Промышленный фильтр: ПКМ показывает уровень радиации рядом и чистит немного. */
public class ScrubberBlock extends Block {
    public static final MapCodec<ScrubberBlock> CODEC = simpleCodec(ScrubberBlock::new);

    public ScrubberBlock(Properties props) { super(props); }

    @Override protected MapCodec<? extends Block> codec() { return CODEC; }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hit) {
        if (!level.isClientSide) {
            int rad = PollutionData.getRadiation(level, pos);
            PollutionData.clean(level, pos, 5);
            for (int dx = -2; dx <= 2; dx++)
                for (int dz = -2; dz <= 2; dz++)
                    PollutionData.clean(level, pos.offset(dx, 0, dz), 2);
            player.displayClientMessage(Component.translatable("message.techquest.scrubber", rad), true);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
