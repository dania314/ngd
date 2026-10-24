package com.techquest.registry;

import com.techquest.TechQuest;
import com.techquest.item.QuestBookItem;
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

    // ---- Материалы ----
    public static final DeferredItem<Item> RAW_TIN = ITEMS.registerSimpleItem("raw_tin");
    public static final DeferredItem<Item> TIN_INGOT = ITEMS.registerSimpleItem("tin_ingot");
    public static final DeferredItem<Item> BRONZE_INGOT = ITEMS.registerSimpleItem("bronze_ingot");

    // ---- Пыль ----
    public static final DeferredItem<Item> TIN_DUST = ITEMS.registerSimpleItem("tin_dust");
    public static final DeferredItem<Item> COPPER_DUST = ITEMS.registerSimpleItem("copper_dust");
    public static final DeferredItem<Item> IRON_DUST = ITEMS.registerSimpleItem("iron_dust");
    public static final DeferredItem<Item> GOLD_DUST = ITEMS.registerSimpleItem("gold_dust");

    // ---- Пластины ----
    public static final DeferredItem<Item> TIN_PLATE = ITEMS.registerSimpleItem("tin_plate");
    public static final DeferredItem<Item> BRONZE_PLATE = ITEMS.registerSimpleItem("bronze_plate");
    public static final DeferredItem<Item> IRON_PLATE = ITEMS.registerSimpleItem("iron_plate");
    public static final DeferredItem<Item> COPPER_PLATE = ITEMS.registerSimpleItem("copper_plate");
    public static final DeferredItem<Item> GOLD_PLATE = ITEMS.registerSimpleItem("gold_plate");

    // ---- Электроника ----
    public static final DeferredItem<Item> BASIC_CIRCUIT = ITEMS.registerSimpleItem("basic_circuit");

    // ---- Предметы-блоки ----
    static {
        blockItem("tin_ore", ModBlocks.TIN_ORE);
        blockItem("deepslate_tin_ore", ModBlocks.DEEPSLATE_TIN_ORE);
        blockItem("machine_casing", ModBlocks.MACHINE_CASING);
        blockItem("coal_generator", ModBlocks.COAL_GENERATOR);
        blockItem("solar_panel", ModBlocks.SOLAR_PANEL);
        blockItem("macerator", ModBlocks.MACERATOR);
        blockItem("compressor", ModBlocks.COMPRESSOR);
        blockItem("electric_furnace", ModBlocks.ELECTRIC_FURNACE);
        blockItem("battery_box", ModBlocks.BATTERY_BOX);
        blockItem("tin_cable", ModBlocks.TIN_CABLE);
    }

    private static void blockItem(String name, DeferredBlock<?> block) {
        ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    private ModItems() {}
}
