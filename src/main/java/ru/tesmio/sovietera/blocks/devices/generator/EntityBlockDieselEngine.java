package ru.tesmio.sovietera.blocks.devices.generator;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.tesmio.sovietera.core.BlockEntitiesSE;

/**
 * Тайл-сущность дизельного электрогенератора (контроллер).
 * Содержит собственный FluidTank и булевы переменные:
 * - isPowered  — есть ли напряжение (горит ли топливо)
 * - isActivated — включён ли генератор игроком (SHIFT+ПКМ)
 *
 * Мультиблочная проверка упрощена: генератор потребляет топливо
 * из собственного бака + из бака соседнего BlockEntityDieselTank
 * (если тот стоит в правильной позиции). Никакого формируемого
 * мультиблока — просто проверка соседей при активации.
 */
public class EntityBlockDieselEngine extends BlockEntity {

    /** Вместимость собственного бака в mB (8 вёдер) */
    private static final int TANK_CAPACITY = 8000;
    /** Сколько mB жидкости потребляется за один цикл горения */
    private static final int FLUID_PER_BURN = 100;
    /** Длительность одного цикла горения в тиках (10 секунд) */
    private static final int BURN_TIME_TICKS = 200;

    private final FluidTank tank;
    private final LazyOptional<FluidTank> fluidLazyOptional;

    /** Включён ли генератор (SHIFT+ПКМ) */
    private boolean isActivated = false;
    /** Есть ли напряжение (горит ли топливо) */
    private boolean isPowered = false;
    /** Оставшиеся тики текущего цикла горения */
    private int burnCounter = 0;

    public EntityBlockDieselEngine(BlockPos pos, BlockState state) {
        super(BlockEntitiesSE.ENTITY_BLOCK_ELECTRO_GENERATOR.get(), pos, state);

        this.tank = new FluidTank(TANK_CAPACITY) {
            @Override
            protected void onContentsChanged() {
                super.onContentsChanged();
                EntityBlockDieselEngine.this.setChanged();
                if (EntityBlockDieselEngine.this.level != null) {
                    EntityBlockDieselEngine.this.level.sendBlockUpdated(
                            EntityBlockDieselEngine.this.worldPosition,
                            EntityBlockDieselEngine.this.getBlockState(),
                            EntityBlockDieselEngine.this.getBlockState(),
                            Block.UPDATE_ALL);
                }
            }

            @Override
            public boolean isFluidValid(net.minecraftforge.fluids.FluidStack stack) {
                return stack.getFluid() == Fluids.WATER;
            }
        };

        this.fluidLazyOptional = LazyOptional.of(() -> this.tank);
    }

    // ===================== Логика тика =====================

    /**
     * Серверный тик. Если генератор активирован — потребляет топливо
     * из собственного бака, а если не хватает — из бака соседнего DieselTank.
     */
    public static void serverTick(Level level, BlockPos pos, BlockState state, EntityBlockDieselEngine entity) {
        if (level.isClientSide) return;

        // Если генератор не активирован — не тикаем логику горения
        if (!entity.isActivated) {
            if (entity.isPowered) {
                entity.isPowered = false;
                entity.updatePoweredState(level, pos, state, false);
                entity.setChanged();
            }
            return;
        }

        if (entity.burnCounter > 0) {
            entity.burnCounter--;
            if (!entity.isPowered) {
                entity.isPowered = true;
                entity.updatePoweredState(level, pos, state, true);
            }
        } else {
            // Попытка потребить жидкость: сначала из своего бака
            boolean consumed = false;

            if (entity.tank.getFluidAmount() >= FLUID_PER_BURN
                    && entity.tank.getFluid().getFluid() == Fluids.WATER) {
                entity.tank.drain(FLUID_PER_BURN, IFluidHandler.FluidAction.EXECUTE);
                consumed = true;
            } else {
                // Если в своём баке не хватает — пробуем соседний бак
                consumed = entity.tryConsumeFromNeighborTank(level, pos, state);
            }

            if (consumed) {
                entity.burnCounter = BURN_TIME_TICKS;
                entity.isPowered = true;
                entity.updatePoweredState(level, pos, state, true);
            } else {
                if (entity.isPowered) {
                    entity.isPowered = false;
                    entity.updatePoweredState(level, pos, state, false);
                }
            }
        }

        entity.setChanged();
    }

    /**
     * Пробует потребить FLUID_PER_BURN из бака соседнего DieselTank.
     * Сосед определяется по FACING блока: бак на CCW-стороне.
     */
    private boolean tryConsumeFromNeighborTank(Level level, BlockPos pos, BlockState state) {
        if (!state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) return false;

        Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        BlockPos neighborPos = pos.relative(facing.getCounterClockWise());
        BlockEntity be = level.getBlockEntity(neighborPos);

        if (be instanceof EntityBlockDieselTank dieselTank) {
            // Проверяем, что блок действительно DieselTank с правильным FACING
            BlockState neighborState = level.getBlockState(neighborPos);
            if (neighborState.hasProperty(BlockStateProperties.HORIZONTAL_FACING)
                    && neighborState.getValue(BlockStateProperties.HORIZONTAL_FACING) == facing) {

                var neighborTank = dieselTank.getTank();
                if (neighborTank.getFluidAmount() >= FLUID_PER_BURN
                        && neighborTank.getFluid().getFluid() == Fluids.WATER) {
                    neighborTank.drain(FLUID_PER_BURN, IFluidHandler.FluidAction.EXECUTE);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Обновляет blockstate POWERED блока в мире для визуальной обратной связи.
     */
    private void updatePoweredState(Level level, BlockPos pos, BlockState state, boolean powered) {
        if (state.hasProperty(BlockStateProperties.POWERED)
                && state.getValue(BlockStateProperties.POWERED) != powered) {
            level.setBlock(pos, state.setValue(BlockStateProperties.POWERED, powered), Block.UPDATE_ALL);
        }
    }

    // ===================== Активация (SHIFT+ПКМ) =====================

    /**
     * Проверяет, есть ли топливо в любом из доступных баков
     * (собственный + соседний DieselTank).
     */
    public boolean hasFuel() {
        // Проверяем собственный бак
        if (this.tank.getFluidAmount() >= FLUID_PER_BURN
                && this.tank.getFluid().getFluid() == Fluids.WATER) {
            return true;
        }

        // Проверяем соседний бак
        if (this.level != null && this.getBlockState().hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            Direction facing = this.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
            BlockPos neighborPos = this.worldPosition.relative(facing.getCounterClockWise());
            BlockEntity be = this.level.getBlockEntity(neighborPos);

            if (be instanceof EntityBlockDieselTank dieselTank) {
                BlockState neighborState = this.level.getBlockState(neighborPos);
                if (neighborState.hasProperty(BlockStateProperties.HORIZONTAL_FACING)
                        && neighborState.getValue(BlockStateProperties.HORIZONTAL_FACING) == facing) {
                    var neighborTank = dieselTank.getTank();
                    return neighborTank.getFluidAmount() >= FLUID_PER_BURN
                            && neighborTank.getFluid().getFluid() == Fluids.WATER;
                }
            }
        }

        return false;
    }

    /**
     * Включён ли генератор игроком.
     */
    public boolean isActivated() {
        return this.isActivated;
    }

    /**
     * Переключает состояние активации. Возвращает новое состояние.
     */
    public boolean toggleActivated() {
        this.isActivated = !this.isActivated;
        this.setChanged();

        // При выключении — сбрасываем горение
        if (!this.isActivated) {
            this.burnCounter = 0;
            this.isPowered = false;
            if (this.level != null) {
                this.updatePoweredState(this.level, this.worldPosition, this.getBlockState(), false);
            }
        }

        return this.isActivated;
    }

    /**
     * Проверяет, собрана ли структура из 3 блоков:
     * DieselTank (CCW) + DieselEngine (этот) + ElectroGenerator (CW),
     * все смотрят в одном направлении.
     *
     * Это простая проверка соседей — без формирования мультиблока.
     */
    public boolean isNeighborStructureFormed() {
        if (this.level == null) return false;
        return BlockDieselEngine.isMultiblockFormed(this.level, this.worldPosition, this.getBlockState());
    }

    // ===================== NBT =====================

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("FluidTank", this.tank.writeToNBT(new CompoundTag()));
        tag.putBoolean("IsPowered", this.isPowered);
        tag.putBoolean("IsActivated", this.isActivated);
        tag.putInt("BurnCounter", this.burnCounter);
    }

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        this.tank.readFromNBT(tag.getCompound("FluidTank"));
        this.isPowered = tag.getBoolean("IsPowered");
        this.isActivated = tag.getBoolean("IsActivated");
        this.burnCounter = tag.getInt("BurnCounter");
    }

    // ===================== Синхронизация с клиентом =====================

    @Override
    public @NotNull CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        saveAdditional(tag);
        return tag;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    // ===================== Capabilities =====================

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable net.minecraft.core.Direction side) {
        if (cap == ForgeCapabilities.FLUID_HANDLER) {
            return this.fluidLazyOptional.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        this.fluidLazyOptional.invalidate();
    }

    // ===================== Геттеры =====================

    public boolean isPowered() {
        return this.isPowered;
    }

    public FluidTank getTank() {
        return this.tank;
    }

    public int getBurnCounter() {
        return this.burnCounter;
    }
}
