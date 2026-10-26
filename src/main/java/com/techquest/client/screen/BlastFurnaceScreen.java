package com.techquest.client.screen;

import com.techquest.TechQuest;
import com.techquest.menu.BlastFurnaceMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class BlastFurnaceScreen extends AbstractContainerScreen<BlastFurnaceMenu> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(TechQuest.MODID, "textures/gui/processing_machine.png");

    public BlastFurnaceScreen(BlastFurnaceMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        imageWidth = 176;
        imageHeight = 166;
    }

    @Override
    protected void renderBg(GuiGraphics g, float partial, int mx, int my) {
        g.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        int w = menu.getProgress() * 24 / 60;
        g.blit(TEXTURE, leftPos + 100, topPos + 40, 176, 0, w, 17);
        int color = menu.isFormed() ? 0xFF44FF44 : 0xFFFF4444;
        g.fill(leftPos + 160, topPos + 10, leftPos + 168, topPos + 18, color);
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float partial) {
        renderBackground(g, mx, my, partial);
        super.render(g, mx, my, partial);
        renderTooltip(g, mx, my);
        g.drawString(font, menu.isFormed()
                ? Component.translatable("gui.techquest.blast.formed")
                : Component.translatable("gui.techquest.blast.unformed"),
                leftPos + 8, topPos + 6, 0xFFFFFFFF, false);
        g.drawString(font, Component.literal(menu.getEnergy() + " FE"), leftPos + 100, topPos + 60, 0xFF88DDFF, false);
    }
}
