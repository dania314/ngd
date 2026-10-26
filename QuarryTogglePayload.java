package com.techquest.network;

import com.techquest.TechQuest;
import com.techquest.quarry.QuarryBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record QuarryTogglePayload(BlockPos pos) implements CustomPacketPayload {
    public static final Type<QuarryTogglePayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(TechQuest.MODID, "quarry_toggle"));

    public static final StreamCodec<RegistryFriendlyByteBuf, QuarryTogglePayload> STREAM_CODEC =
            StreamCodec.composite(BlockPos.STREAM_CODEC, QuarryTogglePayload::pos, QuarryTogglePayload::new);

    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(QuarryTogglePayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (ctx.player() instanceof ServerPlayer player) {
                BlockEntity be = player.level().getBlockEntity(payload.pos());
                if (be instanceof QuarryBlockEntity q) {
                    q.toggle();
                }
            }
        });
    }
}
