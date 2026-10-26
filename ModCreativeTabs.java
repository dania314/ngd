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

                        // Ores
                        output.accept(ModBlocks.TIN_ORE);
                        output.accept(ModBlocks.DEEPSLATE_TIN_ORE);
                        output.accept(ModBlocks.LEAD_ORE);
                        output.accept(ModBlocks.DEEPSLATE_LEAD_ORE);
                        output.accept(ModBlocks.SILVER_ORE);
                        output.accept(ModBlocks.DEEPSLATE_SILVER_ORE);
                        output.accept(ModBlocks.ALUMINUM_ORE);
                        output.accept(ModBlocks.DEEPSLATE_ALUMINUM_ORE);
                        output.accept(ModBlocks.NICKEL_ORE);
                        output.accept(ModBlocks.DEEPSLATE_NICKEL_ORE);

                        // Raw + ingots
                        output.accept(ModItems.RAW_TIN);
                        output.accept(ModItems.TIN_INGOT);
                        output.accept(ModItems.BRONZE_INGOT);
                        output.accept(ModItems.RAW_LEAD);
                        output.accept(ModItems.LEAD_INGOT);
                        output.accept(ModItems.RAW_SILVER);
                        output.accept(ModItems.SILVER_INGOT);
                        output.accept(ModItems.RAW_ALUMINUM);
                        output.accept(ModItems.ALUMINUM_INGOT);
                        output.accept(ModItems.RAW_NICKEL);
                        output.accept(ModItems.NICKEL_INGOT);

                        // Dusts
                        output.accept(ModItems.TIN_DUST);
                        output.accept(ModItems.COPPER_DUST);
                        output.accept(ModItems.IRON_DUST);
                        output.accept(ModItems.GOLD_DUST);
                        output.accept(ModItems.LEAD_DUST);
                        output.accept(ModItems.SILVER_DUST);
                        output.accept(ModItems.ALUMINUM_DUST);
                        output.accept(ModItems.NICKEL_DUST);

                        // Plates
                        output.accept(ModItems.TIN_PLATE);
                        output.accept(ModItems.BRONZE_PLATE);
                        output.accept(ModItems.IRON_PLATE);
                        output.accept(ModItems.COPPER_PLATE);
                        output.accept(ModItems.GOLD_PLATE);
                        output.accept(ModItems.LEAD_PLATE);
                        output.accept(ModItems.SILVER_PLATE);
                        output.accept(ModItems.ALUMINUM_PLATE);
                        output.accept(ModItems.NICKEL_PLATE);

                        output.accept(ModItems.BASIC_CIRCUIT);
                        output.accept(ModItems.ADVANCED_CIRCUIT);

                        // Blades
                        output.accept(ModItems.BLADE_WOOD);
                        output.accept(ModItems.BLADE_TIN);
                        output.accept(ModItems.BLADE_BRONZE);
                        output.accept(ModItems.BLADE_IRON);
                        output.accept(ModItems.BLADE_ALUMINUM);
                        output.accept(ModItems.BLADE_ADVANCED);

                        // Machines
                        output.accept(ModBlocks.MACHINE_CASING);
                        output.accept(ModBlocks.COAL_GENERATOR);
                        output.accept(ModBlocks.SOLAR_PANEL);
                        output.accept(ModBlocks.WIND_TURBINE);
                        output.accept(ModBlocks.WATER_WHEEL);
                        output.accept(ModBlocks.MACERATOR);
                        output.accept(ModBlocks.COMPRESSOR);
                        output.accept(ModBlocks.ELECTRIC_FURNACE);
                        output.accept(ModBlocks.BATTERY_BOX);
                        output.accept(ModBlocks.TIN_CABLE);
                        output.accept(ModBlocks.DRILL_FRAME);
                        output.accept(ModBlocks.DRILL_HEAD);
                        output.accept(ModBlocks.DRILL_CONTROLLER);
                        output.accept(ModBlocks.ALLOY_SMELTER);
                        output.accept(ModBlocks.CONVEYOR);
                        output.accept(ModBlocks.CONVEYOR_SEALED);
                        output.accept(ModBlocks.CABLE_LV);
                        output.accept(ModBlocks.CABLE_MV);
                        output.accept(ModBlocks.CABLE_HV);
                        output.accept(ModBlocks.TRANSFORMER);
                        output.accept(ModBlocks.QUARRY);
                        output.accept(ModBlocks.GREENHOUSE);
                        output.accept(ModBlocks.OIL_DEPOSIT);
                        output.accept(ModBlocks.REFINERY);
                        output.accept(ModBlocks.CHEMICAL_REACTOR);
                        output.accept(ModBlocks.BOILER);
                        output.accept(ModBlocks.SCRUBBER);
                        output.accept(ModBlocks.GEIGER);
                        output.accept(ModBlocks.SAFETY_VALVE);
                        output.accept(ModBlocks.SPRINKLER);
                        output.accept(ModBlocks.NUCLEAR_REACTOR);
                        output.accept(ModBlocks.VITRIFICATION);
                        output.accept(ModBlocks.RTG);
                        output.accept(ModBlocks.BLAST_FURNACE);
                        output.accept(ModBlocks.LASER_DRILL);
                        output.accept(ModBlocks.ASSEMBLY_LINE);
                        output.accept(ModItems.STRUCTURE_WRENCH);
                        output.accept(ModBlocks.COOLING_TOWER);
                        output.accept(ModItems.CRUDE_OIL);
                        output.accept(ModItems.GASOLINE);
                        output.accept(ModItems.PLASTIC);
                        output.accept(ModItems.RUBBER);
                        output.accept(ModItems.SULFURIC_ACID);
                        output.accept(ModItems.STEEL_INGOT);
                        output.accept(ModItems.EXO_CHEST);
                        output.accept(ModItems.HAZMAT_CHEST);
                        output.accept(ModItems.MODULE_SERVO);
                        output.accept(ModItems.URANIUM_INGOT);
                        output.accept(ModItems.ENERGY_METER);
                        output.accept(ModItems.SPENT_FUEL);
                    })
                    .build());

    private ModCreativeTabs() {}
}
