package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.Whale;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.animation.AnimationProcessor;

/**
 * GeckoLib Whale (the hooks, landed by the fifth Tier-2 slice T2e): {@link ModelWhale#setupAnim} verbatim on the
 * converted rig, ON THE HOOK (no keyframe layer, no transcription - the
 * self-gate stays closed until an artist delivers {@code idle} and {@code walk}). No wingspeed: the THRESHOLD
 * idiom twice - above a walking speed of a tenth the fins roll on {@code cos(age x 0.3) x PI x 0.2 x
 * limbSwingAmount} (idle {@code cos(age x 0.08) x PI x 0.05}) about +-0.436 rad, the inner fins at half the
 * outer; the tail pitches on {@code cos(age x 0.4) x PI x 0.16 x limbSwingAmount} (idle {@code cos(age x 0.05)
 * x PI x 0.03}) at 0.5 / 1.25 / 2.25 of it down the three links - the jaw on a 0.03 cosine at 0.02 x PI about 0.087
 * rad; and the POSITION-write idiom: the second tail link's pivot FOLLOWS the first 14 units along (cos, -sin) of
 * its pitch and the two flukes follow the second 8 units along its pitch (y and z only, through {@link #moveYZ}; the
 * first link's pivot is never written - the bind, read through {@link #classicPosition}); every value the classic
 * reads back from a part it just wrote is held in a local.
 *
 *
 * <p>Scale and shadow follow {@link WhaleRenderer}: a 0.1 x 1.0 shadow (ENT-S-092) and a render scale of 1.0 halved
 * for a baby (orig RenderWhale.preRenderScale :39-45, which the port's renderer applies around {@code super.render};
 * uniform, so it commutes with the flip and the yaw and the {@link GeoReplacementDescriptor#applyScale} slot is the
 * same transform). The descriptor scales by {@link WhaleRenderer#SCALE} (made public by the landing slice T2e - the hook
 * survey passed an equal literal while it was private; the T2d form for the Baryonyx and Cassowary).</p>
 */
public final class WhaleGeoReplacement extends OreSpawnGeoReplacement<Whale> {
    private static final GeoReplacementDescriptor<Whale> DESCRIPTOR = new GeoReplacementDescriptor<>(
            () -> ModEntities.WHALE.get(),  // lambda: a bound method ref would initialise ModEntities eagerly
            Whale.class,
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "geo/entity/whale.geo.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "animations/entity/whale.animation.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/whale.png"),
            WhaleRenderer.SHADOW) {
        @Override
        public void applyScale(Whale entity, PoseStack poseStack, float partialTick) {
            // orig RenderWhale.preRenderScale (:39-45; WhaleRenderer.render): a child gets glScalef(scale / 2), otherwise glScalef(scale)
            if (entity.isBaby()) {
                poseStack.scale(WhaleRenderer.SCALE / 2.0f, WhaleRenderer.SCALE / 2.0f, WhaleRenderer.SCALE / 2.0f);
                return;
            }
            poseStack.scale(WhaleRenderer.SCALE, WhaleRenderer.SCALE, WhaleRenderer.SCALE);
        }
    };

    public WhaleGeoReplacement() {
        super(DESCRIPTOR);
    }

    @Override
    protected void applyCustomAnimations(AnimationProcessor<?> processor, PoseInputs inputs) {
        float limbSwingAmount = inputs.limbSwingAmount();
        float ageInTicks = inputs.ageInTicks();
        // ModelWhale.setupAnim verbatim.
        float newangle;
        newangle = limbSwingAmount > 0.1F
                ? Mth.cos(ageInTicks * 0.3F) * (float) Math.PI * 0.2F * limbSwingAmount
                : Mth.cos(ageInTicks * 0.08F) * (float) Math.PI * 0.05F;

        float lfin2Z = 0.436F + newangle;
        rotateZ(processor, "lfin2", lfin2Z);
        rotateZ(processor, "lfin1", lfin2Z / 2.0F);
        float rfin2Z = -0.436F - newangle;
        rotateZ(processor, "rfin2", rfin2Z);
        rotateZ(processor, "rfin1", rfin2Z / 2.0F);

        newangle = Mth.cos(ageInTicks * 0.03F) * (float) Math.PI * 0.02F;
        rotateX(processor, "jaw", 0.087F + newangle);

        newangle = limbSwingAmount > 0.1F
                ? Mth.cos(ageInTicks * 0.4F) * (float) Math.PI * 0.16F * limbSwingAmount
                : Mth.cos(ageInTicks * 0.05F) * (float) Math.PI * 0.03F;

        float tail1X = newangle * 0.5F;
        float tail2X = newangle * 1.25F;
        float tailfinX = newangle * 2.25F;
        rotateX(processor, "tail1", tail1X);
        rotateX(processor, "tail2", tail2X);
        rotateX(processor, "tailfin1", tailfinX);
        rotateX(processor, "tailfin2", tailfinX);

        float[] tail1 = classicPosition(bone(processor, "tail1"));  // never written: the bind pivot
        float tail2Z = tail1[2] + (float) Math.cos(tail1X) * 14.0F;
        float tail2Y = tail1[1] - (float) Math.sin(tail1X) * 14.0F;
        moveYZ(processor, "tail2", tail2Y, tail2Z);

        float tailfinZ = tail2Z + (float) Math.cos(tail2X) * 8.0F;
        float tailfinY = tail2Y - (float) Math.sin(tail2X) * 8.0F;
        moveYZ(processor, "tailfin1", tailfinY, tailfinZ);
        moveYZ(processor, "tailfin2", tailfinY, tailfinZ);
    }

    /** {@code part.y = y; part.z = z} in classic terms, the part's x left as it is (the bind: the classic never writes it). */
    private static void moveYZ(AnimationProcessor<?> processor, String name, float y, float z) {
        float[] current = classicPosition(bone(processor, name));
        moveTo(processor, name, current[0], y, z);
    }

    public static final class Renderer extends OreSpawnGeoReplacedEntityRenderer<Whale, WhaleGeoReplacement> {
        public Renderer(EntityRendererProvider.Context context) {
            super(context, new WhaleGeoReplacement());
        }
    }

    /** The SPEC's {@code locomotion: swimmer} (tools/artist_specs/whale.json): {@code inWater} is {@code isInWater()} (contract section 3; the weights slice: every landed species carries its seed's word, the asset audit pins it). */
    @Override
    public danger.orespawn.entity.client.animation.LocomotionKind locomotion() {
        return danger.orespawn.entity.client.animation.LocomotionKind.SWIMMER;
    }
}
