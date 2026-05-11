package ru.tesmio.sovietera.blocks.devices.generator;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import ru.tesmio.sovietera.blocks.baseblock.BlockModelSide;
import ru.tesmio.sovietera.utils.ShapesUtil;

/**
 * Декоративный блок бака дизельного генератора.
 * Часть мультиблока. Не имеет BlockEntity.
 */
public class BlockDieselTank extends BlockModelSide {
    private static final VoxelShape AABB = Shapes.or(
            Block.box(2D, 0D, 3D, 16D, 13.0D, 13D),
            Block.box(1D, 0D, 4D, 16D, 12.0D, 12D),
            Block.box(0D, 0D, 5D, 16D, 11.0D, 11D));
    public BlockDieselTank(Properties properties, float shadingInside) {
        super(properties, shadingInside);
    }

    @Override
    public float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return 1.0F;
    }

    public VoxelShape getFacingShape(BlockState s) {
        return ShapesUtil.rotate(AABB, ShapesUtil.RotationDegree.D90);
    }
}
