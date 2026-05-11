package ru.tesmio.sovietera.blocks.baseblock;

import net.minecraft.world.level.block.Block;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Базовый класс для всех блоков мода.
 * Предоставляет общий функционал (ThreadLocalRandom и т.д.).
 */
public class BaseBlock extends Block {
    public final ThreadLocalRandom tr = ThreadLocalRandom.current();

    public BaseBlock(Properties properties) {
        super(properties);
    }
}
