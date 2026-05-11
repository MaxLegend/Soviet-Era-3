package ru.tesmio.sovietera.blocks.baseblock.subtype;


import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import ru.tesmio.sovietera.blocks.baseblock.BlockInfo;

/**
 * Железобетонный блок. Очень прочный: твёрдость 6, стойкость 16.
 */
public class FerroconcreteBlock extends BlockInfo {
    public FerroconcreteBlock(String info) {
        super(Block.Properties.of()
                .requiresCorrectToolForDrops()
                .strength(6f, 16f)
                .sound(SoundType.STONE), info);
    }
}
