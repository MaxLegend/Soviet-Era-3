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
import ru.tesmio.sovietera.blocks.devices.lamps.EntityBlockLamp;
import ru.tesmio.sovietera.blocks.storages.stillage.EntityBlockStillage;

/**
 * Central registration class for all BlockEntityType entries.
 */
public class BlockEntitiesSE {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, SovietEra.MODID);

    public static final RegistryObject<BlockEntityType<EntityBlockDieselEngine>> ENTITY_BLOCK_ELECTRO_GENERATOR =
            BLOCK_ENTITIES.register("electro_generator",
                    () -> BlockEntityType.Builder.of(EntityBlockDieselEngine::new,
                                    BlocksSE.DIESEL_ENGINE.get())
                            .build(null));

    public static final RegistryObject<BlockEntityType<EntityBlockDieselTank>> ENTITY_BLOCK_DIESEL_TANK =
            BLOCK_ENTITIES.register("diesel_tank", () ->
                    BlockEntityType.Builder.of(EntityBlockDieselTank::new,
                                           BlocksSE.DIESEL_TANK.get())
                                           .build(null));
    public static final RegistryObject<BlockEntityType<EntityBlockLamp>> ENTITY_BLOCK_LAMP =
            BLOCK_ENTITIES.register("entity_block_lamp", () ->
                    BlockEntityType.Builder.<EntityBlockLamp>of(
                            EntityBlockLamp::new,
                            BlocksSE.FLUO_LAMP.get(),
                            BlocksSE.FLUO_LAMP2.get(),
                            BlocksSE.FLUO_LAMP3.get(),
                            BlocksSE.INC_LAMP.get(),
                            BlocksSE.STREET_LAMP.get(),
                            BlocksSE.BROKEN_FLUO_LAMP.get(),
                            BlocksSE.BROKEN_FLUO_LAMP2.get(),
                            BlocksSE.BROKEN_FLUO_LAMP3.get()
                    ).build(null));
    public static final RegistryObject<BlockEntityType<EntityBlockPowerConnector>> ENTITY_BLOCK_POWER_CONNECTOR =
            BLOCK_ENTITIES.register("power_connector",
                    () -> BlockEntityType.Builder.of(EntityBlockPowerConnector::new, BlocksSE.POWER_CONNECTOR.get())
                                                 .build(null));
    public static final RegistryObject<BlockEntityType<EntityBlockStillage>> ENTITY_BLOCK_STILLAGE =
            BLOCK_ENTITIES.register("stillage",
                    () -> BlockEntityType.Builder.of(EntityBlockStillage::new,
                                    BlocksSE.STILLAGE.get())
                            .build(null));
    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
