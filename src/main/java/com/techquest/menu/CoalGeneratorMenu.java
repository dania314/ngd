package com.techquest.menu;

import com.techquest.block.entity.CoalGeneratorBlockEntity;
import com.techquest.registry.ModBlocks;
import com.techquest.registry.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.SlotItemHandler;

public class CoalGeneratorMenu extends AbstractContainerMenu {
    private static final int MACHINE_SLOTS = 1;
    private static final int PLAYER_INV_START = MACHINE_SLOTS;          // 1
    private static final int HOTBAR_START = PLAYER_INV_START + 27;      // 28
    private static final int END = HOTBAR_START + 9;                    // 37

    public final CoalGeneratorBlockEntity blockEntity;
    private final ContainerLevelAccess access;
    private final ContainerData data;

    /** Клиентский конструктор (вызывается через MenuType). */
    public CoalGeneratorMenu(int containerId, Inventory inventory, RegistryFriendlyByteBuf buf) {
        this(containerId, inventory, findBlockEntity(inventory, buf.readBlockPos()),
                new SimpleContainerData(CoalGeneratorBlockEntity.DATA_COUNT));
    }

    /** Серверный конструктор. */
    public CoalGeneratorMenu(int containerId, Inventory inventory, CoalGeneratorBlockEntity be, ContainerData data) {
        super(ModMenus.COAL_GENERATOR.get(), containerId);
        this.blockEntity = be;
        this.data = data;
        this.access = ContainerLevelAccess.create(be.getLevel(), be.getBlockPos());

        addSlot(new SlotItemHandler(be.getInventory(), 0, 56, 44));

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

    private static CoalGeneratorBlockEntity findBlockEntity(Inventory inventory, BlockPos pos) {
        if (inventory.player.level().getBlockEntity(pos) instanceof CoalGeneratorBlockEntity be) {
            return be;
        }
        throw new IllegalStateException("No coal generator at " + pos);
    }

    public int getEnergy() {
        return ((data.get(1) & 0xFFFF) << 16) | (data.get(0) & 0xFFFF);
    }

    public int getBurnTime() {
        return data.get(2);
    }

    public int getBurnTimeTotal() {
        return data.get(3);
    }

    public boolean isBurning() {
        return getBurnTime() > 0;
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
            // из машины в инвентарь игрока
            if (!moveItemStackTo(stack, PLAYER_INV_START, END, true)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(stack, 0, MACHINE_SLOTS, false)) {
            // из инвентаря игрока: сначала в машину, иначе между инвентарём и хотбаром
            if (index < HOTBAR_START) {
                if (!moveItemStackTo(stack, HOTBAR_START, END, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!moveItemStackTo(stack, PLAYER_INV_START, HOTBAR_START, false)) {
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
        return stillValid(access, player, ModBlocks.COAL_GENERATOR.get());
    }
}
