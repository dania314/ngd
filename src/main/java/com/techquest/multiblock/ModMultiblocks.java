package com.techquest.multiblock;

import com.techquest.registry.ModBlocks;
import net.minecraft.world.level.block.state.BlockState;

/** Готовые схемы мультиблоков. Паттерны создаются лениво после регистрации блоков. */
public final class ModMultiblocks {

    private static MultiblockPattern alloySmelter;
    private static MultiblockPattern drill;

    public static MultiblockPattern ALLOY_SMELTER() {
        if (alloySmelter == null) {
            alloySmelter = new MultiblockPattern("alloy_smelter")
                    .add(0, -1, 0, s -> s.is(ModBlocks.MACHINE_CASING.get()), "Machine Casing (base)")
                    .add(0, 0, 0, s -> true, "Alloy Smelter Controller")
                    .add(0, 1, 0, s -> s.is(ModBlocks.MACHINE_CASING.get()), "Machine Casing (top)");
        }
        return alloySmelter;
    }

    public static MultiblockPattern DRILL() {
        if (drill == null) {
            drill = new MultiblockPattern("drill")
                    .add(0, 0, 0, s -> true, "Drill Controller")
                    .add(-1, 0, 0, frame(), "Drill Frame")
                    .add(1, 0, 0, frame(), "Drill Frame")
                    .add(0, 0, -1, frame(), "Drill Frame")
                    .add(0, 0, 1, frame(), "Drill Frame")
                    .add(-1, 1, 0, frame(), "Drill Frame")
                    .add(1, 1, 0, frame(), "Drill Frame")
                    .add(0, 1, 0, frame(), "Drill Frame")
                    .add(0, -1, 0, s -> s.is(ModBlocks.DRILL_HEAD.get()), "Drill Head");
        }
        return drill;
    }

    private static java.util.function.Predicate<BlockState> frame() {
        return s -> s.is(ModBlocks.DRILL_FRAME.get());
    }

    private ModMultiblocks() {}
}
