package ru.tesmio.sovietera.blocks.baseblock.subtype;


import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import ru.tesmio.sovietera.blocks.baseblock.BlockInfo;

/**
 * Блок обоев. Растительный материал, звук травы.
 */
public class WallpaperBlock extends BlockInfo {
    public WallpaperBlock(String info) {
        super(Block.Properties.of()
                .requiresCorrectToolForDrops()
                .strength(1.5f, 2f)
                .sound(SoundType.GRASS), info);
    }
}
