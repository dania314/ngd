package com.techquest.block.entity;

import com.techquest.block.CoalGeneratorBlock;
import com.techquest.energy.MachineEnergyStorage;
import com.techquest.menu.CoalGeneratorMenu;
import com.techquest.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

/**
 * Угольный генератор: сжигает топливо и вырабатывает FE.
 * Если буфер энергии полон, топливо не горит (не тратится впустую).
 */
public class CoalGeneratorBlockEntity extends BlockEntity implements MenuProvider {
    public static final int CAPACITY = 30_000;      // FE
    public static final int ENERGY_PER_TICK = 20;   // FE/t при горении
    public static final int MAX_OUTPUT = 200;       // FE/t наружу
    public static final int DATA_COUNT = 4;

    private final MachineEnergyStorage energy = new MachineEnergyStorage(CAPACITY, 0, MAX_OUTPUT);

    private final ItemStackHandler inventory = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return CommonHooks.getBurnTime(stack, RecipeType.SMELTING) > 0;
        }
    };

    private int burnTime;
    private int burnTimeTotal;

    /**
     * Данные для GUI. Ванильная синхронизация шлёт значения как short (16 бит),
     * поэтому энергию режем на две половинки.
     */
    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> energy.getEnergyStored() & 0xFFFF;
                case 1 -> (energy.getEnergyStored() >>> 16) & 0xFFFF;
                case 2 -> burnTime;
                case 3 -> burnTimeTotal;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            // Только сервер → клиент, менять с клиента нельзя.
        }

        @Override
        public int getCount() {
            return DATA_COUNT;
        }
    };

    public CoalGeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.COAL_GENERATOR.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, CoalGeneratorBlockEntity be) {
        boolean dirty = false;
        boolean roomInBuffer = be.energy.getEnergyStored() < be.energy.getMaxEnergyStored();

        if (be.burnTime > 0) {
            if (roomInBuffer) {
                be.energy.addInternal(ENERGY_PER_TICK);
                be.burnTime--;
                if (level.getGameTime() % 20 == 0) {
                    dirty = true;
                }
            }
        } else if (roomInBuffer) {
            ItemStack fuel = be.inventory.getStackInSlot(0);
            if (!fuel.isEmpty()) {
                int burn = CommonHooks.getBurnTime(fuel, RecipeType.SMELTING);
                if (burn > 0) {
                    ItemStack remainder = fuel.hasCraftingRemainingItem()
                            ? fuel.getCraftingRemainingItem()
                            : ItemStack.EMPTY;
                    be.inventory.extractItem(0, 1, false);
                    if (!remainder.isEmpty() && be.inventory.getStackInSlot(0).isEmpty()) {
                        be.inventory.setStackInSlot(0, remainder);
                    }
                    be.burnTime = burn;
                    be.burnTimeTotal = burn;
                    dirty = true;
                }
            }
        }

        be.pushEnergy(level, pos);

        boolean lit = be.burnTime > 0;
        if (state.getValue(CoalGeneratorBlock.LIT) != lit) {
            level.setBlock(pos, state.setValue(CoalGeneratorBlock.LIT, lit), Block.UPDATE_ALL);
            dirty = true;
        }

        if (dirty) {
            be.setChanged();
        }
    }

    /** Раздаём энергию всем соседям, которые умеют принимать FE. */
    private void pushEnergy(Level level, BlockPos pos) {
        if (energy.getEnergyStored() <= 0) {
            return;
        }
        for (Direction dir : Direction.values()) {
            IEnergyStorage target = level.getCapability(Capabilities.EnergyStorage.BLOCK,
                    pos.relative(dir), dir.getOpposite());
            if (target == null || !target.canReceive()) {
                continue;
            }
            int offer = energy.extractEnergy(MAX_OUTPUT, true);
            if (offer <= 0) {
                return;
            }
            int accepted = target.receiveEnergy(offer, false);
            if (accepted > 0) {
                energy.extractEnergy(accepted, false);
            }
        }
    }

    public void dropContents(Level level, BlockPos pos) {
        SimpleContainer container = new SimpleContainer(inventory.getSlots());
        for (int i = 0; i < inventory.getSlots(); i++) {
            container.setItem(i, inventory.getStackInSlot(i));
        }
        Containers.dropContents(level, pos, container);
    }

    public IEnergyStorage getEnergyStorage() {
        return energy;
    }

    public ItemStackHandler getInventory() {
        return inventory;
    }

    public ContainerData getData() {
        return data;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.techquest.coal_generator");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new CoalGeneratorMenu(containerId, playerInventory, this, data);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Inventory", inventory.serializeNBT(registries));
        tag.putInt("Energy", energy.getEnergyStored());
        tag.putInt("BurnTime", burnTime);
        tag.putInt("BurnTimeTotal", burnTimeTotal);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        inventory.deserializeNBT(registries, tag.getCompound("Inventory"));
        energy.setEnergy(tag.getInt("Energy"));
        burnTime = tag.getInt("BurnTime");
        burnTimeTotal = tag.getInt("BurnTimeTotal");
    }
}
