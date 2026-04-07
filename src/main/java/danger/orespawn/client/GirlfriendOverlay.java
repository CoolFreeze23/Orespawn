package danger.orespawn.client;

import danger.orespawn.OreSpawnConfig;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.Girlfriend;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

import java.util.List;

@EventBusSubscriber(modid = OreSpawnMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class GirlfriendOverlay {
    private static final ResourceLocation OVERLAY_ID =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "gf_overlay");
    private static final int SEARCH_RADIUS = 16;

    @SubscribeEvent
    public static void registerOverlay(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.HOTBAR, OVERLAY_ID, GirlfriendOverlay::render);
    }

    private static void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
        if (!OreSpawnConfig.GUI_OVERLAY_ENABLE.get()) return;

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || mc.options.hideGui) return;

        AABB area = player.getBoundingBox().inflate(SEARCH_RADIUS);
        List<Girlfriend> nearby = player.level().getEntitiesOfClass(Girlfriend.class, area,
                gf -> gf.isTame() && gf.isOwnedBy(player));

        if (nearby.isEmpty()) return;

        int y = 4;
        for (Girlfriend gf : nearby) {
            String name = gf.hasCustomName() ? gf.getCustomName().getString() : "Girlfriend";
            int health = (int) gf.getHealth();
            int maxHealth = (int) gf.getMaxHealth();
            int barWidth = 60;
            int filledWidth = (int) ((float) health / maxHealth * barWidth);

            graphics.drawString(mc.font, name, 4, y, 0xFFFFFF);
            y += 10;

            graphics.fill(4, y, 4 + barWidth, y + 6, 0xFF333333);
            int barColor = health > maxHealth / 2 ? 0xFF00FF00 : (health > maxHealth / 4 ? 0xFFFFFF00 : 0xFFFF0000);
            graphics.fill(4, y, 4 + filledWidth, y + 6, barColor);
            graphics.drawString(mc.font, health + "/" + maxHealth, 4 + barWidth + 4, y - 1, 0xFFFFFF);
            y += 12;
        }
    }
}
