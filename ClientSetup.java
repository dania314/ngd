package com.techquest.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.techquest.TechQuest;
import com.techquest.block.entity.WindTurbineBlockEntity;
import com.techquest.client.render.WindTurbineRenderer;
import com.techquest.client.screen.CoalGeneratorScreen;
import com.techquest.client.screen.ProcessingScreen;
import com.techquest.client.screen.ChemMachineScreen;
import com.techquest.client.screen.QuarryScreen;
import com.techquest.client.screen.BoilerScreen;
import com.techquest.client.screen.NuclearReactorScreen;
import com.techquest.client.screen.BlastFurnaceScreen;
import com.techquest.client.screen.AssemblyLineScreen;
import com.techquest.registry.ModBlockEntities;
import com.techquest.registry.ModMenus;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = TechQuest.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientSetup {
    public static final KeyMapping OPEN_QUESTS = new KeyMapping(
            "key.techquest.open_quests", InputConstants.KEY_K, "key.categories.techquest");

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() ->
                BlockEntityRenderers.register(ModBlockEntities.WIND_TURBINE.get(), WindTurbineRenderer::new));
    }

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.COAL_GENERATOR.get(), CoalGeneratorScreen::new);
        event.register(ModMenus.MACERATOR.get(), ProcessingScreen::new);
        event.register(ModMenus.COMPRESSOR.get(), ProcessingScreen::new);
        event.register(ModMenus.ELECTRIC_FURNACE.get(), ProcessingScreen::new);
        event.register(ModMenus.REFINERY.get(), ChemMachineScreen::new);
        event.register(ModMenus.CHEMICAL_REACTOR.get(), ChemMachineScreen::new);
        event.register(ModMenus.QUARRY.get(), QuarryScreen::new);
        event.register(ModMenus.BOILER.get(), BoilerScreen::new);
        event.register(ModMenus.NUCLEAR_REACTOR.get(), NuclearReactorScreen::new);
        event.register(ModMenus.BLAST_FURNACE.get(), BlastFurnaceScreen::new);
        event.register(ModMenus.ASSEMBLY_LINE.get(), AssemblyLineScreen::new);
    }

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(OPEN_QUESTS);
    }

    private ClientSetup() {}
}
