package com.techquest.multiblock;

import com.techquest.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Упрощённые схемы: бур проверяется без поворота (симметричный),
 * сплавная печь — только вертикаль.
 */
public final class ModMultiblocks {

    private static MultiblockPattern alloySmelter;
    private static MultiblockPattern drill;

    public static MultiblockPattern ALLOY_SMELTER() {
        if (alloySmelter == null) {
            alloySmelter = new MultiblockPattern("alloy_smelter")
                    .add(0, -1, 0, s -> s.is(ModBlocks.MACHINE_CASING.get()), "Корпус снизу")
                    .add(0, 0, 0, s -> true, "Сплавная печь")
                    .add(0, 1, 0, s -> s.is(ModBlocks.MACHINE_CASING.get()), "Корпус сверху");
        }
        return alloySmelter;
    }

    public static MultiblockPattern DRILL() {
        if (drill == null) {
            // Симметричный паттерн: 4 рамы по сторонам, 1 сверху, головка снизу
            drill = new MultiblockPattern("drill")
                    .add(0, 0, 0, s -> true, "Контроллер")
                    .add(-1, 0, 0, frame(), "Рама Запад")
                    .add(1, 0, 0, frame(), "Рама Восток")
                    .add(0, 0, -1, frame(), "Рама Север")
                    .add(0, 0, 1, frame(), "Рама Юг")
                    .add(0, 1, 0, frame(), "Рама сверху")
                    .add(0, -1, 0, s -> s.is(ModBlocks.DRILL_HEAD.get()), "Головка снизу");
        }
        return drill;
    }

    private static java.util.function.Predicate<BlockState> frame() {
        return s -> s.is(ModBlocks.DRILL_FRAME.get());
    }

    /** Бур: не крутим по facing — структура крестом, одинакова с любой стороны. */
    public static boolean matchesDrill(Level level, BlockPos origin) {
        return DRILL().matches(level, origin, Direction.NORTH);
    }

    public static boolean matchesAlloy(Level level, BlockPos origin, Direction facing) {
        return ALLOY_SMELTER().matches(level, origin, Direction.NORTH); // только Y, facing не важен
    }

    private ModMultiblocks() {}
}
