package danger.orespawn.network;

import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.SpiderRobot;
import danger.orespawn.entity.gait.SpiderRigProfile;
import io.netty.handler.codec.DecoderException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * 2.0 spider overhaul (S2): S2C full gait keyframe — all planted foot
 * positions plus grounded flags. Sent when a player starts tracking a modern
 * spider and every {@code ModernSpiderGait.KEYFRAME_INTERVAL} ticks
 * (phase-shifted per entity) as a drift snap; the grounded bits ride a
 * single bitmask byte (bits 0-7 survive {@code readByte} sign extension —
 * verified under independent review).
 */
public record SpiderGaitKeyframePayload(int entityId, double[] footX, double[] footY,
                                        double[] footZ, boolean[] grounded)
        implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<SpiderGaitKeyframePayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "spider_gait_keyframe"));

    public static final StreamCodec<FriendlyByteBuf, SpiderGaitKeyframePayload> STREAM_CODEC = StreamCodec.of(
            (buf, p) -> {
                buf.writeVarInt(p.entityId);
                int legs = p.footX.length;
                buf.writeByte(legs);
                int mask = 0;
                for (int i = 0; i < legs; ++i) {
                    buf.writeDouble(p.footX[i]);
                    buf.writeDouble(p.footY[i]);
                    buf.writeDouble(p.footZ[i]);
                    if (p.grounded[i]) {
                        mask |= 1 << i;
                    }
                }
                buf.writeByte(mask);
            },
            buf -> {
                int entityId = buf.readVarInt();
                // Validate the wire length BEFORE allocating from it — a
                // corrupt byte must not become a NegativeArraySizeException
                // on the network thread (independent-review finding).
                int legs = buf.readByte();
                if (legs != SpiderRigProfile.LEG_COUNT) {
                    throw new DecoderException("spider_gait_keyframe: bad leg count " + legs);
                }
                double[] xs = new double[legs];
                double[] ys = new double[legs];
                double[] zs = new double[legs];
                for (int i = 0; i < legs; ++i) {
                    xs[i] = buf.readDouble();
                    ys[i] = buf.readDouble();
                    zs[i] = buf.readDouble();
                }
                int mask = buf.readByte();
                boolean[] grounded = new boolean[legs];
                for (int i = 0; i < legs; ++i) {
                    grounded[i] = (mask & (1 << i)) != 0;
                }
                return new SpiderGaitKeyframePayload(entityId, xs, ys, zs, grounded);
            });

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SpiderGaitKeyframePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Entity entity = context.player().level().getEntity(payload.entityId());
            if (entity instanceof SpiderRobot spider && spider.getModernGait() != null) {
                spider.getModernGait().applyKeyframe(payload);
            }
        });
    }
}
