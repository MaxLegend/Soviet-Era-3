package ru.tesmio.sovietera.blocks.storages.stillage;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import ru.tesmio.sovietera.core.BlockEntitiesSE;

/**
 * Single-slot BlockEntity for the stillage shelf.
 * Stores one ItemStack, rendered on top via BlockEntityRenderer.
 */
public class EntityBlockStillage extends BlockEntity implements Container {

    private final NonNullList<ItemStack> items = NonNullList.withSize(1, ItemStack.EMPTY);

    public EntityBlockStillage(BlockPos pos, BlockState state) {
        super(BlockEntitiesSE.ENTITY_BLOCK_STILLAGE.get(), pos, state);
    }

    // ===================== Container =====================

    @Override
    public int getContainerSize() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return items.get(0).isEmpty();
    }

    @Override
    public @NotNull ItemStack getItem(int slot) {
        return items.get(slot);
    }

    /**
     * Sets the item in the slot. Triggers client sync so the renderer updates.
     */
    @Override
    public void setItem(int slot, @NotNull ItemStack stack) {
        items.set(slot, stack);
        setChanged();
        syncToClient();
    }

    @Override
    public @NotNull ItemStack removeItem(int slot, int amount) {
        ItemStack result = ContainerHelper.removeItem(items, slot, amount);
        if (!result.isEmpty()) {
            setChanged();
            syncToClient();
        }
        return result;
    }

    @Override
    public @NotNull ItemStack removeItemNoUpdate(int slot) {
        ItemStack result = ContainerHelper.takeItem(items, slot);
        setChanged();
        syncToClient();
        return result;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        // No GUI, but required by Container interface
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public void clearContent() {
        items.clear();
        setChanged();
        syncToClient();
    }

    @Override
    public int getMaxStackSize() {
        return 1; // Only one item visually fits on the shelf
    }

    // ===================== NBT / Sync =====================

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        ContainerHelper.loadAllItems(tag, items);
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        ContainerHelper.saveAllItems(tag, items);
    }

    /** Sends block update to client for renderer sync */
    private void syncToClient() {
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        ContainerHelper.saveAllItems(tag, items);
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        ContainerHelper.loadAllItems(tag, items);
    }

    // ===================== Helpers =====================

    /** Returns the stored item (or EMPTY) */
    public ItemStack getStoredItem() {
        return items.get(0);
    }

    /** Sets the stored item directly */
    public void setStoredItem(ItemStack stack) {
        items.set(0, stack);
        setChanged();
        syncToClient();
    }
}
