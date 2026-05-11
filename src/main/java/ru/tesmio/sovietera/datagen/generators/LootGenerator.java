package ru.tesmio.sovietera.datagen.generators;

import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

import java.util.List;
import java.util.Set;

/**
 * Фабрика LootTableProvider.
 * Объединяет все суб-провайдеры лут-таблиц.
 */
public class LootGenerator {

    public static LootTableProvider create(PackOutput output) {
        return new LootTableProvider(output, Set.of(), List.of(
                new LootTableProvider.SubProviderEntry(BlockLootGenerator::new, LootContextParamSets.BLOCK)
        ));
    }
}
