package com.techquest.client.screen;

import com.techquest.TechQuest;
import com.techquest.menu.NuclearReactorMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class NuclearReactorScreen extends AbstractContainerScreen<NuclearReactorMenu> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(TechQuest.MODID, "textures/gui/processing_machine.png");

    public NuclearReactorScreen(NuclearReactorMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        imageWidth = 176;
        imageHeight = 166;
    }

    @Override
    protected void renderBg(GuiGraphics g, float partial, int mx, int my) {
        g.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        int heatH = Math.min(52, menu.getHeat() * 52 / 2000);
        int color = menu.getHeat() > 1500 ? 0xFFFF0000 : 0xFF44FF44;
        g.fill(leftPos + 10, topPos + 16 + (52 - heatH), leftPos + 16, topPos + 68, color);
        int burnW = menu.getBurn() * 24 / 400;
        g.blit(TEXTURE, leftPos + 79, topPos + 34, 176, 0, burnW, 17);
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float partial) {
        renderBackground(g, mx, my, partial);
        super.render(g, mx, my, partial);
        renderTooltip(g, mx, my);
        g.drawString(font, Component.literal(menu.getEnergy() + " FE"), leftPos + 100, topPos + 10, 0xFF88FF88, false);
        g.drawString(font, Component.literal("Heat " + menu.getHeat()), leftPos + 100, topPos + 60, 0xFFFFAA88, false);
        if (menu.getHeat() > 1500) {
            g.drawString(font, Component.translatable("gui.techquest.reactor.meltdown"), leftPos + 40, topPos + 6, 0xFFFF2222, false);
        }
    }
}
