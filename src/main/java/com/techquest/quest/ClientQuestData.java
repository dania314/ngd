package com.techquest.quest;

/** Клиентская копия прогресса (приходит пакетом QuestSyncPayload). */
public final class ClientQuestData {
    private static QuestProgress progress = new QuestProgress();

    public static QuestProgress get() {
        return progress;
    }

    public static void set(QuestProgress value) {
        progress = value;
    }

    private ClientQuestData() {}
}
