package ru.tesmio.sovietera.blocks.baseblock;


import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class BlockModelSideUD extends BlockModelSide {

    public static final EnumProperty<EnumPart> PART = EnumProperty.create("part", EnumPart.class);
    // Assuming FACING and WATERLOGGED are defined in BlockModelSide parent.
    // If not, declare them here:
    // public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public BlockModelSideUD(Properties properties, float shadingInside) {
        super(properties, shadingInside);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        var fluidState = level.getFluidState(pos);

        BlockState placementState = this.defaultBlockState()
                                        .setValue(FACING, context.getHorizontalDirection().getOpposite())
                                        .setValue(WATERLOGGED, fluidState.getType() == Fluids.WATER);

        // If block below is solid → attach DOWN
        if (level.getBlockState(pos.below()).isSolidRender(level, pos.below())) {
            return placementState.setValue(PART, EnumPart.DOWN);
        }
        // If block above is not the same type → UP (top piece)
        if (level.getBlockState(pos.above()).getBlock() != this) {
            return placementState.setValue(PART, EnumPart.UP);
        }

        return placementState.setValue(PART, EnumPart.MIDDLE);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, PART, WATERLOGGED);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState state2, LevelAccessor levelAccessor, BlockPos pos1, BlockPos pos2) {

        if (levelAccessor instanceof WorldGenRegion) return state;
        return updateState(levelAccessor, pos1, state);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos,
            CollisionContext context) {
        return this.getShape(state, level, pos, context);
    }

    /**
     * Recalculates PART based on neighbours:
     * - Solid below → DOWN (attached to floor)
     * - BlockModelSideUD below + BlockModelSideUD above → MIDDLE
     * - BlockModelSideUD below but no same block above → UP
     */
    public BlockState updateState(LevelReader level, BlockPos pos, BlockState state) {
        if (level instanceof Level && !((Level) level).isClientSide()) {
            BlockState current = level.getBlockState(pos);
            BlockState above = level.getBlockState(pos.above());
            BlockState below = level.getBlockState(pos.below());

            if (below.isSolidRender(level, pos.below())) {
                return current.setValue(PART, EnumPart.DOWN);
            }
            if (below.getBlock() instanceof BlockModelSideUD) {
                if (above.getBlock() instanceof BlockModelSideUD) {
                    return current.setValue(PART, EnumPart.MIDDLE);
                }
                // Was UP → stay UP; otherwise also UP (top of stack)
                return current.setValue(PART, EnumPart.UP);
            }
        }
        return state;
    }

    public enum EnumPart implements StringRepresentable {
        DOWN("down"),
        MIDDLE("mid"),
        UP("up");

        private final String serializedName;

        EnumPart(String serializedName) {
            this.serializedName = serializedName;
        }

        @Override
        public String getSerializedName() {
            return this.serializedName;
        }
    }
}
