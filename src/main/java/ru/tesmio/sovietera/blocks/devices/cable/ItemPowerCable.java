package ru.tesmio.sovietera.blocks.devices.cable;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.Vec3;
import ru.tesmio.sovietera.blocks.devices.devicesnetwork.INetworkNode;
import ru.tesmio.sovietera.blocks.devices.lamps.BlockLampBase;
import ru.tesmio.sovietera.blocks.devices.lamps.EntityBlockLamp;
import ru.tesmio.sovietera.network.PowerCtrlPressedPacket;
import ru.tesmio.sovietera.SovietEra;
import ru.tesmio.sovietera.utils.BaseEnumOrientation;

import java.util.List;

/**
 * Предмет «Силовой кабель» — инструмент для соединения коннекторов.
 *
 * Механика:
 *   - Shift + ПКМ по коннектору №1 → сохраняет позицию в NBT предмета
 *   - Shift + ПКМ по коннектору №2 → создаёт провод между ними (если дистанция ≤ 20 блоков)
 *   - Ctrl + ПКМ по коннектору → отключает все провода от него (пакет на сервер)
 *   - Shift + ПКМ в воздух → очищает сохранённую позицию
 */
public class ItemPowerCable extends Item {

    public ItemPowerCable(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (player.isShiftKeyDown()) {
            if (!level.isClientSide) {
                CompoundTag tag = stack.getOrCreateTag();
                if (tag.contains("LinkX")) {
                    tag.remove("LinkX");
                    tag.remove("LinkY");
                    tag.remove("LinkZ");
                    tag.remove("LinkEndpoint");
                    player.displayClientMessage(
                            Component.translatable("cable.sovietera.positions.cleared"), true);
                }
            }
            return InteractionResultHolder.success(stack);
        }
        return InteractionResultHolder.pass(stack);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos clickedPos = context.getClickedPos();
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        BlockEntity be = level.getBlockEntity(clickedPos);

        INetworkNode node = INetworkNode.from(be);
        if (node == null) return InteractionResult.PASS;

        CompoundTag tag = stack.getOrCreateTag();

        // Ctrl + ПКМ — отключить все провода (клиент отправляет пакет)
        if (level.isClientSide) {
            if (InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 341)) { // GLFW_KEY_LEFT_CONTROL
                SovietEra.CHANNEL.sendToServer(new PowerCtrlPressedPacket(clickedPos));
                return InteractionResult.SUCCESS;
            }
        }

        // Shift + ПКМ — соединение
        if (player.isShiftKeyDown()) {
            return handleShiftClick(level, player, clickedPos, node, tag, stack, context);
        }

        return InteractionResult.PASS;
    }

    private InteractionResult handleShiftClick(Level level, Player player, BlockPos clickedPos,
            INetworkNode node, CompoundTag tag, ItemStack stack, UseOnContext context) {
        if (tag.contains("LinkX")) {
            BlockPos startPos = new BlockPos(tag.getInt("LinkX"), tag.getInt("LinkY"), tag.getInt("LinkZ"));
            byte startEndpoint = tag.getByte("LinkEndpoint");
            tag.remove("LinkX");
            tag.remove("LinkY");
            tag.remove("LinkZ");
            tag.remove("LinkEndpoint");
            if (node.getConnections().contains(startPos)) {
                player.displayClientMessage(
                        Component.translatable("cable.sovietera.error.already_connected").withStyle(ChatFormatting.YELLOW), true);
                return InteractionResult.FAIL;
            }
            if (!startPos.equals(clickedPos)) {
                double distanceSq = startPos.distSqr(clickedPos);
                if (distanceSq > 400) { // 20 * 20
                    player.displayClientMessage(Component.translatable(
                            "cable.sovietera.error.too_far", 20, (int) Math.sqrt(distanceSq)).withStyle(ChatFormatting.RED), true);
                    return InteractionResult.FAIL;
                }

                INetworkNode startNode = INetworkNode.from(level.getBlockEntity(startPos));
                if (startNode != null) {
                    // Determine endpoint for the clicked node (second click)
                    byte clickedEndpoint = determineEndpoint(level, clickedPos, context.getClickLocation());

                    // Add connection: start → clicked (with start's endpoint)
                    addConnectionWithEndpoint(startNode, clickedPos, startEndpoint);
                    startNode.asBlockEntity().setChanged();
                    level.sendBlockUpdated(startPos, startNode.asBlockEntity().getBlockState(),
                            startNode.asBlockEntity().getBlockState(), 3);

                    // Add connection: clicked → start (with clicked's endpoint)
                    addConnectionWithEndpoint(node, startPos, clickedEndpoint);
                    node.asBlockEntity().setChanged();
                    level.sendBlockUpdated(clickedPos, node.asBlockEntity().getBlockState(),
                            node.asBlockEntity().getBlockState(), 3);

                    player.displayClientMessage(
                            Component.translatable("cable.sovietera.positions.connected",
                                    formatBlockPos(startPos), formatBlockPos(clickedPos),
                                    (int) Math.sqrt(distanceSq)).withStyle(ChatFormatting.GREEN), true);

                    stack.shrink(1);
                    return InteractionResult.SUCCESS;
                }
            }
        } else {
            // First click — save position and determine endpoint for longitudinal lamps
            byte endpoint = determineEndpoint(level, clickedPos, context.getClickLocation());

            tag.putInt("LinkX", clickedPos.getX());
            tag.putInt("LinkY", clickedPos.getY());
            tag.putInt("LinkZ", clickedPos.getZ());
            tag.putByte("LinkEndpoint", endpoint);

            player.displayClientMessage(
                    Component.translatable("cable.sovietera.positions.saved", formatBlockPos(clickedPos)), true);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.FAIL;
    }

    /**
     * Determines which endpoint (0 or 1) of a longitudinal lamp the player clicked on.
     * For non-lamp blocks or non-longitudinal lamps, returns 0.
     *
     * @param level         world
     * @param blockPos      block position
     * @param clickLocation world-space click location
     * @return 0 = negative end of long axis, 1 = positive end
     */
    private static byte determineEndpoint(Level level, BlockPos blockPos, Vec3 clickLocation) {
        BlockState state = level.getBlockState(blockPos);
        if (!(state.getBlock() instanceof BlockLampBase)) return 0;

        // Find the BaseEnumOrientation "facing" property (longitudinal lamps only)
        for (var prop : state.getProperties()) {
            if (!"facing".equals(prop.getName())) continue;
            if (prop instanceof EnumProperty<?> ep && ep.getValueClass() == BaseEnumOrientation.class) {
                @SuppressWarnings("unchecked")
                EnumProperty<BaseEnumOrientation> facing = (EnumProperty<BaseEnumOrientation>) ep;
                BaseEnumOrientation orient = state.getValue(facing);
                net.minecraft.core.Direction.Axis longAxis = orient.getLongAxis();

                // Local click position within the block (0..1)
                double lx = clickLocation.x - blockPos.getX();
                double ly = clickLocation.y - blockPos.getY();
                double lz = clickLocation.z - blockPos.getZ();

                return switch (longAxis) {
                    case X -> lx < 0.5 ? (byte) 0 : (byte) 1;
                    case Z -> lz < 0.5 ? (byte) 0 : (byte) 1;
                    default -> 0; // Y-axis shouldn't happen for longitudinal lamps
                };
            }
        }
        return 0;
    }

    /**
     * Adds a connection, passing endpoint data for EntityBlockLamp.
     */
    private static void addConnectionWithEndpoint(INetworkNode node, BlockPos target, byte endpoint) {
        if (node instanceof EntityBlockLamp lamp) {
            lamp.addConnection(target, endpoint);
        } else {
            node.addConnection(target);
        }
    }

    private static Component formatBlockPos(BlockPos pos) {
        return Component.literal("(" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + ")");
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("LinkX")) {
            BlockPos pos = new BlockPos(tag.getInt("LinkX"), tag.getInt("LinkY"), tag.getInt("LinkZ"));
            tooltip.add(Component.translatable("cable.sovietera.saved_point", pos.toShortString()).withStyle(ChatFormatting.GRAY));
        } else {
            tooltip.add(Component.translatable("cable.sovietera.not_saved_point").withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}
