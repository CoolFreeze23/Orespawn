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
        // STANDING RULE (independent review, S3a): bump this version with
        // ANY payload wire-format change — same-version format drift passes
        // negotiation and desyncs mid-session instead of failing cleanly at
        // login. 1.1 = S3a strand flag + keyframe strand mask; 1.2 = S5
        // part-stream gate — a CONSERVATIVE compat fence, not a format
        // change (the gated stream rides MHLib's channel and both mixed
        // pairings would interoperate); it exists to refuse mixed dev
        // builds cleanly rather than because the rule's wire-format
        // trigger fired.
        PayloadRegistrar registrar = event.registrar(OreSpawnMod.MOD_ID).versioned("1.2");
        registrar.playToServer(
                RiderInputPayload.TYPE,
                RiderInputPayload.STREAM_CODEC,
                RiderInputPayload::handle
        );
        // 2.0 spider overhaul (S2): modern-gait step events + keyframes.
        registrar.playToClient(
                SpiderStepPayload.TYPE,
                SpiderStepPayload.STREAM_CODEC,
                SpiderStepPayload::handle
        );
        registrar.playToClient(
                SpiderGaitKeyframePayload.TYPE,
                SpiderGaitKeyframePayload.STREAM_CODEC,
                SpiderGaitKeyframePayload::handle
        );
    }
}
