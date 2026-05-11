package ru.tesmio.sovietera.blocks.baseblock.subtype;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import ru.tesmio.sovietera.blocks.baseblock.BaseBlock;
import ru.tesmio.sovietera.blocks.baseblock.BlockInfo;

/**
 * Металлический блок. Стандартные свойства: камень, твёрдость 3, стойкость 8.
 */
public class MetalBlock extends BaseBlock {
    public MetalBlock() {
        super(Block.Properties.of()
                .requiresCorrectToolForDrops()
                .strength(3f, 8f));
    }
}
