package ru.tesmio.sovietera.blocks.baseblock;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BaseSlab extends BlockForFacingModel {
    public final VoxelShape SHP2 = Block.box(0, 0, 0, 16, 16, 8);
    String info;

    public BaseSlab(String info) {
        super(BlockBehaviour.Properties.of()
                                       .requiresCorrectToolForDrops()
                                       .strength(1f, 4f)
                                       .sound(SoundType.STONE)
                                       .noOcclusion(), 1f);
        this.info = info;
    }

    public boolean disableJSONDrop() {
        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable(info));
    }

    public VoxelShape getFacingShape(BlockState s) {
        return this.SHP2;
    }
}
