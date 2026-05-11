package ru.tesmio.sovietera.blocks.devices.generator;

import net.minecraft.core.BlockPos;
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
 * Тайл-сущность дизельного электрогенератора.
 * Содержит FluidTank для хранения жидкости (вода для тестирования)
 * и булевы переменные isPowered и isActivated.
 *
 * isActivated — переключается игроком через Shift+ПКМ.
 *   Генератор потребляет топливо и выдаёт напряжение ТОЛЬКО если:
 *   1) isActivated == true (игрок включил)
 *   2) Мультблок собран (бак + катушка динамо-машины рядом)
 *   3) Внутри есть достаточное количество топлива
 *
 * isPowered — отражает реальное состояние: есть напряжение или нет.
 */
public class BlockEntityElectroGenerator extends BlockEntity {

    /** Вместимость бака в mB (8 вёдер) */
    private static final int TANK_CAPACITY = 8000;
    /** Сколько mB жидкости потребляется за один цикл горения */
    private static final int FLUID_PER_BURN = 100;
    /** Длительность одного цикла горения в тиках (10 секунд) */
    private static final int BURN_TIME_TICKS = 200;

    private final FluidTank tank;
    private final LazyOptional<FluidTank> fluidLazyOptional;

    /** Основная переменная энергии: true = напряжение есть, false = нет */
    private boolean isPowered = false;
    /** Переключатель игрока: true = генератор включён, false = выключен */
    private boolean isActivated = false;
    /** Оставшиеся тики текущего цикла горения */
    private int burnCounter = 0;

    public BlockEntityElectroGenerator(BlockPos pos, BlockState state) {
        super(BlockEntitiesSE.ELECTRO_GENERATOR.get(), pos, state);

        this.tank = new FluidTank(TANK_CAPACITY) {
            @Override
            protected void onContentsChanged() {
                super.onContentsChanged();
                BlockEntityElectroGenerator.this.setChanged();
                if (BlockEntityElectroGenerator.this.level != null) {
                    BlockEntityElectroGenerator.this.level.sendBlockUpdated(
                            BlockEntityElectroGenerator.this.worldPosition,
                            BlockEntityElectroGenerator.this.getBlockState(),
                            BlockEntityElectroGenerator.this.getBlockState(),
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

    /**
     * Метод тика, вызываемый из блока через getTicker.
     * Генератор работает только если isActivated=true.
     * В противном случае — принудительно выключается.
     *
     * TODO: Раскомментировать проверку мультблока после отладки:
     *   boolean canOperate = entity.isActivated
     *           && BlockElectroGenerator.isMultiblockFormed(level, pos, state);
     */
    public static void serverTick(Level level, BlockPos pos, BlockState state, BlockEntityElectroGenerator entity) {
        if (level.isClientSide) return;

        // Проверяем, может ли генератор работать: включён игроком
        // Временно убрана проверка мультблока для тестирования
        boolean canOperate = entity.isActivated;

        if (canOperate) {
            if (entity.burnCounter > 0) {
                entity.burnCounter--;
                if (!entity.isPowered) {
                    entity.isPowered = true;
                    entity.updatePoweredState(level, pos, state, true);
                }
            } else {
                // Попытка потребить жидкость
                if (entity.tank.getFluidAmount() >= FLUID_PER_BURN
                        && entity.tank.getFluid().getFluid() == Fluids.WATER) {
                    entity.tank.drain(FLUID_PER_BURN, IFluidHandler.FluidAction.EXECUTE);
                    entity.burnCounter = BURN_TIME_TICKS;
                    entity.isPowered = true;
                    entity.updatePoweredState(level, pos, state, true);
                } else {
                    // Топливо закончилось — выключаем
                    if (entity.isPowered) {
                        entity.isPowered = false;
                        entity.updatePoweredState(level, pos, state, false);
                    }
                }
            }
        } else {
            // Генератор не может работать (выключен игроком или мультблок разобран)
            // Принудительно снимаем напряжение и сбрасываем цикл горения
            if (entity.isPowered) {
                entity.isPowered = false;
                entity.updatePoweredState(level, pos, state, false);
            }
            if (entity.burnCounter > 0) {
                entity.burnCounter = 0;
            }
        }

        entity.setChanged();
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

    /**
     * Переключает состояние isActivated (вкл/выкл генератора).
     * Вызывается при Shift+ПКМ, если мультблок собран.
     */
    public void toggleActivated() {
        this.isActivated = !this.isActivated;
        this.setChanged();
        if (this.level != null) {
            this.level.sendBlockUpdated(
                    this.worldPosition,
                    this.getBlockState(),
                    this.getBlockState(),
                    Block.UPDATE_ALL);
        }
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

    // ===================== Геттеры / Сеттеры =====================

    public boolean isPowered() {
        return this.isPowered;
    }

    public boolean isActivated() {
        return this.isActivated;
    }

    public void setActivated(boolean activated) {
        this.isActivated = activated;
        this.setChanged();
    }

    public FluidTank getTank() {
        return this.tank;
    }

    public int getBurnCounter() {
        return this.burnCounter;
    }
}
