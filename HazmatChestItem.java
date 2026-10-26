package com.techquest.armor;

import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;

public class HazmatChestItem extends ArmorItem {
    public HazmatChestItem(Properties props) {
        super(ArmorMaterials.LEATHER, Type.CHESTPLATE, props.stacksTo(1).durability(200));
    }
}
