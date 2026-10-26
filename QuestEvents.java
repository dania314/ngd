package com.techquest.quest;

import com.techquest.TechQuest;
import com.techquest.registry.ModItems;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/** Подписки на игровые события, которые двигают квесты. */
@EventBusSubscriber(modid = TechQuest.MODID)
public final class QuestEvents {

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            QuestProgress progress = QuestManager.progress(player);
            if (!progress.isBookGiven()) {
                progress.setBookGiven(true);
                ItemStack book = new ItemStack(ModItems.QUEST_BOOK.get());
                player.getInventory().add(book);
                if (!book.isEmpty()) {
                    player.drop(book, false);
                }
            }
            QuestManager.sync(player);
        }
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            QuestManager.sync(player);
        }
    }

    @SubscribeEvent
    public static void onChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            QuestManager.sync(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player && player.tickCount % 20 == 0) {
            QuestManager.tick(player);
        }
    }

    @SubscribeEvent
    public static void onCrafted(PlayerEvent.ItemCraftedEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ItemStack crafted = event.getCrafting();
            QuestManager.addProgress(player, QuestTask.Type.CRAFT,
                    target -> QuestTask.matchesItem(target, crafted), crafted.getCount());
        }
    }

    @SubscribeEvent
    public static void onSmelted(PlayerEvent.ItemSmeltedEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ItemStack smelted = event.getSmelting();
            QuestManager.addProgress(player, QuestTask.Type.SMELT,
                    target -> QuestTask.matchesItem(target, smelted), smelted.getCount());
        }
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getPlayer() instanceof ServerPlayer player) {
            var state = event.getState();
            QuestManager.addProgress(player, QuestTask.Type.MINE,
                    target -> QuestTask.matchesBlock(target, state), 1);
        }
    }

    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            var state = event.getPlacedBlock();
            QuestManager.addProgress(player, QuestTask.Type.PLACE,
                    target -> QuestTask.matchesBlock(target, state), 1);
        }
    }

    private QuestEvents() {}
}
