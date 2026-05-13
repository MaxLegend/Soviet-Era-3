package ru.tesmio.sovietera.blocks.devices.lamps;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import ru.tesmio.sovietera.blocks.devices.devicesnetwork.INetworkNode;
import ru.tesmio.sovietera.blocks.devices.devicesnetwork.NetworkUtils;
import ru.tesmio.sovietera.core.BlockEntitiesSE;


import java.util.*;

/**
 * Тайл-сущность лампы — узел силовой сети.
 *
 * Лампа выступает как полноценный участник сети (как коннектор):
 *   - К ней можно подключать кабели
 *   - Она передаёт isPowered через сеть
 *   - Если isPowered = true, блок переходит в состояние POWERED и излучает свет (15)
 *
 * Сеть формируется из коннекторов и ламп, соединённых кабелями (точка-точка).
 * Питание определяется наличием рядом работающего генератора (EntityBlockDieselEngine).
 */
public class EntityBlockLamp extends BlockEntity implements INetworkNode {

    /** Максимальная дистанция одного провода */
    private static final int MAX_CONNECTION_DISTANCE = 24;
    /** Интервал обновления сети в тиках */
    private static final int UPDATE_INTERVAL = 10;
    /** Задержка перед снятием питания (тики) — защита от мерцания */
    private static final int POWER_LOSS_DELAY = 5;

    /** Прямые соединения (кабели) — до 3 штук */
    private final List<BlockPos> connections = new ArrayList<>();
    /** Все узлы сети (транзитивное замыкание) */
    private final Set<BlockPos> network = new HashSet<>();
    /**
     * Для продольных ламп: какой конец (0 или 1) используется для каждого соединения.
     * 0 = отрицательный край длинной оси (x/z = 0), 1 = положительный (x/z = 1).
     * Для непродольных ламп (IncLamp, StreetLamp) не используется.
     */
    private final Map<BlockPos, Byte> connectionEndpoints = new HashMap<>();

    private boolean networkDirty = true;
    private int ticksSinceLastUpdate = 0;
    private boolean isUpdating = false;
    private int ticksWithoutPower = 0;
    private boolean cachedPowered = false;

    public EntityBlockLamp(BlockPos pos, BlockState state) {
        super(BlockEntitiesSE.ENTITY_BLOCK_LAMP.get(), pos, state);
    }

    // ===================== INetworkNode =====================

    @Override
    public List<BlockPos> getConnections() {
        return connections;
    }

    @Override
    public void addConnection(BlockPos target) {
        addConnection(target, (byte) 0);
    }

    /**
     * Добавляет соединение с указанием конечной точки (для продольных ламп).
     * @param target   позиция другого узла
     * @param endpoint 0 = отрицательный край длинной оси, 1 = положительный край
     */
    public void addConnection(BlockPos target, byte endpoint) {
        if (connections.contains(target)) return;
        if (worldPosition.distSqr(target) > MAX_CONNECTION_DISTANCE * MAX_CONNECTION_DISTANCE) return;

        connections.add(target);
        connectionEndpoints.put(target, endpoint);
        networkDirty = true;
        setChanged();
        syncToClient();

        // Сливаем сети при новом соединении
        BlockEntity be = level.getBlockEntity(target);
        if (be instanceof INetworkNode otherNode) {
            mergeWithOtherNetwork(otherNode);
        }
    }

    /**
     * Возвращает конечную точку (0 или 1) для данного соединения.
     * Используется рендерером для определения, к какому краю лампы крепится провод.
     */
    public byte getConnectionEndpoint(BlockPos target) {
        return connectionEndpoints.getOrDefault(target, (byte) 0);
    }

    @Override
    public void removeConnection(BlockPos target) {
        if (connections.remove(target)) {
            connectionEndpoints.remove(target);
            networkDirty = true;
            setChanged();
            syncToClient();
            invalidateNetwork();

            BlockEntity be = level.getBlockEntity(target);
            INetworkNode otherNode = INetworkNode.from(be);
            if (otherNode != null) {
                otherNode.markNetworkDirty();
            }
        }
    }

    @Override
    public void clearConnections() {
        List<BlockPos> oldConnections = new ArrayList<>(connections);
        connections.clear();
        connectionEndpoints.clear();
        networkDirty = true;
        setChanged();
        syncToClient();
        invalidateNetwork();

        for (BlockPos target : oldConnections) {
            BlockEntity be = level.getBlockEntity(target);
            INetworkNode otherNode = INetworkNode.from(be);
            if (otherNode != null) {
                otherNode.markNetworkDirty();
            }
        }
    }

    @Override
    public void markNetworkDirty() {
        networkDirty = true;
        setChanged();
    }

    @Override
    public boolean isNetworkPowered() {
        return cachedPowered;
    }

    @Override
    public BlockEntity asBlockEntity() {
        return this;
    }

    // ===================== Сеть =====================

    /**
     * Инвалидирует всю сеть — помечает все узлы как dirty.
     */
    public void invalidateNetwork() {
        if (level == null) return;
        Set<BlockPos> visited = new HashSet<>();
        Queue<BlockPos> queue = new LinkedList<>();
        queue.add(this.worldPosition);

        while (!queue.isEmpty()) {
            BlockPos current = queue.poll();
            if (!visited.add(current)) continue;

            BlockEntity be = level.getBlockEntity(current);
            INetworkNode node = INetworkNode.from(be);
            if (node != null) {
                node.markNetworkDirty();
                for (BlockPos neighbor : node.getConnections()) {
                    if (!visited.contains(neighbor)) {
                        queue.add(neighbor);
                    }
                }
            }
        }
    }

    /**
     * Перестраивает сеть (BFS от текущего узла).
     */
    public void rebuildNetwork() {
        if (level == null) return;
        Set<BlockPos> visited = NetworkUtils.bfsNetwork(level, worldPosition);
        network.clear();
        network.addAll(visited);
    }

    /**
     * Сливает две сети при добавлении нового соединения.
     */
    private void mergeWithOtherNetwork(INetworkNode other) {
        // Просто инвалидируем обе сети — они перестроятся при следующем обновлении
        this.networkDirty = true;
        other.markNetworkDirty();
    }

    // ===================== Питание =====================

    /**
     * Основной метод обновления питания в сети.
     */
    public void updatePoweredInNetwork() {
        if (level == null || level.isClientSide) return;
        if (isUpdating) return;
        isUpdating = true;
        try {
            if (networkDirty) {
                rebuildNetwork();
                networkDirty = false;
            }

            boolean hasPower = NetworkUtils.computeNetworkPowered(level, network);

            if (hasPower) {
                cachedPowered = true;
                ticksWithoutPower = 0;
            } else {
                ticksWithoutPower++;
                if (ticksWithoutPower >= POWER_LOSS_DELAY) {
                    cachedPowered = false;
                }
            }

            NetworkUtils.applyPoweredToNetwork(level, network, cachedPowered);
        } finally {
            isUpdating = false;
        }
    }

    // ===================== Тик =====================

    public static void tick(Level level, BlockPos pos, BlockState state, BlockEntity be) {
        if (!(be instanceof EntityBlockLamp lamp)) return;
        if (level.isClientSide) return;

        lamp.ticksSinceLastUpdate++;
        if (lamp.ticksSinceLastUpdate >= UPDATE_INTERVAL) {
            lamp.ticksSinceLastUpdate = 0;
            lamp.updatePoweredInNetwork();
        }
    }

    // ===================== Клиентская синхронизация =====================

    private void syncToClient() {
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    public AABB getRenderBoundingBox() {
        return super.getRenderBoundingBox().inflate(MAX_CONNECTION_DISTANCE);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        this.load(tag);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag);
        return tag;
    }

    // ===================== NBT =====================

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ListTag list = new ListTag();
        for (BlockPos pos : connections) {
            CompoundTag posTag = new CompoundTag();
            posTag.putInt("x", pos.getX());
            posTag.putInt("y", pos.getY());
            posTag.putInt("z", pos.getZ());
            posTag.putByte("endpoint", connectionEndpoints.getOrDefault(pos, (byte) 0));
            list.add(posTag);
        }
        tag.put("Connections", list);
        tag.putBoolean("CachedPowered", this.cachedPowered);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        connections.clear();
        connectionEndpoints.clear();
        ListTag list = tag.getList("Connections", Tag.TAG_COMPOUND);
        for (Tag t : list) {
            CompoundTag posTag = (CompoundTag) t;
            BlockPos pos = new BlockPos(posTag.getInt("x"), posTag.getInt("y"), posTag.getInt("z"));
            connections.add(pos);
            connectionEndpoints.put(pos, posTag.getByte("endpoint"));
        }
        this.cachedPowered = tag.getBoolean("CachedPowered");
        networkDirty = true;
    }
}
