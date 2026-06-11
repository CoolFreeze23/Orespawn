package danger.orespawn.client;

import com.mojang.blaze3d.platform.InputConstants;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.network.RiderInputPayload;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

/**
 * Client rider keys. The original had a single "OreSpawn UP/FAST" key
 * (Left Alt, orig KeyHandler.java:15-18) whose state was mirrored to a global
 * server flag; the port splits it into fly-up / fly-down / special keys and
 * sends a per-player {@link RiderInputPayload} (sanctioned modernization).
 *
 * <p>Because mount movement is client-predicted (vanilla horse-style), the key
 * state is also applied directly to the local vehicle every tick — the server
 * payload only keeps the authoritative copy consistent and carries the
 * special-key press. A single all-false payload is sent on release so the
 * server-side state always clears (the original sent on key-state change,
 * orig RiderControl.java:22-33).</p>
 */
@EventBusSubscriber(modid = OreSpawnMod.MOD_ID, value = Dist.CLIENT)
public class KeybindHandler {
    public static final KeyMapping FLY_UP_KEY = new KeyMapping(
            "key.orespawn.fly_up",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_SPACE,
            "key.categories.orespawn"
    );

    public static final KeyMapping FLY_DOWN_KEY = new KeyMapping(
            "key.orespawn.fly_down",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_LEFT_CONTROL,
            "key.categories.orespawn"
    );

    public static final KeyMapping SPECIAL_KEY = new KeyMapping(
            "key.orespawn.special",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_G,
            "key.categories.orespawn"
    );

    /** Previous tick's key state, used to emit one release payload per key drop. */
    private static boolean lastUp = false;
    private static boolean lastDown = false;
    private static boolean lastSpecial = false;

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        Entity vehicle = mc.player.getVehicle();
        if (vehicle == null) {
            lastUp = lastDown = lastSpecial = false;
            return;
        }

        boolean keysUsable = mc.screen == null;
        boolean up = keysUsable && FLY_UP_KEY.isDown();
        boolean down = keysUsable && FLY_DOWN_KEY.isDown();
        boolean special = keysUsable && SPECIAL_KEY.isDown();

        // Local prediction: the riding client owns the vehicle's movement, so
        // the flight physics read the key state straight from the entity.
        if (vehicle instanceof RiderInputPayload.RideableFlyer flyer) {
            flyer.setRiderVerticalInput(up, down);
        }

        // Send while any key is held, plus exactly one all-false payload on release.
        if (up || down || special || lastUp || lastDown || lastSpecial) {
            PacketDistributor.sendToServer(new RiderInputPayload(up, down, special));
        }
        lastUp = up;
        lastDown = down;
        lastSpecial = special;
    }

    @EventBusSubscriber(modid = OreSpawnMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class Registration {
        @SubscribeEvent
        public static void registerKeys(RegisterKeyMappingsEvent event) {
            event.register(FLY_UP_KEY);
            event.register(FLY_DOWN_KEY);
            event.register(SPECIAL_KEY);
        }
    }
}
