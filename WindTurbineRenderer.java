package com.techquest.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.techquest.block.WindTurbineBlock;
import com.techquest.block.entity.WindTurbineBlockEntity;
import com.techquest.item.TurbineBladeItem;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;

/**
 * Рисует крест лопастей над ветряком и крутит их.
 */
public class WindTurbineRenderer implements BlockEntityRenderer<WindTurbineBlockEntity> {

    public WindTurbineRenderer(BlockEntityRendererProvider.Context ctx) {}

    @Override
    public void render(WindTurbineBlockEntity be, float partialTick, PoseStack pose,
                       MultiBufferSource buffers, int light, int overlay) {
        if (!be.getBlockState().getValue(WindTurbineBlock.HAS_BLADES) || be.getBlade().isEmpty()) {
            return;
        }

        ItemStack blade = be.getBlade();
        int color = bladeColor(blade);
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;

        // Износ: бледнеет
        float dur = be.getDurabilityFraction();
        r *= 0.5f + 0.5f * dur;
        g *= 0.5f + 0.5f * dur;
        b *= 0.5f + 0.5f * dur;

        float angle = be.bladeAngle + be.bladeSpeed * partialTick;
        Direction facing = be.getBlockState().getValue(WindTurbineBlock.FACING);

        pose.pushPose();
        pose.translate(0.5, 1.15, 0.5);
        // Повернуть «лицом» куда смотрит блок
        float yaw = switch (facing) {
            case SOUTH -> 180f;
            case WEST -> 90f;
            case EAST -> -90f;
            default -> 0f;
        };
        pose.mulPose(Axis.YP.rotationDegrees(yaw));
        pose.mulPose(Axis.ZP.rotationDegrees(angle));

        VertexConsumer vc = buffers.getBuffer(RenderType.solid());
        Matrix4f mat = pose.last().pose();

        // Две лопасти крестом (тонкие пластины)
        drawBlade(vc, mat, light, overlay, r, g, b, 0);
        drawBlade(vc, mat, light, overlay, r, g, b, 90);

        pose.popPose();
    }

    private static void drawBlade(VertexConsumer vc, Matrix4f mat, int light, int overlay,
                                  float r, float g, float b, float rotDeg) {
        // Простая пластина вдоль X
        float len = 0.85f;
        float halfW = 0.06f;
        float thick = 0.02f;
        // 4 угла пластины в плоскости XY после rot
        double rad = Math.toRadians(rotDeg);
        float cos = (float) Math.cos(rad);
        float sin = (float) Math.sin(rad);

        // Две стороны «ленты» от -len до +len
        float[][] pts = {
                {-len, -halfW}, {len, -halfW}, {len, halfW}, {-len, halfW}
        };
        // front
        for (int i = 0; i < 4; i++) {
            float lx = pts[i][0];
            float ly = pts[i][1];
            float x = lx * cos - ly * sin;
            float y = lx * sin + ly * cos;
            vc.addVertex(mat, x, y, thick)
                    .setColor(r, g, b, 1f)
                    .setUv(0, 0)
                    .setOverlay(overlay)
                    .setLight(light)
                    .setNormal(0, 0, 1);
        }
    }

    private static int bladeColor(ItemStack stack) {
        if (!(stack.getItem() instanceof TurbineBladeItem bi)) return 0xAAB8C8;
        return switch (bi.getCraftTier()) {
            case 1 -> 0x8B6437; // wood
            case 2 -> 0xAAB8C8; // tin
            case 3 -> 0xB97D37; // bronze
            case 4 -> 0xB4B4B8; // iron
            case 5 -> 0xC8D2DC; // aluminum
            case 6 -> 0x32C878; // advanced
            default -> 0xAAB8C8;
        };
    }

    @Override
    public boolean shouldRenderOffScreen(WindTurbineBlockEntity be) {
        return true;
    }
}
