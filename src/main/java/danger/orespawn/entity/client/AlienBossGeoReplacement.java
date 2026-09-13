package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.ModEntities;
import danger.orespawn.entity.AlienBoss;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.animation.AnimationProcessor;

/**
 * GeckoLib Alien Boss (the hook lanes, 2026-09-14): the second registry of the Alien rig. orig ClientProxyOreSpawn.java:
 * 435 drew both the Alien and the Alien Boss with the one {@code RenderAlien(new ModelAlien(0.22f), 0.35f, 1.1f)} (the
 * port's {@link AlienRenderer} serves both), so this descriptor shares {@link AlienGeoReplacement}'s geo, clip file,
 * texture, scale, shadow and hook ({@link AlienGeoReplacement#poseRig}) under its own registry path ({@code alien_boss};
 * one profile per registry path even for a shared rig - the Ant precedent). {@link AlienBoss} extends {@code Alien} and
 * so already implements {@code AlienPose}.
 */
public final class AlienBossGeoReplacement extends OreSpawnGeoReplacement<AlienBoss> {
    private static final GeoReplacementDescriptor<AlienBoss> DESCRIPTOR = new GeoReplacementDescriptor<>(
            () -> ModEntities.ALIEN_BOSS.get(),  // lambda: a bound method ref would initialise ModEntities eagerly
            AlienBoss.class, AlienGeoReplacement.MODEL, AlienGeoReplacement.ANIMATION, AlienGeoReplacement.TEXTURE,
            AlienRenderer.SHADOW) {
        @Override
        public void applyScale(AlienBoss entity, PoseStack poseStack, float partialTick) {
            // orig RenderAlien.preRenderScale (:39-41): GL11.glScalef(scale, scale, scale) (AlienRenderer.scale)
            poseStack.scale(AlienRenderer.SCALE, AlienRenderer.SCALE, AlienRenderer.SCALE);
        }
    };

    public AlienBossGeoReplacement() {
        super(DESCRIPTOR);
    }

    @Override
    protected void applyCustomAnimations(AnimationProcessor<?> processor, PoseInputs inputs) {
        AlienGeoReplacement.poseRig(processor, inputs);
    }

    public static final class Renderer extends OreSpawnGeoReplacedEntityRenderer<AlienBoss, AlienBossGeoReplacement> {
        public Renderer(EntityRendererProvider.Context context) {
            super(context, new AlienBossGeoReplacement());
        }
    }
}
