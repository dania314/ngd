package com.techquest.quarry;

import com.techquest.energy.MachineEnergyStorage;
import com.techquest.registry.ModBlockEntities;
import com.techquest.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.items.ItemStackHandler;

/**
 * Лазерный бур: сверлит столб 1×1 вниз.
 * Может пробивать deepslate быстро; bedrock не ломает, но на глубине Y<=0
 * иногда выдаёт «глубинный» лут (уран, алмазы).
 */
public class LaserDrillBlockEntity extends BlockEntity {
    public static final int CAPACITY = 200_000;
    public static final int ENERGY_PER = 500;
    public static final int INTERVAL = 8;

    private final MachineEnergyStorage energy = new MachineEnergyStorage(CAPACITY, 1000, 0);
    private final ItemStackHandler buffer = new ItemStackHandler(9) {
        @Override protected void onContentsChanged(int slot) { setChanged(); }
    };
    private boolean active;
    private int digY;
    private boolean inited;
    private int cooldown;

    public LaserDrillBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.LASER_DRILL.get(), pos, state);
    }

    public IEnergyStorage getEnergyStorage() { return energy; }
    public ItemStackHandler getItems() { return buffer; }
    public ItemStackHandler getAutomationHandler() { return buffer; }

    public void toggle(Player player) {
        active = !active;
        if (!inited) {
            digY = worldPosition.getY() - 1;
            inited = true;
        }
        player.displayClientMessage(Component.translatable(
                active ? "message.techquest.laser.on" : "message.techquest.laser.off", digY), true);
        setChanged();
    }

    private boolean insert(ItemStack stack) {
        for (int i = 0; i < 9; i++) {
            stack = buffer.insertItem(i, stack, false);
            if (stack.isEmpty()) return true;
        }
        return false;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, LaserDrillBlockEntity be) {
        if (!be.active || !(level instanceof ServerLevel server)) return;
        if (be.cooldown > 0) { be.cooldown--; return; }
        if (be.energy.getEnergyStored() < ENERGY_PER) return;

        int minY = level.getMinBuildHeight() + 1;
        if (be.digY < minY) {
            be.active = false;
            return;
        }

        BlockPos dig = new BlockPos(pos.getX(), be.digY, pos.getZ());
        BlockState target = level.getBlockState(dig);

        be.energy.removeInternal(ENERGY_PER);

        if (target.is(Blocks.BEDROCK)) {
            // deep loot pulse
            if (level.random.nextInt(20) == 0) {
                ItemStack loot = level.random.nextBoolean()
                        ? new ItemStack(ModItems.URANIUM_INGOT.get())
                        : new ItemStack(Items.DIAMOND);
                if (!be.insert(loot)) Block.popResource(level, pos.above(), loot);
            }
            be.digY--;
        } else if (!target.isAir() && target.getDestroySpeed(level, dig) >= 0) {
            var drops = Block.getDrops(target, server, dig, level.getBlockEntity(dig));
            level.setBlock(dig, Blocks.AIR.defaultBlockState(), 3);
            for (ItemStack d : drops) {
                if (!be.insert(d.copy())) Block.popResource(level, pos.above(), d);
            }
            be.digY--;
        } else {
            be.digY--;
        }

        server.sendParticles(ParticleTypes.END_ROD,
                pos.getX() + 0.5, be.digY + 1, pos.getZ() + 0.5,
                3, 0.05, 0.5, 0.05, 0.01);

        be.cooldown = INTERVAL;
        be.setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider r) {
        super.saveAdditional(tag, r);
        tag.putInt("Energy", energy.getEnergyStored());
        tag.putBoolean("Active", active);
        tag.putInt("DigY", digY);
        tag.putBoolean("Inited", inited);
        tag.put("Buf", buffer.serializeNBT(r));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider r) {
        super.loadAdditional(tag, r);
        energy.setEnergy(tag.getInt("Energy"));
        active = tag.getBoolean("Active");
        digY = tag.getInt("DigY");
        inited = tag.getBoolean("Inited");
        if (tag.contains("Buf")) buffer.deserializeNBT(r, tag.getCompound("Buf"));
    }
}
