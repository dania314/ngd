package com.techquest.client.screen;

import com.techquest.TechQuest;
import com.techquest.menu.AssemblyLineMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class AssemblyLineScreen extends AbstractContainerScreen<AssemblyLineMenu> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(TechQuest.MODID, "textures/gui/processing_machine.png");

    public AssemblyLineScreen(AssemblyLineMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        imageWidth = 176;
        imageHeight = 166;
    }

    @Override
    protected void renderBg(GuiGraphics g, float partial, int mx, int my) {
        g.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        int w = menu.getProgress() * 24 / menu.getMaxProgress();
        g.blit(TEXTURE, leftPos + 84, topPos + 35, 176, 0, w, 17);
        int eH = Math.min(52, menu.getEnergy() * 52 / 80000);
        g.fill(leftPos + 10, topPos + 16 + (52 - eH), leftPos + 16, topPos + 68, 0xFF33CCFF);
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float partial) {
        renderBackground(g, mx, my, partial);
        super.render(g, mx, my, partial);
        renderTooltip(g, mx, my);
    }
}
