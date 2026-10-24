package com.techquest.registry;

import com.techquest.TechQuest;
import com.techquest.block.BatteryBoxBlock;
import com.techquest.block.CableBlock;
import com.techquest.block.CoalGeneratorBlock;
import com.techquest.block.CompressorBlock;
import com.techquest.block.ElectricFurnaceBlock;
import com.techquest.block.MaceratorBlock;
import com.techquest.block.SolarPanelBlock;
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

    // ---- Каркас и генераторы ----
    public static final DeferredBlock<Block> MACHINE_CASING = BLOCKS.registerSimpleBlock("machine_casing",
            machineProps());

    public static final DeferredBlock<CoalGeneratorBlock> COAL_GENERATOR = BLOCKS.registerBlock("coal_generator",
            CoalGeneratorBlock::new,
            machineProps().lightLevel(state -> state.getValue(BlockStateProperties.LIT) ? 13 : 0));

    public static final DeferredBlock<SolarPanelBlock> SOLAR_PANEL = BLOCKS.registerBlock("solar_panel",
            SolarPanelBlock::new, machineProps());

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

    private static BlockBehaviour.Properties machineProps() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(3.5F, 6.0F)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops();
    }

    private ModBlocks() {}
}
