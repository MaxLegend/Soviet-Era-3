package ru.tesmio.sovietera.datagen.generators;

import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.RegistryObject;
import ru.tesmio.sovietera.core.BlocksSE;

import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Генератор лут-таблиц для блоков.
 * По умолчанию все блоки дропают сами себя (dropSelf).
 * Для особых случаев (руды, кастомный дроп) переопределяйте в generate().
 */
public class BlockLootGenerator extends BlockLootSubProvider {

    public BlockLootGenerator() {
        super(java.util.Set.of(), FeatureFlags.REGISTRY.allFlags());
    }

    @Override
    protected void generate() {
        // Автоматический dropSelf для всех блоков из всех DeferredRegister
        allBlocksDropSelf();
    }

    /**
     * Регистрирует dropSelf для всех блоков из всех категорий.
     */
    protected void allBlocksDropSelf() {
        Stream.of(
                        BlocksSE.BLOCKS.getEntries().stream(),
                        BlocksSE.BLOCKS_CUSTOM_MODELS.getEntries().stream(),
                        BlocksSE.BLOCKS_CUSTOM_MODELS_COLORED.getEntries().stream(),
                        BlocksSE.NOT_DEFAULT_BLOCKS.getEntries().stream(),
                        BlocksSE.ONLY_CUSTOM_BLOCKS.getEntries().stream()
                )
                .flatMap(Function.identity())
                .map(RegistryObject::get)
                .forEach(this::dropSelf);
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return Stream.of(
                        BlocksSE.BLOCKS.getEntries().stream(),
                        BlocksSE.BLOCKS_CUSTOM_MODELS.getEntries().stream(),
                        BlocksSE.BLOCKS_CUSTOM_MODELS_COLORED.getEntries().stream(),
                        BlocksSE.NOT_DEFAULT_BLOCKS.getEntries().stream(),
                        BlocksSE.ONLY_CUSTOM_BLOCKS.getEntries().stream()
                )
                .flatMap(Function.identity())
                .map(RegistryObject::get)
                .collect(Collectors.toList());
    }
}
