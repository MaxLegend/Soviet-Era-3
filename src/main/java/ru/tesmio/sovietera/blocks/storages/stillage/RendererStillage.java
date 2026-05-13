package ru.tesmio.sovietera.blocks.storages.stillage;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import ru.tesmio.sovietera.blocks.baseblock.BlockModelSideUD;

/**
 * Renders the stored item standing upright on the stillage shelf.
 * Uses GROUND transform type (like dropped items) so they appear as 3D objects.
 * Ported from 1.16.5 StillageTER.
 */
public class RendererStillage implements BlockEntityRenderer<EntityBlockStillage> {

    private static final float ITEM_SCALE = 3.1F;

    public RendererStillage(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(EntityBlockStillage be, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {

        ItemStack stack = be.getStoredItem();
        if (stack.isEmpty()) return;

        Direction facing = be.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);

        // Calculate yaw - same logic as 1.16.5: X axis uses facing, other axes use opposite
        float angle;
        if (facing.getAxis() == Direction.Axis.X) {
            angle = facing.toYRot();
        } else {
            angle = facing.getOpposite().toYRot();
        }

        // Y offset depends on PART: UP shelf is higher, MIDDLE/DOWN is lower
        double yOffset;
        if (be.getBlockState().hasProperty(BlockModelSideUD.PART)
                && be.getBlockState().getValue(BlockModelSideUD.PART) == BlockModelSideUD.EnumPart.UP) {
            yOffset = -0.15D;
        } else {
            yOffset = 0.05D;
        }

        poseStack.pushPose();

        // Position at center of block with calculated Y offset
        poseStack.translate(0.5D, yOffset, 0.5D);

        // Rotate around Y axis to face the correct direction
        poseStack.mulPose(Axis.YP.rotationDegrees(angle));

        // Scale up - GROUND transform is tiny by default
        poseStack.scale(ITEM_SCALE, ITEM_SCALE, ITEM_SCALE);

        // Use proper lighting from the world
        int lightLevel = getLightLevel(be);

        // GROUND transform renders items as 3D dropped objects (standing upright)
        Minecraft.getInstance().getItemRenderer().renderStatic(
                stack,
                ItemDisplayContext.GROUND,
                lightLevel,
                OverlayTexture.NO_OVERLAY,
                poseStack,
                bufferSource,
                be.getLevel(),
                0
        );

        poseStack.popPose();
    }

    private int getLightLevel(EntityBlockStillage be) {
        if (be.getLevel() == null) return 15728880;
        int bLight = be.getLevel().getBrightness(LightLayer.BLOCK, be.getBlockPos().above());
        int sLight = be.getLevel().getBrightness(LightLayer.SKY, be.getBlockPos().above());
        return bLight << 4 | sLight << 20;
    }
}
