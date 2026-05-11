package ru.tesmio.sovietera.blocks.baseblock.subtype;


import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import ru.tesmio.sovietera.blocks.baseblock.BlockInfo;

/**
 * Повреждённый железобетонный блок. Те же свойства, что и FerroconcreteBlock,
 * но используется для визуально повреждённых вариантов.
 */
public class FerroconcreteBlockDmg extends BlockInfo {
    public FerroconcreteBlockDmg(String info) {
        super(Block.Properties.of()
                .requiresCorrectToolForDrops()
                .strength(6f, 16f)
                .sound(SoundType.STONE), info);
    }
}
