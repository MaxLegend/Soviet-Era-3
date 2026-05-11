package ru.tesmio.sovietera.blocks.baseblock.subtype;

import net.minecraft.world.level.block.Block;
import ru.tesmio.sovietera.blocks.baseblock.BaseBlock;

/**
 * Кирпичный блок. Стандартные свойства: камень, твёрдость 3, стойкость 8.
 */
public class BrickBlock extends BaseBlock {
    public BrickBlock() {
        super(Block.Properties.of()
                .requiresCorrectToolForDrops()
                .strength(3f, 8f));
    }
}
