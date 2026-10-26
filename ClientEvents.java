package com.techquest.client;

import com.techquest.TechQuest;
import com.techquest.client.screen.QuestBookScreen;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** Игровые клиентские события: горячая клавиша квестбука (K). */
@EventBusSubscriber(modid = TechQuest.MODID, value = Dist.CLIENT)
public final class ClientEvents {

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        while (ClientSetup.OPEN_QUESTS.consumeClick()) {
            if (mc.player != null && mc.screen == null) {
                mc.setScreen(new QuestBookScreen());
            }
        }
    }

    private ClientEvents() {}
}
