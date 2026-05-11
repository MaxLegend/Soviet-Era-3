package ru.tesmio.sovietera;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import ru.tesmio.sovietera.core.BlockEntitiesSE;
import ru.tesmio.sovietera.core.BlocksSE;
import ru.tesmio.sovietera.core.ItemsSE;
import ru.tesmio.sovietera.core.MenuTypesSE;
import ru.tesmio.sovietera.core.TabsSE;
import ru.tesmio.sovietera.network.PowerCtrlPressedPacket;

@Mod(SovietEra.MODID)
public class SovietEra {

    public static final String MODID = "soviet";

    private static int packetId = 0;
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(MODID, "power_network"),
            () -> "1.0",
            s -> true,
            s -> true
    );

    public SovietEra() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        // Регистрация креативных вкладок
        TabsSE.register(bus);
        MinecraftForge.EVENT_BUS.register(this);
        ItemsSE.register(bus);
        BlocksSE.register(bus);
        BlockEntitiesSE.register(bus);
        MenuTypesSE.register(bus);

        bus.addListener(this::commonSetup);

    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        CHANNEL.registerMessage(packetId++, PowerCtrlPressedPacket.class,
                PowerCtrlPressedPacket::encode,
                PowerCtrlPressedPacket::new,
                PowerCtrlPressedPacket::handle);
    }
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
    }
}
