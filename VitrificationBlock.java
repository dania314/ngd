package com.techquest.ecology;

import com.mojang.serialization.MapCodec;
import com.techquest.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class VitrificationBlock extends BaseEntityBlock {
    public static final MapCodec<VitrificationBlock> CODEC = simpleCodec(VitrificationBlock::new);
    public VitrificationBlock(Properties p) { super(p); }
    @Override protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }
    @Override protected RenderShape getRenderShape(BlockState s) { return RenderShape.MODEL; }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hit) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof VitrificationBlockEntity be) {
            player.displayClientMessage(Component.translatable("message.techquest.vitrify.status",
                    be.getEnergyStorage().getEnergyStored(), be.getProgress()), true);
            if (player.isShiftKeyDown()) {
                var stack = be.getItems().extractItem(2, 64, false);
                if (!stack.isEmpty()) {
                    if (!player.addItem(stack)) player.drop(stack, false);
                }
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) return ItemInteractionResult.SUCCESS;
        if (level.getBlockEntity(pos) instanceof VitrificationBlockEntity be) {
            for (int i = 0; i < 2; i++) {
                ItemStack left = be.getItems().insertItem(i, stack.copy(), false);
                int taken = stack.getCount() - left.getCount();
                if (taken > 0) { stack.shrink(taken); return ItemInteractionResult.CONSUME; }
            }
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Nullable @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new VitrificationBlockEntity(pos, state);
    }

    @Nullable @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level l, BlockState s, BlockEntityType<T> t) {
        if (l.isClientSide) return null;
        return createTickerHelper(t, ModBlockEntities.VITRIFICATION.get(), VitrificationBlockEntity::serverTick);
    }
}
