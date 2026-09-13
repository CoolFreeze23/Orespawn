package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.EntityHerculesBeetle;
import danger.orespawn.entity.pose.HerculesBeetlePose;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.animation.AnimationProcessor;

/**
 * GeckoLib Hercules Beetle (the third Tier-2 slice, 2026-09-13): {@link HerculesBeetleModel#poseFrom} verbatim on the
 * converted rig, ON THE HOOK (owner 2026-09-13, Amendment 2 to Amendment 1: no keyframe layer, no transcription - the
 * self-gate stays closed until an artist delivers {@code idle} and {@code walk}). Wingspeed 1.0f (orig
 * ModelHerculesBeetle.java:14,54 / ClientProxyOreSpawn.java:489): the GAIT-scaled idiom on the eighteen leg parts -
 * {@code cos(age * ws * 0.45f) * PI * 0.12f * limbSwingAmount} about Y, the front and rear pairs around +-0.349 rad,
 * the middle pair around 0 (orig :286-292; no threshold) - and the ATTACKING-branch idiom on the nine jaw parts (orig
 * :293 {@code getAttacking() == 0}: 0.051f x PI x 0.01f at rest, 0.51f x PI x 0.07f attacking, about X around 0.122 /
 * 0 / 0.314 rad rests). The entity is read through {@link HerculesBeetlePose} (the S4 doctrine).
 *
 * <p>Scale and shadow follow {@link HerculesBeetleRenderer}: 1.1 render scale and a 0.99 x 1.1 shadow (ENT-S-092).</p>
 */
public final class HerculesBeetleGeoReplacement extends OreSpawnGeoReplacement<EntityHerculesBeetle> {
    /** orig ModelHerculesBeetle.java:14,54 {@code wingspeed} = 1.0f (ClientProxyOreSpawn.java:489): the chain's second multiply. */
    static final float WINGSPEED = 1.0F;
    private static final GeoReplacementDescriptor<EntityHerculesBeetle> DESCRIPTOR = new GeoReplacementDescriptor<>(
            () -> ModEntities.ENTITY_HERCULES_BEETLE.get(),  // lambda: a bound method ref would initialise ModEntities eagerly
            EntityHerculesBeetle.class,
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "geo/entity/herculesbeetle.geo.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "animations/entity/herculesbeetle.animation.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/herculesbeetle.png"),
            HerculesBeetleRenderer.SHADOW) {
        @Override
        public void applyScale(EntityHerculesBeetle entity, PoseStack poseStack, float partialTick) {
            // orig RenderHerculesBeetle.preRenderCallback: GL11.glScalef(scale, scale, scale) (HerculesBeetleRenderer.scale)
            poseStack.scale(HerculesBeetleRenderer.SCALE, HerculesBeetleRenderer.SCALE, HerculesBeetleRenderer.SCALE);
        }
    };

    public HerculesBeetleGeoReplacement() {
        super(DESCRIPTOR);
    }

    @Override
    protected void applyCustomAnimations(AnimationProcessor<?> processor, PoseInputs inputs) {
        HerculesBeetlePose entity = inputs.subject(HerculesBeetlePose.class);
        float limbSwingAmount = inputs.limbSwingAmount();
        float ageInTicks = inputs.ageInTicks();
        // HerculesBeetleModel.poseFrom (the body of the classic setupAnim) verbatim, the float chain left to right.
        float newangle = Mth.cos(ageInTicks * WINGSPEED * 0.45f) * (float) Math.PI * 0.12f * limbSwingAmount;
        float front = 0.349f + newangle;
        float middle = -newangle;
        float rear = -0.349f + newangle;
        rotateY(processor, "lfleg1", front);
        rotateY(processor, "lfleg2", front);
        rotateY(processor, "lfleg3", front);
        rotateY(processor, "lmleg1", middle);
        rotateY(processor, "lmleg2", middle);
        rotateY(processor, "lmleg3", middle);
        rotateY(processor, "lrleg1", rear);
        rotateY(processor, "lrleg2", rear);
        rotateY(processor, "lrleg3", rear);
        rotateY(processor, "rfleg1", rear);
        rotateY(processor, "rfleg2", rear);
        rotateY(processor, "rfleg3", rear);
        rotateY(processor, "rmleg1", middle);
        rotateY(processor, "rmleg2", middle);
        rotateY(processor, "rmleg3", middle);
        rotateY(processor, "rrleg1", front);
        rotateY(processor, "rrleg2", front);
        rotateY(processor, "rrleg3", front);
        newangle = entity.getAttacking() == 0
                ? Mth.cos(ageInTicks * 0.051f * WINGSPEED) * (float) Math.PI * 0.01f
                : Mth.cos(ageInTicks * 0.51f * WINGSPEED) * (float) Math.PI * 0.07f;
        rotateX(processor, "jaw1", 0.122f + newangle);
        rotateX(processor, "jaw2", 0.122f + newangle);
        rotateX(processor, "jaw3", 0.0f + newangle);
        rotateX(processor, "jaw4", 0.0f + newangle);
        rotateX(processor, "jaw5", 0.122f + newangle);
        rotateX(processor, "jaw6", 0.122f + newangle);
        rotateX(processor, "jaw7", 0.0f + newangle);
        rotateX(processor, "jaw8", 0.0f + newangle);
        rotateX(processor, "jaw9", 0.314f + newangle);
    }

    public static final class Renderer extends OreSpawnGeoReplacedEntityRenderer<EntityHerculesBeetle, HerculesBeetleGeoReplacement> {
        public Renderer(EntityRendererProvider.Context context) {
            super(context, new HerculesBeetleGeoReplacement());
        }
    }
}
