package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.EntityMantis;
import danger.orespawn.entity.pose.MantisPose;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.animation.AnimationProcessor;

/**
 * GeckoLib Mantis (the hook lanes, 2026-09-14, addendum item 10; landed by the fifth Tier-2 slice T2e, 2026-09-15, the owner's closing set item 4):
 * {@link MantisModel#poseFrom} verbatim on the
 * converted rig, ON THE HOOK (no keyframe layer, no transcription - the self-gate stays closed until an artist
 * delivers {@code idle} and {@code walk}). Wingspeed 2.0f (orig ModelMantis.java:14,53 / ClientProxyOreSpawn.java:488):
 * the four wings about Z at 0.9 ws - the front pair {@code PI * 0.25} around -+0.698, the rear pair {@code PI * 0.35}
 * around -+0.349, mirrored; and the ATTACKING-branch forearms - at rest {@code cos(age * 0.051 ws) * PI * 0.013} around
 * a1 = -0.2, attacking {@code cos(age * 0.51 ws) * PI * 0.25} around a1 = -0.698 - folded down each three-part arm as
 * pitch (a1 + angle, -a1 - angle, a1 + angle; the right arm's angle the negative) with a POSITION follow through
 * {@link #moveTo}: the second part 22 units along the first's pitch from one unit ahead, the third 17 units back along
 * the second's. The entity is read through {@link MantisPose}.
 *
 * <p>Scale and shadow follow {@link MantisRenderer}: 1.1 render scale and a 0.9 x 1.1 shadow (ENT-S-092).</p>
 */
public final class MantisGeoReplacement extends OreSpawnGeoReplacement<EntityMantis> {
    /** orig ModelMantis.java:14,53 {@code wingspeed} = 2.0f (ClientProxyOreSpawn.java:488): the chain's third multiply. */
    static final float WINGSPEED = 2.0F;
    private static final GeoReplacementDescriptor<EntityMantis> DESCRIPTOR = new GeoReplacementDescriptor<>(
            () -> ModEntities.ENTITY_MANTIS.get(),  // lambda: a bound method ref would initialise ModEntities eagerly
            EntityMantis.class,
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "geo/entity/mantis.geo.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "animations/entity/mantis.animation.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/mantis.png"),
            MantisRenderer.SHADOW) {
        @Override
        public void applyScale(EntityMantis entity, PoseStack poseStack, float partialTick) {
            // orig RenderMantis.preRenderCallback: GL11.glScalef(scale, scale, scale) (MantisRenderer.scale)
            poseStack.scale(MantisRenderer.SCALE, MantisRenderer.SCALE, MantisRenderer.SCALE);
        }
    };

    public MantisGeoReplacement() {
        super(DESCRIPTOR);
    }

    @Override
    protected void applyCustomAnimations(AnimationProcessor<?> processor, PoseInputs inputs) {
        MantisPose entity = inputs.subject(MantisPose.class);
        float ageInTicks = inputs.ageInTicks();
        // MantisModel.poseFrom (the body of the classic setupAnim) verbatim, the float / double chain exactly as the
        // classic casts it; the first arm parts' pivots are never written (the bind), the followers' x stays the bind.
        float a1;
        float newangle = 0.0f;
        newangle = Mth.cos(ageInTicks * 0.9f * WINGSPEED) * (float) Math.PI * 0.25f;
        rotateZ(processor, "lfwing", -0.698f - newangle);
        rotateZ(processor, "rfwing", 0.698f + newangle);
        newangle = Mth.cos(ageInTicks * 0.9f * WINGSPEED) * (float) Math.PI * 0.35f;
        rotateZ(processor, "lrwing", -0.349f + newangle);
        rotateZ(processor, "rrwing", 0.349f - newangle);
        if (entity.getAttacking() == 0) {
            newangle = Mth.cos(ageInTicks * 0.051f * WINGSPEED) * (float) Math.PI * 0.013f;
            a1 = -0.2f;
        } else {
            newangle = Mth.cos(ageInTicks * 0.51f * WINGSPEED) * (float) Math.PI * 0.25f;
            a1 = -0.698f;
        }
        float[] larm1 = classicPosition(bone(processor, "larm1"));
        float[] larm2 = classicPosition(bone(processor, "larm2"));
        float[] larm3 = classicPosition(bone(processor, "larm3"));
        float larm1XRot = a1 + newangle;
        rotateX(processor, "larm1", larm1XRot);
        float larm2Z = (float) ((double) (larm1[2] + 1.0f) + Math.sin(larm1XRot) * 22.0);
        float larm2Y = (float) ((double) larm1[1] + Math.cos(larm1XRot) * 22.0);
        moveTo(processor, "larm2", larm2[0], larm2Y, larm2Z);
        float larm2XRot = -a1 - newangle;
        rotateX(processor, "larm2", larm2XRot);
        float larm3Z = (float) ((double) (larm2Z + 1.0f) - Math.sin(larm2XRot) * 17.0);
        float larm3Y = (float) ((double) larm2Y - Math.cos(larm2XRot) * 17.0);
        moveTo(processor, "larm3", larm3[0], larm3Y, larm3Z);
        rotateX(processor, "larm3", a1 + newangle);
        float[] rarm1 = classicPosition(bone(processor, "rarm1"));
        float[] rarm2 = classicPosition(bone(processor, "rarm2"));
        float[] rarm3 = classicPosition(bone(processor, "rarm3"));
        float rarm1XRot = a1 - newangle;
        rotateX(processor, "rarm1", rarm1XRot);
        float rarm2Z = (float) ((double) (rarm1[2] + 1.0f) + Math.sin(rarm1XRot) * 22.0);
        float rarm2Y = (float) ((double) rarm1[1] + Math.cos(rarm1XRot) * 22.0);
        moveTo(processor, "rarm2", rarm2[0], rarm2Y, rarm2Z);
        float rarm2XRot = -a1 + newangle;
        rotateX(processor, "rarm2", rarm2XRot);
        float rarm3Z = (float) ((double) (rarm2Z + 1.0f) - Math.sin(rarm2XRot) * 17.0);
        float rarm3Y = (float) ((double) rarm2Y - Math.cos(rarm2XRot) * 17.0);
        moveTo(processor, "rarm3", rarm3[0], rarm3Y, rarm3Z);
        rotateX(processor, "rarm3", a1 - newangle);
    }

    public static final class Renderer extends OreSpawnGeoReplacedEntityRenderer<EntityMantis, MantisGeoReplacement> {
        public Renderer(EntityRendererProvider.Context context) {
            super(context, new MantisGeoReplacement());
        }
    }
}
