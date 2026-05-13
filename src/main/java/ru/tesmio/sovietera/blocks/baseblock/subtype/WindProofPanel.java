package ru.tesmio.sovietera.blocks.baseblock.subtype;


import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import ru.tesmio.sovietera.blocks.baseblock.BlockModelSide;
import ru.tesmio.sovietera.utils.ShapesUtil;


import javax.annotation.Nullable;
import java.util.List;

public class WindProofPanel extends BlockModelSide {
    String info;

    public WindProofPanel(Properties properties, float shadingInside, String info) {
        super(properties, shadingInside);
        this.info = info;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable(info));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        var fluidstate = context.getLevel().getFluidState(context.getClickedPos());
        return this.defaultBlockState()
                   .setValue(FACING, context.getHorizontalDirection().getOpposite())
                   .setValue(WATERLOGGED, fluidstate.getType() == Fluids.WATER);
    }

    // Компоненты формы для ориентации EAST
    private static final VoxelShape SHAPE_COL = Block.box(0, 0, 4, 4, 16, 12);
    private static final VoxelShape SHAPE_PANEL = Block.box(0, 0, 6, 16, 16, 10);

    // Предрасчитанные формы для каждого направления (базовая — EAST)
    private static final VoxelShape SHAPE_EAST  = Shapes.or(SHAPE_COL, SHAPE_PANEL);
    private static final VoxelShape SHAPE_WEST  = ShapesUtil.rotate(SHAPE_EAST, ShapesUtil.RotationDegree.D180);
    private static final VoxelShape SHAPE_NORTH = ShapesUtil.rotate(SHAPE_EAST, ShapesUtil.RotationDegree.D90);
    private static final VoxelShape SHAPE_SOUTH = ShapesUtil.rotate(SHAPE_EAST, ShapesUtil.RotationDegree.D270);

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case EAST  -> SHAPE_EAST;
            case WEST  -> SHAPE_WEST;
            case NORTH -> SHAPE_NORTH;
            case SOUTH -> SHAPE_SOUTH;
            default    -> ShapesUtil.FULL_CUBE;
        };
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case EAST  -> SHAPE_EAST;
            case WEST  -> SHAPE_WEST;
            case NORTH -> SHAPE_NORTH;
            case SOUTH -> SHAPE_SOUTH;
            default    -> ShapesUtil.FULL_CUBE;
        };
    }
}