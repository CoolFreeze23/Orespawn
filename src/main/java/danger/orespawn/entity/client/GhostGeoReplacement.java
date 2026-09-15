package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.Ghost;
import java.util.function.Function;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.animation.AnimationProcessor;

/**
 * GeckoLib Ghost (the hook survey, landed by the remainder slice, under ENT-S-160 (a) with (d)): {@link
 * GhostModel#setupAnim} verbatim on the converted rig, ON THE HOOK (no keyframe layer, no transcription - the
 * self-gate stays closed until an artist delivers {@code idle} and {@code walk}). The 1.7.10 rig's four slow
 * cosines on the two arms (orig ModelGhost.render, ENT-S-091): the left arm about Z at 0.30 and about X at 0.34, the
 * right about Z at 0.32 and about X at 0.36, every one {@code PI * 0.05} around the +-0.33 rad splay; no wingspeed,
 * no gait, no entity state.
 *
 * <p>Scale and shadow follow {@link GhostRenderer}: 0.65 render scale and a 0.0 x 0.65 shadow (ENT-S-092: 1.7.10 drew
 * no shadow under the ghost; a 0.0 radius fails the dispatcher's {@code f > 0.0F} gate the same way). The render type
 * is the classic renderer's translucent pipeline, handed over as the classic model's own function object
 * {@link GhostModel#RENDER_TYPE} (the ENT-S-146 / {@link FairyModel#RENDER_TYPE} form - the ENT-S-160 (a) move:
 * {@link GhostRenderer#getRenderType} applies the same object) so the harness proves the two renderers equal by
 * identity; a blending rig, so the shipped geo carries the classic face order.</p>
 */
public final class GhostGeoReplacement extends OreSpawnGeoReplacement<Ghost> {
    private static final GeoReplacementDescriptor<Ghost> DESCRIPTOR = new GeoReplacementDescriptor<>(
            () -> ModEntities.GHOST.get(),  // lambda: a bound method ref would initialise ModEntities eagerly
            Ghost.class,
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "geo/entity/ghost.geo.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "animations/entity/ghost.animation.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/ghost.png"),
            GhostRenderer.SHADOW) {
        @Override
        public void applyScale(Ghost entity, PoseStack poseStack, float partialTick) {
            // orig preRenderScale: GL11.glScalef(scale, scale, scale) (GhostRenderer.scale)
            poseStack.scale(GhostRenderer.SCALE, GhostRenderer.SCALE, GhostRenderer.SCALE);
        }

        /** GhostRenderer.getRenderType applies {@link GhostModel#RENDER_TYPE} to the texture: the same object, handed over by identity. */
        @Override
        public Function<ResourceLocation, RenderType> renderType(Ghost entity) {
            return GhostModel.RENDER_TYPE;
        }

        /**
         * A blending rig (entity_translucent) with no zero-thickness cube: the order a cube's six faces are emitted in is
         * visible in the blend, so the shipped geo carries the classic within-cube order ({@link FaceOrder#KEY}; ENT-S-146, the
         * Ghost Skelly form - the harness refuses a blending rig whose generated geo carries none) and the seam expects it.
         */
        @Override
        public boolean cubeFaceOrderRequired() {
            return true;
        }
    };

    public GhostGeoReplacement() {
        super(DESCRIPTOR);
    }

    @Override
    protected void applyCustomAnimations(AnimationProcessor<?> processor, PoseInputs inputs) {
        float ageInTicks = inputs.ageInTicks();
        // GhostModel.setupAnim verbatim: four slow cosines on the arms (0.30, 0.32, 0.34, 0.36), amplitude PI*0.05.
        rotateZ(processor, "LArm", -0.33F + Mth.cos(ageInTicks * 0.3F) * (float) Math.PI * 0.05F);
        rotateZ(processor, "RArm", 0.33F + Mth.cos(ageInTicks * 0.32F) * (float) Math.PI * 0.05F);
        rotateX(processor, "LArm", -0.33F + Mth.cos(ageInTicks * 0.34F) * (float) Math.PI * 0.05F);
        rotateX(processor, "RArm", 0.33F + Mth.cos(ageInTicks * 0.36F) * (float) Math.PI * 0.05F);
    }

    public static final class Renderer extends OreSpawnGeoReplacedEntityRenderer<Ghost, GhostGeoReplacement> {
        public Renderer(EntityRendererProvider.Context context) {
            super(context, new GhostGeoReplacement());
        }
    }
}
