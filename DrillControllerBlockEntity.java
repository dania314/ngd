package com.techquest.block.entity;

import com.techquest.block.DrillControllerBlock;
import com.techquest.energy.EnergyUtil;
import com.techquest.energy.MachineEnergyStorage;
import com.techquest.multiblock.ModMultiblocks;
import com.techquest.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.IEnergyStorage;

import java.util.ArrayList;
import java.util.List;

/**
 * Бур: когда assembled+active, каждые N тиков копает блок под головкой (вниз),
 * тратит энергию, дропает лут рядом с контроллером.
 */
public class DrillControllerBlockEntity extends BlockEntity {
    public static final int CAPACITY = 50_000;
    public static final int MAX_RECEIVE = 200;
    public static final int ENERGY_PER_BLOCK = 500;
    public static final int DIG_INTERVAL = 40; // ticks

    private final MachineEnergyStorage energy = new MachineEnergyStorage(CAPACITY, MAX_RECEIVE, 0);
    private boolean assembled;
    private int digCooldown;
    private int digDepth; // how many blocks already dug below head
    private final List<BlockPos> structurePositions = new ArrayList<>();

    public DrillControllerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.DRILL_CONTROLLER.get(), pos, state);
    }

    public IEnergyStorage getEnergyStorage() {
        return energy;
    }

    public void assemble(Direction facing) {
        assembled = true;
        structurePositions.clear();
        structurePositions.addAll(ModMultiblocks.DRILL().collectPositions(worldPosition, Direction.NORTH));
        digDepth = 0;
        digCooldown = 0;
        setChanged();
    }

    public void disassemble() {
        assembled = false;
        structurePositions.clear();
        digDepth = 0;
        setChanged();
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, DrillControllerBlockEntity be) {
        // Accept energy from neighbors
        // (generators/cables push into us via capability)

        if (!state.getValue(DrillControllerBlock.FORMED) || !state.getValue(DrillControllerBlock.ACTIVE)) {
            return;
        }

        // Validate structure still intact
        Direction facing = state.getValue(DrillControllerBlock.FACING);
        if (!ModMultiblocks.matchesDrill(level, pos)) {
            be.disassemble();
            level.setBlock(pos, state.setValue(DrillControllerBlock.FORMED, false)
                    .setValue(DrillControllerBlock.ACTIVE, false), Block.UPDATE_ALL);
            return;
        }

        if (be.digCooldown > 0) {
            be.digCooldown--;
            return;
        }

        if (be.energy.getEnergyStored() < ENERGY_PER_BLOCK) {
            return;
        }

        // Head is at controller.below()
        BlockPos headPos = pos.below();
        BlockPos digPos = headPos.below(be.digDepth + 1);

        if (digPos.getY() < level.getMinBuildHeight() + 1) {
            return; // bottom of world
        }

        BlockState target = level.getBlockState(digPos);
        if (target.isAir() || target.getDestroySpeed(level, digPos) < 0) {
            // unbreakable or air — skip deeper
            be.digDepth++;
            be.digCooldown = 5;
            return;
        }

        // Don't dig our own structure
        if (be.structurePositions.contains(digPos)) {
            be.digDepth++;
            return;
        }

        be.energy.removeInternal(ENERGY_PER_BLOCK);

        if (level instanceof ServerLevel server) {
            List<ItemStack> drops = Block.getDrops(target, server, digPos, level.getBlockEntity(digPos));
            level.setBlock(digPos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            // Drop items near controller
            for (ItemStack drop : drops) {
                Block.popResource(level, pos.above(), drop);
            }
        }

        be.digDepth++;
        be.digCooldown = DIG_INTERVAL;
        be.setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("Energy", energy.getEnergyStored());
        tag.putBoolean("Assembled", assembled);
        tag.putInt("DigDepth", digDepth);
        tag.putInt("DigCooldown", digCooldown);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        energy.setEnergy(tag.getInt("Energy"));
        assembled = tag.getBoolean("Assembled");
        digDepth = tag.getInt("DigDepth");
        digCooldown = tag.getInt("DigCooldown");
    }
}
