package danger.orespawn.entity.client;

import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.EnderReaper;
import danger.orespawn.entity.pose.EnderReaperPose;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.animation.AnimationProcessor;

/**
 * GeckoLib Ender Reaper (the hooks): {@link ModelEnderReaper#poseFrom} verbatim on the converted rig, ON THE HOOK (no
 * keyframe layer, no transcription - the self-gate stays closed until an artist delivers {@code idle} and {@code
 * walk}). Wingspeed 0.23f (orig ModelEnderReaper.java:80,83 / ClientProxyOreSpawn.java:474): the THRESHOLD idiom
 * feeding a {@code |cos|} - above a walking speed of a tenth the three scythe parts roll on {@code 1.0f - |cos(age * 1.3f
 * * ws) * PI * 0.25f * limbSwingAmount|}, 1.0 at or below it (orig :589-593); the SCREAMING branch (orig :492-507,
 * read through {@link EnderReaperPose}): screaming, the scythe swings on a 1.9 ws cosine x 0.25 around 1.0 rad, the left
 * arm rises to -0.436 / -0.488 and the six wing parts beat on a 2.7 ws cosine x 0.3; else the left arm hangs at
 * -2.436 / 1.0 and the wings sway on a 0.7 ws cosine x 0.06, both about Y around +-0.785 rad; and the HEAD-LOOK idiom
 * (orig :508-514): yaw = {@code toRadians(netHeadYaw) * 0.45f} clamped to +-0.45 rad (no pitch).
 *
 *
 * <p>Shadow follows {@link EnderReaperRenderer}: a 0.2 x 1.0 shadow (ENT-S-092); the classic renderer scales by 1.0, so no
 * scale hook. The rig has zero-thickness cubes (four: the two wing membranes, 0 x 50 x 17, and two blades), so the shipped
 * geo carries the classic within-cube face order ({@link FaceOrder#KEY}; TEST-007) and the seam expects it.</p>
 */
public final class EnderReaperGeoReplacement extends OreSpawnGeoReplacement<EnderReaper> {
    /** orig ModelEnderReaper.java:80,83 {@code wingspeed} = 0.23f (ClientProxyOreSpawn.java:474): the chain's third multiply. */
    static final float WINGSPEED = 0.23F;
    private static final GeoReplacementDescriptor<EnderReaper> DESCRIPTOR = new GeoReplacementDescriptor<>(
            () -> ModEntities.ENDER_REAPER.get(),  // lambda: a bound method ref would initialise ModEntities eagerly
            EnderReaper.class,
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "geo/entity/enderreaper.geo.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "animations/entity/enderreaper.animation.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/enderreaper.png"),
            EnderReaperRenderer.SHADOW) {
        /** Four zero-thickness cubes (the wing membranes and two blades): the seam expects {@link FaceOrder#KEY} in the shipped geo. */
        @Override
        public boolean cubeFaceOrderRequired() {
            return true;
        }
    };

    public EnderReaperGeoReplacement() {
        super(DESCRIPTOR);
    }

    @Override
    protected void applyCustomAnimations(AnimationProcessor<?> processor, PoseInputs inputs) {
        EnderReaperPose entity = inputs.subject(EnderReaperPose.class);
        float limbSwingAmount = inputs.limbSwingAmount();
        float ageInTicks = inputs.ageInTicks();
        float netHeadYaw = inputs.netHeadYaw();
        // ModelEnderReaper.poseFrom (the body of the classic setupAnim) verbatim, the float chain left to right.
        float newangle = 0.0f;
        newangle = (double) limbSwingAmount > 0.1 ? Mth.cos((float) (ageInTicks * 1.3f * WINGSPEED)) * (float) Math.PI * 0.25f * limbSwingAmount : 0.0f;
        float scytheZRot = 1.0f - Math.abs(newangle);
        rotateZ(processor, "scythe1", scytheZRot);
        rotateZ(processor, "scythe2", scytheZRot);
        rotateZ(processor, "scythe3", scytheZRot);
        if (entity.isScreaming()) {
            newangle = Mth.cos((float) (ageInTicks * 1.9f * WINGSPEED)) * (float) Math.PI * 0.25f;
            scytheZRot = 1.0f + newangle;
            rotateZ(processor, "scythe1", scytheZRot);
            rotateZ(processor, "scythe2", scytheZRot);
            rotateZ(processor, "scythe3", scytheZRot);
            rotateX(processor, "larm1", -0.436f);
            rotateY(processor, "larm1", -0.488f);
            newangle = Mth.cos((float) (ageInTicks * 2.7f * WINGSPEED)) * (float) Math.PI * 0.3f;
        } else {
            rotateX(processor, "larm1", -2.436f);
            rotateY(processor, "larm1", 1.0f);
            newangle = Mth.cos((float) (ageInTicks * 0.7f * WINGSPEED)) * (float) Math.PI * 0.06f;
        }
        rotateY(processor, "lwing3", 0.785f + newangle);
        rotateY(processor, "lwing2", 0.785f + newangle);
        rotateY(processor, "lwing1", 0.785f + newangle);
        rotateY(processor, "rwing3", -0.785f - newangle);
        rotateY(processor, "rwing2", -0.785f - newangle);
        rotateY(processor, "rwing1", -0.785f - newangle);
        float headYRot = (float) Math.toRadians(netHeadYaw) * 0.45f;
        if (headYRot > 0.45f) {
            headYRot = 0.45f;
        }
        if (headYRot < -0.45f) {
            headYRot = -0.45f;
        }
        rotateY(processor, "head", headYRot);
    }

    public static final class Renderer extends OreSpawnGeoReplacedEntityRenderer<EnderReaper, EnderReaperGeoReplacement> {
        public Renderer(EntityRendererProvider.Context context) {
            super(context, new EnderReaperGeoReplacement());
        }
    }
}
