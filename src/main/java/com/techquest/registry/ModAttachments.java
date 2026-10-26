package com.techquest.registry;

import com.techquest.TechQuest;
import com.techquest.quest.QuestProgress;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public final class ModAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, TechQuest.MODID);

    public static final Supplier<AttachmentType<QuestProgress>> QUEST_PROGRESS = ATTACHMENTS.register(
            "quest_progress",
            () -> AttachmentType.builder(() -> new QuestProgress())
                    .serialize(QuestProgress.CODEC)
                    .copyOnDeath()
                    .build());

    private ModAttachments() {}
}
