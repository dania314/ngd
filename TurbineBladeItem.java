package com.techquest.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/**
 * Лопасть ветряка. 6 типов:
 * energyPerTick — FE/т
 * maxDurability — тики работы до поломки (0 = вечная)
 * craftTier — сложность
 */
public class TurbineBladeItem extends Item {
    private final int energyPerTick;
    private final int maxDurability;
    private final int craftTier;

    public TurbineBladeItem(Properties props, int energyPerTick, int maxDurability, int craftTier) {
        super(props.stacksTo(16));
        this.energyPerTick = energyPerTick;
        this.maxDurability = maxDurability;
        this.craftTier = craftTier;
    }

    public int getEnergyPerTick() {
        return energyPerTick;
    }

    public int getMaxDurability() {
        return maxDurability;
    }

    public int getCraftTier() {
        return craftTier;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.techquest.blade.energy", energyPerTick));
        if (maxDurability <= 0) {
            tooltip.add(Component.translatable("tooltip.techquest.blade.durability.infinite"));
        } else {
            tooltip.add(Component.translatable("tooltip.techquest.blade.durability", maxDurability, maxDurability));
        }
        tooltip.add(Component.translatable("tooltip.techquest.blade.tier", craftTier));
    }
}
