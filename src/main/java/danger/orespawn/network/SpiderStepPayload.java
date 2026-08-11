package danger.orespawn.network;

import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.IModernLeggedRobot;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * 2.0 spider overhaul (S2): S2C step event for the modern gait — one leg
 * begins a swing from {@code from} to {@code to} starting at server game time
 * {@code startTime} over {@code duration} ticks. The client replays the
 * deterministic swing curve locally ({@code ModernSpiderGait.clientTick});
 * planted feet are world-anchored constants between events, so a standing
 * spider costs zero gait traffic (the OPT-002/003 change-only philosophy).
 * With {@code strand=true} (S3) the payload is instead a strand transition:
 * the leg lost all reachable footing and the client should dangle it locally
 * (the from/to fields carry the initial dangle anchor).
 *
 * <p>Entity-id reuse note (independent review, accepted): a step arriving
 * for a recycled id could animate one leg of the wrong spider until the next
 * keyframe corrects it (≤ {@code KEYFRAME_INTERVAL} ticks); requires
 * same-tick id recycling, self-healing, not worth a UUID per packet.</p>
 */
public record SpiderStepPayload(int entityId, int leg, boolean strand,
                                double fromX, double fromY, double fromZ,
                                double toX, double toY, double toZ,
                                long startTime, int duration) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<SpiderStepPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "spider_step"));

    public static final StreamCodec<FriendlyByteBuf, SpiderStepPayload> STREAM_CODEC = StreamCodec.of(
            (buf, p) -> {
                buf.writeVarInt(p.entityId);
                buf.writeByte(p.leg);
                buf.writeBoolean(p.strand);
                buf.writeDouble(p.fromX);
                buf.writeDouble(p.fromY);
                buf.writeDouble(p.fromZ);
                buf.writeDouble(p.toX);
                buf.writeDouble(p.toY);
                buf.writeDouble(p.toZ);
                buf.writeVarLong(p.startTime);
                buf.writeVarInt(p.duration);
            },
            buf -> new SpiderStepPayload(
                    buf.readVarInt(), buf.readByte(), buf.readBoolean(),
                    buf.readDouble(), buf.readDouble(), buf.readDouble(),
                    buf.readDouble(), buf.readDouble(), buf.readDouble(),
                    buf.readVarLong(), buf.readVarInt()));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SpiderStepPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            // S5b: rig-generalized — the loose static bound rejects garbage
            // before the entity lookup; the EXACT bound is the entity's own
            // rig (a 6-leg ant drops leg indices 6-7 that would fit a
            // spider's payload).
            if (payload.leg() < 0 || payload.leg() >= danger.orespawn.entity.gait.SpiderRigProfile.LEG_COUNT
                    || payload.duration() <= 0) {
                return;
            }
            Entity entity = context.player().level().getEntity(payload.entityId());
            if (entity instanceof IModernLeggedRobot robot && robot.getModernGait() != null
                    && payload.leg() < robot.getModernGait().legCount()) {
                robot.getModernGait().applyStep(payload);
            }
        });
    }
}
