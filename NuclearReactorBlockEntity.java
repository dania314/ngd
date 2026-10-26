package com.techquest.ecology;

import com.techquest.energy.EnergyUtil;
import com.techquest.energy.MachineEnergyStorage;
import com.techquest.menu.NuclearReactorMenu;
import com.techquest.registry.ModBlockEntities;
import com.techquest.registry.ModBlocks;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.items.ItemStackHandler;

/**
 * Уран → много FE + heat. Нужны охлаждение (градирня) и желательно скруббер.
 * Без охлаждения heat растёт → радиация + шанс взрыва.
 * После сгорания урана → spent_fuel в выход.
 */
public class NuclearReactorBlockEntity extends BlockEntity implements MenuProvider {
    public static final int CAPACITY = 500_000;
    public static final int MAX_HEAT = 2000;
    public static final int FUEL_BURN = 400; // ticks per uranium
    public static final int DATA_COUNT = 5;

    private final MachineEnergyStorage energy = new MachineEnergyStorage(CAPACITY, 0, 2048);
    private final ItemStackHandler inv = new ItemStackHandler(2) {
        @Override protected void onContentsChanged(int slot) { setChanged(); }
        @Override public boolean isItemValid(int slot, ItemStack stack) {
            if (slot == 0) return stack.is(ModItems.URANIUM_INGOT.get());
            return false;
        }
    };

    private int heat;
    private int burnLeft;
    private boolean running;

    private final ContainerData data = new ContainerData() {
        @Override public int get(int i) {
            return switch (i) {
                case 0 -> energy.getEnergyStored() & 0xFFFF;
                case 1 -> (energy.getEnergyStored() >>> 16) & 0xFFFF;
                case 2 -> heat;
                case 3 -> burnLeft;
                case 4 -> running ? 1 : 0;
                default -> 0;
            };
        }
        @Override public void set(int i, int v) {}
        @Override public int getCount() { return DATA_COUNT; }
    };

    public NuclearReactorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.NUCLEAR_REACTOR.get(), pos, state);
    }

    public IEnergyStorage getEnergyStorage() { return energy; }
    public ItemStackHandler getItems() { return inv; }
    public ItemStackHandler getAutomationHandler() { return inv; }
    public ContainerData getData() { return data; }

    private boolean hasCooling(Level level, BlockPos pos) {
        for (Direction d : Direction.values()) {
            if (level.getBlockState(pos.relative(d)).is(ModBlocks.COOLING_TOWER.get())) return true;
        }
        return false;
    }

    private boolean hasScrubber(Level level, BlockPos pos) {
        for (Direction d : Direction.values()) {
            if (level.getBlockState(pos.relative(d)).is(ModBlocks.SCRUBBER.get())) return true;
        }
        return false;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, NuclearReactorBlockEntity be) {
        // start fuel
        if (be.burnLeft <= 0) {
            ItemStack fuel = be.inv.getStackInSlot(0);
            if (!fuel.isEmpty() && fuel.is(ModItems.URANIUM_INGOT.get())) {
                ItemStack out = be.inv.getStackInSlot(1);
                if (out.isEmpty() || (out.is(ModItems.SPENT_FUEL.get()) && out.getCount() < out.getMaxStackSize())) {
                    be.inv.extractItem(0, 1, false);
                    be.burnLeft = FUEL_BURN;
                    be.running = true;
                }
            } else {
                be.running = false;
            }
        }

        if (be.burnLeft > 0) {
            be.burnLeft--;
            int gen = be.hasCooling(level, pos) ? 120 : 80;
            be.energy.addInternal(gen);
            be.heat = Math.min(MAX_HEAT, be.heat + (be.hasCooling(level, pos) ? 2 : 6));

            // radiation leak
            int rad = be.hasScrubber(level, pos) ? 1 : 4;
            if (be.heat > 800) rad += 3;
            PollutionData.leakRadiation(level, pos, rad);

            if (be.burnLeft == 0) {
                be.inv.insertItem(1, new ItemStack(ModItems.SPENT_FUEL.get()), false);
                be.running = false;
            }
        } else {
            be.heat = Math.max(0, be.heat - 1);
        }

        if (be.hasCooling(level, pos)) {
            be.heat = Math.max(0, be.heat - 5);
        }

        // meltdown
        if (be.heat >= MAX_HEAT - 50 && !be.hasCooling(level, pos)) {
            if (level.random.nextInt(60) == 0) {
                PollutionData.leakRadiation(level, pos, 40);
                level.removeBlock(pos, false);
                level.explode(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                        5.0f, true, Level.ExplosionInteraction.BLOCK);
                return;
            }
        }

        EnergyUtil.push(level, pos, be.energy, 1024, false);
        be.setChanged();
    }

    @Override public Component getDisplayName() {
        return Component.translatable("block.techquest.nuclear_reactor");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        return new NuclearReactorMenu(id, inv, this, data);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider r) {
        super.saveAdditional(tag, r);
        tag.putInt("Energy", energy.getEnergyStored());
        tag.putInt("Heat", heat);
        tag.putInt("Burn", burnLeft);
        tag.put("Inv", inv.serializeNBT(r));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider r) {
        super.loadAdditional(tag, r);
        energy.setEnergy(tag.getInt("Energy"));
        heat = tag.getInt("Heat");
        burnLeft = tag.getInt("Burn");
        if (tag.contains("Inv")) inv.deserializeNBT(r, tag.getCompound("Inv"));
    }
}
