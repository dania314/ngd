package com.techquest.block;

import com.mojang.serialization.MapCodec;
import com.techquest.block.entity.DrillControllerBlockEntity;
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

/**
 * Контроллер бура. Shift+ПКМ — собрать/разобрать мультиблок.
 * ПКМ без shift — вкл/выкл бурение (если собран).
 */
public class DrillControllerBlock extends BaseEntityBlock {
    public static final MapCodec<DrillControllerBlock> CODEC = simpleCodec(DrillControllerBlock::new);
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty FORMED = BooleanProperty.create("formed");
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

    public DrillControllerBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(FORMED, false)
                .setValue(ACTIVE, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, FORMED, ACTIVE);
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
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof DrillControllerBlockEntity drill)) {
            return InteractionResult.PASS;
        }

        if (player.isShiftKeyDown()) {
            // Assemble / disassemble
            if (state.getValue(FORMED)) {
                drill.disassemble();
                level.setBlock(pos, state.setValue(FORMED, false).setValue(ACTIVE, false), Block.UPDATE_ALL);
                player.displayClientMessage(Component.translatable("message.techquest.drill.disassembled"), true);
            } else {
                if (ModMultiblocks.matchesDrill(level, pos)) {
                    drill.assemble(state.getValue(FACING));
                    level.setBlock(pos, state.setValue(FORMED, true), Block.UPDATE_ALL);
                    player.displayClientMessage(Component.translatable("message.techquest.drill.assembled"), true);
                } else {
                    player.displayClientMessage(Component.translatable("message.techquest.drill.invalid"), true);
                    // Show steps
                    player.displayClientMessage(Component.translatable("message.techquest.drill.hint1"), false);
                    player.displayClientMessage(Component.translatable("message.techquest.drill.hint2"), false);
                    player.displayClientMessage(Component.translatable("message.techquest.drill.hint3"), false);
                }
            }
            return InteractionResult.CONSUME;
        }

        // Toggle active
        if (state.getValue(FORMED)) {
            boolean next = !state.getValue(ACTIVE);
            level.setBlock(pos, state.setValue(ACTIVE, next), Block.UPDATE_ALL);
            player.displayClientMessage(Component.translatable(
                    next ? "message.techquest.drill.on" : "message.techquest.drill.off"), true);
            return InteractionResult.CONSUME;
        }

        player.displayClientMessage(Component.translatable("message.techquest.drill.not_formed"), true);
        return InteractionResult.CONSUME;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DrillControllerBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                  BlockEntityType<T> type) {
        if (level.isClientSide) return null;
        return createTickerHelper(type, ModBlockEntities.DRILL_CONTROLLER.get(),
                DrillControllerBlockEntity::serverTick);
    }
}
