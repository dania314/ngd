package com.techquest.menu;

import com.techquest.chemistry.ChemicalReactorBlockEntity;
import com.techquest.chemistry.RefineryBlockEntity;
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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

public class ChemMachineMenu extends AbstractContainerMenu {
    public static final int DATA_COUNT = 4; // energyLo, energyHi, progress, maxProgress

    private final BlockEntity blockEntity;
    private final ContainerLevelAccess access;
    private final ContainerData data;
    private final int machineSlots;

    public ChemMachineMenu(MenuType<?> type, int id, Inventory inv, RegistryFriendlyByteBuf buf, int machineSlots) {
        this(type, id, inv, inv.player.level().getBlockEntity(buf.readBlockPos()),
                new SimpleContainerData(DATA_COUNT), machineSlots);
    }

    public ChemMachineMenu(MenuType<?> type, int id, Inventory inv, BlockEntity be,
                           ContainerData data, int machineSlots) {
        super(type, id);
        this.blockEntity = be;
        this.data = data;
        this.machineSlots = machineSlots;
        this.access = ContainerLevelAccess.create(be.getLevel(), be.getBlockPos());

        IItemHandler handler = getHandler(be);
        if (machineSlots == 4) {
            // Refinery: input center-left, 3 outputs
            addSlot(new SlotItemHandler(handler, 0, 44, 35));
            addSlot(new OutputSlot(handler, 1, 98, 17));
            addSlot(new OutputSlot(handler, 2, 98, 35));
            addSlot(new OutputSlot(handler, 3, 98, 53));
        } else {
            // Reactor: 2 in, 1 out
            addSlot(new SlotItemHandler(handler, 0, 44, 26));
            addSlot(new SlotItemHandler(handler, 1, 44, 44));
            addSlot(new OutputSlot(handler, 2, 116, 35));
        }

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(inv, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(inv, col, 8 + col * 18, 142));
        }
        addDataSlots(data);
    }

    private static IItemHandler getHandler(BlockEntity be) {
        if (be instanceof RefineryBlockEntity r) return r.getItems();
        if (be instanceof ChemicalReactorBlockEntity c) return c.getItems();
        throw new IllegalStateException("Unsupported BE " + be);
    }

    public int getEnergy() {
        return ((data.get(1) & 0xFFFF) << 16) | (data.get(0) & 0xFFFF);
    }

    public int getProgress() { return data.get(2); }
    public int getMaxProgress() { return Math.max(1, data.get(3)); }
    public BlockEntity getBlockEntity() { return blockEntity; }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot == null || !slot.hasItem()) return result;
        ItemStack stack = slot.getItem();
        result = stack.copy();
        int playerStart = machineSlots;
        int end = slots.size();
        if (index < machineSlots) {
            if (!moveItemStackTo(stack, playerStart, end, true)) return ItemStack.EMPTY;
        } else {
            if (!moveItemStackTo(stack, 0, machineSlots == 4 ? 1 : 2, false)) return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY);
        else slot.setChanged();
        return result;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, blockEntity.getBlockState().getBlock());
    }

    private static class OutputSlot extends SlotItemHandler {
        public OutputSlot(IItemHandler h, int i, int x, int y) { super(h, i, x, y); }
        @Override public boolean mayPlace(ItemStack stack) { return false; }
    }
}
