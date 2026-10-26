package com.techquest.registry;

import com.techquest.TechQuest;
import com.techquest.item.QuestBookItem;
import com.techquest.item.TurbineBladeItem;
import com.techquest.armor.ExoChestItem;
import com.techquest.armor.HazmatChestItem;
import com.techquest.item.EnergyMeterItem;
import com.techquest.item.StructureWrenchItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(TechQuest.MODID);

    // ---- Книга квестов ----
    public static final DeferredItem<QuestBookItem> QUEST_BOOK = ITEMS.registerItem("quest_book",
            QuestBookItem::new, new Item.Properties().stacksTo(1));

    // ---- Материалы: Tin / Bronze ----
    public static final DeferredItem<Item> RAW_TIN = ITEMS.registerSimpleItem("raw_tin");
    public static final DeferredItem<Item> TIN_INGOT = ITEMS.registerSimpleItem("tin_ingot");
    public static final DeferredItem<Item> BRONZE_INGOT = ITEMS.registerSimpleItem("bronze_ingot");

    // ---- Lead ----
    public static final DeferredItem<Item> RAW_LEAD = ITEMS.registerSimpleItem("raw_lead");
    public static final DeferredItem<Item> LEAD_INGOT = ITEMS.registerSimpleItem("lead_ingot");
    public static final DeferredItem<Item> LEAD_DUST = ITEMS.registerSimpleItem("lead_dust");
    public static final DeferredItem<Item> LEAD_PLATE = ITEMS.registerSimpleItem("lead_plate");

    // ---- Silver ----
    public static final DeferredItem<Item> RAW_SILVER = ITEMS.registerSimpleItem("raw_silver");
    public static final DeferredItem<Item> SILVER_INGOT = ITEMS.registerSimpleItem("silver_ingot");
    public static final DeferredItem<Item> SILVER_DUST = ITEMS.registerSimpleItem("silver_dust");
    public static final DeferredItem<Item> SILVER_PLATE = ITEMS.registerSimpleItem("silver_plate");

    // ---- Aluminum ----
    public static final DeferredItem<Item> RAW_ALUMINUM = ITEMS.registerSimpleItem("raw_aluminum");
    public static final DeferredItem<Item> ALUMINUM_INGOT = ITEMS.registerSimpleItem("aluminum_ingot");
    public static final DeferredItem<Item> ALUMINUM_DUST = ITEMS.registerSimpleItem("aluminum_dust");
    public static final DeferredItem<Item> ALUMINUM_PLATE = ITEMS.registerSimpleItem("aluminum_plate");

    // ---- Nickel ----
    public static final DeferredItem<Item> RAW_NICKEL = ITEMS.registerSimpleItem("raw_nickel");
    public static final DeferredItem<Item> NICKEL_INGOT = ITEMS.registerSimpleItem("nickel_ingot");
    public static final DeferredItem<Item> NICKEL_DUST = ITEMS.registerSimpleItem("nickel_dust");
    public static final DeferredItem<Item> NICKEL_PLATE = ITEMS.registerSimpleItem("nickel_plate");

    // ---- Пыль (старые) ----
    public static final DeferredItem<Item> TIN_DUST = ITEMS.registerSimpleItem("tin_dust");
    public static final DeferredItem<Item> COPPER_DUST = ITEMS.registerSimpleItem("copper_dust");
    public static final DeferredItem<Item> IRON_DUST = ITEMS.registerSimpleItem("iron_dust");
    public static final DeferredItem<Item> GOLD_DUST = ITEMS.registerSimpleItem("gold_dust");

    // ---- Пластины (старые) ----
    public static final DeferredItem<Item> TIN_PLATE = ITEMS.registerSimpleItem("tin_plate");
    public static final DeferredItem<Item> BRONZE_PLATE = ITEMS.registerSimpleItem("bronze_plate");
    public static final DeferredItem<Item> IRON_PLATE = ITEMS.registerSimpleItem("iron_plate");
    public static final DeferredItem<Item> COPPER_PLATE = ITEMS.registerSimpleItem("copper_plate");
    public static final DeferredItem<Item> GOLD_PLATE = ITEMS.registerSimpleItem("gold_plate");

    // ---- Электроника ----
    public static final DeferredItem<Item> BASIC_CIRCUIT = ITEMS.registerSimpleItem("basic_circuit");
    public static final DeferredItem<Item> ADVANCED_CIRCUIT = ITEMS.registerSimpleItem("advanced_circuit");

    // ---- Chemistry ----
    public static final DeferredItem<Item> CRUDE_OIL = ITEMS.registerSimpleItem("crude_oil");
    public static final DeferredItem<Item> MAZUT = ITEMS.registerSimpleItem("mazut");
    public static final DeferredItem<Item> GASOLINE = ITEMS.registerSimpleItem("gasoline");
    public static final DeferredItem<Item> LUBRICANT = ITEMS.registerSimpleItem("lubricant");
    public static final DeferredItem<Item> PLASTIC = ITEMS.registerSimpleItem("plastic");
    public static final DeferredItem<Item> RUBBER = ITEMS.registerSimpleItem("rubber");
    public static final DeferredItem<Item> SULFURIC_ACID = ITEMS.registerSimpleItem("sulfuric_acid");
    public static final DeferredItem<Item> CARBON_FIBER = ITEMS.registerSimpleItem("carbon_fiber");
    public static final DeferredItem<Item> KEVLAR = ITEMS.registerSimpleItem("kevlar");
    public static final DeferredItem<Item> ROCKET_FUEL = ITEMS.registerSimpleItem("rocket_fuel");
    public static final DeferredItem<Item> SOOT = ITEMS.registerSimpleItem("soot");
    public static final DeferredItem<Item> URANIUM_INGOT = ITEMS.registerSimpleItem("uranium_ingot");
    public static final DeferredItem<Item> SPENT_FUEL = ITEMS.registerSimpleItem("spent_fuel");
    public static final DeferredItem<Item> GLASSED_WASTE = ITEMS.registerSimpleItem("glassed_waste");

    // ---- Exo modules ----
    public static final DeferredItem<Item> MODULE_SERVO = ITEMS.registerSimpleItem("module_servo");
    public static final DeferredItem<Item> MODULE_BATTERY = ITEMS.registerSimpleItem("module_battery");
    public static final DeferredItem<Item> MODULE_FILTER = ITEMS.registerSimpleItem("module_filter");
    public static final DeferredItem<Item> MODULE_PLATING = ITEMS.registerSimpleItem("module_plating");

    public static final DeferredItem<ExoChestItem> EXO_CHEST = ITEMS.registerItem("exo_chestplate",
            ExoChestItem::new, new Item.Properties());
    public static final DeferredItem<HazmatChestItem> HAZMAT_CHEST = ITEMS.registerItem("hazmat_chestplate",
            HazmatChestItem::new, new Item.Properties());

    public static final DeferredItem<Item> STEEL_INGOT = ITEMS.registerSimpleItem("steel_ingot");
    public static final DeferredItem<EnergyMeterItem> ENERGY_METER = ITEMS.registerItem("energy_meter",
            EnergyMeterItem::new, new Item.Properties());
    public static final DeferredItem<StructureWrenchItem> STRUCTURE_WRENCH = ITEMS.registerItem("structure_wrench",
            StructureWrenchItem::new, new Item.Properties());
    public static final DeferredItem<Item> LEAD_FOAM = ITEMS.registerSimpleItem("lead_foam");


    // ---- Лопасти ветряка (6 видов) ----
    // energy/tick, durability (0=infinite), craft tier
    public static final DeferredItem<TurbineBladeItem> BLADE_WOOD = ITEMS.registerItem("blade_wood",
            p -> new TurbineBladeItem(p, 4, 2400, 1));          // легко, мало энергии, быстро ломается
    public static final DeferredItem<TurbineBladeItem> BLADE_TIN = ITEMS.registerItem("blade_tin",
            p -> new TurbineBladeItem(p, 8, 6000, 2));           // проще, средне
    public static final DeferredItem<TurbineBladeItem> BLADE_BRONZE = ITEMS.registerItem("blade_bronze",
            p -> new TurbineBladeItem(p, 12, 12000, 3));         // нормально
    public static final DeferredItem<TurbineBladeItem> BLADE_IRON = ITEMS.registerItem("blade_iron",
            p -> new TurbineBladeItem(p, 18, 20000, 4));         // хорошо, труднее
    public static final DeferredItem<TurbineBladeItem> BLADE_ALUMINUM = ITEMS.registerItem("blade_aluminum",
            p -> new TurbineBladeItem(p, 24, 28000, 5));         // много энергии, долго
    public static final DeferredItem<TurbineBladeItem> BLADE_ADVANCED = ITEMS.registerItem("blade_advanced",
            p -> new TurbineBladeItem(p, 32, 0, 6));             // макс, не ломается, сложно

    // ---- Предметы-блоки ----
    static {
        blockItem("tin_ore", ModBlocks.TIN_ORE);
        blockItem("deepslate_tin_ore", ModBlocks.DEEPSLATE_TIN_ORE);
        blockItem("lead_ore", ModBlocks.LEAD_ORE);
        blockItem("deepslate_lead_ore", ModBlocks.DEEPSLATE_LEAD_ORE);
        blockItem("silver_ore", ModBlocks.SILVER_ORE);
        blockItem("deepslate_silver_ore", ModBlocks.DEEPSLATE_SILVER_ORE);
        blockItem("aluminum_ore", ModBlocks.ALUMINUM_ORE);
        blockItem("deepslate_aluminum_ore", ModBlocks.DEEPSLATE_ALUMINUM_ORE);
        blockItem("nickel_ore", ModBlocks.NICKEL_ORE);
        blockItem("deepslate_nickel_ore", ModBlocks.DEEPSLATE_NICKEL_ORE);

        blockItem("machine_casing", ModBlocks.MACHINE_CASING);
        blockItem("coal_generator", ModBlocks.COAL_GENERATOR);
        blockItem("solar_panel", ModBlocks.SOLAR_PANEL);
        blockItem("wind_turbine", ModBlocks.WIND_TURBINE);
        blockItem("water_wheel", ModBlocks.WATER_WHEEL);
        blockItem("macerator", ModBlocks.MACERATOR);
        blockItem("compressor", ModBlocks.COMPRESSOR);
        blockItem("electric_furnace", ModBlocks.ELECTRIC_FURNACE);
        blockItem("battery_box", ModBlocks.BATTERY_BOX);
        blockItem("tin_cable", ModBlocks.TIN_CABLE);
        blockItem("drill_frame", ModBlocks.DRILL_FRAME);
        blockItem("drill_head", ModBlocks.DRILL_HEAD);
        blockItem("drill_controller", ModBlocks.DRILL_CONTROLLER);
        blockItem("alloy_smelter", ModBlocks.ALLOY_SMELTER);
        blockItem("conveyor", ModBlocks.CONVEYOR);
        blockItem("conveyor_sealed", ModBlocks.CONVEYOR_SEALED);
        blockItem("cable_lv", ModBlocks.CABLE_LV);
        blockItem("cable_mv", ModBlocks.CABLE_MV);
        blockItem("cable_hv", ModBlocks.CABLE_HV);
        blockItem("quarry", ModBlocks.QUARRY);
        blockItem("greenhouse", ModBlocks.GREENHOUSE);
        blockItem("boiler", ModBlocks.BOILER);
        blockItem("scrubber", ModBlocks.SCRUBBER);
        blockItem("geiger_sensor", ModBlocks.GEIGER);
        blockItem("safety_valve", ModBlocks.SAFETY_VALVE);
        blockItem("sprinkler", ModBlocks.SPRINKLER);
        blockItem("nuclear_reactor", ModBlocks.NUCLEAR_REACTOR);
        blockItem("vitrification_furnace", ModBlocks.VITRIFICATION);
        blockItem("rtg", ModBlocks.RTG);
        blockItem("blast_furnace_controller", ModBlocks.BLAST_FURNACE);
        blockItem("laser_drill", ModBlocks.LASER_DRILL);
        blockItem("assembly_line", ModBlocks.ASSEMBLY_LINE);
        blockItem("oil_deposit", ModBlocks.OIL_DEPOSIT);
        blockItem("refinery", ModBlocks.REFINERY);
        blockItem("chemical_reactor", ModBlocks.CHEMICAL_REACTOR);
        blockItem("cooling_tower", ModBlocks.COOLING_TOWER);
        blockItem("transformer", ModBlocks.TRANSFORMER);
    }

    private static void blockItem(String name, DeferredBlock<?> block) {
        ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    private ModItems() {}
}
