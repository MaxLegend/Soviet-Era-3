package ru.tesmio.sovietera;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import ru.tesmio.sovietera.core.BlockEntitiesSE;
import ru.tesmio.sovietera.blocks.devices.cable.PowerCableRenderer;

@Mod.EventBusSubscriber(modid = "soviet", bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientRegistration {
    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(BlockEntitiesSE.POWER_CONNECTOR.get(),
                PowerCableRenderer::new);
    }
}
