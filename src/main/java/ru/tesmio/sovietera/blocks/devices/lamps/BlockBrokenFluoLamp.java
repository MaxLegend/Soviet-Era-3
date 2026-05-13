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
 * Сломанная люминесцентная лампа (тип 1).
 *
 * Получается разборкой рабочей лампы (BlockFluoLamp) инструментом PULLER.
 * Можно починить предметом FLUOLAMP (2 шт.).
 * Не излучает свет даже при POWERED=true (лампа разбита).
 * Всё ещё выступает как коннектор силовой сети (проводка цела).
 */
public class BlockBrokenFluoLamp extends BlockLampBase {

    public static final EnumProperty<BaseEnumOrientation> FACING =
            EnumProperty.create("facing", BaseEnumOrientation.class);

    VoxelShape BOXS[] = new VoxelShape[] {
            Block.box(0, 0, 5, 16, 2, 11),       // up_x  (было 16,0,11,0,2,5)
            Block.box(5, 0, 0, 11, 2, 16),       // up_z  (было 11,0,16,5,2,0)
            Block.box(5, 14, 0, 11, 16, 16),     // down_z (было 11,16,16,5,14,0)
            Block.box(0, 14, 5, 16, 16, 11),     // down_x (было 16,16,11,0,14,5)
            Block.box(0, 5, 0, 16, 11, 2),       // north
            Block.box(0, 5, 14, 16, 11, 16),     // south
            Block.box(14, 5, 0, 16, 11, 16),     // east
            Block.box(0, 5, 0, 2, 11, 16)        // west
    };

    public BlockBrokenFluoLamp(BlockBehaviour.Properties properties) {
        // Сломанная лампа НИКОГДА не излучает свет, даже если POWERED=true
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

        // Починка: 2× FLUOLAMP → рабочая лампа
        // TODO: Заменить при регистрации предметов
        // if (heldItem.is(ItemsSE.FLUOLAMP.get()) && heldItem.getCount() >= 2) {
        //     level.setBlock(pos, BlocksSE.FLUO_LAMP.get().defaultBlockState()
        //             .setValue(BlockFluoLamp.FACING, state.getValue(FACING))
        //             .setValue(BlockFluoLamp.CLOSED, false)
        //             .setValue(WATERLOGGED, state.getValue(WATERLOGGED)), 3);
        //     if (!player.isCreative()) heldItem.shrink(2);
        //     return InteractionResult.sidedSuccess(level.isClientSide);
        // }

        return InteractionResult.PASS;
    }
}
