package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.Elevator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * GeckoLib hoverboard. The rig is static (the G1 Tier-3 proof); the classic
 * renderer's two behaviors — paint-colour texture and the boat-style hit
 * wobble from {@code ElevatorRenderer.setupRotations} — live in the descriptor.
 */
public final class ElevatorGeoReplacement extends OreSpawnGeoReplacement<Elevator> {
    /** Index 0 unused; 1..10 match the original's texture1..texture10. */
    private static final ResourceLocation[] TEXTURES = new ResourceLocation[11];

    static {
        for (int i = 1; i <= 10; ++i) {
            TEXTURES[i] = ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID,
                    "textures/entity/elevator" + i + ".png");
        }
    }

    private static final GeoReplacementDescriptor<Elevator> DESCRIPTOR = new GeoReplacementDescriptor<>(
            () -> ModEntities.ELEVATOR.get(),  // lambda: a bound method ref would initialise ModEntities eagerly and trip Bootstrap.checkBootstrapCalled in a headless leg (OPT-029 R0; item 15 refuter A, D4)
            Elevator.class,
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "geo/entity/elevator.geo.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "animations/entity/elevator.animation.json"),
            TEXTURES[1],
            0.25F) { // orig RenderElevator.java:23 — shadow 0.25 (same as the classic ctor).
        @Override
        public ResourceLocation texture(Elevator entity) {
            int color = entity.getColor();
            if (color < 1 || color > 10) color = 1;
            return TEXTURES[color];
        }

        @Override
        public void applyRotations(Elevator entity, PoseStack poseStack, float ageInTicks, float partialTick) {
            // orig RenderElevator.java:31-38 — boat-style hit wobble.
            float hitTime = (float) entity.getTimeSinceHit() - partialTick;
            float damage = entity.getDamageTaken() - partialTick;
            if (damage < 0.0f) {
                damage = 0.0f;
            }
            if (hitTime > 0.0f) {
                poseStack.mulPose(Axis.XP.rotationDegrees(
                        Mth.sin(hitTime) * hitTime * damage / 10.0f * (float) entity.getForwardDirection()));
            }
            // ENT-S-091, re-derived under TEST-015 (owner 2026-09-15, closing set continued, second, item 35 (2)): the
            // seam's chain after this descriptor slot is now exactly vanilla's living chain M - the bake's 1.5-block datum,
            // GeckoLib's translate(0, 0.01, 0) (actuallyRender bytecode 727 vs 396) and the seam's height compensation
            // (OreSpawnGeoReplacedEntityRenderer.applyRotations: 1.501 - 1.5 - 0.01) add up to the classic's 1.501 lift.
            // The classic ElevatorRenderer cancels that lift in its scale hook (+1.501 in the flipped frame: orig
            // RenderElevator.java:43-44 flips and renders with NO lift), so the same cancellation here, -1.501 in this
            // yaw+wobble frame, and the deck sits at the feet as the classic draws it. (Before TEST-015 this was
            // -1.5 - 0.01, the datum and GeckoLib's lift alone; the compensation the seam now carries closes the last
            // 0.001 to the classic's constant.) Not in applyScale, so a hit wobble leaves no residual.
            poseStack.translate(0.0F, -1.501F, 0.0F);
        }
    };

    public ElevatorGeoReplacement() {
        super(DESCRIPTOR);
    }

    /**
     * ENT-S-094: orig RenderElevator.java:19 extends the plain {@code Render}, so
     * the seam skips GeckoLib's death flip, shaking/sleeping/upside-down rotations,
     * hurt red overlay and name tag for this species — the same set the classic
     * {@code ElevatorRenderer} drops, and draws at the lerped ENTITY yaw the 1.7.10
     * RenderManager passed (not the living body yaw), as the classic path does.
     * Hit wobble, colour texture and the slice-B lift cancellation in the descriptor
     * are untouched.
     */
    @Override
    public boolean nonLivingRender() {
        return true;
    }

    public static final class Renderer extends OreSpawnGeoReplacedEntityRenderer<Elevator, ElevatorGeoReplacement> {
        public Renderer(net.minecraft.client.renderer.entity.EntityRendererProvider.Context context) {
            super(context, new ElevatorGeoReplacement());
        }
    }
}
