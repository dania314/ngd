package com.techquest.ecology;

import com.techquest.energy.EnergyUtil;
import com.techquest.energy.MachineEnergyStorage;
import com.techquest.registry.ModBlockEntities;
import com.techquest.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.items.ItemStackHandler;

/** RTG: glassed_waste внутри → 2 FE/т почти вечно, без взрывов. */
public class RtgBlockEntity extends BlockEntity {
    private final MachineEnergyStorage energy = new MachineEnergyStorage(10_000, 0, 8);
    private final ItemStackHandler inv = new ItemStackHandler(1) {
        @Override protected void onContentsChanged(int slot) { setChanged(); }
        @Override public boolean isItemValid(int slot, ItemStack stack) {
            return stack.is(ModItems.GLASSED_WASTE.get()) || stack.is(ModItems.SPENT_FUEL.get());
        }
    };

    public RtgBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RTG.get(), pos, state);
    }

    public IEnergyStorage getEnergyStorage() { return energy; }
    public ItemStackHandler getItems() { return inv; }
    public ItemStackHandler getAutomationHandler() { return inv; }
    public boolean hasFuel() { return !inv.getStackInSlot(0).isEmpty(); }

    public static void serverTick(Level level, BlockPos pos, BlockState state, RtgBlockEntity be) {
        if (be.inv.getStackInSlot(0).isEmpty()) return;
        // spent fuel still leaks a bit
        if (be.inv.getStackInSlot(0).is(ModItems.SPENT_FUEL.get())) {
            PollutionData.leakRadiation(level, pos, 1);
        }
        be.energy.addInternal(2);
        EnergyUtil.push(level, pos, be.energy, 4, false);
        if (level.getGameTime() % 40 == 0) be.setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider r) {
        super.saveAdditional(tag, r);
        tag.putInt("Energy", energy.getEnergyStored());
        tag.put("Inv", inv.serializeNBT(r));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider r) {
        super.loadAdditional(tag, r);
        energy.setEnergy(tag.getInt("Energy"));
        if (tag.contains("Inv")) inv.deserializeNBT(r, tag.getCompound("Inv"));
    }
}
