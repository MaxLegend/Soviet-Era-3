package ru.tesmio.sovietera.blocks.devices.lamps;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import ru.tesmio.sovietera.utils.ShapesUtil;

/**
 * Лампа накаливания.
 *
 * Компактная лампочка, размещаемая на любой стороне блока (6 направлений).
 * Питание — от силовой сети. Форма определяется из одной базовой AABB
 * и вращается через ShapesUtil.
 */
public class BlockIncLamp extends BlockLampBase {

    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    final   VoxelShape[] SHAPES = new VoxelShape[] {
            Shapes.create(0.31D, 0.31D, 0.55D, 0.69D, 0.69D, 1D),
            Shapes.create(0.31D, 0.31D, 0D, 0.69D, 0.69D, 0.45D),
            Shapes.create(0.45D, 0.31D, 0.31D, 0D, 0.69D, 0.69D),
            Shapes.create(0.55D, 0.31D, 0.31D, 1D, 0.69D, 0.69D),
            Shapes.create(0.31D, 0D, 0.31D, 0.69D, 0.45D, 0.69D),
            Shapes.create(0.31D, 0.55D, 0.31D, 0.69D, 1D, 0.69D)
    };
    public BlockIncLamp(BlockBehaviour.Properties properties) {
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
                .setValue(FACING, context.getClickedFace())
                .setValue(WATERLOGGED, context.getLevel().getFluidState(context.getClickedPos())
                        .getType() == net.minecraft.world.level.material.Fluids.WATER);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case SOUTH -> SHAPES[1];
            case NORTH -> SHAPES[0];
            case WEST  -> SHAPES[3];
            case EAST  -> SHAPES[2];
            case DOWN    -> SHAPES[5];
            case UP  -> SHAPES[4];
        };
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, net.minecraft.world.phys.BlockHitResult hit) {
        // В творческом: переключаем POWERED
        if (player.isCreative() && player.getItemInHand(hand).isEmpty()) {
            level.setBlock(pos, state.cycle(POWERED), 3);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }
}
