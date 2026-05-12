package ru.tesmio.sovietera.blocks.devices.generator;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import org.jetbrains.annotations.Nullable;
import ru.tesmio.sovietera.blocks.baseblock.BlockModelSide;

import ru.tesmio.sovietera.core.BlocksSE;
import ru.tesmio.sovietera.utils.ShapesUtil;

/**
 * Блок электрогенератора — декоративная часть структуры дизельного генератора.
 *
 */
public class BlockElectroGenerator extends BlockModelSide  {

    private static final VoxelShape AABB = Shapes.or(
            Block.box(2D, 1D, 3D, 13D, 5.0D, 13D),
            Block.box(2D, 0D, 4D, 13D, 7.0D, 12D),
            Block.box(2D, 0D, 6D, 13D, 8.0D, 10D),
            Block.box(2D, 0D, 5D, 14D, 6.0D, 11D),
            Block.box(0D, 0D, 6D, 16D, 6.0D, 10D),
            Block.box(0D, 1D, 5D, 16D, 5.0D, 11D));

    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    public BlockElectroGenerator(Properties properties, float shadingInside) {
        super(properties, shadingInside);
        this.registerDefaultState(this.defaultBlockState()
                                      .setValue(FACING, Direction.NORTH)
                                      .setValue(WATERLOGGED, false)
                                      .setValue(POWERED, false));
    }

    public VoxelShape getFacingShape(BlockState s) {
        return ShapesUtil.rotate(AABB, ShapesUtil.RotationDegree.D90);
    }
    @Override
    public float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return 1.0F;
    }
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, WATERLOGGED, POWERED);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;

        // Делегируем взаимодействие к контроллеру (BlockDieselEngine),
        // который стоит на CCW-стороне от этого блока.
        if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
            BlockPos controllerPos = pos.relative(facing.getCounterClockWise());
            BlockState controllerState = level.getBlockState(controllerPos);

            if (controllerState.getBlock() == BlocksSE.DIESEL_ENGINE.get()) {
                // Делегируем ПКМ к контроллеру
                return controllerState.use(level, player, hand, new BlockHitResult(
                        pos.getCenter().add(0, 0.5, 0),
                        hit.getDirection(),
                        controllerPos,
                        false
                ));
            }
        }

        return InteractionResult.PASS;
    }

    @Override
    public void onRemove(BlockState oldState, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!oldState.is(newState.getBlock())) {
            level.removeBlockEntity(pos);
        }
    }
}