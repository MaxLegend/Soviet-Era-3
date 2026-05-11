package ru.tesmio.sovietera.blocks.baseblock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.stats.Stats;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import org.jetbrains.annotations.Nullable;

public class BlockForFacing extends BaseBlock {
    public static final EnumProperty<EnumOrientation> FACING = EnumProperty.create("facing", EnumOrientation.class);

    public BlockForFacing(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, EnumOrientation.NORTH));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        for (Direction direction : context.getNearestLookingDirections()) {
            if (direction.getAxis() == Direction.Axis.Y) {
                return this.defaultBlockState().setValue(FACING, EnumOrientation.forFacing(direction, context.getHorizontalDirection()));
            } else {
                return this.defaultBlockState().setValue(FACING, EnumOrientation.forFacing(direction, direction));
            }
        }
        return this.defaultBlockState();
    }

    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack tool) {
        if (this.disableJSONDrop()) {
            if (!level.isClientSide) {
                if (!player.isCreative()) {
                    getDropsWithBlock(level, pos, player);
                    getAdditionDrops(level, pos, getStackAddDrop(player));
                }
            }
        } else {
            player.awardStat(Stats.BLOCK_MINED.get(this));
            player.causeFoodExhaustion(0.005F);
            Block.dropResources(state, level, pos, blockEntity, player, tool);
        }
    }

    public boolean disableJSONDrop() {
        return true;
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
    public BlockState rotate(BlockState state, Rotation rot) {
        switch (rot) {
            case CLOCKWISE_180:
                switch (state.getValue(FACING)) {
                    case EAST:
                        return state.setValue(FACING, EnumOrientation.WEST);
                    case WEST:
                        return state.setValue(FACING, EnumOrientation.EAST);
                    case SOUTH:
                        return state.setValue(FACING, EnumOrientation.NORTH);
                    case NORTH:
                        return state.setValue(FACING, EnumOrientation.SOUTH);
                    default:
                        return state;
                }
            case COUNTERCLOCKWISE_90:
                switch (state.getValue(FACING)) {
                    case EAST:
                        return state.setValue(FACING, EnumOrientation.NORTH);
                    case WEST:
                        return state.setValue(FACING, EnumOrientation.SOUTH);
                    case SOUTH:
                        return state.setValue(FACING, EnumOrientation.EAST);
                    case NORTH:
                        return state.setValue(FACING, EnumOrientation.WEST);
                    case UP:
                        return state.setValue(FACING, EnumOrientation.UP);
                    case DOWN:
                        return state.setValue(FACING, EnumOrientation.DOWN);
                }
            case CLOCKWISE_90:
                switch (state.getValue(FACING)) {
                    case EAST:
                        return state.setValue(FACING, EnumOrientation.SOUTH);
                    case WEST:
                        return state.setValue(FACING, EnumOrientation.NORTH);
                    case SOUTH:
                        return state.setValue(FACING, EnumOrientation.WEST);
                    case NORTH:
                        return state.setValue(FACING, EnumOrientation.EAST);
                    case UP:
                        return state.setValue(FACING, EnumOrientation.UP);
                    case DOWN:
                        return state.setValue(FACING, EnumOrientation.DOWN);
                }
            default:
                return state;
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    public enum EnumOrientation implements StringRepresentable {
        EAST("east", Direction.EAST),
        WEST("west", Direction.WEST),
        SOUTH("south", Direction.SOUTH),
        NORTH("north", Direction.NORTH),
        UP("up", Direction.UP),
        DOWN("down", Direction.DOWN);

        private final String name;
        private final Direction dir;

        EnumOrientation(String name, Direction dir) {
            this.name = name;
            this.dir = dir;
        }

        public static EnumOrientation forFacing(Direction clickedSide, Direction entityFacing) {
            switch (clickedSide) {
                case DOWN:
                    return DOWN;
                case UP:
                    return UP;
                case NORTH:
                    return NORTH;
                case SOUTH:
                    return SOUTH;
                case WEST:
                    return WEST;
                case EAST:
                    return EAST;
                default:
                    throw new IllegalArgumentException("Invalid facing: " + clickedSide);
            }
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        public Direction getDirection() {
            return this.dir;
        }
    }
}