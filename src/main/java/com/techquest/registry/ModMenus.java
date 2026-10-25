package com.techquest.registry;

import com.techquest.TechQuest;
import com.techquest.menu.CoalGeneratorMenu;
import com.techquest.menu.ProcessingMenu;
import com.techquest.menu.ChemMachineMenu;
import com.techquest.menu.QuarryMenu;
import com.techquest.menu.BoilerMenu;
import com.techquest.menu.NuclearReactorMenu;
import com.techquest.menu.BlastFurnaceMenu;
import com.techquest.menu.AssemblyLineMenu;
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

    public static final DeferredHolder<MenuType<?>, MenuType<AssemblyLineMenu>> ASSEMBLY_LINE =
            MENUS.register("assembly_line",
                    () -> IMenuTypeExtension.create(AssemblyLineMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<BlastFurnaceMenu>> BLAST_FURNACE =
            MENUS.register("blast_furnace",
                    () -> IMenuTypeExtension.create(BlastFurnaceMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<NuclearReactorMenu>> NUCLEAR_REACTOR =
            MENUS.register("nuclear_reactor",
                    () -> IMenuTypeExtension.create(NuclearReactorMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<BoilerMenu>> BOILER =
            MENUS.register("boiler",
                    () -> IMenuTypeExtension.create(BoilerMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<QuarryMenu>> QUARRY =
            MENUS.register("quarry",
                    () -> IMenuTypeExtension.create(QuarryMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<ChemMachineMenu>> REFINERY =
            MENUS.register("refinery",
                    () -> IMenuTypeExtension.create((id, inv, buf) ->
                            new ChemMachineMenu(ModMenus.REFINERY.get(), id, inv, buf, 4)));

    public static final DeferredHolder<MenuType<?>, MenuType<ChemMachineMenu>> CHEMICAL_REACTOR =
            MENUS.register("chemical_reactor",
                    () -> IMenuTypeExtension.create((id, inv, buf) ->
                            new ChemMachineMenu(ModMenus.CHEMICAL_REACTOR.get(), id, inv, buf, 3)));

    public static final DeferredHolder<MenuType<?>, MenuType<ProcessingMenu>> ELECTRIC_FURNACE =
            MENUS.register("electric_furnace",
                    () -> IMenuTypeExtension.create((id, inv, buf) ->
                            new ProcessingMenu(ModMenus.ELECTRIC_FURNACE.get(), id, inv, buf)));

    private ModMenus() {}
}
