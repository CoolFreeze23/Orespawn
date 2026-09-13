package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.EntityTerribleTerror;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.animation.AnimationProcessor;

/**
 * GeckoLib Terrible Terror (the third Tier-2 slice, 2026-09-13): {@link TerribleTerrorModel#setupAnim} verbatim on
 * the converted rig, ON THE HOOK (owner 2026-09-13, Amendment 2 to Amendment 1: no keyframe layer, no transcription -
 * the self-gate stays closed until an artist delivers {@code idle} and {@code walk}). The port's classic model as it
 * is: the wings about Z at 1.3 rad/tick around +-2.0 rad, the jaw's {@code |cos|} idiom at 0.3, and four leg parts
 * about X at 1.25 around +-0.349 rad (orig ModelTerribleTerror.java:171-177, :179, :181, :183 for the lines the port
 * keeps - the register line of this slice records where the port's pose departs from the 1.7.10 one). The horns, the
 * wings and the tail tip are zero-thickness cubes: the seam draws every cube with its true transformed normal
 * (ENT-S-161) and its two coplanar faces in the classic order (below).
 *
 * <p>Scale and shadow follow {@link TerribleTerrorRenderer}: 0.75 render scale and a 0.45 x 0.75 shadow (ENT-S-092).</p>
 */
public final class TerribleTerrorGeoReplacement extends OreSpawnGeoReplacement<EntityTerribleTerror> {
    private static final GeoReplacementDescriptor<EntityTerribleTerror> DESCRIPTOR = new GeoReplacementDescriptor<>(
            () -> ModEntities.ENTITY_TERRIBLE_TERROR.get(),  // lambda: a bound method ref would initialise ModEntities eagerly
            EntityTerribleTerror.class,
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "geo/entity/terribleterror.geo.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "animations/entity/terribleterror.animation.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/terribleterror.png"),
            TerribleTerrorRenderer.SHADOW) {
        @Override
        public void applyScale(EntityTerribleTerror entity, PoseStack poseStack, float partialTick) {
            // orig RenderTerribleTerror.preRenderScale (:39-45): GL11.glScalef(scale, scale, scale) unconditionally
            poseStack.scale(TerribleTerrorRenderer.SCALE, TerribleTerrorRenderer.SCALE, TerribleTerrorRenderer.SCALE);
        }

        /**
         * A rig with zero-thickness cubes (the two horns 0 x 2 x 2, the two wings 0 x 11 x 15, the tail tip 3 x 0 x 2):
         * their two coplanar faces z-fight and the winner is the face emitted last, so the shipped geo carries the
         * classic within-cube order ({@link FaceOrder#KEY}; the FaceOrder contract's open item for a cutout rig,
         * TEST-007) and the seam expects it.
         */
        @Override
        public boolean cubeFaceOrderRequired() {
            return true;
        }
    };

    public TerribleTerrorGeoReplacement() {
        super(DESCRIPTOR);
    }

    @Override
    protected void applyCustomAnimations(AnimationProcessor<?> processor, PoseInputs inputs) {
        float ageInTicks = inputs.ageInTicks();
        // TerribleTerrorModel.setupAnim verbatim, the float chain left to right.
        float wingAngle = Mth.cos(ageInTicks * 1.3f) * (float) Math.PI * 0.25f;
        rotateZ(processor, "wing1", -2.0f + wingAngle);
        rotateZ(processor, "wing2", 2.0f - wingAngle);
        float jawAngle = Mth.cos(ageInTicks * 0.3f) * (float) Math.PI * 0.1f;
        rotateX(processor, "jaw", Math.abs(jawAngle));
        float legAngle = Mth.cos(ageInTicks * 1.25f) * (float) Math.PI * 0.35f;
        rotateX(processor, "fl21", 0.349f + legAngle);
        rotateX(processor, "fl11", 0.349f - legAngle);
        rotateX(processor, "bl21", -0.349f - legAngle);
        rotateX(processor, "bl11", -0.349f + legAngle);
    }

    public static final class Renderer extends OreSpawnGeoReplacedEntityRenderer<EntityTerribleTerror, TerribleTerrorGeoReplacement> {
        public Renderer(EntityRendererProvider.Context context) {
            super(context, new TerribleTerrorGeoReplacement());
        }
    }
}
