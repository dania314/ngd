package com.techquest.armor;

import com.techquest.TechQuest;
import com.techquest.ecology.PollutionData;
import com.techquest.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = TechQuest.MODID)
public final class ExoEvents {

    /** ПКМ модулем по экзоскелету в руке — установить. */
    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();
        ItemStack held = event.getItemStack();
        ItemStack off = player.getOffhandItem();

        ItemStack exo = ItemStack.EMPTY;
        ItemStack mod = ItemStack.EMPTY;
        if (held.getItem() instanceof ExoChestItem) {
            exo = held;
            mod = off;
        } else if (off.getItem() instanceof ExoChestItem) {
            exo = off;
            mod = held;
        } else {
            return;
        }

        String slot = moduleSlot(mod);
        if (slot == null) return;

        ExoChestItem.setMod(exo, slot, mod.getItem().toString());
        if (!player.getAbilities().instabuild) {
            mod.shrink(1);
        }
        player.displayClientMessage(Component.translatable("message.techquest.exo.installed", slot), true);
        event.setCanceled(true);
    }

    private static String moduleSlot(ItemStack stack) {
        if (stack.isEmpty()) return null;
        if (stack.is(ModItems.MODULE_SERVO.get())) return "servo";
        if (stack.is(ModItems.MODULE_BATTERY.get())) return "battery";
        if (stack.is(ModItems.MODULE_FILTER.get())) return "filter";
        if (stack.is(ModItems.MODULE_PLATING.get())) return "plating";
        return null;
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide) return;
        if (player.tickCount % 20 != 0) return;

        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        if (!(chest.getItem() instanceof ExoChestItem)) return;

        CompoundTag mods = ExoChestItem.getMods(chest);

        // servo → speed + jump
        if (mods.contains("servo")) {
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 40, 0, true, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.JUMP, 40, 0, true, false, true));
        }
        // plating → resistance
        if (mods.contains("plating")) {
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 40, 0, true, false, true));
        }
        // filter → clear poison / reduce radiation effect + clean local rad slightly while wearing
        if (mods.contains("filter")) {
            player.removeEffect(MobEffects.POISON);
            player.removeEffect(MobEffects.WITHER);
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 40, 0, true, false, false));
            BlockPos pos = player.blockPosition();
            PollutionData.clean(player.level(), pos, 1);
        }
        // battery → saturation-like (slow hunger) via absorption nibble
        if (mods.contains("battery")) {
            if (player.getArmorValue() >= 0 && player.tickCount % 100 == 0) {
                player.getFoodData().eat(1, 0.1f);
            }
        }

        // hazmat full check optional on separate armor - filter module acts as partial hazmat
    }

    private ExoEvents() {}
}
