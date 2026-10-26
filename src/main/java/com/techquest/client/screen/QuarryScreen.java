package com.techquest.client.screen;

import com.techquest.TechQuest;
import com.techquest.menu.QuarryMenu;
import com.techquest.network.QuarryTogglePayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

public class QuarryScreen extends AbstractContainerScreen<QuarryMenu> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(TechQuest.MODID, "textures/gui/processing_machine.png");

    public QuarryScreen(QuarryMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        imageWidth = 176;
        imageHeight = 166;
    }

    @Override
    protected void init() {
        super.init();
        addRenderableWidget(Button.builder(
                Component.translatable("gui.techquest.quarry.toggle"),
                b -> PacketDistributor.sendToServer(new QuarryTogglePayload(menu.getBlockEntity().getBlockPos()))
        ).bounds(leftPos + 115, topPos + 64, 52, 16).build());
    }

    @Override
    protected void renderBg(GuiGraphics g, float partial, int mx, int my) {
        g.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        int pct = menu.getEnergyPct();
        int h = pct * 52 / 100;
        g.fill(leftPos + 8, topPos + 66 + (52 - h) - 52, leftPos + 14, topPos + 66, 0xFF33CCFF);
        // status
        int color = menu.isActive() ? 0xFF44FF44 : 0xFFFF4444;
        g.fill(leftPos + 160, topPos + 18, leftPos + 168, topPos + 26, color);
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float partial) {
        renderBackground(g, mx, my, partial);
        super.render(g, mx, my, partial);
        renderTooltip(g, mx, my);
        g.drawString(font, Component.translatable("gui.techquest.quarry.filter"), leftPos + 8, topPos + 6, 0xFFAAAAAA, false);
        g.drawString(font, Component.translatable("gui.techquest.quarry.buffer"), leftPos + 8, topPos + 38, 0xFFAAAAAA, false);
        g.drawString(font, Component.literal("Y-" + menu.getDepth()), leftPos + 130, topPos + 18, 0xFFFFFFFF, false);
        if (mx >= leftPos + 8 && mx <= leftPos + 14 && my >= topPos + 14 && my <= topPos + 66) {
            g.renderTooltip(font, Component.literal(menu.getEnergy() + " FE"), mx, my);
        }
    }
}
