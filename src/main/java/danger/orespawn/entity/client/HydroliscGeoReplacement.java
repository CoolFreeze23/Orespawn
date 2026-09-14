package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.EntityHydrolisc;
import danger.orespawn.entity.pose.HydroliscPose;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.animation.AnimationProcessor;

/**
 * GeckoLib Hydrolisc (the hook survey, landed by the fifth Tier-2 slice T2e): {@link HydroliscModel#poseFrom} verbatim on the converted rig, ON THE
 * HOOK (no keyframe layer, no transcription - the
 * self-gate stays closed until an artist delivers {@code idle} and {@code walk}). Wingspeed 0.65f (orig
 * ModelHydrolisc.java:14,57 / ClientProxyOreSpawn.java:422): the THRESHOLD idiom on the four six-part legs' pitch -
 * above a walking speed of a tenth {@code cos(age * 1.3 ws) * PI * 0.25 * limbSwingAmount} around each part's rest
 * offset (0 / -0.488 / -2.347 / -0.628 / -0.628 / +0.174), the front left and rear right positive, the other two the
 * negative, 0 at or below it; the SITTING-stilled tail sway ({@code cos(age * 1.0 ws) * PI * 0.15}, or 0
 * when the entity sits) folded down three rings as 0.25 / 0.5 / 0.75 of the yaw with a POSITION follow through {@link
 * #moveTo} (5 and 8 units along cos / sin); and the HEALTH-FREQUENCY idiom on the three feathers: {@code hf = health /
 * maxHealth} scales the 1.25 ws and 0.75 ws rhythms' frequency and their {@code PI * 0.2} amplitude, the outer
 * pair around +-0.9 rad. The entity is read through {@link HydroliscPose}.
 *
 * <p>Scale and shadow follow {@link HydroliscRenderer}: 0.65 render scale, halved for a baby, and a 0.65 x 0.65 shadow
 * (ENT-S-092).</p>
 */
public final class HydroliscGeoReplacement extends OreSpawnGeoReplacement<EntityHydrolisc> {
    /** orig ModelHydrolisc.java:14,57 {@code wingspeed} = 0.65f (ClientProxyOreSpawn.java:422): the chain's third multiply. */
    static final float WINGSPEED = 0.65F;
    private static final GeoReplacementDescriptor<EntityHydrolisc> DESCRIPTOR = new GeoReplacementDescriptor<>(
            () -> ModEntities.ENTITY_HYDROLISC.get(),  // lambda: a bound method ref would initialise ModEntities eagerly
            EntityHydrolisc.class,
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "geo/entity/hydrolisc.geo.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "animations/entity/hydrolisc.animation.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/hydrolisc.png"),
            HydroliscRenderer.SHADOW) {
        @Override
        public void applyScale(EntityHydrolisc entity, PoseStack poseStack, float partialTick) {
            // orig RenderHydrolisc.preRenderScale (:39-45): a child gets glScalef(scale / 2), otherwise glScalef(scale)
            if (entity.isBaby()) {
                poseStack.scale(HydroliscRenderer.SCALE / 2.0F, HydroliscRenderer.SCALE / 2.0F, HydroliscRenderer.SCALE / 2.0F);
                return;
            }
            poseStack.scale(HydroliscRenderer.SCALE, HydroliscRenderer.SCALE, HydroliscRenderer.SCALE);
        }
    };

    public HydroliscGeoReplacement() {
        super(DESCRIPTOR);
    }

    @Override
    protected void applyCustomAnimations(AnimationProcessor<?> processor, PoseInputs inputs) {
        HydroliscPose entity = inputs.subject(HydroliscPose.class);
        float limbSwingAmount = inputs.limbSwingAmount();
        float ageInTicks = inputs.ageInTicks();
        // HydroliscModel.poseFrom (the body of the classic setupAnim) verbatim, the float chain left to right.
        float hf = 0.0f;
        float newangle = 0.0f;
        newangle = (double) limbSwingAmount > 0.1 ? Mth.cos(ageInTicks * 1.3f * WINGSPEED) * (float) Math.PI * 0.25f * limbSwingAmount : 0.0f;
        rotateX(processor, "lf1", newangle);
        rotateX(processor, "lf2", newangle - 0.488f);
        rotateX(processor, "lf3", newangle - 2.347f);
        rotateX(processor, "lf4", newangle - 0.628f);
        rotateX(processor, "lf5", newangle - 0.628f);
        rotateX(processor, "lf6", newangle + 0.174f);
        rotateX(processor, "rf1", -newangle);
        rotateX(processor, "rf2", -newangle - 0.488f);
        rotateX(processor, "rf3", -newangle - 2.347f);
        rotateX(processor, "rf4", -newangle - 0.628f);
        rotateX(processor, "rf5", -newangle - 0.628f);
        rotateX(processor, "rf6", -newangle + 0.174f);
        rotateX(processor, "lb1", -newangle);
        rotateX(processor, "lb2", -newangle - 0.488f);
        rotateX(processor, "lb3", -newangle - 2.347f);
        rotateX(processor, "lb4", -newangle - 0.628f);
        rotateX(processor, "lb5", -newangle - 0.628f);
        rotateX(processor, "lb6", -newangle + 0.174f);
        rotateX(processor, "rb1", newangle);
        rotateX(processor, "rb2", newangle - 0.488f);
        rotateX(processor, "rb3", newangle - 2.347f);
        rotateX(processor, "rb4", newangle - 0.628f);
        rotateX(processor, "rb5", newangle - 0.628f);
        rotateX(processor, "rb6", newangle + 0.174f);
        newangle = Mth.cos(ageInTicks * 1.0f * WINGSPEED) * (float) Math.PI * 0.15f;
        if (entity.isInSittingPose()) {
            newangle = 0.0f;
        }
        float[] tail1 = classicPosition(bone(processor, "tail1"));  // never written: the bind pivot
        float[] tail2 = classicPosition(bone(processor, "tail2"));  // y never written: the bind
        float[] tail3 = classicPosition(bone(processor, "tail3"));  // y never written: the bind
        float tail1YRot = newangle * 0.25f;
        rotateY(processor, "tail1", tail1YRot);
        float tail2Z = tail1[2] + (float) Math.cos(tail1YRot) * 5.0f;
        float tail2X = tail1[0] + (float) Math.sin(tail1YRot) * 5.0f;
        moveTo(processor, "tail2", tail2X, tail2[1], tail2Z);
        float tail2YRot = newangle * 0.5f;
        rotateY(processor, "tail2", tail2YRot);
        float tail3Z = tail2Z + (float) Math.cos(tail2YRot) * 8.0f;
        float tail3X = tail2X + (float) Math.sin(tail2YRot) * 8.0f;
        moveTo(processor, "tail3", tail3X, tail3[1], tail3Z);
        rotateY(processor, "tail3", newangle * 0.75f);
        hf = entity.getHealth() / entity.getMaxHealth();
        rotateY(processor, "feather2", newangle = Mth.cos(ageInTicks * 1.25f * WINGSPEED * hf) * (float) Math.PI * 0.2f * hf);
        newangle = Mth.cos(ageInTicks * 0.75f * WINGSPEED * hf) * (float) Math.PI * 0.2f * hf;
        rotateY(processor, "feather1", newangle - 0.9f);
        rotateY(processor, "feather3", -newangle + 0.9f);
    }

    public static final class Renderer extends OreSpawnGeoReplacedEntityRenderer<EntityHydrolisc, HydroliscGeoReplacement> {
        public Renderer(EntityRendererProvider.Context context) {
            super(context, new HydroliscGeoReplacement());
        }
    }
}
