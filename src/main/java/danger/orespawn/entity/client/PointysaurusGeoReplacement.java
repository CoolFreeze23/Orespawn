package danger.orespawn.entity.client;

import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.Pointysaurus;
import danger.orespawn.entity.pose.PointysaurusPose;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.animation.AnimationProcessor;

/**
 * GeckoLib Pointysaurus (the hooks, owner 2026-09-14, addendum item 10; landed by the fifth Tier-2 slice T2e, 2026-09-15, the owner's closing set item 4):
 * {@link ModelPointysaurus#poseFrom} verbatim on
 * the converted rig, ON THE HOOK (no keyframe layer, no transcription - the self-gate stays closed until an artist
 * delivers {@code idle} and {@code walk}; the geo, the wiring and the proofs landed with T2e). Wingspeed 1.0f
 * (the port's model field): the THRESHOLD idiom on the four legs' pitch - above a walking speed of a tenth
 * ({@code (double) limbSwingAmount > 0.1}) {@code cos(age x 1.3 ws) x PI x 0.25 x amount}, the diagonal pairs opposed,
 * 0 at or below it; the HEAD-LOOK idiom (yaw and pitch {@code toRadians x 0.45} on the head, the nose, the three horns
 * (the side horns -+0.14 yaw / -0.16 pitch), the guard and the sixteen bumps at the guard's -0.262 pitch); and the
 * ATTACKING branch on the tail's yaw ({@code getAttacking() != 0}: a 1.3 ws cosine x PI x 0.25, else 0.3 ws x PI x
 * 0.05) over its 0.02 ws pitch sway around 0.28 rad. The entity is read through {@link PointysaurusPose} (the Slice 4b
 * doctrine).
 *
 * <p>Shadow follows {@link PointysaurusRenderer}'s constructor ({@code super(context, model, 1.0f)}: the literal it
 * passes - no SHADOW constant); its private {@code SCALE} is 1.0 (identity, applied around {@code super.render}), so
 * no scale hook. No zero-thickness cube.</p>
 */
public final class PointysaurusGeoReplacement extends OreSpawnGeoReplacement<Pointysaurus> {
    /** The port's {@code ModelPointysaurus.wingspeed} = 1.0f: the chain's frequency multiplier. */
    static final float WINGSPEED = 1.0f;
    private static final GeoReplacementDescriptor<Pointysaurus> DESCRIPTOR = new GeoReplacementDescriptor<>(
            () -> ModEntities.POINTYSAURUS.get(),  // lambda: a bound method ref would initialise ModEntities eagerly
            Pointysaurus.class,
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "geo/entity/pointysaurus.geo.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "animations/entity/pointysaurus.animation.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/pointysaurus.png"),
            // PointysaurusRenderer's constructor passes the literal 1.0f (no SHADOW constant): the equal literal
            1.0F) {
    };

    private static final String[] BUMPS = {"bump1", "bump2", "bump3", "bump4", "bump5", "bump6", "bump7", "bump8",
            "bump9", "bump10", "bump11", "bump12", "bump13", "bump14", "bump15", "bump16"};

    public PointysaurusGeoReplacement() {
        super(DESCRIPTOR);
    }

    @Override
    protected void applyCustomAnimations(AnimationProcessor<?> processor, PoseInputs inputs) {
        PointysaurusPose entity = inputs.subject(PointysaurusPose.class);
        float limbSwingAmount = inputs.limbSwingAmount();
        float ageInTicks = inputs.ageInTicks();
        float netHeadYaw = inputs.netHeadYaw();
        float headPitch = inputs.headPitch();
        // ModelPointysaurus.poseFrom verbatim.
        float newangle = 0.0f;

        newangle = (double) limbSwingAmount > 0.1 ? Mth.cos(ageInTicks * 1.3f * WINGSPEED) * (float) Math.PI * 0.25f * limbSwingAmount : 0.0f;
        rotateX(processor, "lfleg", newangle);
        rotateX(processor, "rrleg", newangle);
        rotateX(processor, "rfleg", -newangle);
        rotateX(processor, "lrleg", -newangle);

        float headYRot = (float) Math.toRadians(netHeadYaw) * 0.45f;   // nose.yRot = head.yRot = ...
        rotateY(processor, "head", headYRot);
        rotateY(processor, "nose", headYRot);
        rotateY(processor, "chorn", headYRot);
        rotateY(processor, "lhorn", headYRot - 0.14f);
        rotateY(processor, "rhorn", headYRot + 0.14f);
        rotateY(processor, "guard", headYRot);
        for (String bump : BUMPS) {
            rotateY(processor, bump, headYRot);
        }

        float headXRot = (float) Math.toRadians(headPitch) * 0.45f;   // nose.xRot = head.xRot = ...
        rotateX(processor, "head", headXRot);
        rotateX(processor, "nose", headXRot);
        rotateX(processor, "chorn", headXRot);
        rotateX(processor, "lhorn", headXRot - 0.16f);
        rotateX(processor, "rhorn", headXRot - 0.16f);
        float guardXRot = headXRot - 0.262f;   // bump1.xRot = guard.xRot = head.xRot - 0.262f; bump2..16.xRot = guard.xRot
        rotateX(processor, "guard", guardXRot);
        for (String bump : BUMPS) {
            rotateX(processor, bump, guardXRot);
        }

        newangle = entity.getAttacking() != 0 ? Mth.cos(ageInTicks * 1.3f * WINGSPEED) * (float) Math.PI * 0.25f : Mth.cos(ageInTicks * 0.3f * WINGSPEED) * (float) Math.PI * 0.05f;
        rotateY(processor, "tail", newangle);
        newangle = Mth.cos(ageInTicks * 0.02f * WINGSPEED) * (float) Math.PI * 0.15f;
        rotateX(processor, "tail", newangle + 0.28f);
    }

    public static final class Renderer extends OreSpawnGeoReplacedEntityRenderer<Pointysaurus, PointysaurusGeoReplacement> {
        public Renderer(EntityRendererProvider.Context context) {
            super(context, new PointysaurusGeoReplacement());
        }
    }
}
