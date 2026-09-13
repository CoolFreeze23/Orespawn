package danger.orespawn.entity.client;

import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.Ostrich;
import danger.orespawn.entity.pose.OstrichPose;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.animation.AnimationProcessor;

/**
 * GeckoLib Ostrich (the hooks): {@link OstrichModel#poseFrom} verbatim on the rig the landing slice converts, ON THE
 * HOOK (no keyframe layer, no transcription - the self-gate stays closed until an artist delivers {@code idle}
 * and {@code walk}). Wingspeed 0.65f (orig ModelOstrich.java:16,57 / ClientProxyOreSpawn.java:450), the
 * entity read through the EXISTING {@link OstrichPose} (ENT-S-093, the Slice 4b form; nothing added to it): the
 * legs' gait on the entity's own per-tick SPEED ({@code sqrt(dx^2 + dz^2)} of {@code xOld - getX},
 * {@code zOld - getZ}) - {@code cos(age x 1.25 x ws) x PI x speed x 0.4}, CLAMPED to +-0.75 past +-0.5 - over the
 * twenty-two leg parts about X (the left positive about -0.297 / 0.483 / -0.437, the right the negative); the three
 * tail feathers about X on a bare 0.05 cosine at 0.06 x PI about -0.594 and about Y on 0.061 / 0.072 cosines at 0.08 x
 * PI about -+0.334; the head yaw from the RenderInfo ACCUMULATION when ridden (orig :335-337: the entity's own
 * yaw delta x 20, negated, eased into {@code rf1} by a sixtieth and clamped to +-50; the Rotator's per-frame
 * RenderInfo precedent) else halved; the SITTING-and-not-activated branch (orig :349) that inverts the six head
 * parts to 3.1415 rad about X and zeroes the yaw; the HEAD-LOOK idiom {@code
 * toRadians(netHeadYaw) x 0.65} on five head parts; the wings' {@code |cos|} flap on 1.0 x ws at 0.15 x PI (Z,
 * mirrored; Y at half) gated by the {@code ri1} LATCH rolled from the entity's random on the frame the rhythm
 * crosses zero upward (orig :370-373; the PurplePower's subject-RNG precedent); and the two hats' VISIBILITY by the
 * activation level (orig :420-425, through {@link #setVisible}). Every value the classic reads back from a part it
 * just wrote is held in a local. Once per rendered frame, as the classic: the ENT-S-147 record of the per-frame dedup
 * applies as it does to the Rotator. Eight claws and the three tail feathers are zero-thickness cubes (ENT-S-161; the
 * classic face order required below).
 *
 * <p>Shadow follows {@link OstrichRenderer}: a 0.55 x 1.0 shadow (ENT-S-092); the classic renderer scales by 1.0 with no
 * scale override, so no scale hook.</p>
 */
public final class OstrichGeoReplacement extends OreSpawnGeoReplacement<Ostrich> {
    /** orig ModelOstrich.java:16,57 {@code wingspeed} = 0.65f (ClientProxyOreSpawn.java:450): the chain's third multiply. */
    static final float WINGSPEED = 0.65F;
    private static final GeoReplacementDescriptor<Ostrich> DESCRIPTOR = new GeoReplacementDescriptor<>(
            () -> ModEntities.OSTRICH.get(),  // lambda: a bound method ref would initialise ModEntities eagerly
            Ostrich.class,
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "geo/entity/ostrich.geo.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "animations/entity/ostrich.animation.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/ostrich.png"),
            OstrichRenderer.SHADOW) {
        /**
         * A rig with zero-thickness cubes (LClaw1-4 and Rclaw1-4, 0 x 2 x 3 / 0 x 1 x 3; Tail1-3, 4 x 0 x 14 / 3 x 0 x
         * 13): the shipped geo carries the classic within-cube face order ({@link FaceOrder#KEY}; TEST-007) and the
         * seam expects it.
         */
        @Override
        public boolean cubeFaceOrderRequired() {
            return true;
        }
    };

    public OstrichGeoReplacement() {
        super(DESCRIPTOR);
    }

    @Override
    protected void applyCustomAnimations(AnimationProcessor<?> processor, PoseInputs inputs) {
        OstrichPose entity = inputs.subject(OstrichPose.class);
        float ageInTicks = inputs.ageInTicks();
        float netHeadYaw = inputs.netHeadYaw();
        // OstrichModel.poseFrom (the body of the classic setupAnim) verbatim, the float chain left to right (its unused
        // local hf carries nothing and is not declared; limbSwing, limbSwingAmount and headPitch are not read).
        float newangle = 0.0f;
        float nextangle = 0.0f;
        float lspeed = 0.0f;
        lspeed = (float) ((entity.xOld() - entity.getX()) * (entity.xOld() - entity.getX()) + (entity.zOld() - entity.getZ()) * (entity.zOld() - entity.getZ()));
        lspeed = (float) Math.sqrt(lspeed);
        newangle = Mth.cos(ageInTicks * 1.25f * WINGSPEED) * (float) Math.PI * lspeed * 0.4f;
        if ((double) newangle > 0.5) {
            newangle = 0.75f;
        }
        if ((double) newangle < -0.5) {
            newangle = -0.75f;
        }
        rotateX(processor, "leftleg", -0.297f + newangle);
        rotateX(processor, "LLeg1", 0.483f + newangle);
        rotateX(processor, "LLeg2", -0.437f + newangle);
        rotateX(processor, "Lfoot1", newangle);
        rotateX(processor, "Lfoot2", newangle);
        rotateX(processor, "Lfoot3", newangle);
        rotateX(processor, "Lfoot4", newangle);
        rotateX(processor, "LClaw1", newangle);
        rotateX(processor, "LClaw2", newangle);
        rotateX(processor, "LClaw3", newangle);
        rotateX(processor, "LClaw4", newangle);
        rotateX(processor, "rightleg", -0.297f - newangle);
        rotateX(processor, "Rleg1", 0.483f - newangle);
        rotateX(processor, "RLeg2", -0.437f - newangle);
        rotateX(processor, "Rfoot1", -newangle);
        rotateX(processor, "Rfoot2", -newangle);
        rotateX(processor, "Rfoot3", -newangle);
        rotateX(processor, "Rfoot4", -newangle);
        rotateX(processor, "Rclaw1", -newangle);
        rotateX(processor, "Rclaw2", -newangle);
        rotateX(processor, "Rclaw3", -newangle);
        rotateX(processor, "Rclaw4", -newangle);
        float tail1X = -0.594f + Mth.cos(ageInTicks * 0.05f) * (float) Math.PI * 0.06f;
        rotateX(processor, "Tail1", tail1X);
        rotateX(processor, "Tail2", tail1X);
        rotateX(processor, "Tail3", tail1X);
        rotateY(processor, "Tail3", -0.334f + Mth.cos(ageInTicks * 0.061f) * (float) Math.PI * 0.08f);
        rotateY(processor, "Tail2", 0.334f - Mth.cos(ageInTicks * 0.072f) * (float) Math.PI * 0.08f);
        // orig ModelOstrich.java:334 r = o.getRenderInfo() - the per-entity scratch (ENT-S-093), the same object the
        // classic model accumulates in; orig :383 setRenderInfo(r) is a self-copy and is omitted (the classic omits it too).
        RenderInfo r = entity.getRenderInfo();
        // orig ModelOstrich.java:335-337 - ridden: the entity's own per-tick yaw delta, not the body-yaw delta (ENT-S-093 split item 2).
        if (entity.isVehicle()) {
            netHeadYaw = (entity.yRotO() - entity.getYRot()) * 20.0f;
            netHeadYaw = -netHeadYaw;
            r.rf1 += (netHeadYaw - r.rf1) / 60.0f;
            if (r.rf1 > 50.0f) {
                r.rf1 = 50.0f;
            }
            if (r.rf1 < -50.0f) {
                r.rf1 = -50.0f;
            }
            netHeadYaw = r.rf1;
        } else {
            netHeadYaw /= 2.0f;
        }
        // orig ModelOstrich.java:349 - head inverts only for a sitting, non-activated ostrich (ENT-S-093 split item 3).
        if (entity.isInSittingPose() && entity.getIsActivated() == 0) {
            netHeadYaw = 0.0f;
            float head1X = 3.1415f;
            rotateX(processor, "Head1", head1X);
            rotateX(processor, "head", head1X);
            rotateX(processor, "mouth1", head1X);
            rotateX(processor, "Neck1", head1X);
            rotateX(processor, "Hat1", head1X);
            rotateX(processor, "Hat2", head1X);
        } else {
            float head1X = 0.0f;
            rotateX(processor, "Head1", head1X);
            rotateX(processor, "head", head1X);
            rotateX(processor, "mouth1", head1X);
            rotateX(processor, "Neck1", head1X);
            rotateX(processor, "Hat1", head1X);
            rotateX(processor, "Hat2", head1X);
        }
        float head1Y = (float) Math.toRadians(netHeadYaw) * 0.65f;
        rotateY(processor, "Head1", head1Y);
        rotateY(processor, "head", head1Y);
        rotateY(processor, "mouth1", head1Y);
        rotateY(processor, "Hat1", head1Y);
        rotateY(processor, "Hat2", head1Y);
        newangle = Mth.cos(ageInTicks * 1.0f * WINGSPEED) * (float) Math.PI * 0.15f;
        nextangle = Mth.cos((ageInTicks + 0.3f) * 1.0f * WINGSPEED) * (float) Math.PI * 0.15f;
        if (nextangle > 0.0f && newangle < 0.0f) {
            // orig ModelOstrich.java:370-373 - per-entity ri1 (ENT-S-093); the entity RNG as the port's classic reads it.
            r.ri1 = 0;
            if (entity.getRandom().nextInt(3) == 1) {
                r.ri1 = 1;
            }
        }
        if (r.ri1 == 0) {
            newangle = 0.0f;
        }
        newangle = Math.abs(newangle);
        rotateZ(processor, "Lwing", -newangle);
        rotateY(processor, "Lwing", newangle / 2.0f);
        rotateZ(processor, "Rwing", newangle);
        rotateY(processor, "Rwing", -newangle / 2.0f);
        // orig ModelOstrich.java:420-425 - Hat1 only when activated, Hat2 only when activated > 1: the part's visible
        // flag in the classic (ModelChipmunk.java:180-181 precedent), the bone's hidden flag here.
        setVisible(processor, "Hat1", entity.getIsActivated() != 0);
        setVisible(processor, "Hat2", entity.getIsActivated() > 1);
    }

    public static final class Renderer extends OreSpawnGeoReplacedEntityRenderer<Ostrich, OstrichGeoReplacement> {
        public Renderer(EntityRendererProvider.Context context) {
            super(context, new OstrichGeoReplacement());
        }
    }
}
