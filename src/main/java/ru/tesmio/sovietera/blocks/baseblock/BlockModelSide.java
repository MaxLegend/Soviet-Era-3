package ru.tesmio.sovietera.blocks.baseblock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import ru.tesmio.sovietera.utils.ShapesUtil;

public class BlockModelSide extends BlockSide implements SimpleWaterloggedBlock {

    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    private final float shadingInside;

    // ИСПРАВЛЕНИЕ: Убран модификатор static. Если оставить static, то все блоки,
    // унаследованные от этого класса, будут иметь форму последнего созданного блока!
    protected VoxelShape facingShape = ShapesUtil.FULL_CUBE;

    public BlockModelSide(Properties properties, float shadingInside) {
        super(properties);
        this.shadingInside = shadingInside;
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(WATERLOGGED, false));
    }

    public BlockModelSide(float shadingInside) {
        // ИСПРАВЛЕНИЕ: Материалы (Material) удалены в 1.20.1. Заменено на новые Properties.
        super(Properties.of()
                        .mapColor(MapColor.METAL)
                        .requiresCorrectToolForDrops() // Замена setRequiresTool
                        .strength(1f, 4f)             // Замена hardnessAndResistance
                        .noOcclusion()                // Замена notSolid (чтобы блоки за ним не становились невидимыми)
                        .sound(net.minecraft.world.level.block.SoundType.METAL));
        this.shadingInside = shadingInside;
    }

    public boolean isCustomDrop() {
        return true;
    }

    @Override
    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos) {
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        return super.updateShape(state, facing, facingState, level, currentPos, facingPos);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, WATERLOGGED);
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return !state.getValue(WATERLOGGED);
    }

    @Override
    public VoxelShape getShape(BlockState s, BlockGetter level, BlockPos pos, CollisionContext context) {
        switch (s.getValue(FACING)) {
            case NORTH: return getFacingShape(s);
            case SOUTH: return ShapesUtil.rotate(getFacingShape(s), ShapesUtil.RotationDegree.D180);
            case EAST:  return ShapesUtil.rotate(getFacingShape(s), ShapesUtil.RotationDegree.D270);
            case WEST:  return ShapesUtil.rotate(getFacingShape(s), ShapesUtil.RotationDegree.D90);
            default:    return ShapesUtil.FULL_CUBE;
        }
    }

    public VoxelShape getFacingShape(BlockState s) {
        return this.facingShape;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState s, BlockGetter level, BlockPos pos, CollisionContext context) {
        switch (s.getValue(FACING)) {
            case NORTH: return getFacingShape(s);
            case SOUTH: return ShapesUtil.rotate(getFacingShape(s), ShapesUtil.RotationDegree.D180);
            case EAST:  return ShapesUtil.rotate(getFacingShape(s), ShapesUtil.RotationDegree.D270);
            case WEST:  return ShapesUtil.rotate(getFacingShape(s), ShapesUtil.RotationDegree.D90);
            default:    return ShapesUtil.FULL_CUBE;
        }
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        FluidState fluidstate = context.getLevel().getFluidState(context.getClickedPos());
        return this.defaultBlockState()
                   .setValue(FACING, context.getHorizontalDirection().getOpposite())
                   .setValue(WATERLOGGED, fluidstate.getType() == Fluids.WATER);
    }

    // В 1.20.1 этот метод вызывается на обеих сторонах, @OnlyIn больше не нужен!
    @Override
    public float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return this.shadingInside;
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }
    }
