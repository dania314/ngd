package com.techquest.network;

import com.techquest.quest.ClientQuestData;
import com.techquest.quest.QuestManager;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class ModNetwork {

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");

        registrar.playToClient(QuestSyncPayload.TYPE, QuestSyncPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> ClientQuestData.set(payload.progress())));

        registrar.playToServer(ClaimQuestPayload.TYPE, ClaimQuestPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) {
                        QuestManager.claim(player, payload.quest());
                    }
                }));
    }

    private ModNetwork() {}
}
