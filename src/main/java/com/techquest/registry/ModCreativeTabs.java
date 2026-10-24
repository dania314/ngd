package com.techquest.registry;

import com.techquest.TechQuest;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, TechQuest.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN = TABS.register("main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup." + TechQuest.MODID))
                    .icon(() -> new ItemStack(ModItems.TIN_INGOT.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.QUEST_BOOK);

                        output.accept(ModBlocks.TIN_ORE);
                        output.accept(ModBlocks.DEEPSLATE_TIN_ORE);
                        output.accept(ModItems.RAW_TIN);
                        output.accept(ModItems.TIN_INGOT);
                        output.accept(ModItems.BRONZE_INGOT);

                        output.accept(ModItems.TIN_DUST);
                        output.accept(ModItems.COPPER_DUST);
                        output.accept(ModItems.IRON_DUST);
                        output.accept(ModItems.GOLD_DUST);

                        output.accept(ModItems.TIN_PLATE);
                        output.accept(ModItems.BRONZE_PLATE);
                        output.accept(ModItems.IRON_PLATE);
                        output.accept(ModItems.COPPER_PLATE);
                        output.accept(ModItems.GOLD_PLATE);
                        output.accept(ModItems.BASIC_CIRCUIT);

                        output.accept(ModBlocks.MACHINE_CASING);
                        output.accept(ModBlocks.COAL_GENERATOR);
                        output.accept(ModBlocks.SOLAR_PANEL);
                        output.accept(ModBlocks.MACERATOR);
                        output.accept(ModBlocks.COMPRESSOR);
                        output.accept(ModBlocks.ELECTRIC_FURNACE);
                        output.accept(ModBlocks.BATTERY_BOX);
                        output.accept(ModBlocks.TIN_CABLE);
                    })
                    .build());

    private ModCreativeTabs() {}
}
