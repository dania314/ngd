package com.techquest.registry;

import com.techquest.TechQuest;
import com.techquest.menu.CoalGeneratorMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, TechQuest.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<CoalGeneratorMenu>> COAL_GENERATOR =
            MENUS.register("coal_generator",
                    () -> IMenuTypeExtension.create((id, inv, buf) -> new CoalGeneratorMenu(id, inv, buf)));

    private ModMenus() {}
}
