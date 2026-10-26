package com.techquest.menu;

import com.techquest.ecology.BoilerBlockEntity;
import com.techquest.registry.ModMenus;
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

public class BoilerMenu extends AbstractContainerMenu {
    private final BoilerBlockEntity be;
    private final ContainerData data;
    private final ContainerLevelAccess access;

    public BoilerMenu(int id, Inventory inv, RegistryFriendlyByteBuf buf) {
        this(id, inv, (BoilerBlockEntity) inv.player.level().getBlockEntity(buf.readBlockPos()),
                new SimpleContainerData(BoilerBlockEntity.DATA_COUNT));
    }

    public BoilerMenu(int id, Inventory inv, BoilerBlockEntity be, ContainerData data) {
        super(ModMenus.BOILER.get(), id);
        this.be = be;
        this.data = data;
        this.access = ContainerLevelAccess.create(be.getLevel(), be.getBlockPos());

        addSlot(new SlotItemHandler(be.getItems(), 0, 44, 35)); // fuel
        addSlot(new SlotItemHandler(be.getItems(), 1, 80, 35)); // water

        for (int row = 0; row < 3; row++)
            for (int col = 0; col < 9; col++)
                addSlot(new Slot(inv, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
        for (int col = 0; col < 9; col++)
            addSlot(new Slot(inv, col, 8 + col * 18, 142));
        addDataSlots(data);
    }

    public int getEnergy() { return ((data.get(1) & 0xFFFF) << 16) | (data.get(0) & 0xFFFF); }
    public int getHeat() { return data.get(2); }
    public int getPressure() { return data.get(3); }
    public int getWater() { return data.get(5); }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot == null || !slot.hasItem()) return result;
        ItemStack stack = slot.getItem();
        result = stack.copy();
        if (index < 2) {
            if (!moveItemStackTo(stack, 2, slots.size(), true)) return ItemStack.EMPTY;
        } else {
            if (!moveItemStackTo(stack, 0, 2, false)) return ItemStack.EMPTY;
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
