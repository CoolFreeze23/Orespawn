package danger.orespawn.entity.client;

import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.CloudShark;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.animation.AnimationProcessor;

/**
 * GeckoLib Cloud Shark (the third Tier-2 slice, 2026-09-13; built in the first, held by the second on the visual leg's
 * tie rule, TEST-006 - adopted since): {@link ModelCloudShark#setupAnim} verbatim on the converted rig, ON THE HOOK
 * (owner 2026-09-13, Amendment 2 to Amendment 1: a rig lands through the seam on its classic hook; no keyframe layer,
 * no transcription - the self-gate stays closed until an artist delivers {@code idle} and {@code walk}). Three
 * frequencies at wingspeed 1.0f (orig ModelCloudShark.java:13,24 / ClientProxyOreSpawn.java:471): {@code leftfin.yRot
 * = 1.15f + cos(age * 0.7f * ws) * PI * 0.15f}; at 1.5f {@code rightfin.yRot = -0.9f + cos * PI * 0.15f} and
 * {@code fins.yRot = cos * PI * 0.25f}; {@code jaw.xRot = 0.5f + cos(age * 0.5f * ws) * PI * 0.1f} (orig :81-87).
 * The side fins are ROTATED zero-thickness cubes and the tail fin is one: the seam draws every cube with its true
 * transformed normal ({@link TrueNormalCubeRenderer}, ENT-S-161) and its two coplanar faces in the classic order
 * (below).
 *
 * <p>Shadow follows {@link CloudSharkRenderer}'s constructor ({@code super(context, model, 0.5f)}: orig
 * RenderCloudShark.java:22-23, par2 0.5f x par3 1.0f, ENT-S-092); the classic renderer scales by 1.0, so no scale
 * hook.</p>
 */
public final class CloudSharkGeoReplacement extends OreSpawnGeoReplacement<CloudShark> {
    /** orig ModelCloudShark.java:13,24 {@code wingspeed} = 1.0f (ClientProxyOreSpawn.java:471): the chain's second multiply. */
    static final float WINGSPEED = 1.0F;
    private static final GeoReplacementDescriptor<CloudShark> DESCRIPTOR = new GeoReplacementDescriptor<>(
            () -> ModEntities.CLOUD_SHARK.get(),  // lambda: a bound method ref would initialise ModEntities eagerly
            CloudShark.class,
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "geo/entity/cloudshark.geo.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "animations/entity/cloudshark.animation.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/cloudshark.png"),
            // orig RenderCloudShark.java:22-23 super(model, par2 * par3) with 0.5f x 1.0f: the literal CloudSharkRenderer's
            // constructor passes (it declares no SHADOW constant; tools/reference_renderer_pins.py pins the equal literal)
            0.5F) {
        /**
         * A rig with zero-thickness cubes (the tail fin 0 x 10 x 10, the two side fins 0 x 3 x 7): their two coplanar faces
         * z-fight and the winner is the face emitted last, so the shipped geo carries the classic within-cube order
         * ({@link FaceOrder#KEY}; the FaceOrder contract's open item for a cutout rig, TEST-007) and the seam expects it.
         */
        @Override
        public boolean cubeFaceOrderRequired() {
            return true;
        }
    };

    public CloudSharkGeoReplacement() {
        super(DESCRIPTOR);
    }

    @Override
    protected void applyCustomAnimations(AnimationProcessor<?> processor, PoseInputs inputs) {
        float ageInTicks = inputs.ageInTicks();
        // ModelCloudShark.setupAnim:82-90 verbatim, the float chain left to right (the classic's (float) cast of a float
        // product is a no-op).
        float newangle = Mth.cos(ageInTicks * 0.7f * WINGSPEED) * (float) Math.PI * 0.15f;
        rotateY(processor, "leftfin", 1.15f + newangle);
        newangle = Mth.cos(ageInTicks * 1.5f * WINGSPEED) * (float) Math.PI * 0.15f;
        rotateY(processor, "rightfin", -0.9f + newangle);
        rotateY(processor, "fins", Mth.cos(ageInTicks * 1.5f * WINGSPEED) * (float) Math.PI * 0.25f);
        newangle = Mth.cos(ageInTicks * 0.5f * WINGSPEED) * (float) Math.PI * 0.1f;
        rotateX(processor, "jaw", 0.5f + newangle);
    }

    public static final class Renderer extends OreSpawnGeoReplacedEntityRenderer<CloudShark, CloudSharkGeoReplacement> {
        public Renderer(EntityRendererProvider.Context context) {
            super(context, new CloudSharkGeoReplacement());
        }
    }
}
