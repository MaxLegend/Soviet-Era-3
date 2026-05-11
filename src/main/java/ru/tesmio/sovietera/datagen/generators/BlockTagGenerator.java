package ru.tesmio.sovietera.datagen.generators;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import ru.tesmio.sovietera.SovietEra;

import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;

/**
 * Генератор тегов блоков.
 * Добавляет блоки мода в стандартные теги (mineable/pickaxe, needs_iron_tool и т.д.)
 */
public class BlockTagGenerator extends BlockTagsProvider {

    public BlockTagGenerator(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider,
                             @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, SovietEra.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        // TODO: Добавлять теги для блоков по мере их регистрации, например:
        // this.tag(BlockTags.MINEABLE_WITH_PICKAXE)
        //         .add(BlocksSE.SOME_BLOCK.get());
        //
        // this.tag(BlockTags.NEEDS_IRON_TOOL)
        //         .add(BlocksSE.SOME_BLOCK.get());
    }
}
