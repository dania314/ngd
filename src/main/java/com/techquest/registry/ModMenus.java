package com.techquest.registry;

import com.techquest.TechQuest;
import com.techquest.menu.CoalGeneratorMenu;
import com.techquest.menu.ProcessingMenu;
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

    public static final DeferredHolder<MenuType<?>, MenuType<ProcessingMenu>> MACERATOR =
            MENUS.register("macerator",
                    () -> IMenuTypeExtension.create((id, inv, buf) ->
                            new ProcessingMenu(ModMenus.MACERATOR.get(), id, inv, buf)));

    public static final DeferredHolder<MenuType<?>, MenuType<ProcessingMenu>> COMPRESSOR =
            MENUS.register("compressor",
                    () -> IMenuTypeExtension.create((id, inv, buf) ->
                            new ProcessingMenu(ModMenus.COMPRESSOR.get(), id, inv, buf)));

    public static final DeferredHolder<MenuType<?>, MenuType<ProcessingMenu>> ELECTRIC_FURNACE =
            MENUS.register("electric_furnace",
                    () -> IMenuTypeExtension.create((id, inv, buf) ->
                            new ProcessingMenu(ModMenus.ELECTRIC_FURNACE.get(), id, inv, buf)));

    private ModMenus() {}
}
