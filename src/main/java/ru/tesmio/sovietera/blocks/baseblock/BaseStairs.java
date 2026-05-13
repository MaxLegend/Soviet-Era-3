package ru.tesmio.sovietera.blocks.baseblock;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BaseStairs extends StairBlock {
    String info;

    public BaseStairs(BlockState state, Properties properties, String info) {
        super(state, properties);
        this.info = info;
    }

    public BaseStairs(BlockState state, String info) {
        super(state, BlockBehaviour.Properties.of()
                                              .requiresCorrectToolForDrops()
                                              .strength(1f, 4f)
                                              .sound(SoundType.STONE)
                                              .noOcclusion());
        this.info = info;
    }

    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity te, ItemStack stack) {
        player.awardStat(Stats.BLOCK_MINED.get(this));
        player.causeFoodExhaustion(0.005F);
        Block.dropResources(state, level, pos, te, player, stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable(info));
    }
}
