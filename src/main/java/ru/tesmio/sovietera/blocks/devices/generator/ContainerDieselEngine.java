package ru.tesmio.sovietera.blocks.devices.generator;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;
import ru.tesmio.sovietera.core.BlocksSE;
import ru.tesmio.sovietera.core.MenuTypesSE;

/**
 * Контейнер дизельного электрогенератора.
 * Минимальная реализация для будущего GUI.
 * Пока без кастомных слотов — взаимодействие с баком через ПКМ ведром.
 */
public class ContainerDieselEngine extends AbstractContainerMenu {

    private final BlockEntity blockEntity;
    private final ContainerLevelAccess levelAccess;

    // Клиентский конструктор
    public ContainerDieselEngine(int containerId, Inventory playerInv, FriendlyByteBuf additionalData) {
        this(containerId, playerInv, playerInv.player.level().getBlockEntity(additionalData.readBlockPos()));
    }

    // Серверный конструктор
    public ContainerDieselEngine(int containerId, Inventory playerInv, BlockEntity blockEntity) {
        super(MenuTypesSE.ELECTRO_GENERATOR_MENU.get(), containerId);
        this.blockEntity = blockEntity;
        this.levelAccess = ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos());

        // Хотбар игрока (9 слотов)
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(playerInv, column, 8 + column * 18, 142));
        }

        // Основной инвентарь игрока (3x9)
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(playerInv, 9 + column + row * 9, 8 + column * 18, 84 + row * 18));
            }
        }
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        Slot fromSlot = getSlot(index);
        ItemStack fromStack = fromSlot.getItem();

        if (fromStack.getCount() <= 0) {
            fromSlot.set(ItemStack.EMPTY);
        }

        if (!fromSlot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack copyFromStack = fromStack.copy();

        if (index < 36) {
            // Внутри инвентаря игрока — перемещать некуда, возвращаем пусто
            return ItemStack.EMPTY;
        } else {
            System.err.println("Invalid slot index: " + index);
            return ItemStack.EMPTY;
        }
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return stillValid(this.levelAccess, player, BlocksSE.DIESEL_ENGINE.get());
    }

    public BlockEntity getBlockEntity() {
        return this.blockEntity;
    }
}
