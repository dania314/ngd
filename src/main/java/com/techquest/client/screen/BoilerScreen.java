package com.techquest.client.screen;

import com.techquest.TechQuest;
import com.techquest.menu.BoilerMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class BoilerScreen extends AbstractContainerScreen<BoilerMenu> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(TechQuest.MODID, "textures/gui/processing_machine.png");

    public BoilerScreen(BoilerMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        imageWidth = 176;
        imageHeight = 166;
    }

    @Override
    protected void renderBg(GuiGraphics g, float partial, int mx, int my) {
        g.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        // heat bar (red)
        int heatH = menu.getHeat() * 50 / 1000;
        g.fill(leftPos + 116, topPos + 15 + (50 - heatH), leftPos + 124, topPos + 65, 0xFFFF4422);
        // pressure bar (orange) — danger above 90%
        int pr = menu.getPressure();
        int prH = pr * 50 / 1000;
        int prColor = pr > 900 ? 0xFFFF0000 : (pr > 700 ? 0xFFFFAA00 : 0xFF88CCFF);
        g.fill(leftPos + 130, topPos + 15 + (50 - prH), leftPos + 138, topPos + 65, prColor);
        // water
        int wH = menu.getWater() * 50 / 100;
        g.fill(leftPos + 144, topPos + 15 + (50 - wH), leftPos + 152, topPos + 65, 0xFF2266FF);
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float partial) {
        renderBackground(g, mx, my, partial);
        super.render(g, mx, my, partial);
        renderTooltip(g, mx, my);
        g.drawString(font, Component.literal("H " + menu.getHeat()), leftPos + 116, topPos + 68, 0xFFFF8866, false);
        g.drawString(font, Component.literal("P " + menu.getPressure()), leftPos + 8, topPos + 6, 0xFFFFCC88, false);
        g.drawString(font, Component.literal(menu.getEnergy() + " FE"), leftPos + 8, topPos + 68, 0xFF88DDFF, false);
        if (menu.getPressure() > 850) {
            g.drawString(font, Component.translatable("gui.techquest.boiler.danger"), leftPos + 60, topPos + 6, 0xFFFF2222, false);
        }
    }
}
