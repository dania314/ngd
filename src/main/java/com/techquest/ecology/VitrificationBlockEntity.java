package com.techquest.ecology;

import com.techquest.energy.MachineEnergyStorage;
import com.techquest.registry.ModBlockEntities;
import com.techquest.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.items.ItemStackHandler;

/** spent_fuel + sand/glass → glassed_waste. Жрёт FE, убирает «опасность» отходов. */
public class VitrificationBlockEntity extends BlockEntity {
    private final MachineEnergyStorage energy = new MachineEnergyStorage(30_000, 200, 0);
    private final ItemStackHandler inv = new ItemStackHandler(3) {
        @Override protected void onContentsChanged(int slot) { setChanged(); }
    };
    private int progress;

    public VitrificationBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.VITRIFICATION.get(), pos, state);
    }

    public IEnergyStorage getEnergyStorage() { return energy; }
    public ItemStackHandler getItems() { return inv; }
    public ItemStackHandler getAutomationHandler() { return inv; }
    public int getProgress() { return progress; }

    public static void serverTick(Level level, BlockPos pos, BlockState state, VitrificationBlockEntity be) {
        ItemStack fuel = be.inv.getStackInSlot(0);
        ItemStack sand = be.inv.getStackInSlot(1);
        if (!fuel.is(ModItems.SPENT_FUEL.get()) || !(sand.is(Items.SAND) || sand.is(Items.GLASS))) {
            be.progress = 0;
            return;
        }
        if (be.energy.getEnergyStored() < 40) return;
        ItemStack out = be.inv.getStackInSlot(2);
        if (!out.isEmpty() && (!out.is(ModItems.GLASSED_WASTE.get()) || out.getCount() >= 64)) return;

        be.energy.removeInternal(40);
        be.progress++;
        if (be.progress >= 100) {
            be.progress = 0;
            be.inv.extractItem(0, 1, false);
            be.inv.extractItem(1, 1, false);
            be.inv.insertItem(2, new ItemStack(ModItems.GLASSED_WASTE.get()), false);
        }
        be.setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider r) {
        super.saveAdditional(tag, r);
        tag.putInt("Energy", energy.getEnergyStored());
        tag.putInt("Progress", progress);
        tag.put("Inv", inv.serializeNBT(r));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider r) {
        super.loadAdditional(tag, r);
        energy.setEnergy(tag.getInt("Energy"));
        progress = tag.getInt("Progress");
        if (tag.contains("Inv")) inv.deserializeNBT(r, tag.getCompound("Inv"));
    }
}
