package com.techquest.quest;

import com.techquest.TechQuest;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;

public final class QuestRegistries {
    /** Квесты — это датапак-реестр: data/<namespace>/techquest/quest/<name>.json */
    public static final ResourceKey<Registry<Quest>> QUEST_KEY =
            ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(TechQuest.MODID, "quest"));

    public static void registerDatapackRegistries(DataPackRegistryEvent.NewRegistry event) {
        // networkCodec != null → квесты автоматически синхронизируются с клиентом
        event.dataPackRegistry(QUEST_KEY, Quest.CODEC, Quest.CODEC);
    }

    private QuestRegistries() {}
}
