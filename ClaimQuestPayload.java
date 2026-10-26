package com.techquest.network;

import com.techquest.TechQuest;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Клиент → сервер: «забрать награду за квест». */
public record ClaimQuestPayload(ResourceLocation quest) implements CustomPacketPayload {
    public static final Type<ClaimQuestPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(TechQuest.MODID, "claim_quest"));

    public static final StreamCodec<ByteBuf, ClaimQuestPayload> STREAM_CODEC =
            ResourceLocation.STREAM_CODEC.map(ClaimQuestPayload::new, ClaimQuestPayload::quest);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
