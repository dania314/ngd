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
import com.techquest.block.entity.AlloySmelterBlockEntity;
import com.techquest.block.entity.DrillControllerBlockEntity;
import com.techquest.block.entity.WaterWheelBlockEntity;
import com.techquest.block.entity.WindTurbineBlockEntity;
import com.techquest.conveyor.ConveyorBlockEntity;
import com.techquest.farm.GreenhouseBlockEntity;
import com.techquest.quarry.QuarryBlockEntity;
import com.techquest.ecology.BoilerBlockEntity;
import com.techquest.ecology.SprinklerBlockEntity;
import com.techquest.ecology.GeigerBlockEntity;
import com.techquest.ecology.NuclearReactorBlockEntity;
import com.techquest.ecology.VitrificationBlockEntity;
import com.techquest.ecology.RtgBlockEntity;
import com.techquest.ecology.BlastFurnaceBlockEntity;
import com.techquest.quarry.LaserDrillBlockEntity;
import com.techquest.chemistry.AssemblyLineBlockEntity;
import com.techquest.chemistry.RefineryBlockEntity;
import com.techquest.chemistry.ChemicalReactorBlockEntity;
import com.techquest.tier.TransformerBlockEntity;
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

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<WindTurbineBlockEntity>> WIND_TURBINE =
            BLOCK_ENTITIES.register("wind_turbine",
                    () -> BlockEntityType.Builder.of(WindTurbineBlockEntity::new, ModBlocks.WIND_TURBINE.get())
                            .build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<WaterWheelBlockEntity>> WATER_WHEEL =
            BLOCK_ENTITIES.register("water_wheel",
                    () -> BlockEntityType.Builder.of(WaterWheelBlockEntity::new, ModBlocks.WATER_WHEEL.get())
                            .build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BatteryBoxBlockEntity>> BATTERY_BOX =
            BLOCK_ENTITIES.register("battery_box",
                    () -> BlockEntityType.Builder.of(BatteryBoxBlockEntity::new, ModBlocks.BATTERY_BOX.get())
                            .build(null));


    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DrillControllerBlockEntity>> DRILL_CONTROLLER =
            BLOCK_ENTITIES.register("drill_controller",
                    () -> BlockEntityType.Builder.of(DrillControllerBlockEntity::new, ModBlocks.DRILL_CONTROLLER.get())
                            .build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AlloySmelterBlockEntity>> ALLOY_SMELTER =
            BLOCK_ENTITIES.register("alloy_smelter",
                    () -> BlockEntityType.Builder.of(AlloySmelterBlockEntity::new, ModBlocks.ALLOY_SMELTER.get())
                            .build(null));



    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<RefineryBlockEntity>> REFINERY =
            BLOCK_ENTITIES.register("refinery",
                    () -> BlockEntityType.Builder.of(RefineryBlockEntity::new, ModBlocks.REFINERY.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ChemicalReactorBlockEntity>> CHEMICAL_REACTOR =
            BLOCK_ENTITIES.register("chemical_reactor",
                    () -> BlockEntityType.Builder.of(ChemicalReactorBlockEntity::new, ModBlocks.CHEMICAL_REACTOR.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TransformerBlockEntity>> TRANSFORMER =
            BLOCK_ENTITIES.register("transformer",
                    () -> BlockEntityType.Builder.of(TransformerBlockEntity::new, ModBlocks.TRANSFORMER.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ConveyorBlockEntity>> CONVEYOR =
            BLOCK_ENTITIES.register("conveyor",
                    () -> BlockEntityType.Builder.of(ConveyorBlockEntity::new, ModBlocks.CONVEYOR.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ConveyorBlockEntity>> CONVEYOR_SEALED =
            BLOCK_ENTITIES.register("conveyor_sealed",
                    () -> BlockEntityType.Builder.of(
                            (pos, st) -> new ConveyorBlockEntity(pos, st, com.techquest.conveyor.ConveyorType.SEALED),
                            ModBlocks.CONVEYOR_SEALED.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SprinklerBlockEntity>> SPRINKLER =
            BLOCK_ENTITIES.register("sprinkler",
                    () -> BlockEntityType.Builder.of(SprinklerBlockEntity::new, ModBlocks.SPRINKLER.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GeigerBlockEntity>> GEIGER =
            BLOCK_ENTITIES.register("geiger",
                    () -> BlockEntityType.Builder.of(GeigerBlockEntity::new, ModBlocks.GEIGER.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<NuclearReactorBlockEntity>> NUCLEAR_REACTOR =
            BLOCK_ENTITIES.register("nuclear_reactor",
                    () -> BlockEntityType.Builder.of(NuclearReactorBlockEntity::new, ModBlocks.NUCLEAR_REACTOR.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<VitrificationBlockEntity>> VITRIFICATION =
            BLOCK_ENTITIES.register("vitrification",
                    () -> BlockEntityType.Builder.of(VitrificationBlockEntity::new, ModBlocks.VITRIFICATION.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<LaserDrillBlockEntity>> LASER_DRILL =
            BLOCK_ENTITIES.register("laser_drill",
                    () -> BlockEntityType.Builder.of(LaserDrillBlockEntity::new, ModBlocks.LASER_DRILL.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AssemblyLineBlockEntity>> ASSEMBLY_LINE =
            BLOCK_ENTITIES.register("assembly_line",
                    () -> BlockEntityType.Builder.of(AssemblyLineBlockEntity::new, ModBlocks.ASSEMBLY_LINE.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlastFurnaceBlockEntity>> BLAST_FURNACE =
            BLOCK_ENTITIES.register("blast_furnace",
                    () -> BlockEntityType.Builder.of(BlastFurnaceBlockEntity::new, ModBlocks.BLAST_FURNACE.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<RtgBlockEntity>> RTG =
            BLOCK_ENTITIES.register("rtg",
                    () -> BlockEntityType.Builder.of(RtgBlockEntity::new, ModBlocks.RTG.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BoilerBlockEntity>> BOILER =
            BLOCK_ENTITIES.register("boiler",
                    () -> BlockEntityType.Builder.of(BoilerBlockEntity::new, ModBlocks.BOILER.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<QuarryBlockEntity>> QUARRY =
            BLOCK_ENTITIES.register("quarry",
                    () -> BlockEntityType.Builder.of(QuarryBlockEntity::new, ModBlocks.QUARRY.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GreenhouseBlockEntity>> GREENHOUSE =
            BLOCK_ENTITIES.register("greenhouse",
                    () -> BlockEntityType.Builder.of(GreenhouseBlockEntity::new, ModBlocks.GREENHOUSE.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CableBlockEntity>> TIN_CABLE =
            BLOCK_ENTITIES.register("tin_cable",
                    () -> BlockEntityType.Builder.of(CableBlockEntity::new,
                                    ModBlocks.TIN_CABLE.get(),
                                    ModBlocks.CABLE_LV.get(),
                                    ModBlocks.CABLE_MV.get(),
                                    ModBlocks.CABLE_HV.get())
                            .build(null));

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
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, WIND_TURBINE.get(),
                (be, side) -> be.getEnergyStorage());
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, WATER_WHEEL.get(),
                (be, side) -> be.getEnergyStorage());
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, BATTERY_BOX.get(),
                (be, side) -> be.getEnergyStorage());
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, TIN_CABLE.get(),
                (be, side) -> be.getEnergyStorage());
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, DRILL_CONTROLLER.get(),
                (be, side) -> be.getEnergyStorage());
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, ALLOY_SMELTER.get(),
                (be, side) -> be.getEnergyStorage());
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, QUARRY.get(),
                (be, side) -> be.getEnergyStorage());
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, QUARRY.get(),
                (be, side) -> be.getAutomationHandler());
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, GREENHOUSE.get(),
                (be, side) -> be.getEnergyStorage());
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, REFINERY.get(),
                (be, side) -> be.getEnergyStorage());
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, REFINERY.get(),
                (be, side) -> be.getAutomationHandler());
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, CHEMICAL_REACTOR.get(),
                (be, side) -> be.getEnergyStorage());
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, CHEMICAL_REACTOR.get(),
                (be, side) -> be.getAutomationHandler());
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, TRANSFORMER.get(),
                (be, side) -> be.getEnergyStorage());
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, BOILER.get(),
                (be, side) -> be.getEnergyStorage());
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, BOILER.get(),
                (be, side) -> be.getAutomationHandler());
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, NUCLEAR_REACTOR.get(),
                (be, side) -> be.getEnergyStorage());
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, NUCLEAR_REACTOR.get(),
                (be, side) -> be.getAutomationHandler());
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, VITRIFICATION.get(),
                (be, side) -> be.getEnergyStorage());
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, VITRIFICATION.get(),
                (be, side) -> be.getAutomationHandler());
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, RTG.get(),
                (be, side) -> be.getEnergyStorage());
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, RTG.get(),
                (be, side) -> be.getAutomationHandler());
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, BLAST_FURNACE.get(),
                (be, side) -> be.getEnergyStorage());
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, BLAST_FURNACE.get(),
                (be, side) -> be.getAutomationHandler());
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, LASER_DRILL.get(),
                (be, side) -> be.getEnergyStorage());
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, LASER_DRILL.get(),
                (be, side) -> be.getAutomationHandler());
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, ASSEMBLY_LINE.get(),
                (be, side) -> be.getEnergyStorage());
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ASSEMBLY_LINE.get(),
                (be, side) -> be.getAutomationHandler());
    }

    private static <T extends ProcessingBlockEntity> void registerProcessing(RegisterCapabilitiesEvent event,
                                                                             BlockEntityType<T> type) {
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, type, (be, side) -> be.getEnergyStorage());
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, type, (be, side) -> be.getAutomationHandler());
    }

    private ModBlockEntities() {}
}
