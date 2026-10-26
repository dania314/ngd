package com.techquest.block;

import com.mojang.serialization.MapCodec;
import com.techquest.block.entity.CableBlockEntity;
import com.techquest.registry.ModBlockEntities;
import com.techquest.tier.EnergyTier;
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

/** Кабель с тиром: лимит передачи + потери. */
public class CableBlock extends BaseEntityBlock {
    public static final MapCodec<CableBlock> CODEC = simpleCodec(p -> new CableBlock(p, EnergyTier.LV));

    private static final Map<Direction, BooleanProperty> PROPERTIES = new EnumMap<>(Direction.class);
    private static final Map<Direction, VoxelShape> ARMS = new EnumMap<>(Direction.class);
    private static final VoxelShape CORE = Block.box(6, 6, 6, 10, 10, 10);

    static {
        PROPERTIES.put(Direction.NORTH, BlockStateProperties.NORTH);
        PROPERTIES.put(Direction.EAST, BlockStateProperties.EAST);
        PROPERTIES.put(Direction.SOUTH, BlockStateProperties.SOUTH);
        PROPERTIES.put(Direction.WEST, BlockStateProperties.WEST);
        PROPERTIES.put(Direction.UP, BlockStateProperties.UP);
        PROPERTIES.put(Direction.DOWN, BlockStateProperties.DOWN);
        ARMS.put(Direction.NORTH, Block.box(6, 6, 0, 10, 10, 6));
        ARMS.put(Direction.SOUTH, Block.box(6, 6, 10, 10, 10, 16));
        ARMS.put(Direction.EAST, Block.box(10, 6, 6, 16, 10, 10));
        ARMS.put(Direction.WEST, Block.box(0, 6, 6, 6, 10, 10));
        ARMS.put(Direction.UP, Block.box(6, 10, 6, 10, 16, 10));
        ARMS.put(Direction.DOWN, Block.box(6, 0, 6, 10, 6, 10));
    }

    private final EnergyTier tier;

    public CableBlock(Properties properties) {
        this(properties, EnergyTier.LV);
    }

    public CableBlock(Properties properties, EnergyTier tier) {
        super(properties);
        this.tier = tier;
        BlockState s = stateDefinition.any();
        for (BooleanProperty p : PROPERTIES.values()) s = s.setValue(p, false);
        registerDefaultState(s);
    }

    public EnergyTier getTier() { return tier; }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) {
        PROPERTIES.values().forEach(b::add);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return withConnections(defaultBlockState(), ctx.getLevel(), ctx.getClickedPos());
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos from, boolean m) {
        if (!level.isClientSide) {
            BlockState ns = withConnections(state, level, pos);
            if (ns != state) level.setBlock(pos, ns, Block.UPDATE_ALL);
        }
    }

    private BlockState withConnections(BlockState state, Level level, BlockPos pos) {
        for (Direction d : Direction.values()) {
            boolean conn = canConnect(level, pos.relative(d), d.getOpposite());
            state = state.setValue(PROPERTIES.get(d), conn);
        }
        return state;
    }

    private static boolean canConnect(Level level, BlockPos pos, Direction face) {
        if (level.getBlockState(pos).getBlock() instanceof CableBlock) return true;
        return level.getCapability(Capabilities.EnergyStorage.BLOCK, pos, face) != null;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        VoxelShape shape = CORE;
        for (Direction d : Direction.values()) {
            if (state.getValue(PROPERTIES.get(d))) shape = Shapes.or(shape, ARMS.get(d));
        }
        return shape;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CableBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) return null;
        return createTickerHelper(type, ModBlockEntities.TIN_CABLE.get(), CableBlockEntity::serverTick);
    }
}
