package com.techquest.ecology;

import com.techquest.registry.ModItems;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Set;

public final class HazardItems {
    private static final Set<Item> FLAMMABLE = Set.of(
            Items.COAL, Items.CHARCOAL, Items.GUNPOWDER, Items.BLAZE_POWDER,
            Items.FIRE_CHARGE, Items.TNT
    );
    private static final Set<Item> RADIOACTIVE = Set.of(); // filled after registry

    public static boolean isFlammable(ItemStack stack) {
        if (stack.isEmpty()) return false;
        Item i = stack.getItem();
        if (FLAMMABLE.contains(i)) return true;
        String path = i.toString().toLowerCase();
        return path.contains("fuel") || path.contains("gasoline") || path.contains("naphtha")
                || path.contains("mazut") || path.contains("oil") || path.contains("rubber")
                || path.contains("plastic") && path.contains("scrap");
    }

    public static boolean isRadioactive(ItemStack stack) {
        if (stack.isEmpty()) return false;
        String p = stack.getItem().toString().toLowerCase();
        return p.contains("uranium") || p.contains("plutonium") || p.contains("radioactive")
                || p.contains("spent_fuel") || p.contains("rtg");
    }

    private HazardItems() {}
}
