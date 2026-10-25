package com.techquest.registry;

import com.techquest.TechQuest;
import com.techquest.block.BatteryBoxBlock;
import com.techquest.block.CableBlock;
import com.techquest.block.CoalGeneratorBlock;
import com.techquest.block.CompressorBlock;
import com.techquest.block.ElectricFurnaceBlock;
import com.techquest.block.MaceratorBlock;
import com.techquest.block.SolarPanelBlock;
import com.techquest.block.AlloySmelterBlock;
import com.techquest.block.DrillControllerBlock;
import com.techquest.block.WaterWheelBlock;
import com.techquest.block.WindTurbineBlock;
import com.techquest.conveyor.ConveyorBlock;
import com.techquest.conveyor.ConveyorType;
import com.techquest.ecology.GeigerBlock;
import com.techquest.ecology.ScrubberBlock;
import com.techquest.ecology.BoilerBlock;
import com.techquest.ecology.SafetyValveBlock;
import com.techquest.ecology.SprinklerBlock;
import com.techquest.ecology.NuclearReactorBlock;
import com.techquest.ecology.VitrificationBlock;
import com.techquest.ecology.RtgBlock;
import com.techquest.ecology.BlastFurnaceBlock;
import com.techquest.quarry.LaserDrillBlock;
import com.techquest.chemistry.AssemblyLineBlock;
import com.techquest.farm.GreenhouseBlock;
import com.techquest.quarry.QuarryBlock;
import com.techquest.chemistry.RefineryBlock;
import com.techquest.chemistry.OilDepositBlock;
import com.techquest.chemistry.ChemicalReactorBlock;
import com.techquest.tier.TransformerBlock;

import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(TechQuest.MODID);

    // ---- Руды ----
    public static final DeferredBlock<DropExperienceBlock> TIN_ORE = BLOCKS.registerBlock("tin_ore",
            props -> new DropExperienceBlock(UniformInt.of(0, 2), props),
            BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_ORE));

    public static final DeferredBlock<DropExperienceBlock> DEEPSLATE_TIN_ORE = BLOCKS.registerBlock("deepslate_tin_ore",
            props -> new DropExperienceBlock(UniformInt.of(0, 2), props),
            BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE_IRON_ORE));

    public static final DeferredBlock<DropExperienceBlock> LEAD_ORE = BLOCKS.registerBlock("lead_ore",
            props -> new DropExperienceBlock(UniformInt.of(0, 2), props),
            BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_ORE));

    public static final DeferredBlock<DropExperienceBlock> DEEPSLATE_LEAD_ORE = BLOCKS.registerBlock("deepslate_lead_ore",
            props -> new DropExperienceBlock(UniformInt.of(0, 2), props),
            BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE_IRON_ORE));

    public static final DeferredBlock<DropExperienceBlock> SILVER_ORE = BLOCKS.registerBlock("silver_ore",
            props -> new DropExperienceBlock(UniformInt.of(1, 3), props),
            BlockBehaviour.Properties.ofFullCopy(Blocks.GOLD_ORE));

    public static final DeferredBlock<DropExperienceBlock> DEEPSLATE_SILVER_ORE = BLOCKS.registerBlock("deepslate_silver_ore",
            props -> new DropExperienceBlock(UniformInt.of(1, 3), props),
            BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE_GOLD_ORE));

    public static final DeferredBlock<DropExperienceBlock> ALUMINUM_ORE = BLOCKS.registerBlock("aluminum_ore",
            props -> new DropExperienceBlock(UniformInt.of(0, 1), props),
            BlockBehaviour.Properties.ofFullCopy(Blocks.COPPER_ORE));

    public static final DeferredBlock<DropExperienceBlock> DEEPSLATE_ALUMINUM_ORE = BLOCKS.registerBlock("deepslate_aluminum_ore",
            props -> new DropExperienceBlock(UniformInt.of(0, 1), props),
            BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE_COPPER_ORE));

    public static final DeferredBlock<DropExperienceBlock> NICKEL_ORE = BLOCKS.registerBlock("nickel_ore",
            props -> new DropExperienceBlock(UniformInt.of(0, 2), props),
            BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_ORE));

    public static final DeferredBlock<DropExperienceBlock> DEEPSLATE_NICKEL_ORE = BLOCKS.registerBlock("deepslate_nickel_ore",
            props -> new DropExperienceBlock(UniformInt.of(0, 2), props),
            BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE_IRON_ORE));

    // ---- Каркас и генераторы ----
    public static final DeferredBlock<Block> MACHINE_CASING = BLOCKS.registerSimpleBlock("machine_casing",
            machineProps());

    public static final DeferredBlock<CoalGeneratorBlock> COAL_GENERATOR = BLOCKS.registerBlock("coal_generator",
            CoalGeneratorBlock::new,
            machineProps().lightLevel(state -> state.getValue(BlockStateProperties.LIT) ? 13 : 0));

    public static final DeferredBlock<SolarPanelBlock> SOLAR_PANEL = BLOCKS.registerBlock("solar_panel",
            SolarPanelBlock::new, machineProps());

    public static final DeferredBlock<WindTurbineBlock> WIND_TURBINE = BLOCKS.registerBlock("wind_turbine",
            WindTurbineBlock::new, machineProps().noOcclusion());

    public static final DeferredBlock<WaterWheelBlock> WATER_WHEEL = BLOCKS.registerBlock("water_wheel",
            WaterWheelBlock::new, machineProps().noOcclusion());

    // ---- Машины ----
    public static final DeferredBlock<MaceratorBlock> MACERATOR = BLOCKS.registerBlock("macerator",
            MaceratorBlock::new,
            machineProps().lightLevel(state -> state.getValue(BlockStateProperties.LIT) ? 8 : 0));

    public static final DeferredBlock<CompressorBlock> COMPRESSOR = BLOCKS.registerBlock("compressor",
            CompressorBlock::new,
            machineProps().lightLevel(state -> state.getValue(BlockStateProperties.LIT) ? 6 : 0));

    public static final DeferredBlock<ElectricFurnaceBlock> ELECTRIC_FURNACE = BLOCKS.registerBlock("electric_furnace",
            ElectricFurnaceBlock::new,
            machineProps().lightLevel(state -> state.getValue(BlockStateProperties.LIT) ? 13 : 0));

    // ---- Энергосеть ----
    public static final DeferredBlock<BatteryBoxBlock> BATTERY_BOX = BLOCKS.registerBlock("battery_box",
            BatteryBoxBlock::new, machineProps());

    public static final DeferredBlock<CableBlock> TIN_CABLE = BLOCKS.registerBlock("tin_cable",
            CableBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(1.0F, 3.0F)
                    .sound(SoundType.METAL)
                    .noOcclusion());


    // ---- Multiblock / Drill ----
    public static final DeferredBlock<Block> DRILL_FRAME = BLOCKS.registerSimpleBlock("drill_frame",
            machineProps());

    public static final DeferredBlock<Block> DRILL_HEAD = BLOCKS.registerSimpleBlock("drill_head",
            machineProps().strength(5.0F, 8.0F));

    public static final DeferredBlock<DrillControllerBlock> DRILL_CONTROLLER = BLOCKS.registerBlock("drill_controller",
            DrillControllerBlock::new, machineProps());

    public static final DeferredBlock<AlloySmelterBlock> ALLOY_SMELTER = BLOCKS.registerBlock("alloy_smelter",
            AlloySmelterBlock::new,
            machineProps().lightLevel(state -> state.getValue(BlockStateProperties.LIT) ? 10 : 0));


    // ---- 0.5+ expansion ----
    public static final DeferredBlock<ConveyorBlock> CONVEYOR = BLOCKS.registerBlock("conveyor",
            p -> new ConveyorBlock(p, ConveyorType.NORMAL),
            BlockBehaviour.Properties.of().strength(1.5F).noOcclusion());

    public static final DeferredBlock<ConveyorBlock> CONVEYOR_SEALED = BLOCKS.registerBlock("conveyor_sealed",
            p -> new ConveyorBlock(p, ConveyorType.SEALED),
            BlockBehaviour.Properties.of().strength(2.5F).noOcclusion());

    public static final DeferredBlock<CableBlock> CABLE_LV = BLOCKS.registerBlock("cable_lv",
            p -> new CableBlock(p, com.techquest.tier.EnergyTier.LV),
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_ORANGE).strength(1.0F).noOcclusion());

    public static final DeferredBlock<CableBlock> CABLE_MV = BLOCKS.registerBlock("cable_mv",
            p -> new CableBlock(p, com.techquest.tier.EnergyTier.MV),
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_LIGHT_BLUE).strength(1.0F).noOcclusion());

    public static final DeferredBlock<CableBlock> CABLE_HV = BLOCKS.registerBlock("cable_hv",
            p -> new CableBlock(p, com.techquest.tier.EnergyTier.HV),
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_RED).strength(1.0F).noOcclusion());

    public static final DeferredBlock<QuarryBlock> QUARRY = BLOCKS.registerBlock("quarry",
            QuarryBlock::new, machineProps());

    public static final DeferredBlock<GreenhouseBlock> GREENHOUSE = BLOCKS.registerBlock("greenhouse",
            GreenhouseBlock::new, machineProps().noOcclusion());

    public static final DeferredBlock<BoilerBlock> BOILER = BLOCKS.registerBlock("boiler",
            BoilerBlock::new, machineProps());

    public static final DeferredBlock<ScrubberBlock> SCRUBBER = BLOCKS.registerBlock("scrubber",
            ScrubberBlock::new, machineProps());

    public static final DeferredBlock<GeigerBlock> GEIGER = BLOCKS.registerBlock("geiger_sensor",
            GeigerBlock::new, machineProps());

    public static final DeferredBlock<SafetyValveBlock> SAFETY_VALVE = BLOCKS.registerBlock("safety_valve",
            SafetyValveBlock::new, machineProps());

    public static final DeferredBlock<SprinklerBlock> SPRINKLER = BLOCKS.registerBlock("sprinkler",
            SprinklerBlock::new, machineProps().noOcclusion());

    public static final DeferredBlock<NuclearReactorBlock> NUCLEAR_REACTOR = BLOCKS.registerBlock("nuclear_reactor",
            NuclearReactorBlock::new, machineProps());

    public static final DeferredBlock<VitrificationBlock> VITRIFICATION = BLOCKS.registerBlock("vitrification_furnace",
            VitrificationBlock::new, machineProps());

    public static final DeferredBlock<RtgBlock> RTG = BLOCKS.registerBlock("rtg",
            RtgBlock::new, machineProps());

    public static final DeferredBlock<BlastFurnaceBlock> BLAST_FURNACE = BLOCKS.registerBlock("blast_furnace_controller",
            BlastFurnaceBlock::new, machineProps());

    public static final DeferredBlock<LaserDrillBlock> LASER_DRILL = BLOCKS.registerBlock("laser_drill",
            LaserDrillBlock::new, machineProps());

    public static final DeferredBlock<AssemblyLineBlock> ASSEMBLY_LINE = BLOCKS.registerBlock("assembly_line",
            AssemblyLineBlock::new, machineProps());

    public static final DeferredBlock<OilDepositBlock> OIL_DEPOSIT = BLOCKS.registerBlock("oil_deposit",
            OilDepositBlock::new,
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).strength(1.2F).sound(SoundType.SAND));

    public static final DeferredBlock<RefineryBlock> REFINERY = BLOCKS.registerBlock("refinery",
            RefineryBlock::new, machineProps());
    public static final DeferredBlock<ChemicalReactorBlock> CHEMICAL_REACTOR = BLOCKS.registerBlock("chemical_reactor",
            ChemicalReactorBlock::new, machineProps());
    public static final DeferredBlock<Block> COOLING_TOWER = BLOCKS.registerSimpleBlock("cooling_tower", machineProps());
    public static final DeferredBlock<TransformerBlock> TRANSFORMER = BLOCKS.registerBlock("transformer",
            TransformerBlock::new, machineProps());

    private static BlockBehaviour.Properties machineProps() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(3.5F, 6.0F)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops();
    }

    private ModBlocks() {}
}
