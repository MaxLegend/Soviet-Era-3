package ru.tesmio.sovietera.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;
import ru.tesmio.sovietera.blocks.devices.cable.BlockEntityPowerConnector;


import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Пакет для отключения всех проводов от коннектора (Ctrl + ПКМ).
 * Отправляется с клиента на сервер.
 */
public class PowerCtrlPressedPacket {

    private final BlockPos pos;

    public PowerCtrlPressedPacket(BlockPos pos) {
        this.pos = pos;
    }

    public PowerCtrlPressedPacket(FriendlyByteBuf buf) {
        this.pos = buf.readBlockPos();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
    }

    public static void handle(PowerCtrlPressedPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                BlockEntity be = player.level().getBlockEntity(packet.pos);
                if (be instanceof BlockEntityPowerConnector connector) {
                    // Удаляем все соединения с обоих сторон
                    List<BlockPos> connections = new ArrayList<>(connector.getConnections());
                    for (BlockPos other : connections) {
                        BlockEntity otherBe = player.level().getBlockEntity(other);
                        if (otherBe instanceof BlockEntityPowerConnector otherConnector) {
                            otherConnector.removeConnection(packet.pos);
                            otherConnector.setChanged();
                            player.level().sendBlockUpdated(other, otherConnector.getBlockState(),
                                    otherConnector.getBlockState(), 3);
                        }
                    }
                    connector.clearConnections();
                    connector.setChanged();
                    player.level().sendBlockUpdated(packet.pos, connector.getBlockState(),
                            connector.getBlockState(), 3);
                }
            }
        });
        context.setPacketHandled(true);
    }
}
