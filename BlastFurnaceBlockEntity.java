package com.techquest.ecology;

import com.techquest.energy.MachineEnergyStorage;
import com.techquest.menu.BlastFurnaceMenu;
import com.techquest.multiblock.ModMultiblocks;
import com.techquest.registry.ModBlockEntities;
import com.techquest.registry.ModMenus;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.items.ItemStackHandler;

/**
 * Доменный цех 3×3×3 (корпуса + контроллер).
 * Когда formed — плавит пачками по 4 за раз, быстрее обычной печи.
 */
public class BlastFurnaceBlockEntity extends BlockEntity implements MenuProvider {
    public static final int CAPACITY = 100_000;
    public static final int ENERGY = 20;
    public static final int MAX_PROGRESS = 60; // faster than vanilla 200
    public static final int DATA_COUNT = 4;

    private final MachineEnergyStorage energy = new MachineEnergyStorage(CAPACITY, 400, 0);
    private final ItemStackHandler inv = new ItemStackHandler(8) { // 0-3 in, 4-7 out
        @Override protected void onContentsChanged(int slot) { setChanged(); }
    };
    private boolean formed;
    private int progress;

    private final ContainerData data = new ContainerData() {
        @Override public int get(int i) {
            return switch (i) {
                case 0 -> energy.getEnergyStored() & 0xFFFF;
                case 1 -> (energy.getEnergyStored() >>> 16) & 0xFFFF;
                case 2 -> progress;
                case 3 -> formed ? 1 : 0;
                default -> 0;
            };
        }
        @Override public void set(int i, int v) {}
        @Override public int getCount() { return DATA_COUNT; }
    };

    public BlastFurnaceBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BLAST_FURNACE.get(), pos, state);
    }

    public IEnergyStorage getEnergyStorage() { return energy; }
    public ItemStackHandler getItems() { return inv; }
    public ItemStackHandler getAutomationHandler() { return inv; }
    public ContainerData getData() { return data; }
    public boolean isFormed() { return formed; }

    public boolean tryForm(Player player) {
        if (level == null) return false;
        if (formed) {
            formed = false;
            level.setBlock(worldPosition, getBlockState().setValue(BlastFurnaceBlock.FORMED, false), Block.UPDATE_ALL);
            player.displayClientMessage(Component.translatable("message.techquest.blast.unformed"), true);
            setChanged();
            return true;
        }
        if (ModMultiblocks.matchesBlast(level, worldPosition)) {
            formed = true;
            level.setBlock(worldPosition, getBlockState().setValue(BlastFurnaceBlock.FORMED, true), Block.UPDATE_ALL);
            player.displayClientMessage(Component.translatable("message.techquest.blast.formed"), true);
            setChanged();
            return true;
        }
        player.displayClientMessage(Component.translatable("message.techquest.blast.invalid"), true);
        for (String step : ModMultiblocks.BLAST_FURNACE().getBuildSteps()) {
            if (step.contains("Контроллер")) continue;
            player.displayClientMessage(Component.literal(step), false);
            break; // don't spam all 26 lines
        }
        player.displayClientMessage(Component.translatable("message.techquest.blast.hint"), false);
        return false;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BlastFurnaceBlockEntity be) {
        if (!be.formed) {
            if (level.getGameTime() % 40 == 0 && !ModMultiblocks.matchesBlast(level, pos)) {
                // stay unformed
            } else if (be.formed && !ModMultiblocks.matchesBlast(level, pos)) {
                be.formed = false;
                level.setBlock(pos, state.setValue(BlastFurnaceBlock.FORMED, false), Block.UPDATE_ALL);
            }
            be.progress = 0;
            return;
        }
        if (!ModMultiblocks.matchesBlast(level, pos)) {
            be.formed = false;
            level.setBlock(pos, state.setValue(BlastFurnaceBlock.FORMED, false), Block.UPDATE_ALL);
            return;
        }
        if (be.energy.getEnergyStored() < ENERGY) return;

        // process first valid input slot
        for (int i = 0; i < 4; i++) {
            ItemStack in = be.inv.getStackInSlot(i);
            if (in.isEmpty()) continue;
            var opt = level.getRecipeManager().getRecipeFor(RecipeType.SMELTING, new SingleRecipeInput(in), level);
            if (opt.isEmpty()) continue;
            SmeltingRecipe recipe = opt.get().value();
            ItemStack result = recipe.getResultItem(level.registryAccess()).copy();
            if (result.isEmpty()) continue;
            // find output slot
            int outSlot = -1;
            for (int o = 4; o < 8; o++) {
                ItemStack out = be.inv.getStackInSlot(o);
                if (out.isEmpty() || (ItemStack.isSameItemSameComponents(out, result) && out.getCount() < out.getMaxStackSize())) {
                    outSlot = o;
                    break;
                }
            }
            if (outSlot < 0) continue;

            be.energy.removeInternal(ENERGY);
            be.progress++;
            if (be.progress >= MAX_PROGRESS) {
                be.progress = 0;
                be.inv.extractItem(i, 1, false);
                be.inv.insertItem(outSlot, result, false);
            }
            be.setChanged();
            return;
        }
        be.progress = 0;
    }

    @Override public Component getDisplayName() {
        return Component.translatable("block.techquest.blast_furnace_controller");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        return new BlastFurnaceMenu(id, inv, this, data);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider r) {
        super.saveAdditional(tag, r);
        tag.putInt("Energy", energy.getEnergyStored());
        tag.putInt("Progress", progress);
        tag.putBoolean("Formed", formed);
        tag.put("Inv", this.inv.serializeNBT(r));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider r) {
        super.loadAdditional(tag, r);
        energy.setEnergy(tag.getInt("Energy"));
        progress = tag.getInt("Progress");
        formed = tag.getBoolean("Formed");
        if (tag.contains("Inv")) this.inv.deserializeNBT(r, tag.getCompound("Inv"));
    }
}
