package ru.tesmio.sovietera.blocks.devices.devicesnetwork;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import ru.tesmio.sovietera.blocks.devices.cable.BlockPowerConnector;
import ru.tesmio.sovietera.blocks.devices.generator.EntityBlockDieselEngine;
import ru.tesmio.sovietera.blocks.devices.lamps.BlockLampBase;

import java.util.*;

/**
 * Общие утилиты для силовой сети.
 * Используются как EntityBlockPowerConnector, так и EntityBlockLamp
 * для обхода сети (BFS), проверки питания и применения состояния.
 */
public final class NetworkUtils {

    private NetworkUtils() {}

    /**
     * BFS-обход силовой сети от стартовой позиции.
     * Проходит через все INetworkNode (коннекторы и лампы).
     *
     * @param level  мир
     * @param start  стартовая позиция (должна быть INetworkNode)
     * @return множество всех позиций в сети
     */
    public static Set<BlockPos> bfsNetwork(Level level, BlockPos start) {
        Set<BlockPos> visited = new HashSet<>();
        Queue<BlockPos> queue = new LinkedList<>();
        queue.add(start);

        while (!queue.isEmpty()) {
            BlockPos current = queue.poll();
            if (!visited.add(current)) continue;

            BlockEntity be = level.getBlockEntity(current);
            if (be instanceof INetworkNode node) {
                for (BlockPos neighbor : node.getConnections()) {
                    if (!visited.contains(neighbor)) {
                        queue.add(neighbor);
                    }
                }
            }
        }
        return visited;
    }

    /**
     * Проверяет, есть ли питание в сети.
     * Сканирует все узлы на наличие рядом работающего генератора.
     *
     * @param level   мир
     * @param network множество позиций сети
     * @return true, если хотя бы один узел рядом с работающим генератором
     */
    public static boolean computeNetworkPowered(Level level, Set<BlockPos> network) {
        for (BlockPos pos : network) {
            for (Direction dir : Direction.values()) {
                BlockPos neighborPos = pos.relative(dir);
                BlockEntity be = level.getBlockEntity(neighborPos);
                if (be instanceof EntityBlockDieselEngine generator) {
                    if (generator.isPowered()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Устанавливает POWERED на всех блоках сети.
     * Поддерживает BlockPowerConnector и BlockLampBase.
     *
     * @param level   мир
     * @param network множество позиций сети
     * @param powered состояние питания
     */
    public static void applyPoweredToNetwork(Level level, Set<BlockPos> network, boolean powered) {
        for (BlockPos pos : network) {
            BlockState state = level.getBlockState(pos);
            if (state.getBlock() instanceof BlockPowerConnector
                    && state.hasProperty(BlockPowerConnector.POWERED)
                    && state.getValue(BlockPowerConnector.POWERED) != powered) {
                level.setBlock(pos, state.setValue(BlockPowerConnector.POWERED, powered), 2);
            } else if (state.getBlock() instanceof BlockLampBase
                    && state.hasProperty(BlockLampBase.POWERED)
                    && state.getValue(BlockLampBase.POWERED) != powered) {
                level.setBlock(pos, state.setValue(BlockLampBase.POWERED, powered), 2);
            }
        }
    }
}
