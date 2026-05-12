package ru.tesmio.sovietera.core;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import ru.tesmio.sovietera.SovietEra;
import ru.tesmio.sovietera.blocks.devices.generator.EntityBlockDieselTank;
import ru.tesmio.sovietera.blocks.devices.generator.EntityBlockDieselEngine;
import ru.tesmio.sovietera.blocks.devices.cable.EntityBlockPowerConnector;

/**
 * Центральный класс регистрации BlockEntityType для всех тайл-сущностей мода.
 */
public class BlockEntitiesSE {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, SovietEra.MODID);

    public static final RegistryObject<BlockEntityType<EntityBlockDieselEngine>> ELECTRO_GENERATOR =
            BLOCK_ENTITIES.register("electro_generator",
                    () -> BlockEntityType.Builder.of(EntityBlockDieselEngine::new,
                                    BlocksSE.DIESEL_ENGINE.get())
                            .build(null));
    public static final RegistryObject<BlockEntityType<EntityBlockDieselTank>> DIESEL_TANK =
            BLOCK_ENTITIES.register("diesel_tank", () ->
                    BlockEntityType.Builder.of(EntityBlockDieselTank::new,
                                           BlocksSE.DIESEL_TANK.get())
                                           .build(null));

    public static final RegistryObject<BlockEntityType<EntityBlockPowerConnector>> POWER_CONNECTOR =
            BLOCK_ENTITIES.register("power_connector",
                    () -> BlockEntityType.Builder.of(EntityBlockPowerConnector::new, BlocksSE.POWER_CONNECTOR.get())
                                                 .build(null));
    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
