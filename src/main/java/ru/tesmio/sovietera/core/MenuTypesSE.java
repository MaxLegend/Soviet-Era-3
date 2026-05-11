package ru.tesmio.sovietera.core;

import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import ru.tesmio.sovietera.SovietEra;
import ru.tesmio.sovietera.blocks.devices.generator.ElectroGeneratorContainer;

/**
 * Центральный класс регистрации MenuType для всех контейнеров мода.
 */
public class MenuTypesSE {

    public static final DeferredRegister<MenuType<?>> MENU_TYPES =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, SovietEra.MODID);

    public static final RegistryObject<MenuType<ElectroGeneratorContainer>> ELECTRO_GENERATOR_MENU =
            MENU_TYPES.register("electro_generator_menu",
                    () -> IForgeMenuType.create(ElectroGeneratorContainer::new));

    public static void register(IEventBus eventBus) {
        MENU_TYPES.register(eventBus);
    }
}
