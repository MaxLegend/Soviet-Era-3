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
import ru.tesmio.sovietera.network.PowerCtrlPressedPacket;
import ru.tesmio.sovietera.SovietEra;

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
public class PowerCableItem extends Item {

    public PowerCableItem(Properties properties) {
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

        if (!(be instanceof BlockEntityPowerConnector connector)) return InteractionResult.PASS;

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
            return handleShiftClick(level, player, clickedPos, connector, tag, stack);
        }

        return InteractionResult.PASS;
    }

    private InteractionResult handleShiftClick(Level level, Player player, BlockPos clickedPos,
                                               BlockEntityPowerConnector connector, CompoundTag tag, ItemStack stack) {

        if (tag.contains("LinkX")) {
            BlockPos startPos = new BlockPos(tag.getInt("LinkX"), tag.getInt("LinkY"), tag.getInt("LinkZ"));
            tag.remove("LinkX");
            tag.remove("LinkY");
            tag.remove("LinkZ");
            if (connector.getConnections().contains(startPos)) {
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

                BlockEntity startBe = level.getBlockEntity(startPos);
                if (startBe instanceof BlockEntityPowerConnector startConnector) {
                    // Создаём двустороннее соединение
                    startConnector.addConnection(clickedPos);
                    startConnector.setChanged();
                    level.sendBlockUpdated(startPos, startConnector.getBlockState(), startConnector.getBlockState(), 3);

                    connector.addConnection(startPos);
                    connector.setChanged();
                    level.sendBlockUpdated(clickedPos, connector.getBlockState(), connector.getBlockState(), 3);

                    player.displayClientMessage(
                            Component.translatable("cable.sovietera.positions.connected",
                                    formatBlockPos(startPos), formatBlockPos(clickedPos),
                                    (int) Math.sqrt(distanceSq)).withStyle(ChatFormatting.GREEN), true);

                    // Расходуем 1 кабель (кроме творческого режима)
                    stack.shrink(1);
                    return InteractionResult.SUCCESS;
                }
            }
        } else {

            tag.putInt("LinkX", clickedPos.getX());
            tag.putInt("LinkY", clickedPos.getY());
            tag.putInt("LinkZ", clickedPos.getZ());

            player.displayClientMessage(
                    Component.translatable("cable.sovietera.positions.saved", formatBlockPos(clickedPos)), true);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.FAIL;
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
