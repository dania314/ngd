package com.techquest.block.entity;

import com.techquest.energy.EnergyUtil;
import com.techquest.energy.MachineEnergyStorage;
import com.techquest.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.energy.IEnergyStorage;

/**
 * Водяное колесо: мало FE, иногда «съедает» соседнюю воду.
 */
public class WaterWheelBlockEntity extends BlockEntity {
    public static final int CAPACITY = 3000;
    public static final int MAX_OUTPUT = 12;
    /** FE/т за текущую воду */
    public static final int ENERGY_PER_FLOWING = 2;
    /** FE/т за источник */
    public static final int ENERGY_PER_SOURCE = 1;
    /** Макс генерация за тик */
    public static final int GEN_CAP = 8;
    /** Раз в столько тиков шанс съесть воду (~каждые 15–25 сек проверка) */
    public static final int CONSUME_CHECK_INTERVAL = 300; // 15 сек
    /** Шанс съесть один блок воды при проверке */
    public static final float CONSUME_CHANCE = 0.35f;

    private final MachineEnergyStorage energy = new MachineEnergyStorage(CAPACITY, 0, MAX_OUTPUT);
    private int consumeTimer;

    public WaterWheelBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.WATER_WHEEL.get(), pos, state);
    }

    public IEnergyStorage getEnergyStorage() {
        return energy;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, WaterWheelBlockEntity be) {
        int flowing = 0;
        int sources = 0;
        java.util.List<BlockPos> waterPositions = new java.util.ArrayList<>();

        for (Direction dir : Direction.values()) {
            if (dir == Direction.UP) continue;
            BlockPos n = pos.relative(dir);
            FluidState fluid = level.getFluidState(n);
            if (!fluid.isEmpty() && fluid.is(net.minecraft.tags.FluidTags.WATER)) {
                waterPositions.add(n);
                if (fluid.isSource()) {
                    sources++;
                } else {
                    flowing++;
                }
            }
        }

        int gen = Math.min(GEN_CAP, flowing * ENERGY_PER_FLOWING + sources * ENERGY_PER_SOURCE);
        if (gen > 0) {
            be.energy.addInternal(gen);
        }

        // Медленный расход воды
        be.consumeTimer++;
        if (be.consumeTimer >= CONSUME_CHECK_INTERVAL && !waterPositions.isEmpty()) {
            be.consumeTimer = 0;
            RandomSource random = level.getRandom();
            if (random.nextFloat() < CONSUME_CHANCE) {
                BlockPos target = waterPositions.get(random.nextInt(waterPositions.size()));
                FluidState fs = level.getFluidState(target);
                if (fs.isSource()) {
                    // Источник → воздух (вода пропала)
                    level.setBlock(target, Blocks.AIR.defaultBlockState(), 3);
                } else if (!fs.isEmpty()) {
                    // Текущая — убрать
                    level.setBlock(target, Blocks.AIR.defaultBlockState(), 3);
                }
            }
        }

        EnergyUtil.push(level, pos, be.energy, MAX_OUTPUT, false);
        if (level.getGameTime() % 40 == 0) {
            be.setChanged();
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("Energy", energy.getEnergyStored());
        tag.putInt("ConsumeTimer", consumeTimer);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        energy.setEnergy(tag.getInt("Energy"));
        consumeTimer = tag.getInt("ConsumeTimer");
    }
}
