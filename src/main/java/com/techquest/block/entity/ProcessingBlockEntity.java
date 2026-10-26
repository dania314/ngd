package com.techquest.block.entity;

import com.techquest.block.ProcessingMachineBlock;
import com.techquest.energy.MachineEnergyStorage;
import com.techquest.menu.ProcessingMenu;
import com.techquest.recipe.MachineRecipe;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * Основа для всех машин «один вход → один выход»: слот входа (0), слот выхода (1),
 * приём энергии, прогресс. Конкретные машины задают только рецепты.
 */
public abstract class ProcessingBlockEntity extends BlockEntity implements MenuProvider {
    public static final int CAPACITY = 10_000;
    public static final int MAX_RECEIVE = 100;
    public static final int DATA_COUNT = 4;

    /** Результат поиска рецепта. */
    public record Result(ItemStack output, int time) {}

    protected final MachineEnergyStorage energy = new MachineEnergyStorage(CAPACITY, MAX_RECEIVE, 0);
    private final int energyPerTick;

    private final ItemStackHandler inventory = new ItemStackHandler(2) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            // Разрешаем любой предмет во вход: рецепт проверяется при тике.
            // Жёсткая проверка ломала пресс/дробилку, если рецепты ещё не синхронизировались.
            return slot == 0;
        }
    };

    /** Доступ для воронок и труб: вставлять можно только во вход, забирать только из выхода. */
    private final IItemHandler automation = new IItemHandler() {
        @Override
        public int getSlots() {
            return 2;
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return inventory.getStackInSlot(slot);
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            return slot == 0 ? inventory.insertItem(0, stack, simulate) : stack;
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return slot == 1 ? inventory.extractItem(1, amount, simulate) : ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return inventory.getSlotLimit(slot);
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return slot == 0 && inventory.isItemValid(0, stack);
        }
    };

    private int progress;
    private int maxProgress = 100;

    private ItemStack cachedKey = ItemStack.EMPTY;
    private Optional<Result> cachedResult = Optional.empty();

    /** Данные для GUI (энергия режется на две половинки из-за 16-битной синхронизации). */
    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> energy.getEnergyStored() & 0xFFFF;
                case 1 -> (energy.getEnergyStored() >>> 16) & 0xFFFF;
                case 2 -> progress;
                case 3 -> maxProgress;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
        }

        @Override
        public int getCount() {
            return DATA_COUNT;
        }
    };

    protected ProcessingBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, int energyPerTick) {
        super(type, pos, state);
        this.energyPerTick = energyPerTick;
    }

    /** Найти рецепт для данного входного предмета. */
    protected abstract Optional<Result> findRecipe(Level level, ItemStack input);

    /** Тип меню этой машины. */
    protected abstract MenuType<?> menuType();

    protected Optional<Result> findMachineRecipe(Level level, String machine, ItemStack input) {
        return MachineRecipe.find(level, machine, input)
                .map(r -> new Result(r.result().copy(), r.time()));
    }

    private Optional<Result> getRecipe(Level level, ItemStack input) {
        if (!ItemStack.isSameItemSameComponents(cachedKey, input)) {
            if (!cachedKey.isEmpty()) {
                progress = 0;
            }
            cachedKey = input.copyWithCount(1);
            cachedResult = input.isEmpty() ? Optional.empty() : findRecipe(level, input);
        }
        return cachedResult;
    }

    private boolean canStore(ItemStack output) {
        ItemStack current = inventory.getStackInSlot(1);
        if (current.isEmpty()) {
            return true;
        }
        return ItemStack.isSameItemSameComponents(current, output)
                && current.getCount() + output.getCount() <= current.getMaxStackSize();
    }

    private void finishCrafting(Result result) {
        inventory.extractItem(0, 1, false);
        ItemStack current = inventory.getStackInSlot(1);
        if (current.isEmpty()) {
            inventory.setStackInSlot(1, result.output().copy());
        } else {
            ItemStack grown = current.copy();
            grown.grow(result.output().getCount());
            inventory.setStackInSlot(1, grown);
        }
        progress = 0;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ProcessingBlockEntity be) {
        boolean dirty = false;
        boolean working = false;

        ItemStack input = be.inventory.getStackInSlot(0);
        Optional<Result> recipe = be.getRecipe(level, input);

        if (recipe.isPresent() && be.canStore(recipe.get().output())) {
            Result result = recipe.get();
            be.maxProgress = result.time();
            if (be.energy.getEnergyStored() >= be.energyPerTick) {
                be.energy.removeInternal(be.energyPerTick);
                be.progress++;
                working = true;
                if (be.progress >= be.maxProgress) {
                    be.finishCrafting(result);
                    dirty = true;
                }
                if (level.getGameTime() % 20 == 0) {
                    dirty = true;
                }
            }
        } else if (be.progress != 0) {
            be.progress = 0;
            dirty = true;
        }

        if (state.getValue(ProcessingMachineBlock.LIT) != working) {
            level.setBlock(pos, state.setValue(ProcessingMachineBlock.LIT, working), Block.UPDATE_ALL);
            dirty = true;
        }
        if (dirty) {
            be.setChanged();
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

    public IItemHandler getAutomationHandler() {
        return automation;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable(getBlockState().getBlock().getDescriptionId());
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new ProcessingMenu(menuType(), containerId, playerInventory, this, data);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Inventory", inventory.serializeNBT(registries));
        tag.putInt("Energy", energy.getEnergyStored());
        tag.putInt("Progress", progress);
        tag.putInt("MaxProgress", maxProgress);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        inventory.deserializeNBT(registries, tag.getCompound("Inventory"));
        energy.setEnergy(tag.getInt("Energy"));
        progress = tag.getInt("Progress");
        maxProgress = Math.max(1, tag.getInt("MaxProgress"));
    }
}
