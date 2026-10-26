package com.techquest.ecology;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Простое загрязнение по блокам (сервер).
 * Ключ — long позиции, значение — уровень радиации 0–100.
 */
public final class PollutionData {
    private static final Map<String, Map<Long, Integer>> RAD = new ConcurrentHashMap<>();

    private static String dim(Level level) {
        return level.dimension().location().toString();
    }

    public static void leakRadiation(Level level, BlockPos pos, int amount) {
        if (level.isClientSide) return;
        Map<Long, Integer> map = RAD.computeIfAbsent(dim(level), k -> new ConcurrentHashMap<>());
        long key = pos.asLong();
        map.merge(key, amount, (a, b) -> Math.min(100, a + b));
        // лёгкий урон рядом
        if (level instanceof ServerLevel sl && level.getGameTime() % 40 == 0) {
            int rad = map.getOrDefault(key, 0);
            if (rad > 10) {
                AABB box = new AABB(pos).inflate(3);
                for (LivingEntity e : sl.getEntitiesOfClass(LivingEntity.class, box)) {
                    e.addEffect(new MobEffectInstance(MobEffects.POISON, 40, rad > 50 ? 1 : 0));
                }
            }
        }
    }

    public static int getRadiation(Level level, BlockPos pos) {
        Map<Long, Integer> map = RAD.get(dim(level));
        if (map == null) return 0;
        return map.getOrDefault(pos.asLong(), 0);
    }

    public static void clean(Level level, BlockPos pos, int amount) {
        Map<Long, Integer> map = RAD.get(dim(level));
        if (map == null) return;
        long key = pos.asLong();
        int v = map.getOrDefault(key, 0) - amount;
        if (v <= 0) map.remove(key);
        else map.put(key, v);
    }

    private PollutionData() {}
}
