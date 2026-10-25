package com.techquest.armor;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemAttributeModifiers;

import java.util.List;
import java.util.UUID;

/** Модульный нагрудник-экзоскелет: до 4 модулей в NBT. */
public class ExoChestItem extends ArmorItem {
    public static final String[] SLOT_NAMES = {"servo", "battery", "filter", "plating"};

    public ExoChestItem(Properties props) {
        super(ArmorMaterials.IRON, Type.CHESTPLATE, props.stacksTo(1).durability(500));
    }

    public static CompoundTag getMods(ItemStack stack) {
        CustomData data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        return data.copyTag().getCompound("ExoMods");
    }

    public static void setMod(ItemStack stack, String slot, String modId) {
        CustomData data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = data.copyTag();
        CompoundTag mods = tag.getCompound("ExoMods");
        if (modId == null || modId.isEmpty()) mods.remove(slot);
        else mods.putString(slot, modId);
        tag.put("ExoMods", mods);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> tip, TooltipFlag flag) {
        CompoundTag mods = getMods(stack);
        tip.add(Component.translatable("tooltip.techquest.exo.slots"));
        for (String s : SLOT_NAMES) {
            boolean has = mods.contains(s);
            tip.add(Component.translatable("tooltip.techquest.exo." + s)
                    .withStyle(has ? net.minecraft.ChatFormatting.GREEN : net.minecraft.ChatFormatting.DARK_GRAY));
        }
        tip.add(Component.translatable("tooltip.techquest.exo.install_hint").withStyle(net.minecraft.ChatFormatting.GRAY));
    }
}
