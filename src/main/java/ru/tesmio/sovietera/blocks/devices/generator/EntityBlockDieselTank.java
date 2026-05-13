package ru.tesmio.sovietera.blocks.devices.generator;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.tesmio.sovietera.core.BlockEntitiesSE;

/**
 * Тайл-сущность бака дизельного генератора.
 * Содержит собственный FluidTank на 16000mB.
 * При подключении к мультиблоку — его бак расширяет общую вместимость генератора.
 * Также позволяет заливать жидкость напрямую в бак.
 */
public class EntityBlockDieselTank extends BlockEntity {

    /** Вместимость одного бака в mB (16 вёдер) */
    public static final int TANK_CAPACITY_PER_TANK = 16000;

    private final FluidTank tank;
    private final LazyOptional<FluidTank> fluidLazyOptional;

    /** Позиция контроллера (главного генератора), если мультиблок собран */
    @Nullable
    private BlockPos controllerPos;

    public EntityBlockDieselTank(BlockPos pos, BlockState state) {
        super(BlockEntitiesSE.ENTITY_BLOCK_DIESEL_TANK.get(), pos, state);

        this.tank = new FluidTank(TANK_CAPACITY_PER_TANK) {
            @Override
            protected void onContentsChanged() {
                super.onContentsChanged();
                EntityBlockDieselTank.this.setChanged();
                if (EntityBlockDieselTank.this.level != null) {
                    EntityBlockDieselTank.this.level.sendBlockUpdated(
                            EntityBlockDieselTank.this.worldPosition,
                            EntityBlockDieselTank.this.getBlockState(),
                            EntityBlockDieselTank.this.getBlockState(),
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

    // ===================== Обновление мультиблока =====================

    /**
     * Вызывается при формировании мультиблока.
     * Устанавливает ссылку на контроллер.
     */
    public void onMultiblockFormed(BlockPos controllerPosition) {
        this.controllerPos = controllerPosition;
        this.setChanged();
    }

    /**
     * Вызывается при расформировании мультиблока.
     * Очищает ссылку на контроллер.
     */
    public void onMultiblockBroken() {
        this.controllerPos = null;
        this.setChanged();
    }

    /**
     * Возвращает позицию контроллера, или null если мультиблок не собран.
     */
    @Nullable
    public BlockPos getControllerPos() {
        return this.controllerPos;
    }

    /**
     * Проверяет, подключён ли этот бак к мультиблоку.
     */
    public boolean isPartOfMultiblock() {
        return this.controllerPos != null;
    }

    /**
     * Возвращает, питается ли этот бак (состояние берётся от контроллера).
     */
    public boolean isPowered() {
        if (this.controllerPos != null && this.level != null) {
            BlockEntity be = this.level.getBlockEntity(this.controllerPos);
            if (be instanceof EntityBlockDieselEngine generator) {
                return generator.isPowered();
            }
        }
        return false;
    }

    // ===================== NBT =====================

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("FluidTank", this.tank.writeToNBT(new CompoundTag()));
        if (this.controllerPos != null) {
            tag.putLong("ControllerPos", this.controllerPos.asLong());
        }
    }

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        this.tank.readFromNBT(tag.getCompound("FluidTank"));
        if (tag.contains("ControllerPos")) {
            this.controllerPos = BlockPos.of(tag.getLong("ControllerPos"));
        } else {
            this.controllerPos = null;
        }
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
            // Если бак подключён к мультиблоку, прокидываем запрос к объединённому баку контроллера
            if (this.controllerPos != null && this.level != null) {
                BlockEntity be = this.level.getBlockEntity(this.controllerPos);
                if (be instanceof EntityBlockDieselEngine generator) {
                    return generator.getCapability(cap, side);
                }
            }
            // Иначе — собственный бак (для автономной работы)
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

    public FluidTank getTank() {
        return this.tank;
    }
}
