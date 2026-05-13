package ru.tesmio.sovietera.blocks.devices.lamps;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import ru.tesmio.sovietera.blocks.devices.lamps.BlockLampBase;
import ru.tesmio.sovietera.utils.ShapesUtil;

/**
 * Уличный фонарь.
 *
 * Размещается только на горизонтальных поверхностях (4 направления).
 * Питание — от силовой сети. Форма — высокая панель.
 */
public class BlockStreetLamp extends BlockLampBase {

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    // Базовая форма: фонарь面向 север
    // Тонкая панель (7 пикселей глубиной) по центру блока, почти на всю высоту
    private static final VoxelShape SHAPE_NORTH = Block.box(4.5, 0, 0, 11.5, 15.75, 16);

    // Для востока/запата — поворот на 90° вокруг Y
    // (Результат D90 и D270 одинаков из-за симметрии формы относительно X=8)
    private static final VoxelShape SHAPE_EAST = ShapesUtil.rotate(SHAPE_NORTH, ShapesUtil.RotationDegree.D90);

    public BlockStreetLamp(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState()
                .setValue(FACING, Direction.NORTH)
                .setValue(POWERED, false)
                .setValue(WATERLOGGED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getOpposite())
                .setValue(WATERLOGGED, context.getLevel().getFluidState(context.getClickedPos())
                        .getType() == net.minecraft.world.level.material.Fluids.WATER);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case NORTH, SOUTH -> SHAPE_NORTH;
            case EAST, WEST   -> SHAPE_EAST;
            default -> SHAPE_NORTH;
        };
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, net.minecraft.world.phys.BlockHitResult hit) {
        if (player.isCreative() && player.getItemInHand(hand).isEmpty()) {
            level.setBlock(pos, state.cycle(POWERED), 3);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }
}
