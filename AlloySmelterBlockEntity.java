package com.techquest.block.entity;

import com.techquest.block.AlloySmelterBlock;
import com.techquest.energy.MachineEnergyStorage;
import com.techquest.menu.ProcessingMenu;
import com.techquest.multiblock.ModMultiblocks;
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
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

/**
 * Сплавная печь (мультиблок): 2 входа → 1 выход.
 * Пример: медь + олово → бронза (уже есть крафт, но тут быстрее и пачкой).
 */
public class AlloySmelterBlockEntity extends BlockEntity implements MenuProvider {
    public static final int CAPACITY = 20_000;
    public static final int ENERGY_PER_TICK = 40;
    public static final int PROCESS_TIME = 100;

    private final MachineEnergyStorage energy = new MachineEnergyStorage(CAPACITY, 200, 0);
    private final ItemStackHandler inv = new ItemStackHandler(3) { // 0,1 input; 2 output
        @Override
        protected void onContentsChanged(int slot) { setChanged(); }
    };
    private boolean assembled;
    private int progress;
    private final ContainerData data = new SimpleContainerData(4) {
        @Override public int get(int i) {
            return switch (i) {
                case 0 -> energy.getEnergyStored() & 0xFFFF;
                case 1 -> (energy.getEnergyStored() >>> 16) & 0xFFFF;
                case 2 -> progress;
                case 3 -> PROCESS_TIME;
                default -> 0;
            };
        }
        @Override public void set(int i, int v) {}
        @Override public int getCount() { return 4; }
    };

    public AlloySmelterBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ALLOY_SMELTER.get(), pos, state);
    }

    public void assemble() { assembled = true; setChanged(); }
    public void disassemble() { assembled = false; progress = 0; setChanged(); }
    public IEnergyStorage getEnergyStorage() { return energy; }
    public ItemStackHandler getInventory() { return inv; }

    public static void serverTick(Level level, BlockPos pos, BlockState state, AlloySmelterBlockEntity be) {
        if (!state.getValue(AlloySmelterBlock.FORMED)) return;

        if (!ModMultiblocks.matchesAlloy(level, pos, Direction.NORTH)) {
            be.disassemble();
            level.setBlock(pos, state.setValue(AlloySmelterBlock.FORMED, false)
                    .setValue(AlloySmelterBlock.LIT, false), Block.UPDATE_ALL);
            return;
        }

        ItemStack a = be.inv.getStackInSlot(0);
        ItemStack b = be.inv.getStackInSlot(1);
        ItemStack out = be.inv.getStackInSlot(2);

        ItemStack result = findAlloy(a, b);
        boolean can = !result.isEmpty()
                && (out.isEmpty() || (ItemStack.isSameItemSameComponents(out, result)
                && out.getCount() + result.getCount() <= out.getMaxStackSize()));

        boolean working = false;
        if (can && be.energy.getEnergyStored() >= ENERGY_PER_TICK) {
            be.energy.removeInternal(ENERGY_PER_TICK);
            be.progress++;
            working = true;
            if (be.progress >= PROCESS_TIME) {
                be.progress = 0;
                be.inv.extractItem(0, 1, false);
                be.inv.extractItem(1, 1, false);
                if (out.isEmpty()) {
                    be.inv.setStackInSlot(2, result.copy());
                } else {
                    out.grow(result.getCount());
                }
                be.setChanged();
            }
        } else if (be.progress > 0) {
            be.progress = 0;
        }

        if (state.getValue(AlloySmelterBlock.LIT) != working) {
            level.setBlock(pos, state.setValue(AlloySmelterBlock.LIT, working), Block.UPDATE_ALL);
        }
    }

    private static ItemStack findAlloy(ItemStack a, ItemStack b) {
        if (a.isEmpty() || b.isEmpty()) return ItemStack.EMPTY;
        // copper + tin -> 2 bronze
        if (isPair(a, b, Items.COPPER_INGOT, ModItems.TIN_INGOT.get())) {
            return new ItemStack(ModItems.BRONZE_INGOT.get(), 2);
        }
        // iron + nickel -> 2 nickel-steel-ish (use nickel ingot x2 as placeholder alloy)
        if (isPair(a, b, Items.IRON_INGOT, ModItems.NICKEL_INGOT.get())) {
            return new ItemStack(ModItems.NICKEL_INGOT.get(), 2);
        }
        return ItemStack.EMPTY;
    }

    private static boolean isPair(ItemStack a, ItemStack b, net.minecraft.world.item.Item x, net.minecraft.world.item.Item y) {
        return (a.is(x) && b.is(y)) || (a.is(y) && b.is(x));
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.techquest.alloy_smelter");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        // Reuse processing menu layout is awkward (2 inputs). Simple: use processing with slot0 only for now
        // Better: open with custom later. For compile safety use null menu until menu registered.
        return null; // TODO menu — open via message for now
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Inv", inv.serializeNBT(registries));
        tag.putInt("Energy", energy.getEnergyStored());
        tag.putBoolean("Assembled", assembled);
        tag.putInt("Progress", progress);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        inv.deserializeNBT(registries, tag.getCompound("Inv"));
        energy.setEnergy(tag.getInt("Energy"));
        assembled = tag.getBoolean("Assembled");
        progress = tag.getInt("Progress");
    }
}
