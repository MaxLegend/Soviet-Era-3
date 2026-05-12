package ru.tesmio.sovietera.core;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import ru.tesmio.sovietera.SovietEra;
import ru.tesmio.sovietera.blocks.baseblock.BaseBlock;
import ru.tesmio.sovietera.blocks.baseblock.subtype.*;
import ru.tesmio.sovietera.blocks.devices.generator.BlockDieselEngine;
import ru.tesmio.sovietera.blocks.devices.generator.BlockDieselTank;
import ru.tesmio.sovietera.blocks.devices.generator.BlockElectroGenerator;
import ru.tesmio.sovietera.blocks.devices.cable.BlockPowerConnector;

import javax.annotation.Nullable;
import java.util.function.Supplier;

/**
 * Центральный класс регистрации блоков.
 * Содержит несколько DeferredRegister для разных категорий блоков:
 * <ul>
 *   <li>BLOCKS — стандартные блоки с обычной моделью (куб)</li>
 *   <li>BLOCKS_CUSTOM_MODELS — блоки с кастомными моделями</li>
 *   <li>BLOCKS_CUSTOM_MODELS_COLORED — блоки с кастомными моделями и окрашиванием</li>
 *   <li>ONLY_CUSTOM_BLOCKS — блоки только с кастомной моделью (без стандартного рендера)</li>
 *   <li>NOT_DEFAULT_BLOCKS — блоки с нестандартным поведением (панели и т.д.)</li>
 *   <li>ITEM_BLOCKS — BlockItem-ы для всех зарегистрированных блоков</li>
 * </ul>
 */
public class BlocksSE {

    public static final DeferredRegister<Block> ONLY_CUSTOM_BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, SovietEra.MODID);
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, SovietEra.MODID);
    public static final DeferredRegister<Block> NOT_DEFAULT_BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, SovietEra.MODID);
    public static final DeferredRegister<Block> BLOCKS_CUSTOM_MODELS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, SovietEra.MODID);
    public static final DeferredRegister<Block> BLOCKS_CUSTOM_MODELS_COLORED =
            DeferredRegister.create(ForgeRegistries.BLOCKS, SovietEra.MODID);
    public static final DeferredRegister<Item> ITEM_BLOCKS =
            DeferredRegister.create(ForgeRegistries.ITEMS, SovietEra.MODID);

    /**
     * Регистрирует все DeferredRegister на шине событий и вызывает init().
     * Вызывается из Registration.init().
     */
    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
        BLOCKS_CUSTOM_MODELS_COLORED.register(eventBus);
        BLOCKS_CUSTOM_MODELS.register(eventBus);
        NOT_DEFAULT_BLOCKS.register(eventBus);
        ONLY_CUSTOM_BLOCKS.register(eventBus);
        ITEM_BLOCKS.register(eventBus);
        init();
    }

    /**
     * Инициализация всех блоков мода.
     * Блоки сгруппированы по типу. Для каждой группы объявлены
     * общие свойства (Properties), которые переиспользуются.
     */
    public static void init() {
 
        // =====================================================================
        //  CONCRETE — Железобетонные блоки (FerroconcreteBlock / Dmg)
        // =====================================================================
        CONCRETE_ORANGE = registerBlock("concrete/concrete_orange", () -> new FerroconcreteBlock("info.orange"));
        CONCRETE_BLUE = registerBlock("concrete/concrete_blue", () -> new FerroconcreteBlock("info.blue"));
        CONCRETE_RED = registerBlock("concrete/concrete_red", () -> new FerroconcreteBlock("info.red"));
        CONCRETE_YELLOW = registerBlock("concrete/concrete_yellow", () -> new FerroconcreteBlock("info.yellow"));
        CONCRETE_WHITE = registerBlock("concrete/concrete_white", () -> new FerroconcreteBlock("info.white"));
        CONCRETE_BEIGE = registerBlock("concrete/concrete_beige", () -> new FerroconcreteBlock("info.beige"));
        CONCRETE_GREEN = registerBlock("concrete/concrete_green", () -> new FerroconcreteBlock("info.green"));
        CONCRETE_GRAY = registerBlock("concrete/concrete_gray", () -> new FerroconcreteBlock("info.gray"));
        CONCRETE_BEIGE2 = registerBlock("concrete/concrete_beige2", () -> new FerroconcreteBlock("info.beige2"));
        // damaged variants
        CONCRETE_ORANGE_BR = registerBlock("concrete/concrete_orange_br", () -> new FerroconcreteBlockDmg("info.orange"));
        CONCRETE_BLUE_BR = registerBlock("concrete/concrete_blue_br", () -> new FerroconcreteBlockDmg("info.blue"));
        CONCRETE_RED_BR = registerBlock("concrete/concrete_red_br", () -> new FerroconcreteBlockDmg("info.red"));
        CONCRETE_YELLOW_BR = registerBlock("concrete/concrete_yellow_br", () -> new FerroconcreteBlockDmg("info.yellow"));
        CONCRETE_WHITE_BR = registerBlock("concrete/concrete_white_br", () -> new FerroconcreteBlockDmg("info.white"));
        CONCRETE_BEIGE_BR = registerBlock("concrete/concrete_beige_br", () -> new FerroconcreteBlockDmg("info.beige"));
        CONCRETE_GREEN_BR = registerBlock("concrete/concrete_green_br", () -> new FerroconcreteBlockDmg("info.green"));
        CONCRETE_GRAY_BR = registerBlock("concrete/concrete_gray_br", () -> new FerroconcreteBlockDmg("info.gray"));
        CONCRETE_BEIGE2_BR = registerBlock("concrete/concrete_beige2_br", () -> new FerroconcreteBlockDmg("info.beige2"));

        // =====================================================================
        //  BRICKS — Кирпичные блоки (BrickBlock)
        // =====================================================================
        RED_BRICKS = registerBlock("brick/red_bricks", BrickBlock::new);
        RED_BRICKS_BR = registerBlock("brick/red_bricks_br", BrickBlock::new);
        SHORT_BRICKS = registerBlock("brick/short_bricks", BrickBlock::new);
        BRICKS = registerBlock("brick/soviet_bricks", BrickBlock::new);
        BRICKS_BR = registerBlock("brick/soviet_bricks_br", BrickBlock::new);
        BRICKS_WITH_WHITE = registerBlock("brick/soviet_bricks_with_white", BrickBlock::new);
        WALL_BRICKS = registerBlock("brick/light_bricks2", BrickBlock::new);
        WALL_BRICKS_BR = registerBlock("brick/light_bricks2_br", BrickBlock::new);
        LIGHT_BRICKS = registerBlock("brick/light_bricks", BrickBlock::new);
        LIGHT_BRICKS_BR = registerBlock("brick/light_bricks_br", BrickBlock::new);
        YELLOW_BRICKS_1 = registerBlock("brick/yellow_bricks_1", BrickBlock::new);
        YELLOW_BRICKS_1_BR = registerBlock("brick/yellow_bricks_1_br", BrickBlock::new);
        YELLOW_BRICKS_2 = registerBlock("brick/yellow_bricks_2", BrickBlock::new);
        YELLOW_BRICKS_2_BR = registerBlock("brick/yellow_bricks_2_br", BrickBlock::new);
        YELLOW_BRICKS_3 = registerBlock("brick/yellow_bricks_3", BrickBlock::new);
        YELLOW_BRICKS_3_BR = registerBlock("brick/yellow_bricks_3_br", BrickBlock::new);
        WHITE_BRICKS = registerBlock("brick/white_bricks", BrickBlock::new);
        WHITE_BRICKS_BR = registerBlock("brick/white_bricks_br", BrickBlock::new);
        SMALL_BRICKS = registerBlock("brick/small_bricks", BrickBlock::new);

        // =====================================================================
        //  QUAD TILES — Квадратная плитка (TilledBlock)
        // =====================================================================
        TILE_QUAD_GRAY = registerBlock("quadtile/tile_quad_gray", () -> new TilledBlock("info.quadtile_gray"));
        TILE_QUAD_WHITE = registerBlock("quadtile/tile_quad_white", () -> new TilledBlock("info.quadtile_white"));
        TILE_QUAD_BLUE = registerBlock("quadtile/tile_quad_blue", () -> new TilledBlock("info.quadtile_blue"));
        TILE_QUAD_CONCRETE = registerBlock("quadtile/tile_quad_concrete", () -> new TilledBlock("info.quadtile_concrete"));
        TILE_QUAD_WHITE_BR = registerBlock("quadtile/tile_quad_white_br", () -> new TilledBlock("info.quadtile_white_br"));
        TILE_QUAD_BLUE_BR = registerBlock("quadtile/tile_quad_blue_br", () -> new TilledBlock("info.quadtile_blue_br"));
        TILE_QUAD_1 = registerBlock("quadtile/tile_quad_1", () -> new TilledBlock("info.quadtile_1"));
        TILE_QUAD_1_BR = registerBlock("quadtile/tile_quad_1_br", () -> new TilledBlock("info.quadtile_1_br"));
        TILE_QUAD_2 = registerBlock("quadtile/tile_quad_2", () -> new TilledBlock("info.quadtile_2"));
        TILE_QUAD_2_BR = registerBlock("quadtile/tile_quad_2_br", () -> new TilledBlock("info.quadtile_2_br"));
        TILE_QUAD_3 = registerBlock("quadtile/tile_quad_3", () -> new TilledBlock("info.quadtile_3"));
        TILE_QUAD_3_BR = registerBlock("quadtile/tile_quad_3_br", () -> new TilledBlock("info.quadtile_3_br"));
        TILE_QUAD_4 = registerBlock("quadtile/tile_quad_4", () -> new TilledBlock("info.quadtile_4"));
        TILE_QUAD_5 = registerBlock("quadtile/tile_quad_5", () -> new TilledBlock("info.quadtile_5"));
        TILE_QUAD_5_BR = registerBlock("quadtile/tile_quad_5_br", () -> new TilledBlock("info.quadtile_5_br"));
        TILE_QUAD_5_BRf = registerBlock("quadtile/tile_quad_5_brf", () -> new TilledBlock("info.quadtile_5_brf"));
     //   TILE_QUAD_6 = registerBlock("quadtile/tile_quad_6", () -> new TilledBlock("info.quadtile_6"));

        // =====================================================================
        //  REST TILES — Ресторанная плитка (TilledBlock)
        // =====================================================================
        TILE_REST_DARK_BLUE = registerBlock("resttile/tile_rest_dark_blue", () -> new TilledBlock("info.tile_rest_dark_blue"));
        TILE_REST_DARK_BLUE_BR = registerBlock("resttile/tile_rest_dark_blue_br", () -> new TilledBlock("info.tile_rest_dark_blue_br"));
        TILE_REST_BLUE = registerBlock("resttile/tile_rest_blue", () -> new TilledBlock("info.tile_rest_blue"));
        TILE_REST_BLUE_BR = registerBlock("resttile/tile_rest_blue_br", () -> new TilledBlock("info.tile_rest_blue_br"));
        TILE_REST_BLACK = registerBlock("resttile/tile_rest_black", () -> new TilledBlock("info.tile_rest_black"));
        TILE_REST_BLACK_BR = registerBlock("resttile/tile_rest_black_br", () -> new TilledBlock("info.tile_rest_black_br"));
        TILE_REST_BROWN = registerBlock("resttile/tile_rest_brown", () -> new TilledBlock("info.tile_rest_brown"));
        TILE_REST_BROWN_BR = registerBlock("resttile/tile_rest_brown_br", () -> new TilledBlock("info.tile_rest_brown_br"));
        TILE_REST_WHITE = registerBlock("resttile/tile_rest_white", () -> new TilledBlock("info.tile_rest_white"));
        TILE_REST_WHITE_BR = registerBlock("resttile/tile_rest_white_br", () -> new TilledBlock("info.tile_rest_white_br"));

        // =====================================================================
        //  REGULAR TILES — Обычная плитка (TilledBlock)
        // =====================================================================
        REGULAR_BROWN_TILE = registerBlock("regtile/tile_reg_brown", () -> new TilledBlock("info.tile_reg_brown"));
        REGULAR_BROWN_TILE_BR = registerBlock("regtile/tile_reg_brown_br", () -> new TilledBlock("info.tile_reg_brown_br"));
        REGULAR_LIL_TILE = registerBlock("regtile/tile_reg_lil", () -> new TilledBlock("info.tile_reg_lil"));
        REGULAR_LIL_TILE_BR = registerBlock("regtile/tile_reg_lil_br", () -> new TilledBlock("info.tile_reg_lil_br"));
        REGULAR_AM_TILE = registerBlock("regtile/tile_reg_am", () -> new TilledBlock("info.tile_reg_am"));
        REGULAR_AM_TILE_BR = registerBlock("regtile/tile_reg_am_br", () -> new TilledBlock("info.tile_reg_am_br"));

        // =====================================================================
        //  HORIZONTAL TILES — Горизонтальная плитка (TilledBlock)
        // =====================================================================
        HORIZ_TILE_BLUE = registerBlock("horiztile/horiz_tile_blue", () -> new TilledBlock("info.horiz_tile_blue"));
        HORIZ_TILE_BLUE_BR = registerBlock("horiztile/horiz_tile_blue_br", () -> new TilledBlock("info.horiz_tile_blue_br"));
        HORIZ_TILE_WHITE = registerBlock("horiztile/horiz_tile_white", () -> new TilledBlock("info.horiz_tile_white"));
        HORIZ_TILE_WHITE_BR = registerBlock("horiztile/horiz_tile_white_br", () -> new TilledBlock("info.horiz_tile_white_br"));
        HORIZ_TILE_DARK_BLUE = registerBlock("horiztile/horiz_tile_dark_blue", () -> new TilledBlock("info.horiz_tile_dark_blue"));
        HORIZ_TILE_DARK_BLUE_BR = registerBlock("horiztile/horiz_tile_dark_blue_br", () -> new TilledBlock("info.horiz_tile_dark_blue_br"));

        // =====================================================================
        //  SMALL TILES — Мелкая плитка (TilledBlock)
        // =====================================================================
        SMALL_TILE_BLUE = registerBlock("smalltile/small_tile_blue", () -> new TilledBlock("info.small_tile_blue"));
        SMALL_TILE_BLUE_BR = registerBlock("smalltile/small_tile_blue_br", () -> new TilledBlock("info.small_tile_blue_br"));
        SMALL_TILE_WHITE = registerBlock("smalltile/small_tile_white", () -> new TilledBlock("info.small_tile_white"));
        SMALL_TILE_WHITE_BR = registerBlock("smalltile/small_tile_white_br", () -> new TilledBlock("info.small_tile_white_br"));
        SMALL_TILE_RED = registerBlock("smalltile/small_tile_red", () -> new TilledBlock("info.small_tile_red"));
        SMALL_TILE_RED_BR = registerBlock("smalltile/small_tile_red_br", () -> new TilledBlock("info.small_tile_red_br"));
        SMALL_TILE_YELLOW = registerBlock("smalltile/small_tile_yellow", () -> new TilledBlock("info.small_tile_yellow"));
        SMALL_TILE_YELLOW_BR = registerBlock("smalltile/small_tile_yellow_br", () -> new TilledBlock("info.small_tile_yellow_br"));

        // =====================================================================
        //  MOSAIC TILES — Мозаичная плитка (TilledBlock)
        // =====================================================================
        TILE_MOSAIC_1 = registerBlock("mosaictile/tile_mosaic_1", () -> new TilledBlock("info.mosaic_tile1"));
        TILE_MOSAIC_2 = registerBlock("mosaictile/tile_mosaic_2", () -> new TilledBlock("info.mosaic_tile2"));

        // =====================================================================
        //  LINOLEUM — Линолеум (LinoBlock)
        // =====================================================================
        LINO_1 = registerBlock("lino/lino1", LinoBlock::new);
        LINO_2 = registerBlock("lino/lino2", LinoBlock::new);
        LINO_3 = registerBlock("lino/lino3", LinoBlock::new);
        LINO_4 = registerBlock("lino/lino4", LinoBlock::new);
        LINO_5 = registerBlock("lino/lino5", LinoBlock::new);
        LINO_6 = registerBlock("lino/lino6", LinoBlock::new);
        LINO_7 = registerBlock("lino/lino7", LinoBlock::new);
        LINO_8 = registerBlock("lino/lino8", LinoBlock::new);

        // =====================================================================
        //  WALLPAPERS — Обои (WallpaperBlock)
        // =====================================================================
        WALLPAPER_1 = registerBlock("wallpapers/wp1", () -> new WallpaperBlock("wp1.info"));
        WALLPAPER_2 = registerBlock("wallpapers/wp2", () -> new WallpaperBlock("wp2.info"));
        WALLPAPER_3 = registerBlock("wallpapers/wp3", () -> new WallpaperBlock("wp3.info"));
        WALLPAPER_4 = registerBlock("wallpapers/wp4", () -> new WallpaperBlock("wp4.info"));
        WALLPAPER_5 = registerBlock("wallpapers/wp5", () -> new WallpaperBlock("wp5.info"));
        WALLPAPER_6 = registerBlock("wallpapers/wp6", () -> new WallpaperBlock("wp6.info"));
        WALLPAPER_7 = registerBlock("wallpapers/wp7", () -> new WallpaperBlock("wp7.info"));
        WALLPAPER_8 = registerBlock("wallpapers/wp8", () -> new WallpaperBlock("wp8.info"));
        WALLPAPER_9 = registerBlock("wallpapers/wp9", () -> new WallpaperBlock("wp9.info"));
        WALLPAPER_10 = registerBlock("wallpapers/wp10", () -> new WallpaperBlock("wp10.info"));
        WALLPAPER_11 = registerBlock("wallpapers/wp11", () -> new WallpaperBlock("wp11.info"));
        WALLPAPER_12 = registerBlock("wallpapers/wp12", () -> new WallpaperBlock("wp12.info"));
        WALLPAPER_13 = registerBlock("wallpapers/wp13", () -> new WallpaperBlock("wp13.info"));
        WALLPAPER_14 = registerBlock("wallpapers/wp14", () -> new WallpaperBlock("wp14.info"));
        WALLPAPER_15 = registerBlock("wallpapers/wp15", () -> new WallpaperBlock("wp15.info"));

        // =====================================================================
        //  TRIM / STONE — Облицовочный камень и декоративные блоки
        // =====================================================================
        TRIM_TILE_1 = registerBlock("structural/trim_tile_1", TilledBlock::new);
        TRIM_TILE_1_BR = registerBlock("structural/trim_tile_1_br", TilledBlock::new);
        TRIM_TILE_RED = registerBlock("structural/trim_tile_red", TilledBlock::new);
        TRIM_TILE_BLUE = registerBlock("structural/trim_tile_blue", TilledBlock::new);
        LEADCERAMIC_TILE = registerBlock("structural/leadceramic_tile", TilledBlock::new);

        Block.Properties TRIM_STONE_PROPS = Block.Properties.of()
                .requiresCorrectToolForDrops()
                .strength(5.5f, 15f)
                .sound(SoundType.STONE);
        TRIM_STONE_1 = registerBlock("structural/trim_stone_1", () -> new BaseBlock(TRIM_STONE_PROPS));
        TRIM_STONE_2 = registerBlock("structural/trim_stone_2", () -> new BaseBlock(TRIM_STONE_PROPS));
        TRIM_STONE_3 = registerBlock("structural/trim_stone_3", () -> new BaseBlock(TRIM_STONE_PROPS));
        TRIM_STONE_4 = registerBlock("structural/trim_stone_4", () -> new BaseBlock(TRIM_STONE_PROPS));
        CONCRETE_PLATE = registerBlock("structural/concrete_plate", () -> new BaseBlock(TRIM_STONE_PROPS));

        Block.Properties PARQUET_PROPS = Block.Properties.of()
                .requiresCorrectToolForDrops()
                .strength(5.5f, 15f)
                .sound(SoundType.WOOD);
        PARQUET_BLOCK = registerBlock("structural/parquet_block", () -> new WoodBlock(PARQUET_PROPS));
        PARQUET_BLOCK_DIAG = registerBlock("structural/parquet_block_diagonal", () -> new WoodBlock(PARQUET_PROPS));

        // =====================================================================
        //  METAL BLOCKS — Металлические блоки (MetalBlock)
        // =====================================================================
        TRIM_METAL_1 = registerBlock("structural/trim_metal_1", MetalBlock::new);
        TRIM_METAL_2 = registerBlock("structural/trim_metal_2", MetalBlock::new);
        RUSTYMETAL_BLOCK = registerBlock("structural/rusty_block", MetalBlock::new);
        CONTAINMENT_BLOCK = registerBlock("structural/containment_block", MetalBlock::new);

        // =====================================================================
        //  CERAMIC GLASS — Керамическое стекло (GlassBlock)
        // =====================================================================
        CERAMIC_GLASS_BLUE = registerBlock("structural/ceramic_glass_blue", GlassBlock::new);
        CERAMIC_GLASS_GREEN = registerBlock("structural/ceramic_glass_green", GlassBlock::new);
        CERAMIC_GLASS_BROWN = registerBlock("structural/ceramic_glass_brown", GlassBlock::new);

        // =====================================================================
        //  PANELS — Строительные панели (BaseBlock, стандартная модель)
        // =====================================================================
        Block.Properties PANEL_PROPS = Block.Properties.of()
                .requiresCorrectToolForDrops()
                .strength(3f, 8f);
        PANEL_CONCRETE = registerBlock("structural/panel_concrete", () -> new BaseBlock(PANEL_PROPS));
        PANEL_CONCRETE_YELLOW = registerBlock("structural/panel_concrete_yellow", () -> new BaseBlock(PANEL_PROPS));
        PANEL_TILE = registerBlock("structural/panel_tile", () -> new BaseBlock(PANEL_PROPS));

        // =====================================================================
        //  DIESEL GENERATOR
        // =====================================================================
        Block.Properties DIESEL_GEN_PROPS = Block.Properties.of()
                .requiresCorrectToolForDrops()
                .strength(2f, 8f)
                .sound(SoundType.METAL)
                .noOcclusion();


        DIESEL_ENGINE = registerNDBlock("dieselgen/engine", () -> new BlockDieselEngine(DIESEL_GEN_PROPS, 0.0f));
        DIESEL_TANK = registerNDBlock("dieselgen/tank", () -> new BlockDieselTank(DIESEL_GEN_PROPS, 0.0f));
        ELECTRO_GENERATOR = registerNDBlock("dieselgen/generator", () -> new BlockElectroGenerator(DIESEL_GEN_PROPS, 0.0f));
        POWER_CONNECTOR = registerOnlyCustomBlock("dieselgen/power_connector", BlockPowerConnector::new);

    }

    // ===================== Поля блоков =====================

    // --- Concrete ---
    public static RegistryObject<Block> CONCRETE_ORANGE, CONCRETE_ORANGE_BR;
    public static RegistryObject<Block> CONCRETE_BLUE, CONCRETE_BLUE_BR;
    public static RegistryObject<Block> CONCRETE_RED, CONCRETE_RED_BR;
    public static RegistryObject<Block> CONCRETE_GREEN, CONCRETE_GREEN_BR;
    public static RegistryObject<Block> CONCRETE_GRAY, CONCRETE_GRAY_BR;
    public static RegistryObject<Block> CONCRETE_BEIGE, CONCRETE_BEIGE_BR;
    public static RegistryObject<Block> CONCRETE_BEIGE2, CONCRETE_BEIGE2_BR;
    public static RegistryObject<Block> CONCRETE_YELLOW, CONCRETE_YELLOW_BR;
    public static RegistryObject<Block> CONCRETE_WHITE, CONCRETE_WHITE_BR;

    // --- Bricks ---
    public static RegistryObject<Block> SMALL_BRICKS;
    public static RegistryObject<Block> YELLOW_BRICKS_3_BR, YELLOW_BRICKS_3;
    public static RegistryObject<Block> YELLOW_BRICKS_2_BR, YELLOW_BRICKS_2;
    public static RegistryObject<Block> YELLOW_BRICKS_1_BR, YELLOW_BRICKS_1;
    public static RegistryObject<Block> WHITE_BRICKS_BR, WHITE_BRICKS;
    public static RegistryObject<Block> RED_BRICKS_BR, RED_BRICKS;
    public static RegistryObject<Block> BRICKS_WITH_WHITE, BRICKS, BRICKS_BR;
    public static RegistryObject<Block> WALL_BRICKS, WALL_BRICKS_BR;
    public static RegistryObject<Block> LIGHT_BRICKS, LIGHT_BRICKS_BR;
    public static RegistryObject<Block> SHORT_BRICKS;

    // --- Quad Tiles ---
    public static RegistryObject<Block> TILE_QUAD_WHITE, TILE_QUAD_GRAY, TILE_QUAD_BLUE, TILE_QUAD_CONCRETE;
    public static RegistryObject<Block> TILE_QUAD_WHITE_BR, TILE_QUAD_BLUE_BR;
    public static RegistryObject<Block> TILE_QUAD_1, TILE_QUAD_1_BR, TILE_QUAD_2, TILE_QUAD_2_BR;
    public static RegistryObject<Block> TILE_QUAD_3, TILE_QUAD_3_BR, TILE_QUAD_4;
    public static RegistryObject<Block> TILE_QUAD_5, TILE_QUAD_5_BR, TILE_QUAD_5_BRf, TILE_QUAD_6;

    // --- Rest Tiles ---
    public static RegistryObject<Block> TILE_REST_DARK_BLUE, TILE_REST_DARK_BLUE_BR;
    public static RegistryObject<Block> TILE_REST_BLUE, TILE_REST_BLUE_BR;
    public static RegistryObject<Block> TILE_REST_BROWN, TILE_REST_BROWN_BR;
    public static RegistryObject<Block> TILE_REST_WHITE, TILE_REST_WHITE_BR;
    public static RegistryObject<Block> TILE_REST_BLACK, TILE_REST_BLACK_BR;

    // --- Regular Tiles ---
    public static RegistryObject<Block> REGULAR_BROWN_TILE, REGULAR_BROWN_TILE_BR;
    public static RegistryObject<Block> REGULAR_AM_TILE, REGULAR_AM_TILE_BR;
    public static RegistryObject<Block> REGULAR_LIL_TILE, REGULAR_LIL_TILE_BR;

    // --- Horizontal Tiles ---
    public static RegistryObject<Block> HORIZ_TILE_BLUE, HORIZ_TILE_BLUE_BR;
    public static RegistryObject<Block> HORIZ_TILE_WHITE, HORIZ_TILE_WHITE_BR;
    public static RegistryObject<Block> HORIZ_TILE_DARK_BLUE, HORIZ_TILE_DARK_BLUE_BR;

    // --- Small Tiles ---
    public static RegistryObject<Block> SMALL_TILE_BLUE, SMALL_TILE_BLUE_BR;
    public static RegistryObject<Block> SMALL_TILE_WHITE, SMALL_TILE_WHITE_BR;
    public static RegistryObject<Block> SMALL_TILE_RED, SMALL_TILE_RED_BR;
    public static RegistryObject<Block> SMALL_TILE_YELLOW, SMALL_TILE_YELLOW_BR;

    // --- Mosaic Tiles ---
    public static RegistryObject<Block> TILE_MOSAIC_1, TILE_MOSAIC_2;

    // --- Linoleum ---
    public static RegistryObject<Block> LINO_1, LINO_2, LINO_3, LINO_4;
    public static RegistryObject<Block> LINO_5, LINO_6, LINO_7, LINO_8;

    // --- Wallpapers ---
    public static RegistryObject<Block> WALLPAPER_1, WALLPAPER_2, WALLPAPER_3, WALLPAPER_4, WALLPAPER_5;
    public static RegistryObject<Block> WALLPAPER_6, WALLPAPER_7, WALLPAPER_8, WALLPAPER_9, WALLPAPER_10;
    public static RegistryObject<Block> WALLPAPER_11, WALLPAPER_12, WALLPAPER_13, WALLPAPER_14, WALLPAPER_15;

    // --- Trim / Stone ---
    public static RegistryObject<Block> TRIM_TILE_1, TRIM_TILE_1_BR;
    public static RegistryObject<Block> TRIM_STONE_1, TRIM_STONE_2, TRIM_STONE_3, TRIM_STONE_4;
    public static RegistryObject<Block> PARQUET_BLOCK, PARQUET_BLOCK_DIAG;
    public static RegistryObject<Block> TRIM_TILE_RED, TRIM_TILE_BLUE;
    public static RegistryObject<Block> CONCRETE_PLATE;
    public static RegistryObject<Block> LEADCERAMIC_TILE;

    // --- Metal Blocks ---
    public static RegistryObject<Block> TRIM_METAL_1, TRIM_METAL_2;
    public static RegistryObject<Block> RUSTYMETAL_BLOCK, CONTAINMENT_BLOCK;

    // --- Ceramic Glass ---
    public static RegistryObject<Block> CERAMIC_GLASS_BLUE, CERAMIC_GLASS_GREEN, CERAMIC_GLASS_BROWN;

    // --- Panels ---
    public static RegistryObject<Block> PANEL_CONCRETE, PANEL_CONCRETE_YELLOW, PANEL_TILE;


    // --- Diesel Generator ---
    public static RegistryObject<Block> DIESEL_ENGINE;
    public static RegistryObject<Block> DIESEL_TANK;
    public static RegistryObject<Block> ELECTRO_GENERATOR;
    public static RegistryObject<Block> POWER_CONNECTOR;

    // ===================== Методы регистрации =====================

    /**
     * Регистрирует блок с кастомной моделью (BLOCKS_CUSTOM_MODELS).
     */
    public static <T extends Block> RegistryObject<T> registerBlockWithModel(
            String name, Supplier<T> block, @Nullable CreativeModeTab tab) {
        RegistryObject<T> toReturn = BLOCKS_CUSTOM_MODELS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    /**
     * Регистрирует блок только как кастомный (ONLY_CUSTOM_BLOCKS).
     */
    public static <T extends Block> RegistryObject<T> registerOnlyCustomBlock(
            String name, Supplier<T> block) {
        RegistryObject<T> toReturn = ONLY_CUSTOM_BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    /**
     * Регистрирует стандартный блок (BLOCKS).
     * Используется для обычных полноразмерных блоков с кубической моделью.
     */
    public static <T extends Block> RegistryObject<T> registerBlock(
            String name, Supplier<T> block) {
        RegistryObject<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    /**
     * Регистрирует нестандартный блок (NOT_DEFAULT_BLOCKS).
     */
    public static <T extends Block> RegistryObject<T> registerNDBlock(
            String name, Supplier<T> block) {
        RegistryObject<T> toReturn = NOT_DEFAULT_BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    /**
     * Регистрирует блок с кастомной окрашиваемой моделью (BLOCKS_CUSTOM_MODELS_COLORED).
     */
    public static <T extends Block> RegistryObject<T> registerBlockWithModelColored(
            String name, Supplier<T> block) {
        RegistryObject<T> toReturn = BLOCKS_CUSTOM_MODELS_COLORED.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    /**
     * Создаёт BlockItem для блока и опционально добавляет его в креативную вкладку.
     */
    private static <T extends Block> RegistryObject<Item> registerBlockItem(String name, RegistryObject<T> block) {
        return ITEM_BLOCKS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }
}
