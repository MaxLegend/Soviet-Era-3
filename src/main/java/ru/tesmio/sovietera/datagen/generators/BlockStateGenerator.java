package ru.tesmio.sovietera.datagen.generators;

import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import ru.tesmio.sovietera.SovietEra;
import ru.tesmio.sovietera.core.BlocksSE;

/**
 * Генератор blockstate-файлов и моделей блоков.
 * Автоматически создаёт cubeAll модели для стандартных блоков из BLOCKS.
 * Для кастомных моделей (BLOCKS_CUSTOM_MODELS и др.) модели должны быть созданы вручную.
 */
public class BlockStateGenerator extends BlockStateProvider {

    public BlockStateGenerator(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, SovietEra.MODID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        defaultBlocksGenerator();
        // TODO: Добавить генераторы для stairs, slabs, custom models по мере добавления блоков
    }

    /**
     * Генерирует стандартные cubeAll blockstate + модель для всех блоков из BLOCKS.
     * Текстура должна находиться по пути assets/soviet/textures/block/<name>.png
     */
    protected void defaultBlocksGenerator() {
        for (RegistryObject<Block> block : BlocksSE.BLOCKS.getEntries()) {
            String name = ForgeRegistries.BLOCKS.getKey(block.get()).getPath();
            getVariantBuilder(block.get()).forAllStates(state ->
                    ConfiguredModel.builder()
                            .modelFile(models().cubeAll("block/" + name, modLoc("block/" + name)))
                            .build()
            );
        }
    }

    /**
     * Вспомогательный метод для получения пути блока.
     */
    protected String blockName(Block block) {
        return ForgeRegistries.BLOCKS.getKey(block).getPath();
    }

    /**
     * Создаёт модель с 3 текстурами (side, bottom, top) по указанному parent.
     */
    protected void cubeAllWithParent(Block block, String parentModel, ResourceLocation side, ResourceLocation bottom, ResourceLocation top) {
        String name = blockName(block);
        models().withExistingParent("block/" + name, parentModel)
                .texture("side", side)
                .texture("bottom", bottom)
                .texture("top", top);
    }
}
