package ru.tesmio.sovietera.blocks.baseblock.subtype;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import ru.tesmio.sovietera.blocks.baseblock.BlockInfo;

/**
 * Плиточный блок. Камень, твёрдость 5.5, стойкость 15.
 * Имеет два конструктора: с информационным тултипом и без.
 */
public class TilledBlock extends BlockInfo {
    public TilledBlock(String info) {
        super(Block.Properties.of()
                .requiresCorrectToolForDrops()
                .strength(5.5f, 15f)
                .sound(SoundType.STONE), info);
    }

    public TilledBlock() {
        super(Block.Properties.of()
                .requiresCorrectToolForDrops()
                .strength(5.5f, 15f)
                .sound(SoundType.STONE));
    }
}
