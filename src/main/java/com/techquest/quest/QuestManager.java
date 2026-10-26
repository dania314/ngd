package com.techquest.quest;

import com.techquest.network.QuestSyncPayload;
import com.techquest.registry.ModAttachments;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Map;
import java.util.function.Predicate;

/** Вся серверная логика квестов. */
public final class QuestManager {

    public static QuestProgress progress(Player player) {
        return player.getData(ModAttachments.QUEST_PROGRESS);
    }

    public static Registry<Quest> registry(Level level) {
        return level.registryAccess().registryOrThrow(QuestRegistries.QUEST_KEY);
    }

    /** Квест открыт, когда получены награды за все квесты-зависимости. */
    public static boolean isUnlocked(QuestProgress progress, Quest quest) {
        for (ResourceLocation dependency : quest.dependencies()) {
            if (!progress.isClaimed(dependency)) {
                return false;
            }
        }
        return true;
    }

    public static void sync(ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, new QuestSyncPayload(progress(player).copy()));
    }

    /** Раз в секунду: проверяем задания, которые зависят от состояния (предметы в инвентаре и т.п.). */
    public static void tick(ServerPlayer player) {
        Registry<Quest> registry = registry(player.level());
        QuestProgress progress = progress(player);
        boolean changed = false;

        for (Map.Entry<ResourceKey<Quest>, Quest> entry : registry.entrySet()) {
            ResourceLocation id = entry.getKey().location();
            Quest quest = entry.getValue();
            if (progress.isCompleted(id) || !isUnlocked(progress, quest)) {
                continue;
            }
            QuestTask task = quest.task();
            if (task.type() == QuestTask.Type.CHECKMARK) {
                complete(player, id, quest, progress);
                changed = true;
            } else if (task.type() == QuestTask.Type.HAS_ITEM) {
                int have = Math.min(countItems(player, task.target()), task.count());
                if (have != progress.getCounter(id)) {
                    progress.setCounter(id, have);
                    changed = true;
                }
                if (have >= task.count()) {
                    complete(player, id, quest, progress);
                    changed = true;
                }
            }
        }
        if (changed) {
            sync(player);
        }
    }

    /** Событие игрока (скрафтил, сломал, поставил...) → двигаем счётчики подходящих квестов. */
    public static void addProgress(ServerPlayer player, QuestTask.Type type, Predicate<String> targetMatches,
                                   int amount) {
        Registry<Quest> registry = registry(player.level());
        QuestProgress progress = progress(player);
        boolean changed = false;

        for (Map.Entry<ResourceKey<Quest>, Quest> entry : registry.entrySet()) {
            ResourceLocation id = entry.getKey().location();
            Quest quest = entry.getValue();
            QuestTask task = quest.task();
            if (task.type() != type || progress.isCompleted(id) || !isUnlocked(progress, quest)
                    || !targetMatches.test(task.target())) {
                continue;
            }
            int value = progress.addCounter(id, amount);
            if (value >= task.count()) {
                complete(player, id, quest, progress);
            }
            changed = true;
        }
        if (changed) {
            sync(player);
        }
    }

    private static void complete(ServerPlayer player, ResourceLocation id, Quest quest, QuestProgress progress) {
        progress.complete(id);
        player.displayClientMessage(Component.translatable("quest.techquest.completed",
                quest.title().copy().withStyle(ChatFormatting.GOLD)).withStyle(ChatFormatting.GREEN), false);
        player.playNotifySound(SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.8F, 1.0F);
    }

    /** Игрок нажал «Забрать награду». */
    public static void claim(ServerPlayer player, ResourceLocation id) {
        Quest quest = registry(player.level()).get(id);
        if (quest == null) {
            return;
        }
        QuestProgress progress = progress(player);
        if (!progress.isCompleted(id) || progress.isClaimed(id) || !isUnlocked(progress, quest)) {
            return;
        }

        QuestTask task = quest.task();
        if (task.type() == QuestTask.Type.HAS_ITEM && task.consume()) {
            if (countItems(player, task.target()) < task.count()) {
                player.displayClientMessage(Component.translatable("quest.techquest.missing_items")
                        .withStyle(ChatFormatting.RED), true);
                return;
            }
            removeItems(player, task.target(), task.count());
        }

        for (ItemStack reward : quest.rewards()) {
            ItemStack stack = reward.copy();
            player.getInventory().add(stack);
            if (!stack.isEmpty()) {
                player.drop(stack, false);
            }
        }

        progress.claim(id);
        player.displayClientMessage(Component.translatable("quest.techquest.claimed",
                quest.title().copy().withStyle(ChatFormatting.GOLD)).withStyle(ChatFormatting.GREEN), false);
        player.playNotifySound(SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.6F, 1.0F);
        sync(player);
    }

    private static int countItems(Player player, String target) {
        int total = 0;
        Inventory inventory = player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (QuestTask.matchesItem(target, stack)) {
                total += stack.getCount();
            }
        }
        return total;
    }

    private static void removeItems(Player player, String target, int amount) {
        int left = amount;
        Inventory inventory = player.getInventory();
        for (int i = 0; i < inventory.getContainerSize() && left > 0; i++) {
            ItemStack stack = inventory.getItem(i);
            if (QuestTask.matchesItem(target, stack)) {
                int taken = Math.min(left, stack.getCount());
                stack.shrink(taken);
                left -= taken;
            }
        }
        inventory.setChanged();
    }

    private QuestManager() {}
}
