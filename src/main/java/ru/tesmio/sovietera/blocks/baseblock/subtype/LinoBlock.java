package ru.tesmio.sovietera.blocks.baseblock.subtype;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import ru.tesmio.sovietera.blocks.baseblock.BaseBlock;

/**
 * Линолеумный блок. Тканевый материал, мягкий звук.
 */
public class LinoBlock extends BaseBlock {
    public LinoBlock() {
        super(Block.Properties.of()
                .requiresCorrectToolForDrops()
                .strength(1f, 2f)
                .sound(SoundType.WOOL));
    }
}
