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

    public static void handle(RiderInputPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            Entity vehicle = player.getVehicle();
            if (vehicle == null) return;

            if (vehicle instanceof RideableFlyer flyer) {
                if (payload.flyUp()) flyer.riderFlyUp();
                if (payload.flyDown()) flyer.riderFlyDown();
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

    public interface RideableFlyer {
        void riderFlyUp();
        void riderFlyDown();
        void riderSpecial(ServerPlayer rider);
    }
}
