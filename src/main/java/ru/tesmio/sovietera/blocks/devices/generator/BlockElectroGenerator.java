package ru.tesmio.sovietera.blocks.devices.generator;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;
import ru.tesmio.sovietera.blocks.baseblock.BlockModelSide;
import ru.tesmio.sovietera.core.BlockEntitiesSE;
import ru.tesmio.sovietera.core.BlocksSE;
import ru.tesmio.sovietera.utils.ShapesUtil;

/**
 * Главный блок дизельного электрогенератора.
 * Имеет BlockEntity с FluidTank и переменными isPowered / isActivated.
 * Поддерживает заливку воды из ведра через ПКМ.
 * Мультиблок: работает только если рядом стоят BlockDieselTank и BlockDieselElectroGenerator
 * в правильной ориентации.
 *
 * Управление:
 *   - ПКМ с ведром воды → заливка топлива
 *   - Shift + ПКМ (мультблок собран) → включить/выключить генератор
 *   - ПКМ без Shift (мультблок собран) → открыть контейнер GUI
 */
public class BlockElectroGenerator extends BlockModelSide implements EntityBlock {

    private static final VoxelShape AABB = Shapes.or(
            Block.box( 2D,    0D, 0D,   14D,  1.0D, 14D),
            Block.box( 4.25D, 1D, 1D,   12D,  3.0D, 12.5D),
            Block.box( 3D,    4D, 1D,   13D,  9.0D, 15D),
            Block.box( 4D,    9D, 0D,   12D, 11.25D, 16D),
            Block.box( 5.75D, 11.75D, 0D, 10.25D, 13.5D, 16D),
            Block.box( 6.75D, 11.75D, 0D,  9.25D, 14.5D, 16D));


    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    public BlockElectroGenerator(Properties properties, float shadingInside) {
        super(properties, shadingInside);
        this.registerDefaultState(this.defaultBlockState()
                                      .setValue(FACING, Direction.NORTH)
                                      .setValue(WATERLOGGED, false)
                                      .setValue(POWERED, false));
    }

    public VoxelShape getFacingShape(BlockState s) {
        return ShapesUtil.rotate(AABB, ShapesUtil.RotationDegree.D180);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, WATERLOGGED, POWERED);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityElectroGenerator(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> net.minecraft.world.level.block.entity.BlockEntityTicker<T> getTicker(Level level, BlockState state, net.minecraft.world.level.block.entity.BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return null;
        }
        if (type == BlockEntitiesSE.ELECTRO_GENERATOR.get()) {
            return (lvl, pos, st, be) -> BlockEntityElectroGenerator.serverTick(lvl, pos, st, (BlockEntityElectroGenerator) be);
        }
        return null;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;

        ItemStack heldItem = player.getItemInHand(hand);

        // Проверяем, держит ли игрок ведро воды — заливка топлива
        if (heldItem.is(Items.WATER_BUCKET)) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof BlockEntityElectroGenerator generator) {
                return generator.getCapability(ForgeCapabilities.FLUID_HANDLER, null)
                                .map(fluidHandler -> {
                                    // Заливаем 1000mB воды из ведра
                                    int filled = fluidHandler.fill(new FluidStack(Fluids.WATER, 1000),
                                            IFluidHandler.FluidAction.EXECUTE);
                                    if (filled > 0) {
                                        // Заменяем ведро воды на пустое ведро
                                        if (!player.isCreative()) {
                                            player.setItemInHand(hand, new ItemStack(Items.BUCKET));
                                        }
                                        return InteractionResult.CONSUME;
                                    }
                                    return InteractionResult.PASS;
                                })
                                .orElse(InteractionResult.PASS);
            }
        }

        // Временно убрана проверка мультблока для тестирования
        // TODO: Вернуть if (isMultiblockFormed(level, pos, state)) после отладки
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof BlockEntityElectroGenerator generator) {

            // Shift + ПКМ → включить/выключить генератор
            if (player.isShiftKeyDown()) {
                generator.toggleActivated();

                // Отправляем игроку сообщение о текущем состоянии
                if (generator.isActivated()) {
                    player.displayClientMessage(
                            Component.translatable("container.soviet.electro_generator.activated"), true);
                } else {
                    player.displayClientMessage(
                            Component.translatable("container.soviet.electro_generator.deactivated"), true);
                }

                return InteractionResult.CONSUME;
            }

            // Обычный ПКМ без Shift → открыть контейнер (GUI)
            MenuProvider containerProvider = new MenuProvider() {
                @Override
                public Component getDisplayName() {
                    return Component.translatable("container.soviet.electro_generator");
                }

                @Nullable
                @Override
                public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player playerEntity) {
                    return new ElectroGeneratorContainer(containerId, playerInventory, be);
                }
            };
            NetworkHooks.openScreen((ServerPlayer) player, containerProvider, pos);
            return InteractionResult.CONSUME;
        }

        return InteractionResult.PASS;
    }
    @Override
    public float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return 1.0F;
    }
    /**
     * Спавнит частицы дыма, когда генератор работает (POWERED=true).
     * Вызывается на клиенте каждый тик с случайной вероятностью.
     */
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (state.getValue(POWERED)) {
            Direction facing = state.getValue(FACING);

            // Дым идёт из выхлопной трубы — сверху блока, смещён в сторону facing
            double baseX = pos.getX() + 0.5D;
            double baseY = pos.getY() + 0.9D;  // чуть ниже верха для визуального эффекта
            double baseZ = pos.getZ() + 0.5D;

            // Смещение в сторону facing (выхлоп направлен в сторону фасада)
            double offsetX = facing.getStepX() * 0.4D;
            double offsetZ = facing.getStepZ() * 0.4D;

            // Спавним крупный дым (основной выхлоп)
            level.addParticle(
                    ParticleTypes.LARGE_SMOKE,
                    baseX + offsetX + (random.nextDouble() - 0.5D) * 0.15D,
                    baseY + random.nextDouble() * 0.1D,
                    baseZ + offsetZ + (random.nextDouble() - 0.5D) * 0.15D,
                    0.0D,                          // velocityX — нет горизонтального ветра
                    0.04D + random.nextDouble() * 0.03D,  // velocityY — подъём вверх
                    0.0D                           // velocityZ
            );

            // С вероятностью ~30% добавляем второй, более лёгкий дымок
            if (random.nextDouble() < 0.3D) {
                level.addParticle(
                        ParticleTypes.SMOKE,
                        baseX + offsetX + (random.nextDouble() - 0.5D) * 0.25D,
                        baseY + 0.15D + random.nextDouble() * 0.1D,
                        baseZ + offsetZ + (random.nextDouble() - 0.5D) * 0.25D,
                        (random.nextDouble() - 0.5D) * 0.01D,
                        0.02D + random.nextDouble() * 0.02D,
                        (random.nextDouble() - 0.5D) * 0.01D
                );
            }
        }
    }

    /**
     * Проверяет, собран ли мультиблок из 3 блоков:
     * BlockDieselTank + BlockElectroGenerator + BlockDieselElectroGenerator
     * в ряд, все смотрят в одном направлении.
     */
    public static boolean isMultiblockFormed(Level level, BlockPos pos, BlockState state) {
        Direction facing = state.getValue(FACING);

        // Определяем позиции соседних блоков в зависимости от facing
        // Для NORTH: бак на WEST, электрогенератор на EAST
        // Для SOUTH: бак на EAST, электрогенератор на WEST
        // Для EAST: бак на NORTH, электрогенератор на SOUTH
        // Для WEST: бак на SOUTH, электрогенератор на NORTH
        Direction tankDir = facing.getCounterClockWise();
        Direction electroDir = facing.getClockWise();

        BlockPos tankPos = pos.relative(tankDir);
        BlockPos electroPos = pos.relative(electroDir);

        BlockState tankState = level.getBlockState(tankPos);
        BlockState electroState = level.getBlockState(electroPos);

        boolean tankCorrect = tankState.getBlock() == BlocksSE.DIESEL_TANK.get()
                && tankState.hasProperty(BlockStateProperties.HORIZONTAL_FACING)
                && tankState.getValue(BlockStateProperties.HORIZONTAL_FACING) == facing;

        boolean electroCorrect = electroState.getBlock() == BlocksSE.DIESEL_ELECTRO_GENERATOR.get()
                && electroState.hasProperty(BlockStateProperties.HORIZONTAL_FACING)
                && electroState.getValue(BlockStateProperties.HORIZONTAL_FACING) == facing;

        return tankCorrect && electroCorrect;
    }

    @Override
    public void onRemove(BlockState oldState, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!oldState.is(newState.getBlock())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof BlockEntityElectroGenerator) {
                // Очистка при удалении блока
                level.updateNeighbourForOutputSignal(pos, this);
            }
            level.removeBlockEntity(pos);
        }
    }

}

