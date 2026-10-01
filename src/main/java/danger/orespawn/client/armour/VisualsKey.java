package danger.orespawn.client.armour;

import com.mojang.blaze3d.platform.InputConstants;
import danger.orespawn.OreSpawnMod;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

/**
 * The OreSpawn Visuals key: O by default, in the OreSpawn category, live only in game (never while typing in chat, a
 * sign or another screen); it opens the OreSpawn Visuals screen.
 */
@EventBusSubscriber(modid = OreSpawnMod.MOD_ID, value = Dist.CLIENT)
public final class VisualsKey {
    public static final KeyMapping OPEN = new KeyMapping("key.orespawn.visuals", KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_O, "key.categories.orespawn");

    private VisualsKey() {
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        while (OPEN.consumeClick()) {
            if (minecraft.screen == null) {
                minecraft.setScreen(new VisualsScreen(null));
            }
        }
    }

    @EventBusSubscriber(modid = OreSpawnMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static final class Registration {
        private Registration() {
        }

        @SubscribeEvent
        public static void registerKeys(RegisterKeyMappingsEvent event) {
            event.register(OPEN);
        }
    }
}
