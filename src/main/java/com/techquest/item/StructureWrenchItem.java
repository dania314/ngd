package com.techquest.item;

import com.techquest.block.AlloySmelterBlock;
import com.techquest.block.DrillControllerBlock;
import com.techquest.block.entity.AlloySmelterBlockEntity;
import com.techquest.block.entity.DrillControllerBlockEntity;
import com.techquest.ecology.BlastFurnaceBlockEntity;
import com.techquest.multiblock.ModMultiblocks;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Ключ структур: Shift+ПКМ по контроллеру — собрать/разобрать мультиблок. */
public class StructureWrenchItem extends Item {
    public StructureWrenchItem(Properties props) {
        super(props.stacksTo(1).durability(256));
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Level level = ctx.getLevel();
        if (level.isClientSide) return InteractionResult.SUCCESS;
        BlockPos pos = ctx.getClickedPos();
        Player player = ctx.getPlayer();
        BlockEntity be = level.getBlockEntity(pos);
        BlockState state = level.getBlockState(pos);

        if (be instanceof BlastFurnaceBlockEntity blast) {
            boolean ok = blast.tryForm(player);
            // durability optional

            return ok ? InteractionResult.CONSUME : InteractionResult.FAIL;
        }

        if (be instanceof DrillControllerBlockEntity drill) {
            // reuse existing assemble via simulating shift click logic
            if (state.getBlock() instanceof DrillControllerBlock) {
                player.displayClientMessage(Component.translatable("message.techquest.wrench.drill"), true);
            }
            return InteractionResult.CONSUME;
        }

        if (be instanceof AlloySmelterBlockEntity alloy) {
            player.displayClientMessage(Component.translatable("message.techquest.wrench.alloy"), true);
            return InteractionResult.CONSUME;
        }

        player.displayClientMessage(Component.translatable("message.techquest.wrench.none"), true);
        return InteractionResult.FAIL;
    }
}
