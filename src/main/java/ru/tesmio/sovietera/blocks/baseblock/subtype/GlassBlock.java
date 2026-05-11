package ru.tesmio.sovietera.blocks.baseblock.subtype;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import ru.tesmio.sovietera.blocks.baseblock.BaseBlock;

/**
 * Стеклянный (керамический) блок. Прозрачный, не окклюзивный.
 */
public class GlassBlock extends BaseBlock {
    public GlassBlock() {
        super(Block.Properties.of()
                .requiresCorrectToolForDrops()
                .strength(1f, 2f)
                .sound(SoundType.GLASS)
                .noOcclusion());
    }
}
