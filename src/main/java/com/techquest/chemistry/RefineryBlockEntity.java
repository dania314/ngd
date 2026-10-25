package com.techquest.chemistry;

import com.techquest.energy.MachineEnergyStorage;
import com.techquest.menu.ChemMachineMenu;
import com.techquest.registry.ModMenus;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import com.techquest.registry.ModBlockEntities;
import com.techquest.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

/**
 * НПЗ: слот 0 = нефть → слот 1-3 = мазут/бензин/смазка + иногда пластик.
 * 100 FE/т, 100 тиков на партию.
 */
public class RefineryBlockEntity extends BlockEntity implements MenuProvider {
    public static final int CAPACITY = 40_000;
    public static final int ENERGY_PER_TICK = 40;
    public static final int MAX_PROGRESS = 100;

    private final MachineEnergyStorage energy = new MachineEnergyStorage(CAPACITY, 200, 0);
    private final ItemStackHandler inv = new ItemStackHandler(4) {
        @Override protected void onContentsChanged(int slot) { setChanged(); }
        @Override public boolean isItemValid(int slot, ItemStack stack) {
            if (slot == 0) return stack.is(ModItems.CRUDE_OIL.get());
            return false;
        }
    };
    private int progress;

    private final ContainerData data = new ContainerData() {
        @Override public int get(int i) {
            return switch (i) {
                case 0 -> energy.getEnergyStored() & 0xFFFF;
                case 1 -> (energy.getEnergyStored() >>> 16) & 0xFFFF;
                case 2 -> progress;
                case 3 -> MAX_PROGRESS;
                default -> 0;
            };
        }
        @Override public void set(int i, int v) {}
        @Override public int getCount() { return ChemMachineMenu.DATA_COUNT; }
    };


    public RefineryBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.REFINERY.get(), pos, state);
    }

    public IEnergyStorage getEnergyStorage() { return energy; }
    public ItemStackHandler getItems() { return inv; }
    public int getProgress() { return progress; }
    public int getMaxProgress() { return MAX_PROGRESS; }

    public static void serverTick(Level level, BlockPos pos, BlockState state, RefineryBlockEntity be) {
        ItemStack input = be.inv.getStackInSlot(0);
        if (input.isEmpty() || !input.is(ModItems.CRUDE_OIL.get())) {
            be.progress = 0;
            return;
        }
        // need space in outputs
        if (!canInsert(be, 1, new ItemStack(ModItems.MAZUT.get()))
                || !canInsert(be, 2, new ItemStack(ModItems.GASOLINE.get()))
                || !canInsert(be, 3, new ItemStack(ModItems.LUBRICANT.get()))) {
            return;
        }
        if (be.energy.getEnergyStored() < ENERGY_PER_TICK) return;

        be.energy.removeInternal(ENERGY_PER_TICK);
        be.progress++;
        if (be.progress >= MAX_PROGRESS) {
            be.progress = 0;
            be.inv.extractItem(0, 1, false);
            insert(be, 1, new ItemStack(ModItems.MAZUT.get()));
            insert(be, 2, new ItemStack(ModItems.GASOLINE.get()));
            insert(be, 3, new ItemStack(ModItems.LUBRICANT.get()));
            // 25% plastic
            if (level.random.nextInt(4) == 0) {
                // put plastic in lubricant slot if empty extra - or drop
                ItemStack plastic = new ItemStack(ModItems.PLASTIC.get());
                for (int i = 1; i <= 3; i++) {
                    ItemStack left = be.inv.insertItem(i, plastic, false);
                    if (left.isEmpty()) break;
                    plastic = left;
                }
            }
            be.setChanged();
        }
    }

    private static boolean canInsert(RefineryBlockEntity be, int slot, ItemStack stack) {
        ItemStack cur = be.inv.getStackInSlot(slot);
        if (cur.isEmpty()) return true;
        return ItemStack.isSameItemSameComponents(cur, stack) && cur.getCount() < cur.getMaxStackSize();
    }

    private static void insert(RefineryBlockEntity be, int slot, ItemStack stack) {
        be.inv.insertItem(slot, stack, false);
    }

    public ItemStackHandler getAutomationHandler() { return inv; }
    public ContainerData getData() { return data; }

    @Override public Component getDisplayName() {
        return Component.translatable("block.techquest.refinery");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        return new ChemMachineMenu(ModMenus.REFINERY.get(), id, inv, this, data, 4);
    }


    @Override protected void saveAdditional(CompoundTag tag, HolderLookup.Provider r) {
        super.saveAdditional(tag, r);
        tag.putInt("Energy", energy.getEnergyStored());
        tag.putInt("Progress", progress);
        tag.put("Inv", inv.serializeNBT(r));
    }

    @Override protected void loadAdditional(CompoundTag tag, HolderLookup.Provider r) {
        super.loadAdditional(tag, r);
        energy.setEnergy(tag.getInt("Energy"));
        progress = tag.getInt("Progress");
        if (tag.contains("Inv")) inv.deserializeNBT(r, tag.getCompound("Inv"));
    }
}
