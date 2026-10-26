package com.techquest.item;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;

public class EnergyMeterItem extends Item {
    public EnergyMeterItem(Properties props) {
        super(props.stacksTo(1));
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Level level = ctx.getLevel();
        if (level.isClientSide) return InteractionResult.SUCCESS;
        BlockPos pos = ctx.getClickedPos();
        Player player = ctx.getPlayer();
        if (player == null) return InteractionResult.PASS;

        IEnergyStorage storage = level.getCapability(Capabilities.EnergyStorage.BLOCK, pos, ctx.getClickedFace());
        if (storage == null) {
            player.displayClientMessage(Component.translatable("message.techquest.meter.none"), true);
            return InteractionResult.FAIL;
        }
        player.displayClientMessage(Component.translatable("message.techquest.meter.read",
                storage.getEnergyStored(), storage.getMaxEnergyStored()), true);

        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof com.techquest.block.entity.CableBlockEntity cable) {
            player.displayClientMessage(Component.translatable("message.techquest.meter.tier",
                    cable.getTier().name(), cable.getTier().transfer()), true);
        }
        return InteractionResult.CONSUME;
    }
}
