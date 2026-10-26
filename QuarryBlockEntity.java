package com.techquest.quarry;

import com.techquest.energy.MachineEnergyStorage;
import com.techquest.menu.QuarryMenu;
import com.techquest.registry.ModBlockEntities;
import com.techquest.registry.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.items.ItemStackHandler;

import java.util.List;

/**
 * Карьер 9×9 вниз.
 * Слоты 0–8: фильтр (whitelist — если не пусто, дроп только совпадающих предметов).
 * Слоты 9–17: внутренний буфер дропа.
 */
public class QuarryBlockEntity extends BlockEntity implements MenuProvider {
    public static final int CAPACITY = 100_000;
    public static final int ENERGY_PER_BLOCK = 200;
    public static final int RADIUS = 4;
    public static final int INTERVAL = 12;
    public static final int FILTER_SLOTS = 9;
    public static final int BUFFER_SLOTS = 9;
    public static final int DATA_COUNT = 5; // energyLo, energyHi, active, yDepth, energyPct

    private final MachineEnergyStorage energy = new MachineEnergyStorage(CAPACITY, 500, 0);
    private final ItemStackHandler inv = new ItemStackHandler(FILTER_SLOTS + BUFFER_SLOTS) {
        @Override protected void onContentsChanged(int slot) { setChanged(); }
    };

    private boolean active;
    private int cursorX = -RADIUS, cursorZ = -RADIUS, cursorY;
    private int cooldown;
    private boolean inited;

    private final ContainerData data = new ContainerData() {
        @Override public int get(int i) {
            return switch (i) {
                case 0 -> energy.getEnergyStored() & 0xFFFF;
                case 1 -> (energy.getEnergyStored() >>> 16) & 0xFFFF;
                case 2 -> active ? 1 : 0;
                case 3 -> inited ? worldPosition.getY() - cursorY : 0;
                case 4 -> energy.getMaxEnergyStored() <= 0 ? 0
                        : energy.getEnergyStored() * 100 / energy.getMaxEnergyStored();
                default -> 0;
            };
        }
        @Override public void set(int i, int v) {
            if (i == 2) active = v != 0;
        }
        @Override public int getCount() { return DATA_COUNT; }
    };

    public QuarryBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.QUARRY.get(), pos, state);
    }

    public IEnergyStorage getEnergyStorage() { return energy; }
    public ItemStackHandler getItems() { return inv; }
    public ItemStackHandler getAutomationHandler() { return inv; }
    public ContainerData getData() { return data; }
    public boolean isActive() { return active; }

    public void toggle() {
        active = !active;
        if (!inited) {
            cursorY = worldPosition.getY() - 1;
            inited = true;
        }
        setChanged();
    }

    private boolean filterEmpty() {
        for (int i = 0; i < FILTER_SLOTS; i++) {
            if (!inv.getStackInSlot(i).isEmpty()) return false;
        }
        return true;
    }

    private boolean passesFilter(ItemStack drop) {
        if (filterEmpty()) return true;
        for (int i = 0; i < FILTER_SLOTS; i++) {
            ItemStack f = inv.getStackInSlot(i);
            if (!f.isEmpty() && ItemStack.isSameItem(f, drop)) return true;
        }
        return false;
    }

    private boolean insertBuffer(ItemStack stack) {
        for (int i = FILTER_SLOTS; i < FILTER_SLOTS + BUFFER_SLOTS; i++) {
            stack = inv.insertItem(i, stack, false);
            if (stack.isEmpty()) return true;
        }
        return stack.isEmpty();
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, QuarryBlockEntity be) {
        if (!be.active || !(level instanceof ServerLevel server)) return;
        if (be.cooldown > 0) { be.cooldown--; return; }
        if (be.energy.getEnergyStored() < ENERGY_PER_BLOCK) return;

        BlockPos dig = pos.offset(be.cursorX, be.cursorY - pos.getY(), be.cursorZ);
        if (dig.getY() < level.getMinBuildHeight() + 1) {
            be.active = false;
            be.setChanged();
            return;
        }

        BlockState target = level.getBlockState(dig);
        if (!target.isAir() && target.getDestroySpeed(level, dig) >= 0 && !target.is(Blocks.BEDROCK)) {
            be.energy.removeInternal(ENERGY_PER_BLOCK);
            List<ItemStack> drops = Block.getDrops(target, server, dig, level.getBlockEntity(dig));
            level.setBlock(dig, Blocks.AIR.defaultBlockState(), 3);
            for (ItemStack d : drops) {
                if (!be.passesFilter(d)) continue;
                if (!be.insertBuffer(d.copy())) {
                    Block.popResource(level, pos.above(), d);
                }
            }
        }

        be.cursorX++;
        if (be.cursorX > RADIUS) {
            be.cursorX = -RADIUS;
            be.cursorZ++;
            if (be.cursorZ > RADIUS) {
                be.cursorZ = -RADIUS;
                be.cursorY--;
            }
        }
        be.cooldown = INTERVAL;
        be.setChanged();
    }

    @Override public Component getDisplayName() {
        return Component.translatable("block.techquest.quarry");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        return new QuarryMenu(id, inv, this, data);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider reg) {
        super.saveAdditional(tag, reg);
        tag.putInt("Energy", energy.getEnergyStored());
        tag.putBoolean("Active", active);
        tag.putInt("CX", cursorX); tag.putInt("CZ", cursorZ); tag.putInt("CY", cursorY);
        tag.putBoolean("Inited", inited);
        tag.put("Inv", this.inv.serializeNBT(reg));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider reg) {
        super.loadAdditional(tag, reg);
        energy.setEnergy(tag.getInt("Energy"));
        active = tag.getBoolean("Active");
        cursorX = tag.getInt("CX"); cursorZ = tag.getInt("CZ"); cursorY = tag.getInt("CY");
        inited = tag.getBoolean("Inited");
        if (tag.contains("Inv")) this.inv.deserializeNBT(reg, tag.getCompound("Inv"));
    }
}
