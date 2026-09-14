package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.Urchin;
import danger.orespawn.entity.pose.UrchinPose;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.animation.AnimationProcessor;

/**
 * GeckoLib Urchin (the hooks, landed by the sixth Tier-2 slice T2f): {@link ModelUrchin#poseFrom} verbatim on the
 * converted rig, ON THE HOOK (no keyframe layer, no transcription - the self-gate stays closed until an artist
 * delivers {@code idle} and {@code walk}). Wingspeed 1.0f (orig ModelUrchin.java:14,34 /
 * ClientProxyOreSpawn.java:487): the THRESHOLD idiom on the eight fins - above a walking speed of a tenth, five gait
 * cosines (0.7 / 1.7 / 1.65 / 1.75 / 1.8 x ws) at 0.15 x PI x limbSwingAmount about their rest pitches and rolls
 * (+-0.261 / +-0.523 rad), 0 at or below it - and the ATTACKING branch (orig :175 {@code getAttacking() != 0} )
 * that spins the center about Y on {@code (age x 0.2) mod 2 PI} (0.02 at rest) and rocks the
 * eight spines on eight cosines (0.7 ... 0.35 x ws at 0.06 x PI attacking, 0.07 ... 0.035 x ws at 0.02 x PI at rest)
 * about their rest angles. The entity is read through {@link UrchinPose} (the Slice 4b form).
 *
 * <p>Scale and shadow follow {@link UrchinRenderer} : 1.25 render scale and a 0.35 x 1.25 shadow (ENT-S-092).</p>
 */
public final class UrchinGeoReplacement extends OreSpawnGeoReplacement<Urchin> {
    /** orig ModelUrchin.java:14,34 {@code wingspeed} = 1.0f (ClientProxyOreSpawn.java:487): the chain's third multiply. */
    static final float WINGSPEED = 1.0F;
    private static final GeoReplacementDescriptor<Urchin> DESCRIPTOR = new GeoReplacementDescriptor<>(
            () -> ModEntities.URCHIN.get(),  // lambda: a bound method ref would initialise ModEntities eagerly
            Urchin.class,
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "geo/entity/urchin.geo.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "animations/entity/urchin.animation.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/urchin.png"),
            UrchinRenderer.SHADOW) {
        @Override
        public void applyScale(Urchin entity, PoseStack poseStack, float partialTick) {
            // orig RenderUrchin.preRenderScale (:39-41): GL11.glScalef(scale, scale, scale) (UrchinRenderer.scale)
            poseStack.scale(UrchinRenderer.SCALE, UrchinRenderer.SCALE, UrchinRenderer.SCALE);
        }
    };

    public UrchinGeoReplacement() {
        super(DESCRIPTOR);
    }

    @Override
    protected void applyCustomAnimations(AnimationProcessor<?> processor, PoseInputs inputs) {
        UrchinPose entity = inputs.subject(UrchinPose.class);
        float limbSwingAmount = inputs.limbSwingAmount();
        float ageInTicks = inputs.ageInTicks();
        // ModelUrchin.poseFrom (the body of the classic setupAnim) verbatim, the float chain left to right.
        float newangle8;
        float newangle7;
        float newangle6;
        float newangle5;
        float newangle4;
        float newangle3;
        float newangle2;
        float newangle1;
        float newangle;
        if ((double) limbSwingAmount > 0.1) {
            newangle = Mth.cos(ageInTicks * 0.7f * WINGSPEED) * (float) Math.PI * 0.15f * limbSwingAmount;
            newangle1 = Mth.cos(ageInTicks * 1.7f * WINGSPEED) * (float) Math.PI * 0.15f * limbSwingAmount;
            newangle2 = Mth.cos(ageInTicks * 1.65f * WINGSPEED) * (float) Math.PI * 0.15f * limbSwingAmount;
            newangle3 = Mth.cos(ageInTicks * 1.75f * WINGSPEED) * (float) Math.PI * 0.15f * limbSwingAmount;
            newangle4 = Mth.cos(ageInTicks * 1.8f * WINGSPEED) * (float) Math.PI * 0.15f * limbSwingAmount;
        } else {
            newangle = 0.0f;
            newangle1 = 0.0f;
            newangle2 = 0.0f;
            newangle3 = 0.0f;
            newangle4 = 0.0f;
        }
        rotateX(processor, "if1", 0.261f + newangle1);
        rotateX(processor, "if2", -0.261f - newangle2);
        rotateX(processor, "if3", newangle3);
        rotateX(processor, "if4", -newangle4);
        rotateZ(processor, "of1", -0.523f + newangle);
        rotateZ(processor, "of2", 0.523f - newangle);
        rotateX(processor, "of3", -0.523f + newangle);
        rotateX(processor, "of4", 0.523f - newangle);
        if (entity.getAttacking() != 0) {
            newangle = (float) ((double) (ageInTicks * 0.2f) % (Math.PI * 2));
            newangle1 = Mth.cos(ageInTicks * 0.7f * WINGSPEED) * (float) Math.PI * 0.06f;
            newangle2 = Mth.cos(ageInTicks * 0.65f * WINGSPEED) * (float) Math.PI * 0.06f;
            newangle3 = Mth.cos(ageInTicks * 0.75f * WINGSPEED) * (float) Math.PI * 0.06f;
            newangle4 = Mth.cos(ageInTicks * 0.8f * WINGSPEED) * (float) Math.PI * 0.06f;
            newangle5 = Mth.cos(ageInTicks * 0.55f * WINGSPEED) * (float) Math.PI * 0.06f;
            newangle6 = Mth.cos(ageInTicks * 0.45f * WINGSPEED) * (float) Math.PI * 0.06f;
            newangle7 = Mth.cos(ageInTicks * 0.35f * WINGSPEED) * (float) Math.PI * 0.06f;
            newangle8 = Mth.cos(ageInTicks * 0.4f * WINGSPEED) * (float) Math.PI * 0.06f;
        } else {
            newangle = (float) ((double) (ageInTicks * 0.02f) % (Math.PI * 2));
            newangle1 = Mth.cos(ageInTicks * 0.07f * WINGSPEED) * (float) Math.PI * 0.02f;
            newangle2 = Mth.cos(ageInTicks * 0.065f * WINGSPEED) * (float) Math.PI * 0.02f;
            newangle3 = Mth.cos(ageInTicks * 0.075f * WINGSPEED) * (float) Math.PI * 0.02f;
            newangle4 = Mth.cos(ageInTicks * 0.08f * WINGSPEED) * (float) Math.PI * 0.02f;
            newangle5 = Mth.cos(ageInTicks * 0.055f * WINGSPEED) * (float) Math.PI * 0.02f;
            newangle6 = Mth.cos(ageInTicks * 0.045f * WINGSPEED) * (float) Math.PI * 0.02f;
            newangle7 = Mth.cos(ageInTicks * 0.035f * WINGSPEED) * (float) Math.PI * 0.02f;
            newangle8 = Mth.cos(ageInTicks * 0.04f * WINGSPEED) * (float) Math.PI * 0.02f;
        }
        rotateY(processor, "center", newangle);
        rotateX(processor, "tis1", 0.261f + newangle1);
        rotateX(processor, "tis2", -0.261f + newangle2);
        rotateX(processor, "tis3", newangle3);
        rotateX(processor, "tis4", newangle4);
        rotateZ(processor, "tis1", newangle5);
        rotateZ(processor, "tis2", newangle6);
        rotateZ(processor, "tis3", 0.261f + newangle7);
        rotateZ(processor, "tis4", -0.261f + newangle8);
        rotateX(processor, "tos1", -0.532f + newangle1);
        rotateX(processor, "tos2", newangle7);
        rotateX(processor, "tos3", newangle3);
        rotateX(processor, "tos4", 0.532f + newangle5);
        rotateZ(processor, "tos1", newangle4);
        rotateZ(processor, "tos2", -0.523f + newangle6);
        rotateZ(processor, "tos3", 0.523f + newangle2);
        rotateZ(processor, "tos4", newangle8);
    }

    public static final class Renderer extends OreSpawnGeoReplacedEntityRenderer<Urchin, UrchinGeoReplacement> {
        public Renderer(EntityRendererProvider.Context context) {
            super(context, new UrchinGeoReplacement());
        }
    }
}
