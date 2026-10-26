package com.techquest.chemistry;

import com.mojang.serialization.MapCodec;
import com.techquest.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

import java.util.List;

/** Нефтяной песок/залежь: дропает crude_oil. */
public class OilDepositBlock extends Block {
    public static final MapCodec<OilDepositBlock> CODEC = simpleCodec(OilDepositBlock::new);

    public OilDepositBlock(Properties props) {
        super(props.mapColor(MapColor.COLOR_BLACK).strength(1.2F).sound(SoundType.SAND));
    }

    public OilDepositBlock() {
        this(Properties.of());
    }

    @Override
    protected MapCodec<? extends Block> codec() { return CODEC; }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        return List.of(new ItemStack(ModItems.CRUDE_OIL.get(), 1 + builder.getLevel().random.nextInt(2)));
    }
}
