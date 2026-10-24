package com.techquest.menu;

import com.techquest.block.entity.ProcessingBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

/** Меню для всех машин «вход → выход»: слот 0 — вход, слот 1 — выход. */
public class ProcessingMenu extends AbstractContainerMenu {
    private static final int MACHINE_SLOTS = 2;
    private static final int PLAYER_START = MACHINE_SLOTS;        // 2
    private static final int HOTBAR_START = PLAYER_START + 27;    // 29
    private static final int END = HOTBAR_START + 9;              // 38

    public final ProcessingBlockEntity blockEntity;
    private final ContainerLevelAccess access;
    private final ContainerData data;

    /** Клиентский конструктор (через MenuType). */
    public ProcessingMenu(MenuType<?> type, int containerId, Inventory inventory, RegistryFriendlyByteBuf buf) {
        this(type, containerId, inventory, findBlockEntity(inventory, buf.readBlockPos()),
                new SimpleContainerData(ProcessingBlockEntity.DATA_COUNT));
    }

    /** Серверный конструктор. */
    public ProcessingMenu(MenuType<?> type, int containerId, Inventory inventory, ProcessingBlockEntity be,
                          ContainerData data) {
        super(type, containerId);
        this.blockEntity = be;
        this.data = data;
        this.access = ContainerLevelAccess.create(be.getLevel(), be.getBlockPos());

        addSlot(new SlotItemHandler(be.getInventory(), 0, 56, 35));
        addSlot(new OutputSlot(be.getInventory(), 1, 116, 35));

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(inventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(inventory, col, 8 + col * 18, 142));
        }
        addDataSlots(data);
    }

    private static ProcessingBlockEntity findBlockEntity(Inventory inventory, BlockPos pos) {
        if (inventory.player.level().getBlockEntity(pos) instanceof ProcessingBlockEntity be) {
            return be;
        }
        throw new IllegalStateException("No machine at " + pos);
    }

    public int getEnergy() {
        return ((data.get(1) & 0xFFFF) << 16) | (data.get(0) & 0xFFFF);
    }

    public int getProgress() {
        return data.get(2);
    }

    public int getMaxProgress() {
        return data.get(3);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (slot == null || !slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();

        if (index < MACHINE_SLOTS) {
            if (!moveItemStackTo(stack, PLAYER_START, END, true)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(stack, 0, 1, false)) {
            if (index < HOTBAR_START) {
                if (!moveItemStackTo(stack, HOTBAR_START, END, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!moveItemStackTo(stack, PLAYER_START, HOTBAR_START, false)) {
                return ItemStack.EMPTY;
            }
        }

        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        if (stack.getCount() == copy.getCount()) {
            return ItemStack.EMPTY;
        }
        slot.onTake(player, stack);
        return copy;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, blockEntity.getBlockState().getBlock());
    }

    /** Слот выхода: в него нельзя ничего класть руками. */
    private static class OutputSlot extends SlotItemHandler {
        OutputSlot(IItemHandler handler, int index, int x, int y) {
            super(handler, index, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }
    }
}
