package com.techquest.conveyor;

import com.techquest.ecology.HazardItems;
import com.techquest.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class ConveyorBlockEntity extends BlockEntity {
    private final ConveyorType type;
    private int fireTicks;

    public ConveyorBlockEntity(BlockPos pos, BlockState state) {
        this(pos, state, ConveyorType.NORMAL);
    }

    public ConveyorBlockEntity(BlockPos pos, BlockState state, ConveyorType type) {
        super(type == ConveyorType.SEALED ? ModBlockEntities.CONVEYOR_SEALED.get() : ModBlockEntities.CONVEYOR.get(), pos, state);
        this.type = type;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ConveyorBlockEntity be) {
        Direction dir = state.getValue(ConveyorBlock.FACING);
        float speed = be.type.speed();
        if (state.getValue(ConveyorBlock.POWERED)) speed *= 2.0f;

        // Угол: если впереди другая лента — предметы «переедут» на её направление
        BlockPos ahead = pos.relative(dir);
        BlockState aheadState = level.getBlockState(ahead);
        Direction moveDir = dir;
        if (aheadState.getBlock() instanceof ConveyorBlock && aheadState.hasProperty(ConveyorBlock.FACING)) {
            // на стыке слегка тянем к центру следующей ленты
            moveDir = dir;
        }

        AABB box = new AABB(pos).inflate(0.02, 0.35, 0.02).move(0, 0.05, 0);
        List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, box);
        boolean flammableOnBelt = false;
        boolean radioactiveOnBelt = false;

        for (ItemEntity ie : items) {
            ItemStack stack = ie.getItem();
            if (HazardItems.isFlammable(stack)) flammableOnBelt = true;
            if (HazardItems.isRadioactive(stack)) radioactiveOnBelt = true;

            // Центрирование на ленте + движение
            Vec3 center = Vec3.atBottomCenterOf(pos).add(0, 0.2, 0);
            Vec3 posE = ie.position();
            double cx = (center.x - posE.x) * 0.15;
            double cz = (center.z - posE.z) * 0.15;

            ie.setDeltaMovement(
                    moveDir.getStepX() * speed + cx,
                    0.02,
                    moveDir.getStepZ() * speed + cz);
            ie.hurtMarked = true;
            ie.setPickUpDelay(10);
        }

        // Пожар
        if (be.type.canIgnite() && flammableOnBelt) {
            boolean nearHeat = false;
            for (Direction d : Direction.values()) {
                BlockState n = level.getBlockState(pos.relative(d));
                if (n.is(Blocks.FIRE) || n.is(Blocks.LAVA) || n.is(Blocks.MAGMA_BLOCK)) {
                    nearHeat = true;
                    break;
                }
            }
            if (nearHeat || state.getValue(ConveyorBlock.BURNING)) {
                be.fireTicks++;
                if (!state.getValue(ConveyorBlock.BURNING)) {
                    level.setBlock(pos, state.setValue(ConveyorBlock.BURNING, true), 3);
                }
                if (be.fireTicks % 20 == 0) {
                    for (ItemEntity ie : items) {
                        if (HazardItems.isFlammable(ie.getItem())) ie.discard();
                    }
                    BlockPos next = pos.relative(dir);
                    if (level.getBlockState(next).getBlock() instanceof ConveyorBlock) {
                        BlockState ns = level.getBlockState(next);
                        if (ns.hasProperty(ConveyorBlock.BURNING) && !ns.getValue(ConveyorBlock.BURNING)
                                && ((ConveyorBlock) ns.getBlock()).conveyorType().canIgnite()) {
                            level.setBlock(next, ns.setValue(ConveyorBlock.BURNING, true), 3);
                        }
                    }
                }
            }
        } else if (state.getValue(ConveyorBlock.BURNING) && be.fireTicks > 100) {
            level.setBlock(pos, state.setValue(ConveyorBlock.BURNING, false), 3);
            be.fireTicks = 0;
        }

        if (be.type.leaksRadiation() && radioactiveOnBelt) {
            com.techquest.ecology.PollutionData.leakRadiation(level, pos, 2);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("FireTicks", fireTicks);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        fireTicks = tag.getInt("FireTicks");
    }
}
