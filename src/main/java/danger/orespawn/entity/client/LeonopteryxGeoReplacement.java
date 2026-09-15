package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.EntityLeon;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.animation.AnimationProcessor;

/**
 * GeckoLib Leonopteryx (the hook survey, landed by the first Tier-1 slice T1a): the canonical registry id of the
 * one Leon (TF-030: 1.7.10 has ONE entity, class Leon registered as "Leonopteryx", orig OreSpawnMain.java:4377/4381;
 * the port keeps {@code leon} as the save-compat alias and {@code leonopteryx} as the canonical id, both built
 * from {@link EntityLeon} and drawn by the one {@link LeonRenderer} ). This descriptor shares {@link
 * LeonGeoReplacement} 's geo, clip file, texture, scale, shadow, face order and hook ({@link
 * LeonGeoReplacement#poseRig}) under its own registry path - one profile per registry path even for a shared rig
 * (the Ant precedent).
 */
public final class LeonopteryxGeoReplacement extends OreSpawnGeoReplacement<EntityLeon> {
    private static final GeoReplacementDescriptor<EntityLeon> DESCRIPTOR = new GeoReplacementDescriptor<>(
            () -> ModEntities.LEONOPTERYX.get(),  // lambda: a bound method ref would initialise ModEntities eagerly
            // the Leon's shipped rig, one geo for the two registries (the literal is what the asset audit attributes the rig by)
            EntityLeon.class, ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "geo/entity/leon.geo.json"),
            LeonGeoReplacement.ANIMATION, LeonGeoReplacement.TEXTURE, 1.75F) {  // the Leon's 1.0 x 1.75 shadow literal
        @Override
        public void applyScale(EntityLeon entity, PoseStack poseStack, float partialTick) {
            // orig RenderLeon.java:39-41 preRenderScale: GL11.glScalef(scale, scale, scale) (LeonRenderer.scale; public since T1a)
            poseStack.scale(LeonRenderer.SCALE, LeonRenderer.SCALE, LeonRenderer.SCALE);
        }

        /** The Leon rig's four zero-thickness sails: the classic within-cube face order is expected in the shipped geo. */
        @Override
        public boolean cubeFaceOrderRequired() {
            return true;
        }
    };

    public LeonopteryxGeoReplacement() {
        super(DESCRIPTOR);
    }

    @Override
    protected void applyCustomAnimations(AnimationProcessor<?> processor, PoseInputs inputs) {
        LeonGeoReplacement.poseRig(processor, inputs);
    }

    public static final class Renderer extends OreSpawnGeoReplacedEntityRenderer<EntityLeon, LeonopteryxGeoReplacement> {
        public Renderer(EntityRendererProvider.Context context) {
            super(context, new LeonopteryxGeoReplacement());
        }

        /** The classic {@link LeonRenderer#shouldRender} (OPT-013): unconditionally drawn, never frustum-culled by its hitbox (T1a). */
        @Override
        public boolean shouldRender(EntityLeon entity, net.minecraft.client.renderer.culling.Frustum frustum, double x, double y, double z) {
            return true;
        }
    }
}
