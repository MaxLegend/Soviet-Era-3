package ru.tesmio.sovietera;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import ru.tesmio.sovietera.blocks.devices.generator.RendererDieselEngine;
import ru.tesmio.sovietera.blocks.storages.stillage.RendererStillage;

import ru.tesmio.sovietera.core.BlockEntitiesSE;
import ru.tesmio.sovietera.blocks.devices.cable.RendererPowerCable;

@Mod.EventBusSubscriber(modid = "soviet", bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientRegistration {
    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(BlockEntitiesSE.ENTITY_BLOCK_POWER_CONNECTOR.get(),
                RendererPowerCable::new);
        event.registerBlockEntityRenderer(BlockEntitiesSE.ENTITY_BLOCK_ELECTRO_GENERATOR.get(),
                RendererDieselEngine::new);
        event.registerBlockEntityRenderer(BlockEntitiesSE.ENTITY_BLOCK_LAMP.get(),
                RendererPowerCable::new); // unified renderer, dedup happens inside by pos comparison
        event.registerBlockEntityRenderer(BlockEntitiesSE.ENTITY_BLOCK_STILLAGE.get(),
                RendererStillage::new);
    }
}
