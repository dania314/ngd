package com.techquest.client.screen;

import com.techquest.client.ClientSetup;
import com.techquest.network.ClaimQuestPayload;
import com.techquest.quest.ClientQuestData;
import com.techquest.quest.Quest;
import com.techquest.quest.QuestManager;
import com.techquest.quest.QuestProgress;
import com.techquest.quest.QuestRegistries;
import com.techquest.quest.QuestTask;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Квестбук: дерево квестов слева (перетаскивается мышью), описание выбранного квеста справа. */
public class QuestBookScreen extends Screen {
    private static final int NODE = 26;
    private static final int SPACING = 42;
    private static final int DETAIL_W = 200;
    private static final int MARGIN = 14;

    private enum State { LOCKED, ACTIVE, READY, DONE }

    private record QuestNode(ResourceLocation id, Quest quest) {}

    private final List<QuestNode> nodes = new ArrayList<>();
    private int panX;
    private int panY;
    private boolean dragging;
    private ResourceLocation selected;
    private Button claimButton;

    public QuestBookScreen() {
        super(Component.translatable("gui.techquest.quests.title"));
    }

    private int left() { return MARGIN; }
    private int top() { return MARGIN; }
    private int right() { return width - MARGIN; }
    private int bottom() { return height - MARGIN; }
    private int graphRight() { return right() - DETAIL_W; }

    @Override
    protected void init() {
        nodes.clear();
        if (minecraft != null && minecraft.level != null) {
            Registry<Quest> registry = minecraft.level.registryAccess().registryOrThrow(QuestRegistries.QUEST_KEY);
            for (Map.Entry<ResourceKey<Quest>, Quest> entry : registry.entrySet()) {
                nodes.add(new QuestNode(entry.getKey().location(), entry.getValue()));
            }
        }
        if (panX == 0 && panY == 0) {
            panX = left() + 30;
            panY = (top() + bottom()) / 2 - SPACING - NODE / 2;
        }
        claimButton = addRenderableWidget(Button.builder(Component.translatable("gui.techquest.quest.claim"),
                        button -> claimSelected())
                .bounds(graphRight() + 8, bottom() - 28, DETAIL_W - 16, 20)
                .build());
        claimButton.visible = false;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        if (claimButton != null) {
            QuestNode node = findNode(selected);
            claimButton.visible = node != null && stateOf(node, ClientQuestData.get()) == State.READY;
        }
    }

    private void claimSelected() {
        if (selected != null) {
            PacketDistributor.sendToServer(new ClaimQuestPayload(selected));
        }
    }

    private QuestNode findNode(ResourceLocation id) {
        if (id == null) {
            return null;
        }
        for (QuestNode node : nodes) {
            if (node.id().equals(id)) {
                return node;
            }
        }
        return null;
    }

    private State stateOf(QuestNode node, QuestProgress progress) {
        if (progress.isClaimed(node.id())) {
            return State.DONE;
        }
        if (!QuestManager.isUnlocked(progress, node.quest())) {
            return State.LOCKED;
        }
        return progress.isCompleted(node.id()) ? State.READY : State.ACTIVE;
    }

    private int nodeX(QuestNode node) {
        return panX + node.quest().x() * SPACING;
    }

    private int nodeY(QuestNode node) {
        return panY + node.quest().y() * SPACING;
    }

    private boolean inGraph(double mx, double my) {
        return mx >= left() + 2 && mx <= graphRight() - 2 && my >= top() + 2 && my <= bottom() - 2;
    }

    // ------------------------------------------------------------------ ввод

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (super.mouseClicked(mx, my, button)) {
            return true;
        }
        if (button == 0 && inGraph(mx, my)) {
            dragging = true;
            for (QuestNode node : nodes) {
                int x = nodeX(node);
                int y = nodeY(node);
                if (mx >= x && mx <= x + NODE && my >= y && my <= y + NODE) {
                    selected = node.id();
                    break;
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        if (dragging && button == 0) {
            panX += (int) Math.round(dx);
            panY += (int) Math.round(dy);
            return true;
        }
        return super.mouseDragged(mx, my, button, dx, dy);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        dragging = false;
        return super.mouseReleased(mx, my, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (ClientSetup.OPEN_QUESTS.matches(keyCode, scanCode)) {
            onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    // ------------------------------------------------------------------ отрисовка

    /** Screen.render сам вызывает renderBackground, поэтому весь фон и дерево рисуем здесь. */
    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        QuestProgress progress = ClientQuestData.get();

        g.fill(0, 0, width, height, 0x99000000);
        g.fill(left() - 2, top() - 2, right() + 2, bottom() + 2, 0xFF3A3A48);
        g.fill(left(), top(), right(), bottom(), 0xFF14141C);
        g.fill(graphRight(), top(), graphRight() + 1, bottom(), 0xFF3A3A48);

        g.enableScissor(left() + 2, top() + 2, graphRight() - 2, bottom() - 2);
        drawLines(g, progress);
        for (QuestNode node : nodes) {
            drawNode(g, node, stateOf(node, progress));
        }
        g.disableScissor();

        drawDetails(g, progress);
    }

    private void drawLines(GuiGraphics g, QuestProgress progress) {
        for (QuestNode node : nodes) {
            for (ResourceLocation depId : node.quest().dependencies()) {
                QuestNode dep = findNode(depId);
                if (dep == null) {
                    continue;
                }
                int color = progress.isClaimed(depId) ? 0xFF55AA55 : 0xFF555566;
                int x1 = nodeX(dep) + NODE / 2;
                int y1 = nodeY(dep) + NODE / 2;
                int x2 = nodeX(node) + NODE / 2;
                int y2 = nodeY(node) + NODE / 2;
                int midX = (x1 + x2) / 2;
                g.fill(Math.min(x1, midX), y1 - 1, Math.max(x1, midX) + 1, y1 + 1, color);
                g.fill(midX - 1, Math.min(y1, y2), midX + 1, Math.max(y1, y2) + 1, color);
                g.fill(Math.min(midX, x2), y2 - 1, Math.max(midX, x2) + 1, y2 + 1, color);
            }
        }
    }

    private void drawNode(GuiGraphics g, QuestNode node, State state) {
        int x = nodeX(node);
        int y = nodeY(node);
        int frame = switch (state) {
            case LOCKED -> 0xFF4A4A55;
            case ACTIVE -> 0xFFE0B040;
            case READY -> 0xFF66FF66;
            case DONE -> 0xFF3C8C3C;
        };
        if (node.id().equals(selected)) {
            g.fill(x - 4, y - 4, x + NODE + 4, y + NODE + 4, 0xFFFFFFFF);
        }
        g.fill(x - 2, y - 2, x + NODE + 2, y + NODE + 2, frame);
        g.fill(x, y, x + NODE, y + NODE, 0xFF1C1C26);
        g.renderItem(new ItemStack(node.quest().icon()), x + 5, y + 5);
        if (state == State.LOCKED) {
            g.fill(x, y, x + NODE, y + NODE, 0xBB000000);
        } else if (state == State.READY) {
            g.drawString(font, "!", x + NODE - 6, y - 1, 0xFFFFFF55, true);
        } else if (state == State.DONE) {
            g.fill(x, y, x + NODE, y + NODE, 0x44228822);
        }
    }

    private void drawDetails(GuiGraphics g, QuestProgress progress) {
        int dx = graphRight() + 8;
        int dw = DETAIL_W - 16;
        int dy = top() + 8;

        QuestNode node = findNode(selected);
        if (node == null) {
            g.drawWordWrap(font, Component.translatable("gui.techquest.quest.select"), dx, dy, dw, 0xFF9999AA);
            return;
        }
        Quest quest = node.quest();
        State state = stateOf(node, progress);

        Component title = quest.title().copy().withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD);
        g.drawWordWrap(font, title, dx, dy, dw, 0xFFFFFFFF);
        dy += font.wordWrapHeight(title, dw) + 6;

        g.drawWordWrap(font, quest.description(), dx, dy, dw, 0xFFCCCCCC);
        dy += font.wordWrapHeight(quest.description(), dw) + 8;

        // Задание
        QuestTask task = quest.task();
        if (task.type() != QuestTask.Type.CHECKMARK) {
            int have = Math.min(progress.isCompleted(node.id()) ? task.count() : progress.getCounter(node.id()),
                    task.count());
            Component line = Component.translatable("quest.techquest.task." + task.type().getSerializedName(),
                    task.displayName(), have, task.count());
            g.drawWordWrap(font, line, dx, dy, dw, 0xFFFFDD77);
            dy += font.wordWrapHeight(line, dw) + 8;
        }

        // Награды
        if (!quest.rewards().isEmpty()) {
            g.drawString(font, Component.translatable("gui.techquest.quest.rewards"), dx, dy, 0xFFAAAAFF, false);
            dy += 12;
            int rx = dx;
            for (ItemStack reward : quest.rewards()) {
                g.renderItem(reward, rx, dy);
                g.renderItemDecorations(font, reward, rx, dy);
                rx += 20;
                if (rx + 18 > dx + dw) {
                    rx = dx;
                    dy += 20;
                }
            }
            dy += 24;
        }

        // Статус
        Component status = switch (state) {
            case LOCKED -> Component.translatable("gui.techquest.quest.locked").withStyle(ChatFormatting.RED);
            case ACTIVE -> Component.translatable("gui.techquest.quest.active").withStyle(ChatFormatting.YELLOW);
            case READY -> Component.translatable("gui.techquest.quest.ready").withStyle(ChatFormatting.GREEN);
            case DONE -> Component.translatable("gui.techquest.quest.done").withStyle(ChatFormatting.DARK_GREEN);
        };
        g.drawWordWrap(font, status, dx, Math.min(dy, bottom() - 52), dw, 0xFFFFFFFF);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);

        if (inGraph(mouseX, mouseY)) {
            for (QuestNode node : nodes) {
                int x = nodeX(node);
                int y = nodeY(node);
                if (mouseX >= x && mouseX <= x + NODE && mouseY >= y && mouseY <= y + NODE) {
                    g.renderTooltip(font, node.quest().title(), mouseX, mouseY);
                    return;
                }
            }
        }
    }
}
