package com.techquest.block.entity;

import com.techquest.block.WindTurbineBlock;
import com.techquest.energy.EnergyUtil;
import com.techquest.energy.MachineEnergyStorage;
import com.techquest.item.TurbineBladeItem;
import com.techquest.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.IEnergyStorage;

public class WindTurbineBlockEntity extends BlockEntity {
    public static final int CAPACITY = 8000;
    public static final int MAX_OUTPUT = 80;

    private final MachineEnergyStorage energy = new MachineEnergyStorage(CAPACITY, 0, MAX_OUTPUT);
    private ItemStack blade = ItemStack.EMPTY;
    private int bladeLifeUsed = 0;

    public WindTurbineBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.WIND_TURBINE.get(), pos, state);
    }

    public boolean installBlade(ItemStack stack) {
        if (!blade.isEmpty()) return false;
        if (!(stack.getItem() instanceof TurbineBladeItem)) return false;
        blade = stack.copyWithCount(1);
        bladeLifeUsed = 0;
        setChanged();
        return true;
    }

    public ItemStack getBlade() {
        return blade;
    }

    public IEnergyStorage getEnergyStorage() {
        return energy;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, WindTurbineBlockEntity be) {
        if (be.blade.isEmpty() || !(be.blade.getItem() instanceof TurbineBladeItem bladeItem)) {
            if (state.getValue(WindTurbineBlock.HAS_BLADES)) {
                level.setBlock(pos, state.setValue(WindTurbineBlock.HAS_BLADES, false), Block.UPDATE_ALL);
            }
            return;
        }

        if (!level.canSeeSky(pos.above())) {
            EnergyUtil.push(level, pos, be.energy, MAX_OUTPUT, false);
            return;
        }

        int base = bladeItem.getEnergyPerTick();
        int heightBonus = Math.min(8, Math.max(0, (pos.getY() - 64) / 16));
        float dayFactor = level.isDay() ? 1.0f : 0.4f;
        int gen = Math.max(1, Math.round((base + heightBonus) * dayFactor));
        be.energy.addInternal(gen);

        int maxDur = bladeItem.getMaxDurability();
        if (maxDur > 0) {
            be.bladeLifeUsed++;
            if (be.bladeLifeUsed >= maxDur) {
                be.blade = ItemStack.EMPTY;
                be.bladeLifeUsed = 0;
                level.setBlock(pos, state.setValue(WindTurbineBlock.HAS_BLADES, false), Block.UPDATE_ALL);
            }
            be.setChanged();
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
        if (!blade.isEmpty()) {
            tag.put("Blade", blade.save(registries));
        }
        tag.putInt("BladeLife", bladeLifeUsed);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        energy.setEnergy(tag.getInt("Energy"));
        if (tag.contains("Blade")) {
            blade = ItemStack.parse(registries, tag.getCompound("Blade")).orElse(ItemStack.EMPTY);
        } else {
            blade = ItemStack.EMPTY;
        }
        bladeLifeUsed = tag.getInt("BladeLife");
    }
}
