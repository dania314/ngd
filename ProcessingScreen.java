package com.techquest.client.screen;

import com.techquest.TechQuest;
import com.techquest.block.entity.ProcessingBlockEntity;
import com.techquest.menu.ProcessingMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/** Экран для дробилки, пресса и электропечи. */
public class ProcessingScreen extends AbstractContainerScreen<ProcessingMenu> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(TechQuest.MODID, "textures/gui/processing_machine.png");

    private static final int BAR_X = 152;
    private static final int BAR_Y = 16;
    private static final int BAR_W = 12;
    private static final int BAR_H = 54;

    private static final int PROGRESS_X = 80;
    private static final int PROGRESS_Y = 40;
    private static final int PROGRESS_W = 24;
    private static final int PROGRESS_H = 10;

    public ProcessingScreen(ProcessingMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        g.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);

        int filled = (int) ((long) BAR_H * menu.getEnergy() / ProcessingBlockEntity.CAPACITY);
        if (filled > 0) {
            int x = leftPos + BAR_X;
            int yBottom = topPos + BAR_Y + BAR_H;
            g.fill(x, yBottom - filled, x + BAR_W, yBottom, 0xFFD62828);
        }

        int max = menu.getMaxProgress();
        if (menu.getProgress() > 0 && max > 0) {
            int w = PROGRESS_W * menu.getProgress() / max;
            int x = leftPos + PROGRESS_X;
            int y = topPos + PROGRESS_Y;
            g.fill(x, y, x + w, y + PROGRESS_H, 0xFF6FE06F);
        }
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        renderTooltip(g, mouseX, mouseY);

        if (isHovering(BAR_X, BAR_Y, BAR_W, BAR_H, mouseX, mouseY)) {
            g.renderTooltip(font,
                    Component.translatable("gui.techquest.energy", menu.getEnergy(),
                            ProcessingBlockEntity.CAPACITY),
                    mouseX, mouseY);
        }
    }
}
