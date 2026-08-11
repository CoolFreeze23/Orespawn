package danger.orespawn.client;

import danger.orespawn.OreSpawnConfig;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.Alien;
import danger.orespawn.entity.Alosaurus;
import danger.orespawn.entity.AntRobot;
import danger.orespawn.entity.BandP;
import danger.orespawn.entity.Basilisk;
import danger.orespawn.entity.Boyfriend;
import danger.orespawn.entity.Cephadrome;
import danger.orespawn.entity.Crab;
import danger.orespawn.entity.Dragon;
import danger.orespawn.entity.EntityCaterKiller;
import danger.orespawn.entity.EntityEmperorScorpion;
import danger.orespawn.entity.EntityHerculesBeetle;
import danger.orespawn.entity.EntityKyuubi;
import danger.orespawn.entity.EntityMantis;
import danger.orespawn.entity.EntityMolenoid;
import danger.orespawn.entity.EntitySpitBug;
import danger.orespawn.entity.EntitySpyro;
import danger.orespawn.entity.EntityTriffid;
import danger.orespawn.entity.EntityTrooperBug;
import danger.orespawn.entity.EntityVortex;
import danger.orespawn.entity.EntityWormLarge;
import danger.orespawn.entity.GiantRobot;
import danger.orespawn.entity.Girlfriend;
import danger.orespawn.entity.Godzilla;
import danger.orespawn.entity.Hammerhead;
import danger.orespawn.entity.Irukandji;
import danger.orespawn.entity.Kraken;
import danger.orespawn.entity.EntityLeon;
import danger.orespawn.entity.Mothra;
import danger.orespawn.entity.Nastysaurus;
import danger.orespawn.entity.PitchBlack;
import danger.orespawn.entity.Robot2;
import danger.orespawn.entity.Robot4;
import danger.orespawn.entity.SeaMonster;
import danger.orespawn.entity.SeaViper;
import danger.orespawn.entity.SpiderRobot;
import danger.orespawn.entity.TRex;
import danger.orespawn.entity.TheKing;
import danger.orespawn.entity.ThePrince;
import danger.orespawn.entity.ThePrinceAdult;
import danger.orespawn.entity.ThePrinceTeen;
import danger.orespawn.entity.ThePrincess;
import danger.orespawn.entity.TheQueen;
import danger.orespawn.entity.WaterDragon;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

/**
 * Pointed-at-mob health bar HUD (orig GirlfriendOverlayGui.java:75-447).
 *
 * <p>The original drew, for the entity under the crosshair (vanilla pick
 * entity, falling back to a 16-block entity ray trace, orig :105-114), a name
 * label plus a textured 182×5 health bar from {@code girlfriendgui.png}
 * (background at v=0, filled portion at v=5, orig :441-446) centered
 * horizontally at y=25 from the top of the screen (y=15 while the player is
 * eye-deep in water or wearing armor, orig :435-439), name in color 0xFF3434
 * 10px above the bar (orig :440). Eligibility spans ~45 entity types
 * (orig :115-428) with ownership gates on Girlfriend/Boyfriend/Princes/Princess
 * and activity gates on the Princes teen/adult, Dragon, and Cephadrome.
 * Config-gated on {@code GuiOverlayEnable} (orig :102).</p>
 *
 * <p>Port notes: the original Girlfriend "passenger" (riding the player's
 * shoulders) gate has no port equivalent and is omitted; the original's
 * unrelated side writes to {@code current_dimension} / {@code FastGraphicsLeaves}
 * (orig :100-101) belong to other systems and are not replicated here.</p>
 */
@EventBusSubscriber(modid = OreSpawnMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class GirlfriendOverlay {
    private static final ResourceLocation OVERLAY_ID =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "gf_overlay");
    /** orig GirlfriendOverlayGui.java:69 — {@code orespawn:girlfriendgui.png} (256×256). */
    private static final ResourceLocation BAR_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/gui/girlfriendgui.png");
    /** orig GirlfriendOverlayGui.java:88-89 — bar is 182×5. */
    private static final int BAR_WIDTH = 182;
    private static final int BAR_HEIGHT = 5;
    /** orig GirlfriendOverlayGui.java:86 — name label color. */
    private static final int NAME_COLOR = 0xFF3434;
    /** orig GirlfriendOverlayGui.java:107 — 16-block fallback ray trace range. */
    private static final double PICK_RANGE = 16.0;

    @SubscribeEvent
    public static void registerOverlay(RegisterGuiLayersEvent event) {
        // orig hooked the HOTBAR overlay element (orig GirlfriendOverlayGui.java:80).
        event.registerAbove(VanillaGuiLayers.HOTBAR, OVERLAY_ID, GirlfriendOverlay::render);
    }

    private static void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        // orig GirlfriendOverlayGui.java:93-99 — hidden GUI, open screen, no player.
        if (player == null || mc.options.hideGui || mc.screen != null) return;
        // orig GirlfriendOverlayGui.java:102 — GuiOverlayEnable config gate.
        if (!OreSpawnConfig.GUI_OVERLAY_ENABLE.get()) return;

        // orig GirlfriendOverlayGui.java:105-114 — vanilla crosshair pick
        // entity first, then a 16-block entity ray trace fallback that must
        // yield a living entity.
        Entity entity = mc.crosshairPickEntity;
        boolean fromRayTrace = false;
        if (entity == null) {
            entity = rayTraceEntity(player);
            if (entity == null) return;
            fromRayTrace = true;
        }
        // 2.0 S4: multipart surfaces resolve to their PARENT entity — fixes
        // the blank bar when aiming at the King's manual parts (the
        // pre-existing C8 gap), the Queen's MHLib parts, and the modern
        // spider's leg boxes. The classic quirk that the vanilla crosshair
        // pick skips the LivingEntity requirement (orig
        // GirlfriendOverlayGui.java:105-114) is preserved: that check still
        // applies only to the ray-trace fallback, now against the parent.
        if (entity instanceof net.neoforged.neoforge.entity.PartEntity<?> partEntity) {
            entity = partEntity.getParent();
        }
        if (fromRayTrace && !(entity instanceof LivingEntity)) return;

        String label = null;
        float healthFraction = 0.0f;

        // orig GirlfriendOverlayGui.java:115-131 — owned, non-shoulder Girlfriend.
        if (entity instanceof Girlfriend gf) {
            if (!gf.isOwnedBy(player)) return;
            label = customNameOr(gf, "Girlfriend");
            healthFraction = gf.getHealth() / gf.getMaxHealth();
        }
        // orig GirlfriendOverlayGui.java:132-148 — owned Boyfriend.
        if (entity instanceof Boyfriend bf) {
            if (!bf.isOwnedBy(player)) return;
            label = customNameOr(bf, "Boyfriend");
            healthFraction = bf.getHealth() / bf.getMaxHealth();
        }
        // orig GirlfriendOverlayGui.java:149-162 — owned toddler prince.
        if (entity instanceof ThePrince prince) {
            if (!prince.isOwnedBy(player)) return;
            label = customNameOr(prince, "The Toddler Prince");
            healthFraction = prince.getHealth() / prince.getMaxHealth();
        }
        // orig GirlfriendOverlayGui.java:163-176 — owned toddler princess.
        if (entity instanceof ThePrincess princess) {
            if (!princess.isOwnedBy(player)) return;
            label = customNameOr(princess, "The Toddler Princess");
            healthFraction = princess.getHealth() / princess.getMaxHealth();
        }
        // orig GirlfriendOverlayGui.java:177-193 — owned teen prince, hidden while busy.
        if (entity instanceof ThePrinceTeen teen) {
            if (!teen.isOwnedBy(player)) return;
            if (teen.getActivity() != 0) return;
            label = customNameOr(teen, "The Young Prince");
            healthFraction = teen.getHealth() / teen.getMaxHealth();
        }
        // orig GirlfriendOverlayGui.java:194-210 — owned adult prince, hidden while busy.
        if (entity instanceof ThePrinceAdult adult) {
            if (!adult.isOwnedBy(player)) return;
            if (adult.getActivity() != 0) return;
            label = customNameOr(adult, "The Young Adult Prince");
            healthFraction = adult.getHealth() / adult.getMaxHealth();
        }
        // orig GirlfriendOverlayGui.java:211-224 — any Dragon, hidden while busy.
        if (entity instanceof Dragon dragon) {
            if (dragon.getActivity() != 0) return;
            label = customNameOr(dragon, "Dragon");
            healthFraction = dragon.getHealth() / dragon.getMaxHealth();
        }
        // orig GirlfriendOverlayGui.java:225-428 — fixed-label types.
        if (entity instanceof EntityEmperorScorpion target) {
            label = "Emperor Scorpion";
            healthFraction = target.getHealth() / target.getMaxHealth();
        }
        if (entity instanceof Basilisk target) {
            label = "Basilisk";
            healthFraction = target.getHealth() / target.getMaxHealth();
        }
        if (entity instanceof Mothra target) {
            label = "Mothra!";
            healthFraction = target.getHealth() / target.getMaxHealth();
        }
        if (entity instanceof EntitySpyro target) {
            label = customNameOr(target, "Baby Dragon");
            healthFraction = target.getHealth() / target.getMaxHealth();
        }
        // orig GirlfriendOverlayGui.java:250-256 — worm only while not phasing (noClip).
        if (entity instanceof EntityWormLarge target) {
            if (!target.noPhysics) {
                label = "Worm";
                healthFraction = target.getHealth() / target.getMaxHealth();
            }
        }
        if (entity instanceof Alien target) {
            label = "Alien!";
            healthFraction = target.getHealth() / target.getMaxHealth();
        }
        if (entity instanceof WaterDragon target) {
            label = customNameOr(target, "WaterDragon");
            healthFraction = target.getHealth() / target.getMaxHealth();
        }
        if (entity instanceof Kraken target) {
            label = "Kraken";
            healthFraction = target.getHealth() / target.getMaxHealth();
        }
        // orig GirlfriendOverlayGui.java:277-284 — Cephadrome hidden while busy.
        if (entity instanceof Cephadrome target) {
            label = "Cephadrome";
            healthFraction = target.getHealth() / target.getMaxHealth();
            if (target.getActivity() != 0) return;
        }
        if (entity instanceof EntityTrooperBug target) {
            label = "Jumpy Bug";
            healthFraction = target.getHealth() / target.getMaxHealth();
        }
        if (entity instanceof EntitySpitBug target) {
            label = "Spit Bug";
            healthFraction = target.getHealth() / target.getMaxHealth();
        }
        if (entity instanceof PitchBlack target) {
            label = "Nightmare";
            healthFraction = target.getHealth() / target.getMaxHealth();
        }
        if (entity instanceof Alosaurus target) {
            label = "Alosaurus";
            healthFraction = target.getHealth() / target.getMaxHealth();
        }
        if (entity instanceof Nastysaurus target) {
            label = "Nastysaurus";
            healthFraction = target.getHealth() / target.getMaxHealth();
        }
        if (entity instanceof TRex target) {
            label = "T. Rex";
            healthFraction = target.getHealth() / target.getMaxHealth();
        }
        if (entity instanceof EntityKyuubi target) {
            label = "Kyuubi";
            healthFraction = target.getHealth() / target.getMaxHealth();
        }
        if (entity instanceof Robot2 target) {
            label = "Robo-Pounder";
            healthFraction = target.getHealth() / target.getMaxHealth();
        }
        if (entity instanceof Robot4 target) {
            label = "Robo-Warrior";
            healthFraction = target.getHealth() / target.getMaxHealth();
        }
        if (entity instanceof EntityTriffid target) {
            label = "Triffid";
            healthFraction = target.getHealth() / target.getMaxHealth();
        }
        if (entity instanceof Godzilla target) {
            label = "Mobzilla";
            healthFraction = target.getHealth() / target.getMaxHealth();
        }
        if (entity instanceof EntityVortex target) {
            label = "Vortex";
            healthFraction = target.getHealth() / target.getMaxHealth();
        }
        if (entity instanceof Irukandji target) {
            label = "Irukandji";
            healthFraction = target.getHealth() / target.getMaxHealth();
        }
        if (entity instanceof EntityMantis target) {
            label = "Mantis";
            healthFraction = target.getHealth() / target.getMaxHealth();
        }
        if (entity instanceof EntityHerculesBeetle target) {
            label = "Hercules Beetle";
            healthFraction = target.getHealth() / target.getMaxHealth();
        }
        if (entity instanceof TheKing target) {
            label = "The King";
            healthFraction = target.getHealth() / target.getMaxHealth();
        }
        if (entity instanceof TheQueen target) {
            label = "The Queen";
            healthFraction = target.getHealth() / target.getMaxHealth();
        }
        if (entity instanceof SeaViper target) {
            label = "Sea Viper";
            healthFraction = target.getHealth() / target.getMaxHealth();
        }
        if (entity instanceof SeaMonster target) {
            label = "Sea Monster";
            healthFraction = target.getHealth() / target.getMaxHealth();
        }
        if (entity instanceof EntityMolenoid target) {
            label = "Molenoid";
            healthFraction = target.getHealth() / target.getMaxHealth();
        }
        if (entity instanceof EntityCaterKiller target) {
            label = "CaterKiller";
            healthFraction = target.getHealth() / target.getMaxHealth();
        }
        // orig GirlfriendOverlayGui.java:390-398 — Leon shows its custom name
        // or "Leonopteryx". TF-030: consolidated EntityLeon covers both ids.
        if (entity instanceof EntityLeon target) {
            label = customNameOr(target, "Leonopteryx");
            healthFraction = target.getHealth() / target.getMaxHealth();
        }
        if (entity instanceof Hammerhead target) {
            label = "Hammerhead";
            healthFraction = target.getHealth() / target.getMaxHealth();
        }
        // orig GirlfriendOverlayGui.java:405-409 — Banker/Politician by variant.
        if (entity instanceof BandP target) {
            label = target.getWhat() == 0 ? "Banker" : "Politician";
            healthFraction = target.getHealth() / target.getMaxHealth();
        }
        if (entity instanceof SpiderRobot target) {
            label = "Giant Robot Spider";
            healthFraction = target.getHealth() / target.getMaxHealth();
        }
        if (entity instanceof GiantRobot target) {
            label = "Jeffery";
            healthFraction = target.getHealth() / target.getMaxHealth();
        }
        if (entity instanceof AntRobot target) {
            label = "Giant Robot Red Ant";
            healthFraction = target.getHealth() / target.getMaxHealth();
        }
        // orig GirlfriendOverlayGui.java:425-428 — only crabs grown past 0.75 scale.
        if (entity instanceof Crab crab && crab.getCrabScale() > 0.75f) {
            label = "Very Large Crab";
            healthFraction = crab.getHealth() / crab.getMaxHealth();
        }

        // orig GirlfriendOverlayGui.java:429-431 — nothing eligible.
        if (label == null) return;

        // orig GirlfriendOverlayGui.java:432-439 — bar centered horizontally
        // at y=25 from the top, raised to 15 while eye-deep in water or armored;
        // filled width = fraction * (barWidth + 1).
        int screenWidth = graphics.guiWidth();
        int barWidthFilled = (int) (healthFraction * (float) (BAR_WIDTH + 1));
        int x = screenWidth / 2 - BAR_WIDTH / 2;
        int y = 25;
        if (player.isEyeInFluid(FluidTags.WATER) || player.getArmorValue() > 0) {
            y -= 10;
        }

        // orig GirlfriendOverlayGui.java:440 — name with shadow, 10px above the bar.
        graphics.drawString(mc.font, label,
                screenWidth / 2 - mc.font.width(label) / 2, y - 10, NAME_COLOR, true);

        // orig GirlfriendOverlayGui.java:441-446 — background strip at v=0,
        // filled strip at v=5.
        graphics.blit(BAR_TEXTURE, x, y, 0, 0, BAR_WIDTH, BAR_HEIGHT);
        if (barWidthFilled > 0) {
            graphics.blit(BAR_TEXTURE, x, y, 0, BAR_HEIGHT, barWidthFilled, BAR_HEIGHT);
        }
    }

    /**
     * 16-block entity ray trace fallback, equivalent to the original's manual
     * AABB sweep along the look vector (orig OreSpawnMain.java:5795-5831).
     */
    private static Entity rayTraceEntity(LocalPlayer player) {
        Vec3 eye = player.getEyePosition(1.0f);
        Vec3 look = player.getViewVector(1.0f);
        Vec3 end = eye.add(look.x * PICK_RANGE, look.y * PICK_RANGE, look.z * PICK_RANGE);
        AABB sweep = player.getBoundingBox()
                .expandTowards(look.x * PICK_RANGE, look.y * PICK_RANGE, look.z * PICK_RANGE)
                .inflate(1.0);
        EntityHitResult hit = ProjectileUtil.getEntityHitResult(player, eye, end, sweep,
                e -> e != player && e.isPickable(), PICK_RANGE * PICK_RANGE);
        return hit != null ? hit.getEntity() : null;
    }

    /** Custom name if present, else the fixed label (orig pattern, e.g. orig GirlfriendOverlayGui.java:124-129). */
    private static String customNameOr(LivingEntity entity, String fallback) {
        if (entity.hasCustomName()) {
            String name = entity.getCustomName().getString();
            if (!name.isEmpty()) return name;
        }
        return fallback;
    }
}
