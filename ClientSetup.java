package com.techquest.client;

import com.techquest.TechQuest;
import com.techquest.client.screen.CoalGeneratorScreen;
import com.techquest.registry.ModMenus;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = TechQuest.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientSetup {

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.COAL_GENERATOR.get(), CoalGeneratorScreen::new);
    }

    private ClientSetup() {}
}
