package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.AttackSquid;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.animation.AnimationProcessor;

/**
 * GeckoLib Attack Squid (the hook survey, landed by the fourth Tier-2 slice T2d): {@link
 * ModelAttackSquid#setupAnim} verbatim on the converted rig, ON THE HOOK (no
 * keyframe layer, no transcription - the self-gate stays closed until an artist delivers {@code idle} and {@code
 * walk}). Wingspeed 1.0f (orig ModelAttackSquid.java:14,26 / ClientProxyOreSpawn.java: 437): the THRESHOLD idiom on all
 * ten channels - above a walking speed of a tenth the eight tentacles swing at {@code PI * 0.4 *
 * limbSwingAmount} on their own frequencies (1.2 / 1.1 / 1.0 / 1.9 / 1.8 / 1.7 / 1.6 / 1.5 ws) and the body pitches and
 * rolls at {@code PI * 0.04 * limbSwingAmount} on 0.25 / 0.39 ws; at or below it the same frequencies at {@code PI *
 * 0.1} and {@code PI * 0.01} - around the tentacles' rests (-1.03 / 0.37 / 0.6 / -0.48 / 0.63 / -0.26 / -1.03 / 0.43;
 * two about Z, six about X); and the HEAD-LOOK idiom, the body yawing {@code toRadians(netHeadYaw) * 0.75}. No
 * entity state.
 *
 * <p>Scale and shadow follow {@link AttackSquidRenderer}: 0.9 render scale and a 0.25 x 0.9 shadow (ENT-S-092).</p>
 */
public final class AttackSquidGeoReplacement extends OreSpawnGeoReplacement<AttackSquid> {
    /** orig ModelAttackSquid.java:14,26 {@code wingspeed} = 1.0f (ClientProxyOreSpawn.java:437): the chain's third multiply. */
    static final float WINGSPEED = 1.0F;
    private static final GeoReplacementDescriptor<AttackSquid> DESCRIPTOR = new GeoReplacementDescriptor<>(
            () -> ModEntities.ATTACK_SQUID.get(),  // lambda: a bound method ref would initialise ModEntities eagerly
            AttackSquid.class,
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "geo/entity/attacksquid.geo.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "animations/entity/attacksquid.animation.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/attacksquid.png"),
            AttackSquidRenderer.SHADOW) {
        @Override
        public void applyScale(AttackSquid entity, PoseStack poseStack, float partialTick) {
            // orig RenderAttackSquid.preRenderScale (:39-41): GL11.glScalef(scale, scale, scale) (AttackSquidRenderer.scale)
            poseStack.scale(AttackSquidRenderer.SCALE, AttackSquidRenderer.SCALE, AttackSquidRenderer.SCALE);
        }
    };

    public AttackSquidGeoReplacement() {
        super(DESCRIPTOR);
    }

    @Override
    protected void applyCustomAnimations(AnimationProcessor<?> processor, PoseInputs inputs) {
        float limbSwingAmount = inputs.limbSwingAmount();
        float ageInTicks = inputs.ageInTicks();
        float netHeadYaw = inputs.netHeadYaw();
        // ModelAttackSquid.setupAnim verbatim, the float chain left to right.
        float newangleA = 0.0f;
        float newangleB = 0.0f;
        float newangle8 = 0.0f;
        float newangle1 = 0.0f;
        float newangle2 = 0.0f;
        float newangle3 = 0.0f;
        float newangle4 = 0.0f;
        float newangle5 = 0.0f;
        float newangle6 = 0.0f;
        float newangle7 = 0.0f;
        float pi4 = 0.7853982f;
        if ((double) limbSwingAmount > 0.1) {
            newangleA = Mth.cos(ageInTicks * 0.25f * WINGSPEED) * (float) Math.PI * 0.04f * limbSwingAmount;
            newangleB = Mth.cos(ageInTicks * 0.39f * WINGSPEED) * (float) Math.PI * 0.04f * limbSwingAmount;
            newangle1 = Mth.cos(ageInTicks * 1.2f * WINGSPEED) * (float) Math.PI * 0.4f * limbSwingAmount;
            newangle2 = Mth.cos(ageInTicks * 1.1f * WINGSPEED) * (float) Math.PI * 0.4f * limbSwingAmount;
            newangle3 = Mth.cos(ageInTicks * 1.0f * WINGSPEED) * (float) Math.PI * 0.4f * limbSwingAmount;
            newangle4 = Mth.cos(ageInTicks * 1.9f * WINGSPEED) * (float) Math.PI * 0.4f * limbSwingAmount;
            newangle5 = Mth.cos(ageInTicks * 1.8f * WINGSPEED) * (float) Math.PI * 0.4f * limbSwingAmount;
            newangle6 = Mth.cos(ageInTicks * 1.7f * WINGSPEED) * (float) Math.PI * 0.4f * limbSwingAmount;
            newangle7 = Mth.cos(ageInTicks * 1.6f * WINGSPEED) * (float) Math.PI * 0.4f * limbSwingAmount;
            newangle8 = Mth.cos(ageInTicks * 1.5f * WINGSPEED) * (float) Math.PI * 0.4f * limbSwingAmount;
        } else {
            newangleA = Mth.cos(ageInTicks * 0.25f * WINGSPEED) * (float) Math.PI * 0.01f;
            newangleB = Mth.cos(ageInTicks * 0.39f * WINGSPEED) * (float) Math.PI * 0.01f;
            newangle1 = Mth.cos(ageInTicks * 1.2f * WINGSPEED) * (float) Math.PI * 0.1f;
            newangle2 = Mth.cos(ageInTicks * 1.1f * WINGSPEED) * (float) Math.PI * 0.1f;
            newangle3 = Mth.cos(ageInTicks * 1.0f * WINGSPEED) * (float) Math.PI * 0.1f;
            newangle4 = Mth.cos(ageInTicks * 1.9f * WINGSPEED) * (float) Math.PI * 0.1f;
            newangle5 = Mth.cos(ageInTicks * 1.8f * WINGSPEED) * (float) Math.PI * 0.1f;
            newangle6 = Mth.cos(ageInTicks * 1.7f * WINGSPEED) * (float) Math.PI * 0.1f;
            newangle7 = Mth.cos(ageInTicks * 1.6f * WINGSPEED) * (float) Math.PI * 0.1f;
            newangle8 = Mth.cos(ageInTicks * 1.5f * WINGSPEED) * (float) Math.PI * 0.1f;
        }
        rotateX(processor, "tent1", newangle1 - 1.03f);
        rotateZ(processor, "tent7", newangle2 + 0.37f);
        rotateX(processor, "tent5", newangle3 + 0.6f);
        rotateX(processor, "tent6", newangle4 - 0.48f);
        rotateX(processor, "tent4", newangle5 + 0.63f);
        rotateZ(processor, "tent2", newangle6 - 0.26f);
        rotateX(processor, "tent3", newangle7 - 1.03f);
        rotateX(processor, "tent8", newangle8 + 0.43f);
        rotateX(processor, "body", newangleA);
        rotateZ(processor, "body", newangleB);
        rotateY(processor, "body", newangleA = (float) Math.toRadians(netHeadYaw) * 0.75f);
    }

    public static final class Renderer extends OreSpawnGeoReplacedEntityRenderer<AttackSquid, AttackSquidGeoReplacement> {
        public Renderer(EntityRendererProvider.Context context) {
            super(context, new AttackSquidGeoReplacement());
        }
    }

    /** The SPEC's {@code locomotion: swimmer} (tools/artist_specs/attack_squid.json): {@code inWater} is {@code isInWater()} (contract section 3; the weights slice: every landed species carries its seed's word, the asset audit pins it). */
    @Override
    public danger.orespawn.entity.client.animation.LocomotionKind locomotion() {
        return danger.orespawn.entity.client.animation.LocomotionKind.SWIMMER;
    }
}
