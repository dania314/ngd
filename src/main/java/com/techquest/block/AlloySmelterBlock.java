package com.techquest.block;

import com.mojang.serialization.MapCodec;
import com.techquest.block.entity.AlloySmelterBlockEntity;
import com.techquest.multiblock.ModMultiblocks;
import com.techquest.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/** Контроллер сплавной печи. Shift+ПКМ — собрать мультиблок (casing снизу и сверху). */
public class AlloySmelterBlock extends BaseEntityBlock {
    public static final MapCodec<AlloySmelterBlock> CODEC = simpleCodec(AlloySmelterBlock::new);
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty FORMED = BooleanProperty.create("formed");
    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    public AlloySmelterBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(FORMED, false)
                .setValue(LIT, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, FORMED, LIT);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof AlloySmelterBlockEntity smelter)) return InteractionResult.PASS;

        if (player.isShiftKeyDown()) {
            if (state.getValue(FORMED)) {
                smelter.disassemble();
                level.setBlock(pos, state.setValue(FORMED, false).setValue(LIT, false), Block.UPDATE_ALL);
                player.displayClientMessage(Component.translatable("message.techquest.multiblock.disassembled"), true);
            } else {
                Direction facing = state.getValue(FACING);
                if (ModMultiblocks.ALLOY_SMELTER().matches(level, pos, facing)) {
                    smelter.assemble();
                    level.setBlock(pos, state.setValue(FORMED, true), Block.UPDATE_ALL);
                    player.displayClientMessage(Component.translatable("message.techquest.multiblock.assembled"), true);
                } else {
                    player.displayClientMessage(Component.translatable("message.techquest.multiblock.invalid"), true);
                    for (String step : ModMultiblocks.ALLOY_SMELTER().getBuildSteps()) {
                        player.displayClientMessage(Component.literal(step), false);
                    }
                }
            }
            return InteractionResult.CONSUME;
        }

        if (state.getValue(FORMED)) {
            player.openMenu(smelter);
            return InteractionResult.CONSUME;
        }
        player.displayClientMessage(Component.translatable("message.techquest.multiblock.not_formed"), true);
        return InteractionResult.CONSUME;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AlloySmelterBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                  BlockEntityType<T> type) {
        if (level.isClientSide) return null;
        return createTickerHelper(type, ModBlockEntities.ALLOY_SMELTER.get(), AlloySmelterBlockEntity::serverTick);
    }
}
