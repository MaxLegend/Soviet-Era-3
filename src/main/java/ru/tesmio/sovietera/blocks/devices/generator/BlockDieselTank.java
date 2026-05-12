
package ru.tesmio.sovietera.blocks.devices.generator;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.Nullable;
import ru.tesmio.sovietera.blocks.baseblock.BlockModelSide;

import ru.tesmio.sovietera.utils.ShapesUtil;

/**
 * Блок бака дизельного генератора.
 * Имеет BlockEntityDieselTank с FluidTank на 16000mB.
 * Поддерживает заливку воды через ПКМ ведром.
 */
public class BlockDieselTank extends BlockModelSide implements EntityBlock {
    private static final VoxelShape AABB = Shapes.or(
            Block.box(2D, 0D, 3D, 16D, 13.0D, 13D),
            Block.box(1D, 0D, 4D, 16D, 12.0D, 12D),
            Block.box(0D, 0D, 5D, 16D, 11.0D, 11D));
    public BlockDieselTank(Properties properties, float shadingInside) {
        super(properties, shadingInside);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new EntityBlockDieselTank(pos, state);
    }

    // Заливка воды из ведра — автономно, без участия контроллера
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;

        ItemStack heldItem = player.getItemInHand(hand);
        if (heldItem.is(Items.WATER_BUCKET)) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof EntityBlockDieselTank tankBE) {
                return tankBE.getCapability(ForgeCapabilities.FLUID_HANDLER, null)
                             .map(fluidHandler -> {
                                 int filled = fluidHandler.fill(
                                         new FluidStack(Fluids.WATER, 1000),
                                         IFluidHandler.FluidAction.EXECUTE);
                                 if (filled > 0) {
                                     if (!player.isCreative()) {
                                         player.setItemInHand(hand, new ItemStack(Items.BUCKET));
                                     }
                                     return InteractionResult.CONSUME;
                                 }
                                 return InteractionResult.PASS;
                             })
                             .orElse(InteractionResult.PASS);
            }
        }

        return InteractionResult.PASS;
    }
    @Override
    public float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return 1.0F;
    }

    public VoxelShape getFacingShape(BlockState s) {
        return ShapesUtil.rotate(AABB, ShapesUtil.RotationDegree.D90);
    }
    @Override
    public void onRemove(BlockState oldState, Level level, BlockPos pos,
            BlockState newState, boolean isMoving) {
        if (!oldState.is(newState.getBlock())) {
            level.removeBlockEntity(pos);
        }
    }
}