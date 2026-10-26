package com.techquest.block.entity;

import com.techquest.block.WindTurbineBlock;
import com.techquest.energy.EnergyUtil;
import com.techquest.energy.MachineEnergyStorage;
import com.techquest.item.TurbineBladeItem;
import com.techquest.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.jetbrains.annotations.Nullable;

public class WindTurbineBlockEntity extends BlockEntity {
    public static final int CAPACITY = 8000;
    public static final int MAX_OUTPUT = 80;

    private final MachineEnergyStorage energy = new MachineEnergyStorage(CAPACITY, 0, MAX_OUTPUT);
    private ItemStack blade = ItemStack.EMPTY;
    private int bladeLifeUsed = 0;
    /** Для рендера: угол лопастей (клиент). */
    public float bladeAngle;
    public float bladeSpeed;

    public WindTurbineBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.WIND_TURBINE.get(), pos, state);
    }

    public boolean installBlade(ItemStack stack) {
        if (!blade.isEmpty()) return false;
        if (!(stack.getItem() instanceof TurbineBladeItem)) return false;
        blade = stack.copyWithCount(1);
        bladeLifeUsed = 0;
        setChanged();
        sync();
        return true;
    }

    public ItemStack getBlade() {
        return blade;
    }

    public int getBladeLifeUsed() {
        return bladeLifeUsed;
    }

    public float getDurabilityFraction() {
        if (blade.isEmpty() || !(blade.getItem() instanceof TurbineBladeItem bi)) return 0;
        int max = bi.getMaxDurability();
        if (max <= 0) return 1f;
        return 1f - (float) bladeLifeUsed / max;
    }

    public IEnergyStorage getEnergyStorage() {
        return energy;
    }

    private void sync() {
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, WindTurbineBlockEntity be) {
        if (be.blade.isEmpty() || !(be.blade.getItem() instanceof TurbineBladeItem bladeItem)) {
            if (state.getValue(WindTurbineBlock.HAS_BLADES)) {
                level.setBlock(pos, state.setValue(WindTurbineBlock.HAS_BLADES, false), Block.UPDATE_ALL);
            }
            return;
        }

        boolean canGen = level.canSeeSky(pos.above());
        if (!canGen) {
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
            // Синхронизация каждые 100 тиков чтобы клиент видел износ
            if (be.bladeLifeUsed % 100 == 0) {
                be.sync();
            }
            if (be.bladeLifeUsed >= maxDur) {
                be.blade = ItemStack.EMPTY;
                be.bladeLifeUsed = 0;
                level.setBlock(pos, state.setValue(WindTurbineBlock.HAS_BLADES, false), Block.UPDATE_ALL);
                be.sync();
            }
            be.setChanged();
        }

        EnergyUtil.push(level, pos, be.energy, MAX_OUTPUT, false);
        if (level.getGameTime() % 40 == 0) {
            be.setChanged();
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, WindTurbineBlockEntity be) {
        if (!state.getValue(WindTurbineBlock.HAS_BLADES) || be.blade.isEmpty()) {
            be.bladeSpeed = Math.max(0, be.bladeSpeed - 0.5f);
        } else {
            float target = level.canSeeSky(pos.above()) ? (level.isDay() ? 12f : 5f) : 1f;
            be.bladeSpeed += (target - be.bladeSpeed) * 0.05f;
        }
        be.bladeAngle = (be.bladeAngle + be.bladeSpeed) % 360f;
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, registries);
        return tag;
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
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
