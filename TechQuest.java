package com.techquest;

import com.mojang.logging.LogUtils;
import com.techquest.network.ModNetwork;
import com.techquest.quest.QuestRegistries;
import com.techquest.registry.ModAttachments;
import com.techquest.registry.ModBlockEntities;
import com.techquest.registry.ModBlocks;
import com.techquest.registry.ModCreativeTabs;
import com.techquest.registry.ModItems;
import com.techquest.registry.ModMenus;
import com.techquest.registry.ModRecipes;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod(TechQuest.MODID)
public class TechQuest {
    public static final String MODID = "techquest";
    public static final Logger LOGGER = LogUtils.getLogger();

    public TechQuest(IEventBus modEventBus) {
        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        ModMenus.MENUS.register(modEventBus);
        ModCreativeTabs.TABS.register(modEventBus);
        ModRecipes.TYPES.register(modEventBus);
        ModRecipes.SERIALIZERS.register(modEventBus);
        ModAttachments.ATTACHMENTS.register(modEventBus);

        modEventBus.addListener(ModBlockEntities::registerCapabilities);
        modEventBus.addListener(QuestRegistries::registerDatapackRegistries);
        modEventBus.addListener(ModNetwork::register);

        LOGGER.info("TechQuest initialised");
    }
}
