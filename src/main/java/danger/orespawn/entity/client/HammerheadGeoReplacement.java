package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.Hammerhead;
import danger.orespawn.entity.pose.HammerheadPose;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.animation.AnimationProcessor;

/**
 * GeckoLib Hammerhead (the hooks, landed by the first Tier-1 slice T1a): {@link ModelHammerhead#poseFrom} verbatim on
 * the converted rig, ON THE HOOK (no keyframe layer, no transcription - the self-gate stays closed until an artist
 * delivers {@code idle} and {@code walk} ; the geo, the wiring and the proofs landed with T1a). Wingspeed 0.33f (orig
 * ModelHammerhead.java:14,54 / ClientProxyOreSpawn.java:501): the THRESHOLD idiom on the twelve leg parts'
 * pitch - above a walking speed of a tenth ({@code (double) limbSwingAmount > 0.1}) {@code cos(age x 1.3 ws) x PI x
 * 0.1 x amount} and the same an eighth-turn ahead for the middle pair, 0 at or below it - around the rest pitches
 * -0.087 / -0.052 / -0.349 (the lower parts around 0, the right side mirrored); the HEAD-LOOK idiom (yaw {@code
 * toRadians(netHeadYaw) x 0.25} on the neck, its armour, the four horns and their base, the head, the snout and the
 * centre fan, the side fans and ears offset by -+0.122 / 0.226 / 0.227); the back armour's yaw sway at 0.3 ws x
 * PI x 0.03 around -+0.349; and the ATTACKING branch ({@code getAttacking() != 0}: {@code cos(age x 1.3 ws) x PI x
 * 0.13} , else 0) nodding the sixteen neck / head / horn / fan / ear parts over their rest pitches (0.157 / 0.087 /
 * 0.192 / 0.209 / 0.611 / -0.139 / -0.209 / -0.331 / 0.366). The entity is read through {@link HammerheadPose} (the
 * Slice 4b doctrine).
 *
 * <p>Scale and shadow follow {@link HammerheadRenderer} : 2.5 render scale and a 1.0 x 2.5 shadow (ENT-S-092). No
 * zero-thickness cube.</p>
 */
public final class HammerheadGeoReplacement extends OreSpawnGeoReplacement<Hammerhead> {
    /** orig ModelHammerhead.java:14,54 {@code wingspeed} = 0.33f (ClientProxyOreSpawn.java:501): the chain's frequency multiplier. */
    static final float WINGSPEED = 0.33f;
    private static final GeoReplacementDescriptor<Hammerhead> DESCRIPTOR = new GeoReplacementDescriptor<>(
            () -> ModEntities.HAMMERHEAD.get(),  // lambda: a bound method ref would initialise ModEntities eagerly
            Hammerhead.class,
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "geo/entity/hammerhead.geo.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "animations/entity/hammerhead.animation.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/hammerhead.png"),
            HammerheadRenderer.SHADOW) {
        @Override
        public void applyScale(Hammerhead entity, PoseStack poseStack, float partialTick) {
            // orig RenderHammerhead.preRenderScale (:39-41): GL11.glScalef(scale, scale, scale) (HammerheadRenderer.scale)
            poseStack.scale(HammerheadRenderer.SCALE, HammerheadRenderer.SCALE, HammerheadRenderer.SCALE);
        }
    };

    public HammerheadGeoReplacement() {
        super(DESCRIPTOR);
    }

    @Override
    protected void applyCustomAnimations(AnimationProcessor<?> processor, PoseInputs inputs) {
        HammerheadPose entity = inputs.subject(HammerheadPose.class);
        float limbSwingAmount = inputs.limbSwingAmount();
        float ageInTicks = inputs.ageInTicks();
        float netHeadYaw = inputs.netHeadYaw();
        // ModelHammerhead.poseFrom verbatim, the float / double chain exactly as the classic casts it.
        float newangle = 0.0f;
        float newangle2 = 0.0f;
        if ((double) limbSwingAmount > 0.1) {
            newangle = Mth.cos((float) (ageInTicks * 1.3f * WINGSPEED)) * (float) Math.PI * 0.1f * limbSwingAmount;
            newangle2 = Mth.cos((float) ((float) ((double) (ageInTicks * 1.3f * WINGSPEED) + 0.7853981633974483))) * (float) Math.PI * 0.1f * limbSwingAmount;
        } else {
            newangle = 0.0f;
        }
        rotateX(processor, "leg_1", -0.087f + newangle);
        rotateX(processor, "leg_1b", newangle);
        rotateX(processor, "leg_1R", -0.087f - newangle);
        rotateX(processor, "leg_1Rb", -newangle);
        rotateX(processor, "leg_2", -0.052f + newangle2);
        rotateX(processor, "leg_2b", newangle2);
        rotateX(processor, "leg_2R", -0.052f - newangle2);
        rotateX(processor, "leg_2Rb", -newangle2);
        rotateX(processor, "leg_3", -0.349f - newangle);
        rotateX(processor, "leg_3b", -newangle);
        rotateX(processor, "leg_3R", -0.349f + newangle);
        rotateX(processor, "leg_3Rb", newangle);
        float neckYRot = (float) Math.toRadians(netHeadYaw) * 0.25f;   // neck_armour.yRot = neck.yRot = ...
        rotateY(processor, "neck", neckYRot);
        rotateY(processor, "neck_armour", neckYRot);
        rotateY(processor, "horn_base", neckYRot);
        rotateY(processor, "horn_1", neckYRot);
        rotateY(processor, "horn_2", neckYRot);
        rotateY(processor, "horn_L", neckYRot);
        rotateY(processor, "horn_R", neckYRot);
        rotateY(processor, "head", neckYRot);
        rotateY(processor, "snout", neckYRot);
        rotateY(processor, "fan1", neckYRot);
        rotateY(processor, "Lfan2", neckYRot - 0.122f);
        rotateY(processor, "Lfan3", neckYRot - 0.226f);
        rotateY(processor, "Rfan2", neckYRot + 0.122f);
        rotateY(processor, "Rfan3", neckYRot + 0.226f);
        rotateY(processor, "Lear", neckYRot + 0.227f);
        rotateY(processor, "Rear", neckYRot - 0.227f);
        newangle = Mth.cos((float) (ageInTicks * 0.3f * WINGSPEED)) * (float) Math.PI * 0.03f;
        rotateY(processor, "back_armour_4", 0.349f + newangle);
        rotateY(processor, "back_armour_4R", -0.349f - newangle);
        newangle = entity.getAttacking() != 0 ? Mth.cos((float) (ageInTicks * 1.3f * WINGSPEED)) * (float) Math.PI * 0.13f : 0.0f;
        rotateX(processor, "neck", newangle + 0.157f);
        rotateX(processor, "neck_armour", newangle + 0.157f);
        rotateX(processor, "horn_base", newangle + 0.087f);
        rotateX(processor, "horn_1", newangle + 0.192f);
        rotateX(processor, "horn_2", newangle + 0.192f);
        rotateX(processor, "horn_L", newangle + 0.192f);
        rotateX(processor, "horn_R", newangle + 0.192f);
        rotateX(processor, "head", newangle + 0.209f);
        rotateX(processor, "snout", newangle + 0.611f);
        rotateX(processor, "fan1", newangle - 0.139f);
        rotateX(processor, "Lfan2", newangle - 0.209f);
        rotateX(processor, "Lfan3", newangle - 0.331f);
        rotateX(processor, "Rfan2", newangle - 0.209f);
        rotateX(processor, "Rfan3", newangle - 0.331f);
        rotateX(processor, "Lear", newangle + 0.366f);
        rotateX(processor, "Rear", newangle + 0.366f);
    }

    public static final class Renderer extends OreSpawnGeoReplacedEntityRenderer<Hammerhead, HammerheadGeoReplacement> {
        public Renderer(EntityRendererProvider.Context context) {
            super(context, new HammerheadGeoReplacement());
        }
    }
}
