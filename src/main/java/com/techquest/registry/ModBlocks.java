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

    private static BlockBehaviour.Properties machineProps() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(3.5F, 6.0F)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops();
    }

    private ModBlocks() {}
}
