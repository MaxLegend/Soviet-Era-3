package ru.tesmio.sovietera.datagen.generators;

import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import ru.tesmio.sovietera.SovietEra;
import ru.tesmio.sovietera.core.BlocksSE;
import ru.tesmio.sovietera.core.ItemsSE;

/**
 * Генератор моделей предметов.
 * Автоматически создаёт:
 * - item/generated модели для standalone предметов из ItemsSE.ITEMS
 * - модели blockItem (parent = block model) для всех блоков из всех DeferredRegister
 */
public class ItemModelGenerator extends ItemModelProvider {

    public ItemModelGenerator(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, SovietEra.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        ModelFile itemGenerated = getExistingFile(mcLoc("item/generated"));

        defaultItemGenerator(itemGenerated);
        defaultItemBlockGenerator();
    }

    /**
     * Создаёт ModelFile без проверки существования файла.
     * Используется для ссылок на модели блоков, которые генерируются
     * BlockStateGenerator в этом же запуске датагена — ExistingFileHelper
     * ещё не знает о них, поэтому getExistingFile() выбрасывает IllegalStateException.
     */
    private ModelFile uncheckedBlockModel(String name) {
        return new ModelFile(modLoc("block/" + name)) {
            @Override
            protected boolean exists() {
                return true;
            }
        };
    }

    /**
     * Генерирует item/generated модели для standalone предметов.
     * Текстура: assets/soviet/textures/item/<name>.png
     */
    private void defaultItemGenerator(ModelFile parent) {
        for (RegistryObject<Item> item : ItemsSE.ITEMS.getEntries()) {
            String name = ForgeRegistries.ITEMS.getKey(item.get()).getPath();
            getBuilder("item/" + name)
                    .parent(parent)
                    .texture("layer0", modLoc("item/" + name));
        }
    }

    /**
     * Генерирует модели предметов-блоков (parent = block model)
     * для всех блоков из всех DeferredRegister.
     *
     * Использует uncheckedBlockModel() вместо getExistingFile(),
     * потому что модели блоков генерируются в этом же запуске датагена
     * и ExistingFileHelper их ещё не видит.
     */
    private void defaultItemBlockGenerator() {
        // Стандартные блоки (BLOCKS)
        for (RegistryObject<Block> block : BlocksSE.BLOCKS.getEntries()) {
            String name = ForgeRegistries.BLOCKS.getKey(block.get()).getPath();
            getBuilder("item/" + name).parent(uncheckedBlockModel(name));
        }

        // Блоки с кастомными моделями (BLOCKS_CUSTOM_MODELS)
        for (RegistryObject<Block> block : BlocksSE.BLOCKS_CUSTOM_MODELS.getEntries()) {
            String name = ForgeRegistries.BLOCKS.getKey(block.get()).getPath();
            getBuilder("item/" + name).parent(uncheckedBlockModel(name));
        }

        // Блоки с кастомными окрашиваемыми моделями (BLOCKS_CUSTOM_MODELS_COLORED)
        for (RegistryObject<Block> block : BlocksSE.BLOCKS_CUSTOM_MODELS_COLORED.getEntries()) {
            String name = ForgeRegistries.BLOCKS.getKey(block.get()).getPath();
            getBuilder("item/" + name).parent(uncheckedBlockModel(name));
        }

        // Нестандартные блоки (NOT_DEFAULT_BLOCKS)
        for (RegistryObject<Block> block : BlocksSE.NOT_DEFAULT_BLOCKS.getEntries()) {
            String name = ForgeRegistries.BLOCKS.getKey(block.get()).getPath();
            getBuilder("item/" + name).parent(uncheckedBlockModel(name));
        }

        // Только кастомные блоки (ONLY_CUSTOM_BLOCKS)
        for (RegistryObject<Block> block : BlocksSE.ONLY_CUSTOM_BLOCKS.getEntries()) {
            String name = ForgeRegistries.BLOCKS.getKey(block.get()).getPath();
            getBuilder("item/" + name).parent(uncheckedBlockModel(name));
        }
    }
}
