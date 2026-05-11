package ru.tesmio.sovietera.core;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import ru.tesmio.sovietera.SovietEra;
import ru.tesmio.sovietera.blocks.devices.generator.BlockEntityElectroGenerator;
import ru.tesmio.sovietera.blocks.devices.cable.BlockEntityPowerConnector;

/**
 * Центральный класс регистрации BlockEntityType для всех тайл-сущностей мода.
 */
public class BlockEntitiesSE {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, SovietEra.MODID);

    public static final RegistryObject<BlockEntityType<BlockEntityElectroGenerator>> ELECTRO_GENERATOR =
            BLOCK_ENTITIES.register("electro_generator",
                    () -> BlockEntityType.Builder.of(BlockEntityElectroGenerator::new,
                                    BlocksSE.ELECTRO_GENERATOR.get())
                            .build(null));
    public static final RegistryObject<BlockEntityType<BlockEntityPowerConnector>> POWER_CONNECTOR =
            BLOCK_ENTITIES.register("power_connector",
                    () -> BlockEntityType.Builder.of(BlockEntityPowerConnector::new, BlocksSE.POWER_CONNECTOR.get())
                                                 .build(null));
    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
