package com.techquest.client;

import com.techquest.client.screen.QuestBookScreen;
import net.minecraft.client.Minecraft;

/** Отдельный клиентский класс, чтобы общий код не тянул клиентские классы на сервер. */
public final class QuestBookClient {

    public static void open() {
        Minecraft.getInstance().setScreen(new QuestBookScreen());
    }

    private QuestBookClient() {}
}
