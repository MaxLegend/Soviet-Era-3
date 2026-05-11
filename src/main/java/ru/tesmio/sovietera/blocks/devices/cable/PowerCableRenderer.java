package ru.tesmio.sovietera.blocks.devices.cable;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;


/**
 * Рендерер проводов между электроконнекторами.
 *
 * Рисует провисающий кабель (катенарий) между каждым соединением.
 * Точка крепления провода — центр элемента "cubecenter" модели коннектора,
 * с учётом поворота ATTACHED_FACE.
 *
 * Цвет провода зависит от состояния питания:
 *   - Без питания: тёмно-серый (0.2, 0.2, 0.2) — изолированный кабель
 *   - С питанием:  жёлто-янтарный (1.0, 0.8, 0.0) — под напряжением
 */
public class PowerCableRenderer implements BlockEntityRenderer<BlockEntityPowerConnector> {

    public PowerCableRenderer(BlockEntityRendererProvider.Context ctx) {}

    /** Кастомный RenderType для проводов — без отсечения задних граней, с лайтмапой */
    public static final RenderType CABLE_RENDER = RenderType.create(
            "power_cable_render",
            DefaultVertexFormat.NEW_ENTITY,
            VertexFormat.Mode.TRIANGLES,
            256,
            false,
            true,
            RenderType.CompositeState.builder()
                                     .setShaderState(new RenderStateShard.ShaderStateShard(GameRenderer::getPositionColorLightmapShader))
                                     .setCullState(new RenderStateShard.CullStateShard(false))
                                     .setLightmapState(new RenderStateShard.LightmapStateShard(true))
                                     .setOverlayState(new RenderStateShard.OverlayStateShard(true))
                                     .createCompositeState(false)
    );

    @Override
    public int getViewDistance() {
        return 128;
    }

    @Override
    public void render(BlockEntityPowerConnector entity, float partialTicks, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        BlockPos blockPos = entity.getBlockPos();
        BlockState thisState = entity.getBlockState();
        boolean powered = thisState.getValue(PowerConnectorBlock.POWERED);
        Direction thisFacing = thisState.getValue(PowerConnectorBlock.ATTACHED_FACE);

        // Цвет провода: тёмно-серый без питания, янтарный — с питанием
        float r, g, b;
        if (powered) {
          //  r = 1.0f; g = 0.8f; b = 0.0f;
            r = 0.2f; g = 0.2f; b = 0.2f;
        } else {
            r = 0.2f; g = 0.2f; b = 0.2f;
        }

        // Точка крепления провода на текущем коннекторе
        Vec3 from = getConnectionOffset(thisFacing);

        for (BlockPos connection : entity.getConnections()) {
            BlockState otherState = entity.getLevel().getBlockState(connection);
            if (!(otherState.getBlock() instanceof PowerConnectorBlock)) continue;
            Direction otherFacing = otherState.hasProperty(PowerConnectorBlock.ATTACHED_FACE)
                    ? otherState.getValue(PowerConnectorBlock.ATTACHED_FACE)
                    : Direction.NORTH;

            Vec3 otherOffset = getConnectionOffset(otherFacing);

            // Вычисляем конечную точку провода в локальных координатах
            Vec3 to = Vec3.atCenterOf(connection).subtract(Vec3.atCenterOf(blockPos)).add(otherOffset);

            renderCurvedCable(poseStack, buffer, from, to, r, g, b, packedLight, packedOverlay);
        }
    }

    /**
     * Возвращает точку крепления провода для заданного ATTACHED_FACE.
     *
     * Основано на центре элемента "cubecenter" модели коннектора:
     *   from: [7.5, 8.75, 14.25], to: [8.5, 9.75, 15.25]
     *   Центр в пикселях: (8.0, 9.25, 14.75)
     *   Центр в блок-координатах (NORTH, без поворота): (0.5, 0.578125, 0.921875)
     *
     * Для остальных направлений применяется поворот модели вокруг центра блока (0.5, 0.5, 0.5),
     * соответствующий вращению в blockstate JSON:
     *   NORTH → без вращения
     *   SOUTH → Y 180°
     *   EAST  → Y 90° (по часовой сверху)
     *   WEST  → Y 270° (по часовой сверху)
     *   UP    → X 270°
     *   DOWN  → X 90°
     */
    public static Vec3 getConnectionOffset(Direction attachedFace) {
        // Центр cubecenter в модели для NORTH (без вращения), в блок-координатах
        // X: 8.0 / 16 = 0.5
        // Y: 9.25 / 16 = 0.578125
        // Z: 14.75 / 16 = 0.921875
        //
        // Относительно центра блока (0.5, 0.5, 0.5):
        // rx = 0.0
        // ry = 0.078125
        // rz = 0.421875

        final double rx = 0.0;
        final double ry = 0.08125;
        final double rz = 0.421875;

        return switch (attachedFace) {
            case NORTH -> new Vec3(0.5 + rx, 0.5 + ry, 0.5 + rz);   // (0.5,    0.578, 0.922)
            case SOUTH -> new Vec3(0.5 - rx, 0.5 + ry, 0.5 - rz);   // (0.5,    0.578, 0.078)
            case EAST  -> new Vec3(0.5 - rz, 0.5 + ry, 0.5 + rx);   // (0.078,  0.578, 0.5)
            case WEST  -> new Vec3(0.5 + rz, 0.5 + ry, 0.5 - rx);   // (0.922,  0.578, 0.5)
            case DOWN    -> new Vec3(0.5 + rx, 0.5 + rz, 0.5 - ry);   // (0.5,    0.922, 0.422)
            case UP  -> new Vec3(0.5 + rx, 0.5 - rz, 0.5 + ry);   // (0.5,    0.078, 0.578)
        };
    }

    /**
     * Рисует провисающий кабель между двумя точками.
     * Кабель разбит на сегменты, каждый — толстый кубоид.
     */
    public static void renderCurvedCable(PoseStack poseStack, MultiBufferSource buffer,
            Vec3 from, Vec3 to, float r, float g, float b,
            int light, int overlay) {
        VertexConsumer builder = buffer.getBuffer(CABLE_RENDER);
        int segments = 14;
        float thickness = 0.015F;

        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();
        Matrix3f normal = pose.normal();

        for (int i = 0; i < segments; i++) {
            float t1 = i / (float) segments;
            float t2 = (i + 1) / (float) segments;

            Vec3 p1 = interpolateCurved(from, to, t1);
            Vec3 p2 = interpolateCurved(from, to, t2);

            drawThickSegment(builder, matrix, normal, p1, p2, thickness, r, g, b, light, overlay);
        }
    }

    /**
     * Интерполяция с провисанием (катенарий).
     * Вертикальные провода (одинаковые X и Z) не провисают.
     */
    private static Vec3 interpolateCurved(Vec3 from, Vec3 to, float t) {
        Vec3 linear = from.lerp(to, t);
        // Провисание только для наклонных/горизонтальных проводов
        if (Math.abs(from.x - to.x) < 0.001 && Math.abs(from.z - to.z) < 0.001) {
            return linear;
        }
        double curveAmplitude = 0.17;
        double curve = Math.sin(t * Math.PI) * -curveAmplitude; // провисание вниз
        return new Vec3(linear.x, linear.y + curve, linear.z);
    }

    /**
     * Рисует один сегмент толстого кабеля (кубоид из 6 граней).
     */
    private static void drawThickSegment(VertexConsumer builder, Matrix4f matrix, Matrix3f normal,
            Vec3 p1, Vec3 p2, float thickness,
            float r, float g, float b,
            int light, int overlay) {
        Vec3 dir = p2.subtract(p1).normalize();
        Vec3 up = Math.abs(dir.y) > 0.999 ? new Vec3(1, 0, 0) : new Vec3(0, 1, 0);
        Vec3 right = dir.cross(up).normalize().scale(thickness);
        Vec3 forward = dir.cross(right).normalize().scale(thickness);

        Vec3[] corners = new Vec3[]{
                p1.add(right).add(forward),         // 0
                p1.add(right).subtract(forward),     // 1
                p1.subtract(right).subtract(forward),// 2
                p1.subtract(right).add(forward),     // 3
                p2.add(right).add(forward),          // 4
                p2.add(right).subtract(forward),     // 5
                p2.subtract(right).subtract(forward),// 6
                p2.subtract(right).add(forward),     // 7
        };

        int[][] faces = {
                {0, 1, 2, 3}, // низ
                {7, 6, 5, 4}, // верх
                {0, 4, 5, 1}, // право
                {1, 5, 6, 2}, // перед
                {2, 6, 7, 3}, // лево
                {3, 7, 4, 0}, // зад
        };

        for (int[] face : faces) {
            for (int idx : face) {
                Vec3 normalVec = corners[face[1]].subtract(corners[face[0]])
                                                 .cross(corners[face[2]].subtract(corners[face[1]]))
                                                 .normalize();
                Vec3 v = corners[idx];
                builder.vertex(matrix, (float) v.x, (float) v.y, (float) v.z)
                       .color(r, g, b, 1f)
                       .uv(0, 0)
                       .overlayCoords(overlay)
                       .uv2(light)
                       .normal(normal, (float) normalVec.x, (float) normalVec.y, (float) normalVec.z)
                       .endVertex();
            }
        }
    }
}

