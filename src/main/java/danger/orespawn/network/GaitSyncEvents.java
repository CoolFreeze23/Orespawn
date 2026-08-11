package danger.orespawn.network;

import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.SpiderRobot;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * 2.0 spider overhaul (S2): a player that starts tracking a modern-gait
 * spider immediately receives a full keyframe, so its legs render planted
 * (not at the construction placeholder pose) before the first periodic
 * keyframe arrives. {@code buildKeyframe} self-initializes, so a spider
 * tracked before its first server tick ships a real rest pose, never the
 * all-zero placeholder.
 */
@EventBusSubscriber(modid = OreSpawnMod.MOD_ID)
public final class GaitSyncEvents {

    private GaitSyncEvents() {
    }

    @SubscribeEvent
    public static void onStartTracking(PlayerEvent.StartTracking event) {
        if (event.getTarget() instanceof SpiderRobot spider
                && spider.getModernGait() != null
                && event.getEntity() instanceof ServerPlayer player) {
            PacketDistributor.sendToPlayer(player, spider.getModernGait().buildKeyframe(spider));
        }
    }
}
