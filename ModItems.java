package com.techquest.registry;

import com.techquest.TechQuest;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(TechQuest.MODID);

    // ---- Материалы ----
    public static final DeferredItem<Item> RAW_TIN = ITEMS.registerSimpleItem("raw_tin");
    public static final DeferredItem<Item> TIN_INGOT = ITEMS.registerSimpleItem("tin_ingot");
    public static final DeferredItem<Item> BRONZE_INGOT = ITEMS.registerSimpleItem("bronze_ingot");

    // ---- Предметы-блоки ----
    static {
        blockItem("tin_ore", ModBlocks.TIN_ORE);
        blockItem("deepslate_tin_ore", ModBlocks.DEEPSLATE_TIN_ORE);
        blockItem("machine_casing", ModBlocks.MACHINE_CASING);
        blockItem("coal_generator", ModBlocks.COAL_GENERATOR);
    }

    private static void blockItem(String name, DeferredBlock<?> block) {
        ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    private ModItems() {}
}
