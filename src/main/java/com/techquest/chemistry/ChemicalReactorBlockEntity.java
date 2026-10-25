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
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.items.ItemStackHandler;

/**
 * Химреактор: 2 входа + 1 выход.
 * Рецепты:
 * - rubber + coal → carbon_fiber
 * - plastic + iron → kevlar (условно)
 * - mazut + gunpowder → rocket_fuel
 * - sulfuric_acid + iron → (пока) steel boost path
 */
public class ChemicalReactorBlockEntity extends BlockEntity implements MenuProvider {
    public static final int CAPACITY = 50_000;
    public static final int ENERGY = 60;
    public static final int MAX_PROGRESS = 120;

    private final MachineEnergyStorage energy = new MachineEnergyStorage(CAPACITY, 250, 0);
    private final ItemStackHandler inv = new ItemStackHandler(3) {
        @Override protected void onContentsChanged(int slot) { setChanged(); }
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


    public ChemicalReactorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CHEMICAL_REACTOR.get(), pos, state);
    }

    public IEnergyStorage getEnergyStorage() { return energy; }
    public ItemStackHandler getItems() { return inv; }
    public ItemStackHandler getAutomationHandler() { return inv; }
    public int getProgress() { return progress; }
    public ContainerData getData() { return data; }

    @Override public Component getDisplayName() {
        return Component.translatable("block.techquest.chemical_reactor");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        return new ChemMachineMenu(ModMenus.CHEMICAL_REACTOR.get(), id, inv, this, data, 3);
    }


    private static ItemStack recipeOutput(ItemStack a, ItemStack b) {
        if (match(a, b, ModItems.RUBBER.get(), Items.COAL) || match(a, b, ModItems.RUBBER.get(), Items.CHARCOAL))
            return new ItemStack(ModItems.CARBON_FIBER.get());
        if (match(a, b, ModItems.PLASTIC.get(), Items.IRON_INGOT))
            return new ItemStack(ModItems.KEVLAR.get());
        if (match(a, b, ModItems.MAZUT.get(), Items.GUNPOWDER) || match(a, b, ModItems.GASOLINE.get(), Items.GUNPOWDER))
            return new ItemStack(ModItems.ROCKET_FUEL.get());
        if (match(a, b, ModItems.SULFURIC_ACID.get(), Items.IRON_INGOT))
            return new ItemStack(ModItems.STEEL_INGOT.get(), 2);
        if (match(a, b, ModItems.SOOT.get(), ModItems.CRUDE_OIL.get()))
            return new ItemStack(ModItems.RUBBER.get(), 3);
        return ItemStack.EMPTY;
    }

    private static boolean match(ItemStack a, ItemStack b, Item x, Item y) {
        return (a.is(x) && b.is(y)) || (a.is(y) && b.is(x));
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ChemicalReactorBlockEntity be) {
        ItemStack a = be.inv.getStackInSlot(0);
        ItemStack b = be.inv.getStackInSlot(1);
        ItemStack out = recipeOutput(a, b);
        if (out.isEmpty() || a.isEmpty() || b.isEmpty()) {
            be.progress = 0;
            return;
        }
        ItemStack slot2 = be.inv.getStackInSlot(2);
        if (!slot2.isEmpty() && (!ItemStack.isSameItemSameComponents(slot2, out)
                || slot2.getCount() + out.getCount() > slot2.getMaxStackSize())) {
            return;
        }
        if (be.energy.getEnergyStored() < ENERGY) return;
        be.energy.removeInternal(ENERGY);
        be.progress++;
        if (be.progress >= MAX_PROGRESS) {
            be.progress = 0;
            be.inv.extractItem(0, 1, false);
            be.inv.extractItem(1, 1, false);
            be.inv.insertItem(2, out, false);
            be.setChanged();
        }
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
