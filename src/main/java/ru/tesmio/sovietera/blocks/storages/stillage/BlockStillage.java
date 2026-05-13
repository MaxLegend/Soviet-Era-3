package ru.tesmio.sovietera.blocks.storages.stillage;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import ru.tesmio.sovietera.blocks.baseblock.BlockModelSideUD;
import ru.tesmio.sovietera.utils.ShapesUtil;

/**
 * Stillage shelf block — stores a single item without GUI.
 * <p>
 * Right-click with item in hand → place item on shelf.
 * Right-click with empty hand → extract item from shelf.
 */
public class BlockStillage extends BlockModelSideUD implements EntityBlock {

    final VoxelShape AABB = Block.box(0, 0, 0, 16, 1, 16);

    public BlockStillage(Properties properties, float shadingInside) {
        super(properties, shadingInside);
        this.registerDefaultState(this.defaultBlockState().setValue(PART, BlockModelSideUD.EnumPart.MIDDLE));
    }

    public VoxelShape getFacingShape(BlockState state) {
        if (state.getValue(PART) == EnumPart.UP) return AABB;
        return ShapesUtil.FULL_CUBE;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING,PART, WATERLOGGED);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new EntityBlockStillage(pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof EntityBlockStillage stillage)) return InteractionResult.PASS;

     //   if (level.isClientSide) return InteractionResult.SUCCESS;

        ItemStack heldItem = player.getItemInHand(hand);

        if (heldItem.isEmpty()) {
            // Empty hand → extract item from shelf
            ItemStack stored = stillage.getStoredItem();
            if (!stored.isEmpty()) {
                // Give item to player; if inventory is full, drop on ground
                if (!player.getInventory().add(stored.copy())) {
                    player.drop(stored.copy(), false);
                }
                stillage.setStoredItem(ItemStack.EMPTY);
                return InteractionResult.CONSUME;
            }
        } else {
            // Item in hand → place on shelf (only if shelf is empty)
            if (stillage.isEmpty()) {
                stillage.setStoredItem(new ItemStack(heldItem.getItem(), 1));
                if (!player.getAbilities().instabuild) {
                    heldItem.shrink(1);
                }
                return InteractionResult.CONSUME;
            }
        }

        return InteractionResult.PASS;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        FluidState fluidstate = context.getLevel().getFluidState(context.getClickedPos());
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();

        BlockState placementState = this.defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getOpposite())
                .setValue(WATERLOGGED, fluidstate.getType() == Fluids.WATER);

        if (level.getBlockState(pos.above()).getBlock() != this) {
            placementState = placementState.setValue(PART, BlockModelSideUD.EnumPart.UP);
        } else {
            placementState = placementState.setValue(PART, BlockModelSideUD.EnumPart.MIDDLE);
        }
        return placementState;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof Container container) {
                Containers.dropContents(level, pos, container);
                level.updateNeighbourForOutputSignal(pos, this);
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }
}
