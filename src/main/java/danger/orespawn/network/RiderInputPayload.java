package danger.orespawn.network;

import danger.orespawn.OreSpawnMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Client→server rider-key state, sent by {@code client/KeybindHandler} while a
 * player rides a mount. This is the port's per-player modernization of the
 * original's single global {@code OreSpawnMain.flyup_keystate} (orig
 * KeyHandler.java:15-18 / RiderControlMessageHandler.java:25, which was shared
 * by every player on the server — an original bug).
 *
 * <p>For {@link RideableFlyer} mounts the held key state is stored on the
 * vehicle; the actual vertical physics consume it inside the mount's
 * client-predicted {@code tickRidden} flight code (the riding client also
 * sets the flags locally every tick, so the server copy only keeps remote
 * observers/AI consistent). Non-flyer vehicles keep the generic ±0.15 Δy
 * fallback.</p>
 */
public record RiderInputPayload(boolean flyUp, boolean flyDown, boolean special) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<RiderInputPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "rider_input"));

    public static final StreamCodec<ByteBuf, RiderInputPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, RiderInputPayload::flyUp,
            ByteBufCodecs.BOOL, RiderInputPayload::flyDown,
            ByteBufCodecs.BOOL, RiderInputPayload::special,
            RiderInputPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /**
     * Server-side handler: forwards the key state to the player's current
     * vehicle. {@code KeybindHandler} also sends one all-false payload on key
     * release so the stored state always clears.
     */
    public static void handle(RiderInputPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            Entity vehicle = player.getVehicle();
            if (vehicle == null) return;

            if (vehicle instanceof RideableFlyer flyer) {
                flyer.setRiderVerticalInput(payload.flyUp(), payload.flyDown());
                if (payload.special()) flyer.riderSpecial(player);
            } else if (vehicle instanceof Mob mob) {
                Vec3 delta = mob.getDeltaMovement();
                if (payload.flyUp()) {
                    mob.setDeltaMovement(delta.x, Math.min(delta.y + 0.15, 1.5), delta.z);
                }
                if (payload.flyDown()) {
                    mob.setDeltaMovement(delta.x, Math.max(delta.y - 0.15, -1.5), delta.z);
                }
            }
        });
    }

    /**
     * Implemented by mounts whose ridden flight consumes the OreSpawn rider
     * keys (the modern equivalent of polling {@code OreSpawnMain.flyup_keystate}
     * in the original's seven flyup consumers).
     */
    public interface RideableFlyer {
        /**
         * Stores the rider's held vertical-key state for this tick. Called on
         * the riding client every tick (movement prediction) and on the server
         * from {@link RiderInputPayload} (remote consistency).
         */
        void setRiderVerticalInput(boolean up, boolean down);

        /**
         * Reacts to the "special" key (G). Port addition — the original's
         * single key was movement-only, so no original mount implements this.
         */
        default void riderSpecial(ServerPlayer rider) {
        }
    }
}
