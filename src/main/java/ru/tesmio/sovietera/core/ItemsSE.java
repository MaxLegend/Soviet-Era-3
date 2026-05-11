package ru.tesmio.sovietera.core;

import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import ru.tesmio.sovietera.SovietEra;
import ru.tesmio.sovietera.blocks.devices.cable.PowerCableItem;

import java.util.function.Supplier;

/**
 * Класс регистрации standalone-предметов (не BlockItem).
 * BlockItem-ы регистрируются автоматически в BlocksSE.registerBlockItem().
 * Здесь регистрируются слитки, материалы, инструменты и прочие предметы.
 */
public class ItemsSE {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, SovietEra.MODID);

    // TODO: Здесь будет регистрация standalone-предметов (слитки, материалы и т.д.)
    public static RegistryObject<Item> POWER_CABLE;

    public static void init() {
        registerItem("power_cable", () -> new PowerCableItem(new Item.Properties()));
    }

    private static <T extends Item> RegistryObject<T> registerItem(String name, Supplier<T> item) {
        RegistryObject<T> toReturn = ITEMS.register(name, item);
        return toReturn;
    }

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
        init();
    }
}
