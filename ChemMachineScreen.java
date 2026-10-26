package com.techquest.client.screen;

import com.techquest.TechQuest;
import com.techquest.menu.ChemMachineMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class ChemMachineScreen extends AbstractContainerScreen<ChemMachineMenu> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(TechQuest.MODID, "textures/gui/processing_machine.png");

    public ChemMachineScreen(ChemMachineMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void renderBg(GuiGraphics g, float partial, int mx, int my) {
        g.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        // progress bar
        int max = menu.getMaxProgress();
        int prog = menu.getProgress();
        int w = max <= 0 ? 0 : prog * 24 / max;
        g.blit(TEXTURE, leftPos + 68, topPos + 35, 176, 0, w, 17);
        // energy bar left
        int energy = menu.getEnergy();
        int eH = Math.min(52, energy * 52 / 50000);
        g.fill(leftPos + 10, topPos + 16 + (52 - eH), leftPos + 16, topPos + 68, 0xFF33CCFF);
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float partial) {
        renderBackground(g, mx, my, partial);
        super.render(g, mx, my, partial);
        renderTooltip(g, mx, my);
        if (mx >= leftPos + 10 && mx <= leftPos + 16 && my >= topPos + 16 && my <= topPos + 68) {
            g.renderTooltip(font, Component.literal(menu.getEnergy() + " FE"), mx, my);
        }
    }
}
