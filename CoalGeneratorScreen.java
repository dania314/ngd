package com.techquest.client.screen;

import com.techquest.TechQuest;
import com.techquest.block.entity.CoalGeneratorBlockEntity;
import com.techquest.menu.CoalGeneratorMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class CoalGeneratorScreen extends AbstractContainerScreen<CoalGeneratorMenu> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(TechQuest.MODID, "textures/gui/coal_generator.png");

    // Область шкалы энергии (относительно левого верхнего угла GUI)
    private static final int BAR_X = 152;
    private static final int BAR_Y = 16;
    private static final int BAR_W = 12;
    private static final int BAR_H = 54;

    // Область огонька
    private static final int FLAME_X = 56;
    private static final int FLAME_Y = 26;
    private static final int FLAME_SIZE = 14;

    public CoalGeneratorScreen(CoalGeneratorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        g.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);

        // Шкала энергии
        int energy = menu.getEnergy();
        int filled = (int) ((long) BAR_H * energy / CoalGeneratorBlockEntity.CAPACITY);
        if (filled > 0) {
            int x = leftPos + BAR_X;
            int yBottom = topPos + BAR_Y + BAR_H;
            g.fill(x, yBottom - filled, x + BAR_W, yBottom, 0xFFD62828);
        }

        // Огонёк горения
        if (menu.isBurning() && menu.getBurnTimeTotal() > 0) {
            int h = (int) Math.ceil((double) FLAME_SIZE * menu.getBurnTime() / menu.getBurnTimeTotal());
            int x = leftPos + FLAME_X;
            int yBottom = topPos + FLAME_Y + FLAME_SIZE;
            g.fill(x, yBottom - h, x + FLAME_SIZE, yBottom, 0xFFFF8C1A);
        }
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        renderTooltip(g, mouseX, mouseY);

        if (isHovering(BAR_X, BAR_Y, BAR_W, BAR_H, mouseX, mouseY)) {
            g.renderTooltip(font,
                    Component.translatable("gui.techquest.energy", menu.getEnergy(),
                            CoalGeneratorBlockEntity.CAPACITY),
                    mouseX, mouseY);
        }
    }
}
