package com.techquest.quest;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Прогресс игрока по квестам. Хранится как data attachment на игроке. */
public class QuestProgress {
    public static final Codec<QuestProgress> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.unboundedMap(ResourceLocation.CODEC, Codec.INT).fieldOf("counters").forGetter(p -> p.counters),
            ResourceLocation.CODEC.listOf().fieldOf("completed").forGetter(p -> List.copyOf(p.completed)),
            ResourceLocation.CODEC.listOf().fieldOf("claimed").forGetter(p -> List.copyOf(p.claimed)),
            Codec.BOOL.optionalFieldOf("book_given", false).forGetter(p -> p.bookGiven)
    ).apply(inst, QuestProgress::new));

    private final Map<ResourceLocation, Integer> counters = new HashMap<>();
    private final Set<ResourceLocation> completed = new HashSet<>();
    private final Set<ResourceLocation> claimed = new HashSet<>();
    private boolean bookGiven;

    public QuestProgress() {}

    public QuestProgress(Map<ResourceLocation, Integer> counters, List<ResourceLocation> completed,
                         List<ResourceLocation> claimed, boolean bookGiven) {
        this.counters.putAll(counters);
        this.completed.addAll(completed);
        this.claimed.addAll(claimed);
        this.bookGiven = bookGiven;
    }

    public QuestProgress copy() {
        return new QuestProgress(counters, List.copyOf(completed), List.copyOf(claimed), bookGiven);
    }

    public int getCounter(ResourceLocation id) {
        return counters.getOrDefault(id, 0);
    }

    public void setCounter(ResourceLocation id, int value) {
        counters.put(id, value);
    }

    public int addCounter(ResourceLocation id, int amount) {
        int value = getCounter(id) + amount;
        counters.put(id, value);
        return value;
    }

    public boolean isCompleted(ResourceLocation id) {
        return completed.contains(id);
    }

    public boolean isClaimed(ResourceLocation id) {
        return claimed.contains(id);
    }

    public void complete(ResourceLocation id) {
        completed.add(id);
    }

    public void claim(ResourceLocation id) {
        completed.add(id);
        claimed.add(id);
    }

    public boolean isBookGiven() {
        return bookGiven;
    }

    public void setBookGiven(boolean bookGiven) {
        this.bookGiven = bookGiven;
    }
}
