package com.techquest.registry;

import com.techquest.TechQuest;
import com.techquest.block.entity.BatteryBoxBlockEntity;
import com.techquest.block.entity.CableBlockEntity;
import com.techquest.block.entity.CoalGeneratorBlockEntity;
import com.techquest.block.entity.CompressorBlockEntity;
import com.techquest.block.entity.ElectricFurnaceBlockEntity;
import com.techquest.block.entity.MaceratorBlockEntity;
import com.techquest.block.entity.ProcessingBlockEntity;
import com.techquest.block.entity.SolarPanelBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, TechQuest.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CoalGeneratorBlockEntity>> COAL_GENERATOR =
            BLOCK_ENTITIES.register("coal_generator",
                    () -> BlockEntityType.Builder.of(CoalGeneratorBlockEntity::new, ModBlocks.COAL_GENERATOR.get())
                            .build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MaceratorBlockEntity>> MACERATOR =
            BLOCK_ENTITIES.register("macerator",
                    () -> BlockEntityType.Builder.of(MaceratorBlockEntity::new, ModBlocks.MACERATOR.get())
                            .build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CompressorBlockEntity>> COMPRESSOR =
            BLOCK_ENTITIES.register("compressor",
                    () -> BlockEntityType.Builder.of(CompressorBlockEntity::new, ModBlocks.COMPRESSOR.get())
                            .build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ElectricFurnaceBlockEntity>> ELECTRIC_FURNACE =
            BLOCK_ENTITIES.register("electric_furnace",
                    () -> BlockEntityType.Builder.of(ElectricFurnaceBlockEntity::new, ModBlocks.ELECTRIC_FURNACE.get())
                            .build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SolarPanelBlockEntity>> SOLAR_PANEL =
            BLOCK_ENTITIES.register("solar_panel",
                    () -> BlockEntityType.Builder.of(SolarPanelBlockEntity::new, ModBlocks.SOLAR_PANEL.get())
                            .build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BatteryBoxBlockEntity>> BATTERY_BOX =
            BLOCK_ENTITIES.register("battery_box",
                    () -> BlockEntityType.Builder.of(BatteryBoxBlockEntity::new, ModBlocks.BATTERY_BOX.get())
                            .build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CableBlockEntity>> TIN_CABLE =
            BLOCK_ENTITIES.register("tin_cable",
                    () -> BlockEntityType.Builder.of(CableBlockEntity::new, ModBlocks.TIN_CABLE.get())
                            .build(null));

    /** Подключаем энергию (FE) и инвентарь к блокам — так с ними работают кабели и воронки. */
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, COAL_GENERATOR.get(),
                (be, side) -> be.getEnergyStorage());
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, COAL_GENERATOR.get(),
                (be, side) -> be.getInventory());

        registerProcessing(event, MACERATOR.get());
        registerProcessing(event, COMPRESSOR.get());
        registerProcessing(event, ELECTRIC_FURNACE.get());

        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, SOLAR_PANEL.get(),
                (be, side) -> be.getEnergyStorage());
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, BATTERY_BOX.get(),
                (be, side) -> be.getEnergyStorage());
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, TIN_CABLE.get(),
                (be, side) -> be.getEnergyStorage());
    }

    private static <T extends ProcessingBlockEntity> void registerProcessing(RegisterCapabilitiesEvent event,
                                                                             BlockEntityType<T> type) {
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, type, (be, side) -> be.getEnergyStorage());
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, type, (be, side) -> be.getAutomationHandler());
    }

    private ModBlockEntities() {}
}
