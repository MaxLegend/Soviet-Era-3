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
 * Декоративный блок электрогенератора дизельной установки.
 * Часть мультиблока. Не имеет BlockEntity.
 */
public class BlockDieselElectroGenerator extends BlockModelSide {
    private static final VoxelShape AABB = Shapes.or(
            Block.box(2D, 1D, 3D, 13D, 5.0D, 13D),
            Block.box(2D, 0D, 4D, 13D, 7.0D, 12D),
            Block.box(2D, 0D, 6D, 13D, 8.0D, 10D),
            Block.box(2D, 0D, 5D, 14D, 6.0D, 11D),
            Block.box(0D, 0D, 6D, 16D, 6.0D, 10D),
            Block.box(0D, 1D, 5D, 16D, 5.0D, 11D));
    public BlockDieselElectroGenerator(Properties properties, float shadingInside) {
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
