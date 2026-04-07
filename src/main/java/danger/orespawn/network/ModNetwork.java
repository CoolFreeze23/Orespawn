package danger.orespawn.network;

import danger.orespawn.OreSpawnMod;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = OreSpawnMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class ModNetwork {
    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(OreSpawnMod.MOD_ID).versioned("1.0");
        registrar.playToServer(
                RiderInputPayload.TYPE,
                RiderInputPayload.STREAM_CODEC,
                RiderInputPayload::handle
        );
    }
}
