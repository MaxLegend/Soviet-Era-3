package ru.tesmio.sovietera.blocks.devices.lamps;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import ru.tesmio.sovietera.blocks.baseblock.BaseBlock;
import ru.tesmio.sovietera.blocks.devices.devicesnetwork.INetworkNode;
import ru.tesmio.sovietera.core.BlockEntitiesSE;


import java.util.ArrayList;
import java.util.List;

/**
 * Абстрактный базовый блок для всех ламп мода.
 *
 * Обеспечивает:
 *   - Свойство POWERED (питание от сети) — лампа светится при POWERED=true
 *   - Свойство WATERLOGGED (размещение в воде)
 *   - Интеграцию с EntityBlockLamp (кабели, сеть, питание)
 *   - Очистку соединений при удалении блока
 *   - Обновление питания при изменении соседних блоков
 *
 * Подклассы определяют:
 *   - Свойство FACING (тип и значения)
 *   - Форму (getShape)
 *   - Размещение (getStateForPlacement)
 *   - Взаимодействие (use)
 */
public abstract class BlockLampBase extends BaseBlock implements EntityBlock, SimpleWaterloggedBlock {

    /** Лампа получает питание из сети → светит на максимум */
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    /** Размещение в воде */
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    protected BlockLampBase(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState()
                .setValue(POWERED, false)
                .setValue(WATERLOGGED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POWERED, WATERLOGGED);
    }

    // ===================== EntityBlock =====================

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new EntityBlockLamp(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) return null;
        return type == BlockEntitiesSE.ENTITY_BLOCK_LAMP.get()
                ? (lvl, pos, st, be) -> EntityBlockLamp.tick(lvl, pos, st, be)
                : null;
    }

    // ===================== Вода =====================

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    // ===================== Форма =====================

    /** Коллизия = визуальная форма для всех ламп */
    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return getShape(state, level, pos, context);
    }

    @Override
    public float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return 1.0F;
    }

    // ===================== Обновления =====================

    /**
     * При изменении соседнего блока — обновляем питание в сети.
     * Срабатывает, когда генератор рядом меняет состояние.
     */
    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (!level.isClientSide) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof EntityBlockLamp lamp) {
                lamp.updatePoweredInNetwork();
            }
        }
        super.neighborChanged(state, level, pos, block, fromPos, isMoving);
    }

    /**
     * При удалении блока — очищаем все соединения с обеих сторон.
     */
    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof EntityBlockLamp lamp) {
                // Сначала собираем чужие узлы
                List<INetworkNode> others = new ArrayList<>();
                for (BlockPos otherPos : new ArrayList<>(lamp.getConnections())) {
                    BlockEntity otherBe = level.getBlockEntity(otherPos);
                    INetworkNode otherNode = INetworkNode.from(otherBe);
                    if (otherNode != null) {
                        others.add(otherNode);
                    }
                }
                // Очищаем свои соединения
                lamp.clearConnections();
                // Удаляем себя из чужих списков
                for (INetworkNode other : others) {
                    other.removeConnection(pos);
                }
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    // ===================== Размещение =====================

    /**
     * Общая логика размещения: учитывает WATERLOGGED.
     * Подклассы должны вызывать super.getStateForPlacement() или
     * самостоятельно обрабатывать WATERLOGGED.
     */
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        FluidState fluidState = context.getLevel().getFluidState(context.getClickedPos());
        return this.defaultBlockState()
                .setValue(WATERLOGGED, fluidState.getType() == Fluids.WATER);
    }

    @Override
    public BlockState updateShape(BlockState state, net.minecraft.core.Direction direction,
                                   BlockState neighborState, net.minecraft.world.level.LevelAccessor level,
                                   BlockPos currentPos, BlockPos neighborPos) {
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        return super.updateShape(state, direction, neighborState, level, currentPos, neighborPos);
    }
}
