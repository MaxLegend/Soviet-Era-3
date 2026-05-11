package ru.tesmio.sovietera.datagen.generators;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.ExistingFileHelper;
import ru.tesmio.sovietera.SovietEra;

import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;

/**
 * Генератор тегов предметов.
 * Наследует теги блоков через blockTagProvider.contentsGetter().
 */
public class ItemTagGenerator extends ItemTagsProvider {

    public ItemTagGenerator(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider,
                            CompletableFuture<TagLookup<Block>> blockTagProvider,
                            @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, blockTagProvider, SovietEra.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        // TODO: Добавлять теги для предметов по мере необходимости, например:
        // this.copy(BlockTags.MINEABLE_WITH_PICKAXE, ItemTags.PICKAXE_MINABLE);
    }
}
