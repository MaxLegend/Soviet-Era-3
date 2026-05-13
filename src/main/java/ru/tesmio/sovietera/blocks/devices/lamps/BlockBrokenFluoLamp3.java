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
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import ru.tesmio.sovietera.utils.BaseEnumOrientation;
import ru.tesmio.sovietera.utils.ShapesUtil;

/**
 * Сломанная люминесцентная лампа (тип 3).
 * Не излучает свет. Починка: 1× FLUOLAMP.
 */
public class BlockBrokenFluoLamp3 extends BlockLampBase {

    public static final EnumProperty<BaseEnumOrientation> FACING =
            EnumProperty.create("facing", BaseEnumOrientation.class);

    final VoxelShape BOXS[] = new VoxelShape[] {
            Block.box(0D, 0D, 5.5D, 16D, 3.2D, 10.5D),
            Block.box(5.5D, 0D, 0D, 10.5D, 3.2D, 16D),
            Block.box(5.5D, 12.8D, 0D, 10.5D, 16D, 16D),
            Block.box(0D, 12.8D, 5.5D, 16D, 16D, 10.5D),
            Block.box(0D, 5.5D, 0D, 16D, 10.5D, 3.2D),
            Block.box(0D, 5D, 12.8D, 16D, 10.5D, 16D),
            Block.box(12.8D, 5.5D, 0D, 16D, 10.5D, 16D),
            Block.box(0D, 5.5D, 0D, 3.2D, 10.5D, 16D)
    };
    public BlockBrokenFluoLamp3(BlockBehaviour.Properties properties) {
        super(properties.lightLevel(state -> 0));
        this.registerDefaultState(this.defaultBlockState()
                .setValue(FACING, BaseEnumOrientation.NORTH)
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
        FluidState fluidState = context.getLevel().getFluidState(context.getClickedPos());
        Direction clickedFace = context.getClickedFace();
        Direction horizontalFacing = context.getHorizontalDirection();

        return this.defaultBlockState()
                   .setValue(FACING, BaseEnumOrientation.forFacing(clickedFace.getOpposite(), horizontalFacing))
                   .setValue(WATERLOGGED, fluidState.getType() == net.minecraft.world.level.material.Fluids.WATER);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case NORTH  -> BOXS[4];
            case SOUTH  -> BOXS[5];
            case EAST   -> BOXS[6];
            case WEST   -> BOXS[7];
            case UP_X   -> BOXS[3];
            case UP_Z   -> BOXS[2];
            case DOWN_X -> BOXS[0];
            case DOWN_Z -> BOXS[1];
        };
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, net.minecraft.world.phys.BlockHitResult hit) {
        ItemStack heldItem = player.getItemInHand(hand);

        // Починка: 1× FLUOLAMP → рабочая лампа типа 3
        // TODO: Заменить при регистрации предметов
        // if (heldItem.is(ItemsSE.FLUOLAMP.get()) && heldItem.getCount() >= 1) {
        //     level.setBlock(pos, BlocksSE.FLUO_LAMP3.get().defaultBlockState()
        //             .setValue(BlockFluoLamp3.FACING, state.getValue(FACING))
        //             .setValue(WATERLOGGED, state.getValue(WATERLOGGED)), 3);
        //     if (!player.isCreative()) heldItem.shrink(1);
        //     return InteractionResult.sidedSuccess(level.isClientSide);
        // }

        return InteractionResult.PASS;
    }
}
