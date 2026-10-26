package com.techquest.chemistry;

import com.techquest.energy.MachineEnergyStorage;
import com.techquest.menu.ChemMachineMenu;
import com.techquest.menu.AssemblyLineMenu;
import com.techquest.registry.ModBlockEntities;
import com.techquest.registry.ModItems;
import com.techquest.registry.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.items.ItemStackHandler;

/**
 * Сборочная линия: 3 входа → 1 выход.
 * Рецепты эндгейм-компонентов. Бонус если рядом 2+ конвейера.
 */
public class AssemblyLineBlockEntity extends BlockEntity implements MenuProvider {
    public static final int CAPACITY = 80_000;
    public static final int ENERGY = 50;
    public static final int MAX_PROGRESS = 80;

    private final MachineEnergyStorage energy = new MachineEnergyStorage(CAPACITY, 300, 0);
    private final ItemStackHandler inv = new ItemStackHandler(4) {
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

    public AssemblyLineBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ASSEMBLY_LINE.get(), pos, state);
    }

    public IEnergyStorage getEnergyStorage() { return energy; }
    public ItemStackHandler getItems() { return inv; }
    public ItemStackHandler getAutomationHandler() { return inv; }
    public ContainerData getData() { return data; }

    private static ItemStack match(ItemStack a, ItemStack b, ItemStack c) {
        // advanced_circuit x2 from basic + gold + plastic
        if (has(a,b,c, ModItems.BASIC_CIRCUIT.get(), Items.GOLD_INGOT, ModItems.PLASTIC.get()))
            return new ItemStack(ModItems.ADVANCED_CIRCUIT.get(), 2);
        // carbon fiber + steel + rubber → kevlar x2
        if (has(a,b,c, ModItems.CARBON_FIBER.get(), ModItems.STEEL_INGOT.get(), ModItems.RUBBER.get()))
            return new ItemStack(ModItems.KEVLAR.get(), 2);
        // kevlar + advanced + steel → module plating
        if (has(a,b,c, ModItems.KEVLAR.get(), ModItems.ADVANCED_CIRCUIT.get(), ModItems.STEEL_INGOT.get()))
            return new ItemStack(ModItems.MODULE_PLATING.get(), 1);
        // uranium + lead + glassed → better containment item (lead foam)
        if (has(a,b,c, ModItems.SPENT_FUEL.get(), ModItems.LEAD_INGOT.get(), ModItems.GLASSED_WASTE.get()))
            return new ItemStack(ModItems.LEAD_FOAM.get(), 4);
        return ItemStack.EMPTY;
    }

    private static boolean has(ItemStack a, ItemStack b, ItemStack c,
                              net.minecraft.world.item.Item x,
                              net.minecraft.world.item.Item y,
                              net.minecraft.world.item.Item z) {
        java.util.Set<net.minecraft.world.item.Item> set = new java.util.HashSet<>();
        if (!a.isEmpty()) set.add(a.getItem());
        if (!b.isEmpty()) set.add(b.getItem());
        if (!c.isEmpty()) set.add(c.getItem());
        return set.contains(x) && set.contains(y) && set.contains(z) && set.size() >= 3;
    }

    private int conveyorBonus(Level level, BlockPos pos) {
        int n = 0;
        for (Direction d : Direction.values()) {
            if (level.getBlockState(pos.relative(d)).getBlock() instanceof com.techquest.conveyor.ConveyorBlock) n++;
        }
        return Math.min(2, n); // up to 2 bonus
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, AssemblyLineBlockEntity be) {
        ItemStack a = be.inv.getStackInSlot(0);
        ItemStack b = be.inv.getStackInSlot(1);
        ItemStack c = be.inv.getStackInSlot(2);
        ItemStack out = match(a, b, c);
        if (out.isEmpty()) { be.progress = 0; return; }

        ItemStack slot3 = be.inv.getStackInSlot(3);
        if (!slot3.isEmpty() && (!ItemStack.isSameItemSameComponents(slot3, out)
                || slot3.getCount() + out.getCount() > slot3.getMaxStackSize())) {
            return;
        }
        if (be.energy.getEnergyStored() < ENERGY) return;

        be.energy.removeInternal(ENERGY);
        int need = MAX_PROGRESS - be.conveyorBonus(level, pos) * 15;
        be.progress++;
        if (be.progress >= need) {
            be.progress = 0;
            be.inv.extractItem(0, 1, false);
            be.inv.extractItem(1, 1, false);
            be.inv.extractItem(2, 1, false);
            be.inv.insertItem(3, out, false);
        }
        be.setChanged();
    }

    @Override public Component getDisplayName() {
        return Component.translatable("block.techquest.assembly_line");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        return new AssemblyLineMenu(id, inv, this, data);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider r) {
        super.saveAdditional(tag, r);
        tag.putInt("Energy", energy.getEnergyStored());
        tag.putInt("Progress", progress);
        tag.put("Inv", this.inv.serializeNBT(r));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider r) {
        super.loadAdditional(tag, r);
        energy.setEnergy(tag.getInt("Energy"));
        progress = tag.getInt("Progress");
        if (tag.contains("Inv")) this.inv.deserializeNBT(r, tag.getCompound("Inv"));
    }
}
