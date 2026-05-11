package ru.tesmio.sovietera.blocks.baseblock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.item.context.BlockPlaceContext;
import ru.tesmio.sovietera.utils.ShapesUtil;

import javax.annotation.Nullable;

public class BlockForFacingModel extends BlockForFacing {
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    private float shadingInside;
    public static VoxelShape FACING_SHAPE = Shapes.block();

    public BlockForFacingModel(Properties properties, float shadingInside) {
        super(properties);
        this.shadingInside = shadingInside;
        this.registerDefaultState(this.stateDefinition.any()
                                                      .setValue(FACING, EnumOrientation.NORTH)
                                                      .setValue(WATERLOGGED, false));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        FluidState fluidState = context.getLevel().getFluidState(context.getClickedPos());
        BlockState defaultState = super.getStateForPlacement(context);
        if (defaultState != null) {
            return defaultState.setValue(WATERLOGGED, fluidState.getType() == Fluids.WATER);
        }
        return this.defaultBlockState().setValue(WATERLOGGED, fluidState.getType() == Fluids.WATER);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos) {
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        return state;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, WATERLOGGED);
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter reader, BlockPos pos) {
        return !state.getValue(WATERLOGGED);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter reader, BlockPos pos, CollisionContext context) {
        VoxelShape base = getFacingShape(state);
        return switch (state.getValue(FACING)) {
            case NORTH -> base;
            case SOUTH -> ShapesUtil.rotate(base, ShapesUtil.RotationDegree.D180);
            case EAST  -> ShapesUtil.rotate(base, ShapesUtil.RotationDegree.D270);
            case WEST  -> ShapesUtil.rotate(base, ShapesUtil.RotationDegree.D90);
            case UP    -> ShapesUtil.rotateAroundXCW(base);
            case DOWN  -> ShapesUtil.rotateAroundXCCW(base);
        };
    }

    public VoxelShape getFacingShape(BlockState state) {
        return this.FACING_SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter reader, BlockPos pos, CollisionContext context) {
        switch (state.getValue(FACING)) {
            case NORTH:
                return getFacingShape(state);
            case SOUTH:
                return  ShapesUtil.rotate(getFacingShape(state), ShapesUtil.RotationDegree.D180);
            case EAST:
                return ShapesUtil.rotate(getFacingShape(state), ShapesUtil.RotationDegree.D270);
            case WEST:
                return ShapesUtil.rotate(getFacingShape(state), ShapesUtil.RotationDegree.D90);
        }
        return Shapes.block();
    }

    @Override
    public float getShadeBrightness(BlockState state, BlockGetter reader, BlockPos pos) {
        return 1.0F;
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }
}
