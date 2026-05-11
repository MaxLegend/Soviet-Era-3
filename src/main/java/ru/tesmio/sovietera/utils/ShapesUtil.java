package ru.tesmio.sovietera.utils;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A util class for turning boundboxes
 *
 * @author Tesmio
 */
public class ShapesUtil {

    public static final VoxelShape FULL_CUBE = Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0);


    public enum RotationDegree {
        D90,
        D180,
        D270
    }

    public static VoxelShape rotate(VoxelShape vs, RotationDegree degree) {
        return switch (degree) {
            case D90 -> rotate90(vs);
            case D180 -> rotate180(vs);
            case D270 -> rotate270(vs);
        };
    }

    private static VoxelShape rotate90(VoxelShape vs) {
        VoxelShape result = Shapes.empty();
        for (AABB box : vs.toAabbs()) {
            double newMinX = 0.5 + (box.minZ - 0.5);
            double newMaxX = 0.5 + (box.maxZ - 0.5);
            double newMinZ = 0.5 - (box.maxX - 0.5);
            double newMaxZ = 0.5 - (box.minX - 0.5);

            double minX = Math.min(newMinX, newMaxX);
            double maxX = Math.max(newMinX, newMaxX);
            double minZ = Math.min(newMinZ, newMaxZ);
            double maxZ = Math.max(newMinZ, newMaxZ);

            result = Shapes.or(result, Shapes.create(minX, box.minY, minZ, maxX, box.maxY, maxZ));
        }
        return result;
    }

    private static VoxelShape rotate180(VoxelShape vs) {
        VoxelShape result = Shapes.empty();
        for (AABB box : vs.toAabbs()) {
            double newMinX = 1.0 - box.maxX;
            double newMaxX = 1.0 - box.minX;
            double newMinZ = 1.0 - box.maxZ;
            double newMaxZ = 1.0 - box.minZ;

            double minX = Math.min(newMinX, newMaxX);
            double maxX = Math.max(newMinX, newMaxX);
            double minZ = Math.min(newMinZ, newMaxZ);
            double maxZ = Math.max(newMinZ, newMaxZ);

            result = Shapes.or(result, Shapes.create(minX, box.minY, minZ, maxX, box.maxY, maxZ));
        }
        return result;
    }

    private static VoxelShape rotate270(VoxelShape vs) {
        VoxelShape result = Shapes.empty();
        for (AABB box : vs.toAabbs()) {
            double newMinX = 0.5 - (box.maxZ - 0.5);
            double newMaxX = 0.5 - (box.minZ - 0.5);
            double newMinZ = 0.5 + (box.minX - 0.5);
            double newMaxZ = 0.5 + (box.maxX - 0.5);

            double minX = Math.min(newMinX, newMaxX);
            double maxX = Math.max(newMinX, newMaxX);
            double minZ = Math.min(newMinZ, newMaxZ);
            double maxZ = Math.max(newMinZ, newMaxZ);

            result = Shapes.or(result, Shapes.create(minX, box.minY, minZ, maxX, box.maxY, maxZ));
        }
        return result;
    }
    /** CW 90° around X-axis (NORTH → UP) */
    public static VoxelShape rotateAroundXCW(VoxelShape vs) {
        VoxelShape result = Shapes.empty();
        for (AABB box : vs.toAabbs()) {
            double newMinY = 0.5 + (box.minZ - 0.5);
            double newMaxY = 0.5 + (box.maxZ - 0.5);
            double newMinZ = 0.5 - (box.maxY - 0.5);
            double newMaxZ = 0.5 - (box.minY - 0.5);

            double minY = Math.min(newMinY, newMaxY);
            double maxY = Math.max(newMinY, newMaxY);
            double minZ = Math.min(newMinZ, newMaxZ);
            double maxZ = Math.max(newMinZ, newMaxZ);

            result = Shapes.or(result, Shapes.create(box.minX, minY, minZ, box.maxX, maxY, maxZ));
        }
        return result;
    }

    /** CCW 90° around X-axis (NORTH → DOWN) */
    public static VoxelShape rotateAroundXCCW(VoxelShape vs) {
        VoxelShape result = Shapes.empty();
        for (AABB box : vs.toAabbs()) {
            double newMinY = 0.5 - (box.maxZ - 0.5);
            double newMaxY = 0.5 - (box.minZ - 0.5);
            double newMinZ = 0.5 + (box.minY - 0.5);
            double newMaxZ = 0.5 + (box.maxY - 0.5);

            double minY = Math.min(newMinY, newMaxY);
            double maxY = Math.max(newMinY, newMaxY);
            double minZ = Math.min(newMinZ, newMaxZ);
            double maxZ = Math.max(newMinZ, newMaxZ);

            result = Shapes.or(result, Shapes.create(box.minX, minY, minZ, box.maxX, maxY, maxZ));
        }
        return result;
    }
}
