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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import ru.tesmio.sovietera.blocks.devices.devicesnetwork.INetworkNode;
import ru.tesmio.sovietera.blocks.devices.lamps.EntityBlockLamp;
import ru.tesmio.sovietera.utils.BaseEnumOrientation;

/**
 * Unified cable renderer for all INetworkNode block entities (connectors and lamps).
 *
 * <p>Generic over T to avoid unchecked casts when registering for different BlockEntityTypes.
 * Deduplication: each cable is rendered only once by the endpoint with the smaller BlockPos
 * (using Vec3i natural ordering), preventing double-draw when both endpoints are rendered.
 */
public class RendererPowerCable<T extends BlockEntity> implements BlockEntityRenderer<T> {

    /** Cable render type: no back-face culling, with lightmap and overlay. */
    public static final RenderType CABLE_RENDER = RenderType.create(
            "power_cable_render",
            DefaultVertexFormat.NEW_ENTITY,
            VertexFormat.Mode.QUADS,
            256,
            false,
            true,
            RenderType.CompositeState.builder()
                                     .setShaderState(new RenderStateShard.ShaderStateShard(
                                             GameRenderer::getPositionColorLightmapShader))
                                     .setCullState(new RenderStateShard.CullStateShard(false))
                                     .setLightmapState(new RenderStateShard.LightmapStateShard(true))
                                     .setOverlayState(new RenderStateShard.OverlayStateShard(true))
                                     .createCompositeState(false)
    );

    public RendererPowerCable(BlockEntityRendererProvider.Context ctx) {}

    @Override
    public int getViewDistance() {
        return 128;
    }

    @Override
    public void render(T entity, float partialTicks, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {

        INetworkNode node = INetworkNode.from(entity);
        if (node == null) return;

        BlockPos blockPos = entity.getBlockPos();
        BlockState thisState = entity.getBlockState();

        float r = 0.2f, g = 0.2f, b = 0.2f;

        for (BlockPos connection : node.getConnections()) {
            // Deduplicate: render each cable only from the endpoint with the smaller position.
            if (blockPos.compareTo(connection) > 0) continue;

            // "from" offset — this entity's attachment point for this connection
            Vec3 from = getConnectionOffsetForNode(entity, connection);

            // "to" offset — the other entity's attachment point for this connection
            BlockEntity otherBe = entity.getLevel().getBlockEntity(connection);
            if (otherBe == null) continue;
            Vec3 otherOffset = getConnectionOffsetForNode(otherBe, blockPos);

            Vec3 to = Vec3.atCenterOf(connection)
                          .subtract(Vec3.atCenterOf(blockPos))
                          .add(otherOffset);

            renderCurvedCable(poseStack, buffer, from, to, r, g, b, packedLight, packedOverlay);
        }
    }

    /**
     * Returns the cable attachment offset for a node, taking into account
     * per-connection endpoint data for longitudinal lamps.
     *
     * @param entity       the block entity (connector or lamp)
     * @param connectionPos the BlockPos of the OTHER end of the cable
     */
    private static Vec3 getConnectionOffsetForNode(BlockEntity entity, BlockPos connectionPos) {
        BlockState state = entity.getBlockState();

        // Power connector — single attachment point
        if (state.hasProperty(BlockPowerConnector.ATTACHED_FACE)) {
            return getConnectionOffset(state.getValue(BlockPowerConnector.ATTACHED_FACE));
        }

        // Lamp — check for per-connection endpoint
        if (entity instanceof EntityBlockLamp lamp) {
            byte endpoint = lamp.getConnectionEndpoint(connectionPos);
            return getLampOffsetForEndpoint(state, endpoint);
        }

        // Fallback
        return new Vec3(0.5, 0.5, 0.5);
    }

    // ===================== Connection-point helpers =====================

    /**
     * Returns the cable attachment offset for a lamp at a specific endpoint.
     *
     * <p>For longitudinal lamps (BaseEnumOrientation): the cable attaches to the
     * corner where the exposed face meets the long axis end (0 or 1).
     * For compact lamps (IncLamp, StreetLamp): the cable attaches to the centre
     * of the exposed face (endpoint is ignored).
     */
    public static Vec3 getLampOffsetForEndpoint(BlockState state, byte endpoint) {
        Direction exposedFace = getLampExposedFace(state);
        Direction.Axis longAxis = getLampLongAxis(state);

        if (longAxis != null) {
            // Longitudinal lamp — two attachment points at the ends of the long axis
            return getEndpointOffset(exposedFace, longAxis, endpoint);
        } else {
            // Compact lamp (IncLamp, StreetLamp) — single centre point
            return getLampConnectionOffset(exposedFace);
        }
    }

    /**
     * Computes the attachment point for a longitudinal lamp's specific endpoint.
     *
     * <p>The point is at the corner where the exposed face meets the long axis end:
     * <ul>
     *   <li>exposed face axis → 0.0 or 1.0 (depending on face direction)</li>
     *   <li>long axis → 0.0 (endpoint 0) or 1.0 (endpoint 1)</li>
     *   <li>remaining axis → 0.5</li>
     * </ul>
     */
    public static Vec3 getEndpointOffset(Direction exposedFace, Direction.Axis longAxis, byte endpoint) {
        double x = 0.5, y = 0.5, z = 0.5;

        // Set the exposed face coordinate
        switch (exposedFace) {
            case NORTH -> z = 0.0;
            case SOUTH -> z = 1.0;
            case WEST  -> x = 0.0;
            case EAST  -> x = 1.0;
            case DOWN  -> y = 0.0;
            case UP    -> y = 1.0;
        }

        // Set the long axis coordinate based on endpoint (0 or 1)
        double endpointValue = endpoint == 0 ? 0.0 : 1.0;
        switch (longAxis) {
            case X -> x = endpointValue;
            case Y -> y = endpointValue;
            case Z -> z = endpointValue;
        }

        return new Vec3(x, y, z);
    }

    /**
     * Returns the long axis for a longitudinal lamp, or null for compact lamps.
     */
    private static Direction.Axis getLampLongAxis(BlockState state) {
        for (var prop : state.getProperties()) {
            if (!"facing".equals(prop.getName())) continue;
            if (prop instanceof EnumProperty<?> ep && ep.getValueClass() == BaseEnumOrientation.class) {
                @SuppressWarnings("unchecked")
                EnumProperty<BaseEnumOrientation> facing = (EnumProperty<BaseEnumOrientation>) ep;
                return state.getValue(facing).getLongAxis();
            }
        }
        return null;
    }

    /**
     * Returns the exposed face direction for a lamp (the face the cable comes out of).
     *
     * <p>This is the OPPOSITE of the wall/ceiling/floor the lamp is mounted on.
     */
    private static Direction getLampExposedFace(BlockState state) {
        for (var prop : state.getProperties()) {
            if (!"facing".equals(prop.getName())) continue;

            // FluoLamp / BrokenFluoLamp — BaseEnumOrientation (8 directions)
            if (prop instanceof EnumProperty<?> ep && ep.getValueClass() == BaseEnumOrientation.class) {
                @SuppressWarnings("unchecked")
                EnumProperty<BaseEnumOrientation> facing = (EnumProperty<BaseEnumOrientation>) ep;
                return state.getValue(facing).getAttachDirection().getOpposite();
            }

            // IncLamp / StreetLamp — DirectionProperty (6 or 4 directions)
            if (prop instanceof DirectionProperty dp) {
                return state.getValue(dp).getOpposite();
            }
        }
        return Direction.NORTH;
    }

    /**
     * Cable attachment point for a power connector.
     *
     * <p>Derived from the "cubecenter" element in the connector model:
     * centre in block-space (NORTH, no rotation): X=0.5, Y≈0.578, Z≈0.922.
     * Other faces are obtained by rotating around the block centre (0.5, 0.5, 0.5).
     */
    public static Vec3 getConnectionOffset(Direction attachedFace) {
        // Offset from block centre for the NORTH facing
        final double rx = 0.0;
        final double ry = 0.08125;
        final double rz = 0.42;

        return switch (attachedFace) {
            case NORTH -> new Vec3(0.5 + rx, 0.5 + ry, 0.5 + rz);
            case SOUTH -> new Vec3(0.5 - rx, 0.5 + ry, 0.5 - rz);
            case EAST  -> new Vec3(0.5 - rz, 0.5 + ry, 0.5 + rx);
            case WEST  -> new Vec3(0.5 + rz, 0.5 + ry, 0.5 - rx);
            case DOWN  -> new Vec3(0.5 + rx, 0.5 + rz, 0.5 - ry);
            case UP    -> new Vec3(0.5 + rx, 0.5 - rz, 0.42 + ry);
        };
    }

    /**
     * Cable attachment point for a lamp — centre of the face it is mounted on.
     */
    public static Vec3 getLampConnectionOffset(Direction attachDir) {
        return switch (attachDir) {
            case NORTH -> new Vec3(0.5, 0.5, 0.0);
            case SOUTH -> new Vec3(0.5, 0.5, 1.0);
            case EAST  -> new Vec3(1.0, 0.5, 0.5);
            case WEST  -> new Vec3(0.0, 0.5, 0.1);
            case UP    -> new Vec3(0.5, 1.0, 0.5);
            case DOWN  -> new Vec3(0.5, 0.0, 0.5);
        };
    }

    // ===================== Cable geometry =====================

    /**
     * Renders a sagging cable (catenary approximation) between two local-space points.
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
     * Linear interpolation with downward sag (sin-based catenary approximation).
     * Purely vertical cables are not sagged.
     */
    private static Vec3 interpolateCurved(Vec3 from, Vec3 to, float t) {
        Vec3 linear = from.lerp(to, t);
        if (Math.abs(from.x - to.x) < 0.001 && Math.abs(from.z - to.z) < 0.001) {
            return linear;
        }
        double curve = Math.sin(t * Math.PI) * -0.17; // negative = sag downward
        return new Vec3(linear.x, linear.y + curve, linear.z);
    }

    /**
     * Emits a thick cable segment as a 6-faced cuboid aligned along the segment direction.
     */
    private static void drawThickSegment(VertexConsumer builder, Matrix4f matrix, Matrix3f normal,
            Vec3 p1, Vec3 p2, float thickness,
            float r, float g, float b,
            int light, int overlay) {
        Vec3 dir = p2.subtract(p1).normalize();
        Vec3 up = Math.abs(dir.y) > 0.999 ? new Vec3(1, 0, 0) : new Vec3(0, 1, 0);
        Vec3 right = dir.cross(up).normalize().scale(thickness);
        Vec3 forward = dir.cross(right).normalize().scale(thickness);

        Vec3[] corners = {
                p1.add(right).add(forward),          // 0
                p1.add(right).subtract(forward),      // 1
                p1.subtract(right).subtract(forward), // 2
                p1.subtract(right).add(forward),      // 3
                p2.add(right).add(forward),           // 4
                p2.add(right).subtract(forward),      // 5
                p2.subtract(right).subtract(forward), // 6
                p2.subtract(right).add(forward),      // 7
        };

        int[][] faces = {
                {0, 1, 2, 3}, // bottom
                {7, 6, 5, 4}, // top
                {0, 4, 5, 1}, // right
                {1, 5, 6, 2}, // front
                {2, 6, 7, 3}, // left
                {3, 7, 4, 0}, // back
        };

        for (int[] face : faces) {
            Vec3 n = corners[face[1]].subtract(corners[face[0]])
                                     .cross(corners[face[2]].subtract(corners[face[1]]))
                                     .normalize();
            for (int idx : face) {
                Vec3 v = corners[idx];
                builder.vertex(matrix, (float) v.x, (float) v.y, (float) v.z)
                       .color(r, g, b, 1f)
                       .uv(0, 0)
                       .overlayCoords(overlay)
                       .uv2(light)
                       .normal(normal, (float) n.x, (float) n.y, (float) n.z)
                       .endVertex();
            }
        }
    }
}