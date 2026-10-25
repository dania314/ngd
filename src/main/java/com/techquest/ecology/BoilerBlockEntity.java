package com.techquest.ecology;

import com.techquest.energy.EnergyUtil;
import com.techquest.energy.MachineEnergyStorage;
import com.techquest.menu.BoilerMenu;
import com.techquest.registry.ModBlockEntities;
import com.techquest.registry.ModBlocks;
import com.techquest.registry.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.items.ItemStackHandler;

/**
 * Паровой котёл:
 * - слот 0: топливо (уголь)
 * - слот 1: вода (ведро) → пустое ведро
 * heat 0–1000, pressure 0–1000
 * При pressure > 900 без охлаждения рядом — взрыв.
 * Генерация FE от пара.
 */
public class BoilerBlockEntity extends BlockEntity implements MenuProvider {
    public static final int CAPACITY = 50_000;
    public static final int MAX_HEAT = 1000;
    public static final int MAX_PRESSURE = 1000;
    public static final int DATA_COUNT = 6;

    private final MachineEnergyStorage energy = new MachineEnergyStorage(CAPACITY, 0, 256);
    private final ItemStackHandler inv = new ItemStackHandler(2) {
        @Override protected void onContentsChanged(int slot) { setChanged(); }
        @Override public boolean isItemValid(int slot, ItemStack stack) {
            if (slot == 0) return stack.is(Items.COAL) || stack.is(Items.CHARCOAL) || stack.is(ItemTags.COALS);
            if (slot == 1) return stack.is(Items.WATER_BUCKET);
            return false;
        }
    };

    private int heat;
    private int pressure;
    private int burnTime;
    private int water;

    private final ContainerData data = new ContainerData() {
        @Override public int get(int i) {
            return switch (i) {
                case 0 -> energy.getEnergyStored() & 0xFFFF;
                case 1 -> (energy.getEnergyStored() >>> 16) & 0xFFFF;
                case 2 -> heat;
                case 3 -> pressure;
                case 4 -> burnTime;
                case 5 -> water;
                default -> 0;
            };
        }
        @Override public void set(int i, int v) {}
        @Override public int getCount() { return DATA_COUNT; }
    };

    public BoilerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BOILER.get(), pos, state);
    }

    public IEnergyStorage getEnergyStorage() { return energy; }
    public ItemStackHandler getItems() { return inv; }
    public ItemStackHandler getAutomationHandler() { return inv; }
    public ContainerData getData() { return data; }
    public int getPressurePublic() { return pressure; }
    public int getHeatPublic() { return heat; }
    public void ventPressure(int amount) { pressure = Math.max(0, pressure - amount); heat = Math.max(0, heat - amount / 4); setChanged(); }

    private boolean hasCooling(Level level, BlockPos pos) {
        for (Direction d : Direction.values()) {
            if (level.getBlockState(pos.relative(d)).is(ModBlocks.COOLING_TOWER.get())) {
                return true;
            }
        }
        return false;
    }

    private boolean hasSafetyValve(Level level, BlockPos pos) {
        for (Direction d : Direction.values()) {
            if (level.getBlockState(pos.relative(d)).is(ModBlocks.SAFETY_VALVE.get())) {
                return true;
            }
        }
        return false;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BoilerBlockEntity be) {
        // fuel
        if (be.burnTime <= 0) {
            ItemStack fuel = be.inv.getStackInSlot(0);
            if (!fuel.isEmpty() && (fuel.is(Items.COAL) || fuel.is(Items.CHARCOAL))) {
                be.inv.extractItem(0, 1, false);
                be.burnTime = 160;
            }
        }
        if (be.burnTime > 0) {
            be.burnTime--;
            be.heat = Math.min(MAX_HEAT, be.heat + 4);
        } else {
            be.heat = Math.max(0, be.heat - 1);
        }

        // water
        ItemStack waterBucket = be.inv.getStackInSlot(1);
        if (be.water < 100 && waterBucket.is(Items.WATER_BUCKET)) {
            be.inv.setStackInSlot(1, new ItemStack(Items.BUCKET));
            be.water += 50;
        }

        // steam / pressure
        if (be.heat > 200 && be.water > 0) {
            be.water--;
            be.pressure = Math.min(MAX_PRESSURE, be.pressure + 3 + be.heat / 200);
            int gen = 8 + be.heat / 100;
            be.energy.addInternal(gen);
        } else {
            be.pressure = Math.max(0, be.pressure - 2);
        }

        // cooling tower
        if (be.hasCooling(level, pos)) {
            be.pressure = Math.max(0, be.pressure - 8);
            be.heat = Math.max(0, be.heat - 2);
        }

        // safety valve vents hard
        if (be.hasSafetyValve(level, pos) && be.pressure > 400) {
            be.pressure = Math.max(0, be.pressure - 15);
            if (level instanceof ServerLevel server && level.getGameTime() % 10 == 0) {
                server.sendParticles(ParticleTypes.CLOUD,
                        pos.getX() + 0.5, pos.getY() + 1.2, pos.getZ() + 0.5,
                        4, 0.2, 0.1, 0.2, 0.03);
            }
        }

        // OVERPRESSURE — valve prevents explosion entirely
        boolean protected_ = be.hasCooling(level, pos) || be.hasSafetyValve(level, pos);
        if (be.pressure >= 900 && !protected_) {
            if (level.random.nextInt(40) == 0) {
                level.removeBlock(pos, false);
                level.explode(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                        3.5f, true, Level.ExplosionInteraction.BLOCK);
                return;
            }
        }

        EnergyUtil.push(level, pos, be.energy, 128, false);

        if (level instanceof ServerLevel server && be.heat > 300 && level.getGameTime() % 15 == 0) {
            server.sendParticles(ParticleTypes.CLOUD,
                    pos.getX() + 0.5, pos.getY() + 1.1, pos.getZ() + 0.5,
                    2, 0.15, 0.05, 0.15, 0.01);
        }
        be.setChanged();
    }

    @Override public Component getDisplayName() {
        return Component.translatable("block.techquest.boiler");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        return new BoilerMenu(id, inv, this, data);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider r) {
        super.saveAdditional(tag, r);
        tag.putInt("Energy", energy.getEnergyStored());
        tag.putInt("Heat", heat);
        tag.putInt("Pressure", pressure);
        tag.putInt("Burn", burnTime);
        tag.putInt("Water", water);
        tag.put("Inv", this.inv.serializeNBT(r));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider r) {
        super.loadAdditional(tag, r);
        energy.setEnergy(tag.getInt("Energy"));
        heat = tag.getInt("Heat");
        pressure = tag.getInt("Pressure");
        burnTime = tag.getInt("Burn");
        water = tag.getInt("Water");
        if (tag.contains("Inv")) this.inv.deserializeNBT(r, tag.getCompound("Inv"));
    }
}
