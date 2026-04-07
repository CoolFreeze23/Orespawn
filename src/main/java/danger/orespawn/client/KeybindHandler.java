package danger.orespawn.client;

import com.mojang.blaze3d.platform.InputConstants;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.network.RiderInputPayload;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

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

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.player.getVehicle() == null) return;
        if (mc.screen != null) return;

        boolean up = FLY_UP_KEY.isDown();
        boolean down = FLY_DOWN_KEY.isDown();
        boolean special = SPECIAL_KEY.isDown();

        if (up || down || special) {
            PacketDistributor.sendToServer(new RiderInputPayload(up, down, special));
        }
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
