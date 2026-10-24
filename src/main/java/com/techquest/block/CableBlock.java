package com.techquest.block;

import com.mojang.serialization.MapCodec;
import com.techquest.block.entity.CableBlockEntity;
import com.techquest.block.entity.EnergyBufferBlockEntity;
import com.techquest.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
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
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.capabilities.Capabilities;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;

/** Кабель: сам подключается ко всем соседям, которые умеют работать с энергией. */
public class CableBlock extends BaseEntityBlock {
    public static final MapCodec<CableBlock> CODEC = simpleCodec(CableBlock::new);

    private static final Map<Direction, BooleanProperty> PROPERTIES = new EnumMap<>(Direction.class);
    private static final Map<Direction, VoxelShape> ARMS = new EnumMap<>(Direction.class);
    private static final VoxelShape CORE = Block.box(5, 5, 5, 11, 11, 11);

    static {
        PROPERTIES.put(Direction.NORTH, BlockStateProperties.NORTH);
        PROPERTIES.put(Direction.EAST, BlockStateProperties.EAST);
        PROPERTIES.put(Direction.SOUTH, BlockStateProperties.SOUTH);
        PROPERTIES.put(Direction.WEST, BlockStateProperties.WEST);
        PROPERTIES.put(Direction.UP, BlockStateProperties.UP);
        PROPERTIES.put(Direction.DOWN, BlockStateProperties.DOWN);

        ARMS.put(Direction.NORTH, Block.box(5, 5, 0, 11, 11, 5));
        ARMS.put(Direction.SOUTH, Block.box(5, 5, 11, 11, 11, 16));
        ARMS.put(Direction.EAST, Block.box(11, 5, 5, 16, 11, 11));
        ARMS.put(Direction.WEST, Block.box(0, 5, 5, 5, 11, 11));
        ARMS.put(Direction.UP, Block.box(5, 11, 5, 11, 16, 11));
        ARMS.put(Direction.DOWN, Block.box(5, 0, 5, 11, 5, 11));
    }

    public CableBlock(Properties properties) {
        super(properties);
        BlockState state = stateDefinition.any();
        for (BooleanProperty property : PROPERTIES.values()) {
            state = state.setValue(property, false);
        }
        registerDefaultState(state);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.NORTH, BlockStateProperties.EAST, BlockStateProperties.SOUTH,
                BlockStateProperties.WEST, BlockStateProperties.UP, BlockStateProperties.DOWN);
    }

    private BlockState computeState(Level level, BlockPos pos) {
        BlockState state = defaultBlockState();
        for (Direction dir : Direction.values()) {
            boolean connect = level.getCapability(Capabilities.EnergyStorage.BLOCK,
                    pos.relative(dir), dir.getOpposite()) != null;
            state = state.setValue(PROPERTIES.get(dir), connect);
        }
        return state;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return computeState(context.getLevel(), context.getClickedPos());
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock,
                                   BlockPos neighborPos, boolean movedByPiston) {
        if (level.isClientSide) {
            return;
        }
        BlockState updated = computeState(level, pos);
        if (updated != state) {
            level.setBlock(pos, updated, Block.UPDATE_ALL);
        }
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        VoxelShape shape = CORE;
        for (Direction dir : Direction.values()) {
            if (state.getValue(PROPERTIES.get(dir))) {
                shape = Shapes.or(shape, ARMS.get(dir));
            }
        }
        return shape;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CableBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                  BlockEntityType<T> type) {
        if (level.isClientSide) {
            return null;
        }
        return createTickerHelper(type, ModBlockEntities.TIN_CABLE.get(), EnergyBufferBlockEntity::serverTick);
    }
}
