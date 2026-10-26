package com.techquest.block;

import com.mojang.serialization.MapCodec;
import com.techquest.block.entity.MaceratorBlockEntity;
import com.techquest.block.entity.ProcessingBlockEntity;
import com.techquest.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class MaceratorBlock extends ProcessingMachineBlock {
    public static final MapCodec<MaceratorBlock> CODEC = simpleCodec(MaceratorBlock::new);

    public MaceratorBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected BlockEntityType<? extends ProcessingBlockEntity> machineEntityType() {
        return ModBlockEntities.MACERATOR.get();
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MaceratorBlockEntity(pos, state);
    }
}
