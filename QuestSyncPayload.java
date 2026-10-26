package com.techquest.network;

import com.techquest.TechQuest;
import com.techquest.quest.QuestProgress;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Сервер → клиент: полный прогресс игрока по квестам. */
public record QuestSyncPayload(QuestProgress progress) implements CustomPacketPayload {
    public static final Type<QuestSyncPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(TechQuest.MODID, "quest_sync"));

    public static final StreamCodec<ByteBuf, QuestSyncPayload> STREAM_CODEC =
            ByteBufCodecs.fromCodec(QuestProgress.CODEC).map(QuestSyncPayload::new, QuestSyncPayload::progress);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
