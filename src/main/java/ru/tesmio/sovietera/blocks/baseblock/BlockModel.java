package ru.tesmio.sovietera.blocks.baseblock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class BlockModel extends BaseBlock {

    protected VoxelShape shape;
    private final float shadingInside;

    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public BlockModel(Properties properties, VoxelShape s, float shadingInside) {
        super(properties);
        this.shape = s;
        this.shadingInside = shadingInside;
        this.registerDefaultState(this.stateDefinition.any().setValue(WATERLOGGED, false));
    }

    public BlockModel(Properties properties) {
        super(properties);
        this.shadingInside = 1.0F;
        // Не забудьте задать дефолтное состояние и здесь, если блок поддерживает WATERLOGGED
        this.registerDefaultState(this.stateDefinition.any().setValue(WATERLOGGED, false));
    }

    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack tool) {
        if (!level.isClientSide) {
            if (!player.isCreative()) {
                getDropsWithBlock(level, pos, player);
                getAdditionDrops(level, pos, getStackAddDrop(player));
            }
        }
    }

    public ItemStack getStackAddDrop(Player player) {
        return ItemStack.EMPTY;
    }

    public void getAdditionDrops(Level level, BlockPos pos, ItemStack stack) {
        // spawnAsEntity переименован в popResource
        Block.popResource(level, pos, stack);
    }

    public ItemStack[] getItemsDrop(Player player) {
        return new ItemStack[] { ItemStack.EMPTY };
    }

    protected void getDropsWithBlock(Level level, BlockPos pos, Player player) {
        for (ItemStack stack : getItemsDrop(player)) {
            Block.popResource(level, pos, stack);
        }
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return this.shape;
    }

    // В 1.20.1 этот метод вызывается на обеих сторонах, @OnlyIn больше не нужен
    @Override
    public float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return this.shadingInside;
    }

    // Аналог onPlayerDestroy из 1.16.5
    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        // Пустое тело, как было в оригинале
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(WATERLOGGED);
    }

    // =======================================================================
    // Horizontal Rotate
    // =======================================================================
    public class HorizontalRotate extends BlockModel {

        boolean isCustomDrop = false;
        public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

        public HorizontalRotate(Properties properties, VoxelShape s, float shadingInside) {
            super(properties, s, shadingInside);
        }


        public HorizontalRotate(Properties properties) {
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
                // Если кастомного дропа нет, вызываем ванильную логику (которая сама спавнит дроп, стат и усталость)
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
            return new ItemStack[] { ItemStack.EMPTY };
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


}


