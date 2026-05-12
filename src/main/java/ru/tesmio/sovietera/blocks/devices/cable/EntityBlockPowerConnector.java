package ru.tesmio.sovietera.blocks.devices.cable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import ru.tesmio.sovietera.blocks.devices.generator.EntityBlockDieselEngine;
import ru.tesmio.sovietera.core.BlockEntitiesSE;

import java.util.*;

/**
 * Тайл-сущность электроконнектора — узел силовой сети.
 *
 * Сеть формируется из коннекторов, соединённых кабелями (точка-точка).
 * Каждый коннектор хранит список прямых соединений (до 3 штук).
 * Сеть — транзитивное замыкание всех достижимых коннекторов через BFS.
 *
 * Если хотя бы один коннектор в сети находится рядом с работающим
 * дизель-генератором (BlockEntityElectroGenerator.isPowered() == true),
 * вся сеть получает питание — POWERED = true на всех узлах.
 */
public class EntityBlockPowerConnector extends BlockEntity {

    /** Максимальная дистанция одного провода (в блоках) */
    private static final int MAX_CONNECTION_DISTANCE = 24;
    /** Интервал обновления сети в тиках */
    private static final int UPDATE_INTERVAL = 10;
    /** Задержка перед снятием питания (тики) — защита от мерцания */
    private static final int POWER_LOSS_DELAY = 5;

    /** Прямые соединения (кабели) — до 3 штук */
    private final List<BlockPos> connections = new ArrayList<>();
    /** Все узлы сети (транзитивное замыкание) */
    private final Set<BlockPos> network = new HashSet<>();

    private boolean networkDirty = true;
    private int ticksSinceLastUpdate = 0;
    private boolean isUpdating = false;
    private int ticksWithoutPower = 0;
    private boolean cachedPowered = false;

    public EntityBlockPowerConnector(BlockPos pos, BlockState state) {
        super(BlockEntitiesSE.POWER_CONNECTOR.get(), pos, state);
    }

    // ===================== Соединения =====================

    public List<BlockPos> getConnections() {
        return connections;
    }

    /**
     * Добавляет прямое соединение с другим коннектором.
     * Максимально 3 соединения, максимальная дистанция — 24 блока.
     */
    public void addConnection(BlockPos target) {
        if (connections.contains(target)) return;

        if (worldPosition.distSqr(target) > MAX_CONNECTION_DISTANCE * MAX_CONNECTION_DISTANCE) return;

        connections.add(target);
        networkDirty = true;
        setChanged();
        syncToClient();
        updateBundledState();
        // Сливаем сети при новом соединении
        BlockEntity be = level.getBlockEntity(target);
        if (be instanceof EntityBlockPowerConnector other) {
            mergeWithOtherNetwork(other);
        }
    }

    /**
     * Удаляет прямое соединение с другим коннектором.
     * Инвалидирует сеть с обеих сторон.
     */
    public void removeConnection(BlockPos target) {
        if (connections.remove(target)) {
            networkDirty = true;
            setChanged();
            syncToClient();
            updateBundledState();
            invalidateNetwork();

            BlockEntity be = level.getBlockEntity(target);
            if (be instanceof EntityBlockPowerConnector cable) {
                cable.invalidateNetwork();
            }
        }
    }

    /**
     * Инвалидирует всю сеть — помечает все узлы как dirty.
     * Сеть будет перестроена при следующем обновлении.
     */
    public void invalidateNetwork() {
        Set<BlockPos> visited = new HashSet<>();
        Queue<BlockPos> queue = new LinkedList<>();
        queue.add(this.worldPosition);

        while (!queue.isEmpty()) {
            BlockPos current = queue.poll();
            if (!visited.add(current)) continue;

            BlockEntity be = level.getBlockEntity(current);
            if (be instanceof EntityBlockPowerConnector cable) {
                cable.networkDirty = true;
                cable.setChanged();
                for (BlockPos neighbor : cable.getConnectedCables()) {
                    if (!visited.contains(neighbor)) {
                        queue.add(neighbor);
                    }
                }
            }
        }
    }
    /**
     * Обновляет blockstate BUNDLED в зависимости от наличия соединений.
     * Если есть хотя бы одно соединение — BUNDLED=true, иначе false.
     */
    private void updateBundledState() {
        if (level == null || level.isClientSide) return;
        BlockState state = getBlockState();
        boolean hasConnections = !connections.isEmpty();
        if (state.hasProperty(BlockPowerConnector.BUNDLED)
                && state.getValue(BlockPowerConnector.BUNDLED) != hasConnections) {
            level.setBlock(worldPosition, state.setValue(BlockPowerConnector.BUNDLED, hasConnections), 3);
        }
    }
    /**
     * Очищает все соединения этого коннектора.
     * Вызывается при удалении блока.
     */
    public void clearConnections() {
        List<BlockPos> oldConnections = new ArrayList<>(connections);
        connections.clear();
        networkDirty = true;
        setChanged();
        syncToClient();
        updateBundledState();
        invalidateNetwork();

        for (BlockPos target : oldConnections) {
            BlockEntity be = level.getBlockEntity(target);
            if (be instanceof EntityBlockPowerConnector cable) {
                cable.invalidateNetwork();
            }
        }
    }

    /**
     * Возвращает список соединений, ведущих к другим коннекторам
     * (фильтрует несуществующие).
     */
    public List<BlockPos> getConnectedCables() {
        List<BlockPos> result = new ArrayList<>();
        for (BlockPos pos : connections) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof EntityBlockPowerConnector && !result.contains(pos)) {
                result.add(pos);
            }
        }
        return result;
    }

    // ===================== Сеть =====================

    /**
     * Перестраивает сеть (BFS от текущего узла).
     */
    public void rebuildNetwork() {
        Set<BlockPos> visited = new HashSet<>();
        Queue<BlockPos> queue = new LinkedList<>();
        queue.add(worldPosition);

        while (!queue.isEmpty()) {
            BlockPos current = queue.poll();
            if (!visited.add(current)) continue;

            BlockEntity be = level.getBlockEntity(current);
            if (be instanceof EntityBlockPowerConnector cable) {
                for (BlockPos neighbor : cable.getConnectedCables()) {
                    if (!visited.contains(neighbor)) {
                        queue.add(neighbor);
                    }
                }
            }
        }

        network.clear();
        network.addAll(visited);
    }

    /**
     * Сливает две сети при добавлении нового соединения.
     * Объединённая сеть присваивается всем узлам.
     */
    private void mergeWithOtherNetwork(EntityBlockPowerConnector other) {
        Set<BlockPos> network1 = new HashSet<>(this.network);
        Set<BlockPos> network2 = new HashSet<>(other.network);

        Set<BlockPos> larger = network1.size() >= network2.size() ? network1 : network2;
        Set<BlockPos> smaller = network1.size() < network2.size() ? network1 : network2;

        Set<BlockPos> merged = new HashSet<>(larger);
        merged.addAll(smaller);

        for (BlockPos pos : merged) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof EntityBlockPowerConnector cable) {
                cable.network.clear();
                cable.network.addAll(merged);
                cable.networkDirty = false;
            }
        }
    }

    // ===================== Питание =====================

    /**
     * Проверяет, есть ли питание в сети.
     * Сканирует все узлы сети на наличие рядом работающего генератора.
     */
    private boolean computeNetworkPowered() {
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
     * Устанавливает POWERED на всех узлах сети.
     */
    private void applyPoweredToNetwork(boolean powered) {
        for (BlockPos pos : network) {
            BlockState state = level.getBlockState(pos);
            if (state.getBlock() instanceof BlockPowerConnector) {
                if (state.getValue(BlockPowerConnector.POWERED) != powered) {
                    level.setBlock(pos, state.setValue(BlockPowerConnector.POWERED, powered), 3);
                }
            }
        }
    }

    /**
     * Основной метод обновления питания в сети.
     * Перестраивает сеть (если dirty), вычисляет питание, применяет ко всем узлам.
     */
    public void updatePoweredInNetwork() {
        if (isUpdating) return;
        isUpdating = true;
        try {
            if (networkDirty) {
                rebuildNetwork();
                networkDirty = false;
            }

            boolean hasPower = computeNetworkPowered();

            if (hasPower) {
                cachedPowered = true;
                ticksWithoutPower = 0;
            } else {
                ticksWithoutPower++;
                if (ticksWithoutPower >= POWER_LOSS_DELAY) {
                    cachedPowered = false;
                }
            }

            applyPoweredToNetwork(cachedPowered);
        } finally {
            isUpdating = false;
        }
    }

    // ===================== Тик =====================

    public static void tick(Level level, BlockPos pos, BlockState state, BlockEntity be) {
        if (!(be instanceof EntityBlockPowerConnector connector)) return;
        if (level.isClientSide) return;

        connector.ticksSinceLastUpdate++;
        if (connector.ticksSinceLastUpdate >= UPDATE_INTERVAL) {
            connector.ticksSinceLastUpdate = 0;
            connector.updatePoweredInNetwork();
        }
    }

    // ===================== Состояние =====================

    /**
     * Возвращает true, если данный коннектор получает питание из сети.
     */
    public boolean isNetworkPowered() {
        return cachedPowered;
    }

    // ===================== Клиентская синхронизация =====================

    private void syncToClient() {
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    public AABB getRenderBoundingBox() {
        return super.getRenderBoundingBox().inflate(50f);
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
            list.add(posTag);
        }
        tag.put("Connections", list);
        tag.putBoolean("CachedPowered", this.cachedPowered);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        connections.clear();
        ListTag list = tag.getList("Connections", Tag.TAG_COMPOUND);
        for (Tag t : list) {
            CompoundTag posTag = (CompoundTag) t;
            connections.add(new BlockPos(posTag.getInt("x"), posTag.getInt("y"), posTag.getInt("z")));
        }
        this.cachedPowered = tag.getBoolean("CachedPowered");
        networkDirty = true;
    }
}
