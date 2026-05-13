package ru.tesmio.sovietera.datagen.generators;

import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.StairsShape;
import net.minecraftforge.client.model.generators.BlockModelBuilder;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ModelBuilder;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import ru.tesmio.sovietera.SovietEra;
import ru.tesmio.sovietera.blocks.baseblock.BaseSlab;
import ru.tesmio.sovietera.blocks.baseblock.BaseStairs;
import ru.tesmio.sovietera.blocks.baseblock.BlockForFacing;
import ru.tesmio.sovietera.blocks.baseblock.BlockModelSide;
import ru.tesmio.sovietera.blocks.baseblock.subtype.WindProofPanel;
import ru.tesmio.sovietera.core.BlocksSE;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Генератор blockstate-файлов и моделей блоков.
 * Автоматически создаёт cubeAll модели для стандартных блоков,
 * а также генерирует модели для stairs, slabs и кастомных моделей.
 */
public class BlockStateGenerator extends BlockStateProvider {

    public BlockStateGenerator(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, SovietEra.MODID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        variantBuilderAll();
        builderStairs();
        blockWithCustomModelBuilder();
    }

    // ===================== Стандартные блоки (cubeAll) =====================

    /**
     * Генерирует стандартные cubeAll blockstate + модель для всех блоков из BLOCKS,
     * исключая ToxicAir, BaseStairs, BaseSlab и блок жидкости.
     */
    protected void variantBuilderAll() {
        for (RegistryObject<Block> b2 : BlocksSE.BLOCKS.getEntries()) {
            if ( !(b2.get() instanceof BaseStairs)
                    && !(b2.get() instanceof BaseSlab)
                /* && !(b2.get() == FluidsSE.TOXIC_WATER_BLOCK.get()) */) {
                String name = blockName(b2.get());
                getVariantBuilder(b2.get()).forAllStates(state ->
                        ConfiguredModel.builder()
                                       .modelFile(models().cubeAll("block/" + name, modLoc("block/" + name)))
                                       .build()
                );
            }
        }
    }

    // ===================== Кастомные модели (WindProofPanel, BaseSlab) =====================

    /**
     * Генерирует blockstate для блоков с кастомными моделями из BLOCKS_CUSTOM_MODELS_COLORED.
     * Обрабатывает WindProofPanel и BaseSlab.
     */
    protected void blockWithCustomModelBuilder() {
        AtomicInteger down = new AtomicInteger();
        AtomicInteger up = new AtomicInteger();

        for (RegistryObject<Block> block : BlocksSE.BLOCKS_CUSTOM_MODELS_COLORED.getEntries()) {

            if (block.get() instanceof WindProofPanel) {
                String name = ForgeRegistries.BLOCKS.getKey(block.get()).toString();
                String loc2 = name.replaceAll(name, "block/concrete/concrete" + name.substring(43));
                getVariantBuilder(block.get())
                        .forAllStatesExcept(state -> {
                            Direction dir = state.getValue(BlockModelSide.FACING);
                            return ConfiguredModel.builder()
                                                  .modelFile(builderForParent("block/" + name.substring(7),
                                                          "soviet:block/outerdeco/streetdeco/windproof_beton",
                                                          modLoc(loc2), "0"))
                                                  .rotationY(dir.getAxis().isVertical() ? 0 : (((int) dir.toYRot())) % 360)
                                                  .build();
                        }, BlockModelSide.WATERLOGGED);
            }

            if (block.get() instanceof BaseSlab) {
                String name = ForgeRegistries.BLOCKS.getKey(block.get()).toString();
                String loc2 = name.replaceAll(name, "block/concrete/concrete" + name.substring(26));
                getVariantBuilder(block.get())
                        .forAllStatesExcept(state -> {
                            BlockForFacing.EnumOrientation enumOrient = state.getValue(BlockForFacing.FACING);
                            Direction orient = enumOrient.getDirection();
                            down.set(orient.getStepY() * 90);
                            up.set(orient.getStepY() * (-270));
                            return ConfiguredModel.builder()
                                                  .modelFile(builderForParent("block/" + name.substring(7),
                                                          "soviet:block/structural/slab",
                                                          modLoc(loc2), "0"))
                                                  .rotationY(orient.getAxis().isVertical() ? 0 : (((int) orient.toYRot())) % 360)
                                                  .rotationX(orient.getStepY() == 1 ? down.get() : up.get())
                                                  .build();
                        }, BlockModelSide.WATERLOGGED);
            }
        }
    }

    /**
     * Генерирует blockstate для блоков с осевой ротацией и кастомными моделями (BaseSlab).
     */
    protected void blockRotAxisCustomModelBuilder() {
        AtomicInteger down = new AtomicInteger();
        AtomicInteger up = new AtomicInteger();

        for (RegistryObject<Block> block : BlocksSE.BLOCKS_CUSTOM_MODELS_COLORED.getEntries()) {
            if (block.get() instanceof BaseSlab) {
                String name = ForgeRegistries.BLOCKS.getKey(block.get()).toString();
                String loc2 = name.replaceAll(name, "block/concrete/concrete" + name.substring(26));
                getVariantBuilder(block.get())
                        .forAllStatesExcept(state -> {
                            BlockForFacing.EnumOrientation enumOrient = state.getValue(BlockForFacing.FACING);
                            Direction orient = enumOrient.getDirection();
                            down.set(orient.getStepY() * 90);
                            up.set(orient.getStepY() * (-270));
                            return ConfiguredModel.builder()
                                                  .modelFile(builderForParent("block/" + name.substring(7),
                                                          "soviet:block/structural/slab",
                                                          modLoc(loc2), "0"))
                                                  .rotationY(orient.getAxis().isVertical() ? 0 : (((int) orient.toYRot())) % 360)
                                                  .rotationX(orient.getStepY() == 1 ? down.get() : up.get())
                                                  .build();
                        }, BlockModelSide.WATERLOGGED);
            }
        }
    }

    // ===================== Лестницы (Stairs) =====================

    /**
     * Генерирует blockstate для лестниц (BaseStairs) со стандартными parent-моделями.
     */
    protected void builderStairs() {
        for (RegistryObject<Block> b2 : BlocksSE.BLOCKS.getEntries()) {
            if (b2.get() instanceof BaseStairs) {
                getVariantBuilder(b2.get())
                        .forAllStatesExcept(state -> {
                            Direction facing = state.getValue(StairBlock.FACING);
                            Half half = state.getValue(StairBlock.HALF);
                            StairsShape shape = state.getValue(StairBlock.SHAPE);
                            int yRot = (int) facing.toYRot();
                            if (shape == StairsShape.INNER_LEFT || shape == StairsShape.OUTER_LEFT) {
                                yRot += 270;
                            }
                            if (shape != StairsShape.STRAIGHT && half == Half.TOP) {
                                yRot += 90;
                            }
                            yRot %= 360;
                            boolean uvlock = yRot != 0 || half == Half.TOP;
                            String name = ForgeRegistries.BLOCKS.getKey(b2.get()).toString();

                            String loc = "block/" + name.substring(7, name.length() - 7);
                            String loc2 = loc.replaceAll(loc, "block/concrete/" + loc.substring(13));

                            switch (shape) {
                                case STRAIGHT:
                                default:
                                    return ConfiguredModel.builder()
                                                          .modelFile(builder3TexturesModel(
                                                                  "block/" + name.substring(7), "block/stairs",
                                                                  modLoc(loc2), modLoc(loc2), modLoc("block/concrete/concrete_gray")))
                                                          .rotationX(half == Half.BOTTOM ? 0 : 180)
                                                          .rotationY(yRot)
                                                          .uvLock(uvlock)
                                                          .build();
                                case INNER_LEFT:
                                case INNER_RIGHT:
                                    return ConfiguredModel.builder()
                                                          .modelFile(builder3TexturesModel(
                                                                  "block/" + name.substring(7) + "_inner", "block/inner_stairs",
                                                                  modLoc(loc2), modLoc(loc2), modLoc("block/concrete/concrete_gray")))
                                                          .rotationX(half == Half.BOTTOM ? 0 : 180)
                                                          .rotationY(yRot)
                                                          .uvLock(uvlock)
                                                          .build();
                                case OUTER_RIGHT:
                                case OUTER_LEFT:
                                    return ConfiguredModel.builder()
                                                          .modelFile(builder3TexturesModel(
                                                                  "block/" + name.substring(7) + "_outer", "block/outer_stairs",
                                                                  modLoc(loc2), modLoc(loc2), modLoc("block/concrete/concrete_gray")))
                                                          .rotationX(half == Half.BOTTOM ? 0 : 180)
                                                          .rotationY(yRot)
                                                          .uvLock(uvlock)
                                                          .build();
                            }
                        }, StairBlock.WATERLOGGED);
            }
        }
    }

    /**
     * Генерирует blockstate для лестниц-сводов (BaseStairs) с vault-моделями.
     */
    protected void builderVault() {
        for (RegistryObject<Block> b2 : BlocksSE.BLOCKS.getEntries()) {
            if (b2.get() instanceof BaseStairs) {
                getVariantBuilder(b2.get())
                        .forAllStatesExcept(state -> {
                            Direction facing = state.getValue(StairBlock.FACING);
                            Half half = state.getValue(StairBlock.HALF);
                            StairsShape shape = state.getValue(StairBlock.SHAPE);
                            int yRot = (int) facing.toYRot();
                            if (shape == StairsShape.INNER_LEFT || shape == StairsShape.OUTER_LEFT) {
                                yRot += 270;
                            }
                            if (shape != StairsShape.STRAIGHT && half == Half.TOP) {
                                yRot += 90;
                            }
                            yRot %= 360;
                            boolean uvlock = yRot != 0 || half == Half.TOP;
                            String name = ForgeRegistries.BLOCKS.getKey(b2.get()).toString();

                            String loc = "block/" + name.substring(7, name.length() - 7);
                            String loc2 = loc.replaceAll(loc, "block/concrete/" + loc.substring(13));

                            switch (shape) {
                                case STRAIGHT:
                                default:
                                    return ConfiguredModel.builder()
                                                          .modelFile(builder3TexturesModel(
                                                                  "block/" + name.substring(7), "block/structural/vault_block",
                                                                  modLoc(loc2), modLoc(loc2), modLoc("block/concrete/concrete_gray")))
                                                          .rotationX(half == Half.BOTTOM ? 0 : 180)
                                                          .rotationY(yRot)
                                                          .uvLock(uvlock)
                                                          .build();
                                case INNER_LEFT:
                                case INNER_RIGHT:
                                    return ConfiguredModel.builder()
                                                          .modelFile(builder3TexturesModel(
                                                                  "block/" + name.substring(7) + "_inner", "block/structural/inner_vault_block",
                                                                  modLoc(loc2), modLoc(loc2), modLoc("block/concrete/concrete_gray")))
                                                          .rotationX(half == Half.BOTTOM ? 0 : 180)
                                                          .rotationY(yRot)
                                                          .uvLock(uvlock)
                                                          .build();
                                case OUTER_RIGHT:
                                case OUTER_LEFT:
                                    return ConfiguredModel.builder()
                                                          .modelFile(builder3TexturesModel(
                                                                  "block/" + name.substring(7) + "_outer", "block/structural/outer_vault_block",
                                                                  modLoc(loc2), modLoc(loc2), modLoc("block/concrete/concrete_gray")))
                                                          .rotationX(half == Half.BOTTOM ? 0 : 180)
                                                          .rotationY(yRot)
                                                          .uvLock(uvlock)
                                                          .build();
                            }
                        }, StairBlock.WATERLOGGED);
            }
        }
    }

    // ===================== Вспомогательные методы =====================

    /**
     * Вспомогательный метод для получения пути блока.
     */
    protected String blockName(Block block) {
        return ForgeRegistries.BLOCKS.getKey(block).getPath();
    }

    /**
     * Создаёт cubeAll модель по имени и текстуре.
     */
    protected ModelBuilder<BlockModelBuilder> builder(String name, ResourceLocation rs) {
        return models().cubeAll(name, rs);
    }

    /**
     * Создаёт модель с указанным parent и одной текстурой по ключу.
     */
    protected ModelBuilder<BlockModelBuilder> builderForParent(String name, String parent, ResourceLocation color, String textureKey) {
        return models().withExistingParent(name, parent)
                       .texture(textureKey, color);
    }

    /**
     * Создаёт модель с 3 текстурами (side, bottom, top) по указанному parent.
     */
    protected ModelBuilder<BlockModelBuilder> builder3TexturesModel(String name, String parent, ResourceLocation side, ResourceLocation bottom, ResourceLocation top) {
        return models().withExistingParent(name, parent)
                       .texture("side", side)
                       .texture("bottom", bottom)
                       .texture("top", top);
    }
}
