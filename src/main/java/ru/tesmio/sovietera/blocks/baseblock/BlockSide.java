package ru.tesmio.sovietera.blocks.baseblock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import org.jetbrains.annotations.Nullable;

public class BlockSide extends BaseBlock {

    boolean isCustomDrop = false;
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    public BlockSide(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    public boolean isCustomDrop() {
        return isCustomDrop;
    }

    @Override
    public BlockState rotate(BlockState state, net.minecraft.world.level.block.Rotation rot) {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, net.minecraft.world.level.block.Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack tool) {
        if (isCustomDrop()) {
            if (!level.isClientSide) {
                if (!player.isCreative()) {
                    getDropsWithBlock(level, pos, player);
                    getAdditionDrops(level, pos, getStackAddDrop(player));
                }
            }
            player.awardStat(Stats.BLOCK_MINED.get(this));
            player.causeFoodExhaustion(0.005F);
        } else {
            super.playerDestroy(level, player, pos, state, blockEntity, tool);
        }

    }

    public ItemStack getStackAddDrop(Player player) {
        return ItemStack.EMPTY;
    }

    public void getAdditionDrops(Level level, BlockPos pos, ItemStack stack) {
        Block.popResource(level, pos, stack);
    }

    public ItemStack[] getItemsDrop(Player player) {
        return new ItemStack[]{ItemStack.EMPTY};
    }

    protected void getDropsWithBlock(Level level, BlockPos pos, Player player) {
        for (ItemStack stack : getItemsDrop(player)) {
            Block.popResource(level, pos, stack);
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }
}