package ru.tesmio.sovietera.blocks.devices.cable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import ru.tesmio.sovietera.blocks.baseblock.BaseBlock;
import ru.tesmio.sovietera.blocks.devices.devicesnetwork.INetworkNode;
import ru.tesmio.sovietera.core.BlockEntitiesSE;
import ru.tesmio.sovietera.utils.ShapesUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Блок электроконнектора — узел силовой сети.
 *
 * Размещается на любой стороне блока (стена/пол/потолок).
 * Имеет два свойства:
 *   - ATTACHED_FACE — к какой стороне прикреплён
 *   - POWERED — есть ли питание в сети
 *
 * При изменении соседних блоков обновляет питание в сети.
 * При удалении — корректно очищает все соединения.
 */
public class BlockPowerConnector extends BaseBlock implements EntityBlock {

    private static final VoxelShape SHAPE = Shapes.or(
            Block.box(7.75, 8, 14.5, 8.25, 8.75, 15),       // элемент 0: тонкий стержень
            Block.box(6.75, 8, 13.5, 9.25, 10.6, 16),         // элемент 1: основное тело
            Block.box(7, 10.6, 13.75, 9, 11, 15.75),           // элемент 2: верхняя крышка
            Block.box(7.5, 7, 14.5, 8.5, 8, 16)                // элемент 3: длинный стержень (обрезан на границе блока)
    );
    private static final VoxelShape SHAPE_UP = Shapes.or(
            Block.box(6.75, 1, 6.75, 9.25, 3.6, 9.25),    // Основное тело
            Block.box(7, 3.6, 7, 9, 4, 9),               // Верхняя крышка
            Block.box(7.5, 0, 7.5, 8.5, 2, 8.5)          // Стержень (обрезан до границы блока)
    );
    private static final VoxelShape SHAPE_DOWN = Shapes.or(
            Block.box(6.75, 12.4, 6.75, 9.25, 15, 9.25),  // Тело
            Block.box(7, 12, 7, 9, 12.4, 9),             // Крышка
            Block.box(7.5, 14, 7.5, 8.5, 16, 8.5)        // Стержень
    );
    public static final DirectionProperty ATTACHED_FACE = BlockStateProperties.FACING;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final BooleanProperty BUNDLED = BooleanProperty.create("bundled");

    public BlockPowerConnector() {
        super(BlockBehaviour.Properties
                .of()
                .strength(2.5F, 2.0F)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops()
                .noOcclusion());
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(ATTACHED_FACE, Direction.NORTH)
                .setValue(POWERED, false).setValue(BUNDLED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ATTACHED_FACE, POWERED, BUNDLED);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new EntityBlockPowerConnector(pos, state);
    }
    @Override
    public float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return 1.0F;
    }
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(ATTACHED_FACE, context.getClickedFace());
    }
    /**
     * Спавнит электрические искры на точках присоединения проводов,
     * когда коннектор под напряжением (POWERED=true и BUNDLED=true).
     */
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (state.getValue(POWERED) && state.getValue(BUNDLED)) {
            Direction facing = state.getValue(ATTACHED_FACE);

            // Получаем точку крепления из рендерера
            Vec3 offset = RendererPowerCable.getConnectionOffset(facing);

            double x = pos.getX() + offset.x;
            double y = pos.getY() + offset.y;
            double z = pos.getZ() + offset.z;

            // Основная искра — ~15% шанс за тик
            if (random.nextDouble() < 0.15D) {
                level.addParticle(
                        ParticleTypes.ELECTRIC_SPARK,
                        x + (random.nextDouble() - 0.5D) * 0.15D,
                        y + (random.nextDouble() - 0.5D) * 0.15D,
                        z + (random.nextDouble() - 0.5D) * 0.15D,
                        (random.nextDouble() - 0.5D) * 0.05D,  // небольшое горизонтальное движение
                        random.nextDouble() * 0.05D + 0.01D,   // подъём вверх
                        (random.nextDouble() - 0.5D) * 0.05D
                );
            }

            // Дополнительный мелкий.spark — ~5% шанс (более частый при нескольких проводах)
            if (random.nextDouble() < 0.05D) {
                level.addParticle(
                        ParticleTypes.END_ROD,
                        x + (random.nextDouble() - 0.5D) * 0.1D,
                        y + (random.nextDouble() - 0.5D) * 0.1D,
                        z + (random.nextDouble() - 0.5D) * 0.1D,
                        0.0D,
                        random.nextDouble() * 0.02D,
                        0.0D
                );
            }
        }
    }
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(ATTACHED_FACE)) {
            case NORTH -> SHAPE;
            case SOUTH -> ShapesUtil.rotate(SHAPE, ShapesUtil.RotationDegree.D180);
            case EAST  -> ShapesUtil.rotate(SHAPE, ShapesUtil.RotationDegree.D270);
            case WEST  -> ShapesUtil.rotate(SHAPE, ShapesUtil.RotationDegree.D90);
            case UP    -> SHAPE_UP;
            case DOWN  -> SHAPE_DOWN;
        };
    }

    /**
     * При изменении соседнего блока — обновляем питание в сети.
     * Это срабатывает, когда генератор рядом меняет своё состояние POWERED.
     */
    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (!level.isClientSide) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof EntityBlockPowerConnector connector) {
                connector.updatePoweredInNetwork();
            }
        }
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) return null;
        return type == BlockEntitiesSE.ENTITY_BLOCK_POWER_CONNECTOR.get()
                ? (lvl, pos, st, be) -> EntityBlockPowerConnector.tick(lvl, pos, st, be)
                : null;
    }

    /**
     * При удалении блока — очищаем все соединения с обоих сторон.
     */
    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof EntityBlockPowerConnector connector) {
                // Собираем ВСЕ узлы (коннекторы И лампы) через INetworkNode
                List<INetworkNode> others = new ArrayList<>();
                for (BlockPos otherPos : new ArrayList<>(connector.getConnections())) {
                    BlockEntity otherBe = level.getBlockEntity(otherPos);
                    INetworkNode otherNode = INetworkNode.from(otherBe);
                    if (otherNode != null) {
                        others.add(otherNode);
                    }
                }
                connector.clearConnections();
                for (INetworkNode other : others) {
                    other.removeConnection(pos);
                }
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }
}
