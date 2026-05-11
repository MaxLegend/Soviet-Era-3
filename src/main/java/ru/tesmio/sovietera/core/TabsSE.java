package ru.tesmio.sovietera.core;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import ru.tesmio.sovietera.SovietEra;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

@Mod.EventBusSubscriber(modid = SovietEra.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class TabsSE {
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, SovietEra.MODID);

    // Список предметов для вкладки
    public static final List<Supplier<? extends ItemLike>> SOVIET_TAB_ITEMS = new ArrayList<>();

    // Основная креативная вкладка мода
    // TODO: Заменить иконку на реальный предмет мода, когда он будет добавлен
    public static final RegistryObject<CreativeModeTab> SOVIET_TAB = TABS.register("soviet_tab",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.soviet_tab"))
                    .icon(() -> new ItemStack(BlocksSE.TRIM_STONE_2.get()))
                                 .displayItems((parameters, output) -> {
                                     ItemsSE.ITEMS.getEntries().stream()
                                                   .map(RegistryObject::get)
                                                   .forEach(output::accept);
                                     BlocksSE.BLOCKS.getEntries().stream()
                                                     .map(RegistryObject::get)
                                                     .map(Item::byBlock)
                                                     .filter(item -> item != Items.AIR) // иногда может быть AIR, если нет BlockItem
                                                     .forEach(output::accept);
                                     BlocksSE.NOT_DEFAULT_BLOCKS.getEntries().stream()
                                                    .map(RegistryObject::get)
                                                    .map(Item::byBlock)
                                                    .filter(item -> item != Items.AIR) // иногда может быть AIR, если нет BlockItem
                                                    .forEach(output::accept);
                                     BlocksSE.ONLY_CUSTOM_BLOCKS.getEntries().stream()
                                                                .map(RegistryObject::get)
                                                                .map(Item::byBlock)
                                                                .filter(item -> item != Items.AIR) // иногда может быть AIR, если нет BlockItem
                                                                .forEach(output::accept);

                                 })
                                 .build()
    );

    /**
     * Добавляет предмет в креативную вкладку мода.
     * Используется в BlocksSE.registerBlockItem() и ItemsSE при регистрации предметов.
     */
    public static <T extends Item> RegistryObject<T> addToTab(RegistryObject<T> itemLike) {
        SOVIET_TAB_ITEMS.add(itemLike);
        return itemLike;
    }
    public static void register(IEventBus eventBus) {
        TABS.register(eventBus);
    }
    /**
     * Обработчик события для добавления предметов мода
     * в стандартные вкладки Minecraft.
     */
    @SubscribeEvent
    public static void buildContents(BuildCreativeModeTabContentsEvent event) {
        // Здесь можно добавлять предметы в стандартные вкладки, например:
        // if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
        //     event.accept(BlocksSE.SOVIET_CONCRETE);
        // }
    }
//
//    public static void init() {
//        // Принудительная загрузка класса (инициализация статических полей)
//    }
}
