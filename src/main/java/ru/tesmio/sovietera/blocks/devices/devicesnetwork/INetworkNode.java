package ru.tesmio.sovietera.blocks.devices.devicesnetwork;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.List;

/**
 * Общий интерфейс для всех участников силовой сети (коннекторы, лампы и т.д.).
 *
 * Позволяет EntityBlockPowerConnector и EntityBlockLamp участвовать
 * в одной BFS-сети без жёсткой связи между классами.
 */
public interface INetworkNode {

    /** Прямые соединения (кабели) данного узла */
    List<BlockPos> getConnections();

    /** Добавляет прямое соединение с другим узлом */
    void addConnection(BlockPos target);

    /** Удаляет прямое соединение */
    void removeConnection(BlockPos target);

    /** Очищает все соединения (при удалении блока) */
    void clearConnections();

    /** Помечает сеть как требующую перестроения */
    void markNetworkDirty();

    /** Возвращает true, если данный узел получает питание из сети */
    boolean isNetworkPowered();

    /** Возвращает BlockEntity как реализацию (для удобства) */
    BlockEntity asBlockEntity();

    /**
     * Статический хелпер — безопасное приведение BlockEntity к INetworkNode.
     */
    static INetworkNode from(BlockEntity be) {
        return be instanceof INetworkNode node ? node : null;
    }

    /**
     * Статический хелпер — возвращает список прямых соединений
     * для любой BlockEntity, реализующей INetworkNode.
     * Фильтрует несуществующие узлы.
     */
    static List<BlockPos> getValidConnections(BlockEntity be) {
        INetworkNode node = from(be);
        if (node == null) return List.of();
        Level level = be.getLevel();
        if (level == null) return List.of();
        return node.getConnections().stream()
                .filter(pos -> level.getBlockEntity(pos) instanceof INetworkNode)
                .toList();
    }
}
