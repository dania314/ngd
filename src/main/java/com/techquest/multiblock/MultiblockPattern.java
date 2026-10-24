package com.techquest.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * Описание мультиблока: относительные позиции + что там должно быть.
 * origin — блок-контроллер.
 */
public class MultiblockPattern {
    public record Relative(int x, int y, int z, Predicate<BlockState> match, String label) {}

    private final List<Relative> parts = new ArrayList<>();
    private final String name;

    public MultiblockPattern(String name) {
        this.name = name;
    }

    public MultiblockPattern add(int x, int y, int z, Predicate<BlockState> match, String label) {
        parts.add(new Relative(x, y, z, match, label));
        return this;
    }

    public MultiblockPattern add(int x, int y, int z, Block block, String label) {
        return add(x, y, z, s -> s.is(block), label);
    }

    public String getName() {
        return name;
    }

    public List<Relative> getParts() {
        return parts;
    }

    /** Проверить структуру относительно origin с учётом поворота (горизонталь). */
    public boolean matches(Level level, BlockPos origin, Direction facing) {
        for (Relative r : parts) {
            BlockPos p = rotate(origin, r.x, r.y, r.z, facing);
            if (!r.match.test(level.getBlockState(p))) {
                return false;
            }
        }
        return true;
    }

    public List<BlockPos> collectPositions(BlockPos origin, Direction facing) {
        List<BlockPos> list = new ArrayList<>();
        for (Relative r : parts) {
            list.add(rotate(origin, r.x, r.y, r.z, facing));
        }
        return list;
    }

    /** Для квест-книги: шаги сборки по порядку. */
    public List<String> getBuildSteps() {
        List<String> steps = new ArrayList<>();
        int i = 1;
        for (Relative r : parts) {
            steps.add(i++ + ". " + r.label + " @ (" + r.x + ", " + r.y + ", " + r.z + ")");
        }
        return steps;
    }

    private static BlockPos rotate(BlockPos origin, int x, int y, int z, Direction facing) {
        return switch (facing) {
            case SOUTH -> origin.offset(-x, y, -z);
            case WEST -> origin.offset(z, y, -x);
            case EAST -> origin.offset(-z, y, x);
            default -> origin.offset(x, y, z); // NORTH
        };
    }
}
