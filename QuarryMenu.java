package com.techquest.menu;

import com.techquest.quarry.QuarryBlockEntity;
import com.techquest.registry.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.SlotItemHandler;

public class QuarryMenu extends AbstractContainerMenu {
    private final QuarryBlockEntity be;
    private final ContainerData data;
    private final ContainerLevelAccess access;

    public QuarryMenu(int id, Inventory inv, RegistryFriendlyByteBuf buf) {
        this(id, inv, (QuarryBlockEntity) inv.player.level().getBlockEntity(buf.readBlockPos()),
                new SimpleContainerData(QuarryBlockEntity.DATA_COUNT));
    }

    public QuarryMenu(int id, Inventory inv, QuarryBlockEntity be, ContainerData data) {
        super(ModMenus.QUARRY.get(), id);
        this.be = be;
        this.data = data;
        this.access = ContainerLevelAccess.create(be.getLevel(), be.getBlockPos());

        // filter 0-8 top row
        for (int i = 0; i < 9; i++) {
            addSlot(new SlotItemHandler(be.getItems(), i, 8 + i * 18, 18));
        }
        // buffer 9-17
        for (int i = 0; i < 9; i++) {
            addSlot(new SlotItemHandler(be.getItems(), 9 + i, 8 + i * 18, 48));
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

    public int getEnergy() {
        return ((data.get(1) & 0xFFFF) << 16) | (data.get(0) & 0xFFFF);
    }

    public boolean isActive() { return data.get(2) != 0; }
    public int getDepth() { return data.get(3); }
    public int getEnergyPct() { return data.get(4); }
    public QuarryBlockEntity getBlockEntity() { return be; }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot == null || !slot.hasItem()) return result;
        ItemStack stack = slot.getItem();
        result = stack.copy();
        if (index < 18) {
            if (!moveItemStackTo(stack, 18, slots.size(), true)) return ItemStack.EMPTY;
        } else {
            // into filter first
            if (!moveItemStackTo(stack, 0, 9, false)) {
                if (!moveItemStackTo(stack, 9, 18, false)) return ItemStack.EMPTY;
            }
        }
        if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY);
        else slot.setChanged();
        return result;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, be.getBlockState().getBlock());
    }
}
