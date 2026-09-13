package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnConfig;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.EntityCaterKiller;
import danger.orespawn.entity.pose.CaterKillerPose;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.animation.AnimationProcessor;

/**
 * GeckoLib CaterKiller (the third Tier-2 slice, 2026-09-13): {@link CaterKillerModel#poseFrom} verbatim on the
 * converted rig, ON THE HOOK (owner 2026-09-13, Amendment 2 to Amendment 1: no keyframe layer, no transcription - the
 * self-gate stays closed until an artist delivers {@code idle} and {@code walk}). Wingspeed 0.22f (orig
 * ModelCaterKiller.java:14,48 / ClientProxyOreSpawn.java:499) and the ATTACKING-branch idiom (orig :248, :251, :289
 * {@code getAttacking() != 0}) on the jaws (Z, 1.7f x PI x 0.07f attacking / 1.3f x PI x 0.025f at rest), the head bob
 * ({@code headoff}: 8 units at 1.7f attacking / 2 units at 0.3f at rest, a POSITION write carried down the head, false
 * head, tusks and jaws and, divided by the segment index plus one, the first three segments' parts) and those
 * segments' legs (X, 2.91f x PI x 0.15f attacking / 0.35f x PI x 0.04f at rest, each a sixteenth-turn behind the last);
 * the second tusks yaw at 2.11f / 2.3f; the first segments' side spikes roll at 0.91f; the six rear segments' z
 * FOLLOWS the walking speed ({@code 39 + (16 + cos(age * 1.7f * ws + i * PI / 4) * 1.5f * limbSwingAmount) * i}, the
 * gait-scaled idiom on a position) with their side spikes rolling at 0.4f; the last segment sits 16 units past the
 * sixth, its back spikes pitching at 0.81f / 0.87f and yawing at 1.11f / 1.3f. The entity is read through
 * {@link CaterKillerPose} (the S4 doctrine); the positions through {@link #moveTo} (an unwritten x or z component is
 * the part's current classic position, {@link #classicPosition}).
 *
 * <p>Scale and shadow follow {@link CaterKillerRenderer}: 1.25 render scale, halved when {@code PLAY_NICELY} is on
 * (orig getPlayNicely), and a 1.0 x 1.25 shadow (ENT-S-092).</p>
 */
public final class CaterKillerGeoReplacement extends OreSpawnGeoReplacement<EntityCaterKiller> {
    /** orig ModelCaterKiller.java:14,48 {@code wingspeed} = 0.22f (ClientProxyOreSpawn.java:499): the chain's second multiply. */
    static final float WINGSPEED = 0.22F;
    private static final GeoReplacementDescriptor<EntityCaterKiller> DESCRIPTOR = new GeoReplacementDescriptor<>(
            () -> ModEntities.ENTITY_CATER_KILLER.get(),  // lambda: a bound method ref would initialise ModEntities eagerly
            EntityCaterKiller.class,
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "geo/entity/caterkiller.geo.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "animations/entity/caterkiller.animation.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/caterkiller.png"),
            CaterKillerRenderer.SHADOW) {
        @Override
        public void applyScale(EntityCaterKiller entity, PoseStack poseStack, float partialTick) {
            // orig RenderCaterKiller.preRenderScale (:39-45): if (getPlayNicely() != 0) glScalef(scale / 2) else glScalef(scale)
            // - the port reads the config directly, as CaterKillerRenderer.scale does
            if (OreSpawnConfig.PLAY_NICELY.get()) {
                poseStack.scale(CaterKillerRenderer.SCALE / 2.0F, CaterKillerRenderer.SCALE / 2.0F, CaterKillerRenderer.SCALE / 2.0F);
                return;
            }
            poseStack.scale(CaterKillerRenderer.SCALE, CaterKillerRenderer.SCALE, CaterKillerRenderer.SCALE);
        }
    };

    public CaterKillerGeoReplacement() {
        super(DESCRIPTOR);
    }

    /** {@code part.y = y} in classic terms: the part's x and z stay where they are. */
    private static void moveY(AnimationProcessor<?> processor, String name, float y) {
        float[] current = classicPosition(bone(processor, name));
        moveTo(processor, name, current[0], y, current[2]);
    }

    /** {@code part.z = z} in classic terms: the part's x and y stay where they are. */
    private static void moveZ(AnimationProcessor<?> processor, String name, float z) {
        float[] current = classicPosition(bone(processor, name));
        moveTo(processor, name, current[0], current[1], z);
    }

    /** {@code part.y = y; part.z = z} in classic terms: the part's x stays where it is. */
    private static void moveYZ(AnimationProcessor<?> processor, String name, float y, float z) {
        float[] current = classicPosition(bone(processor, name));
        moveTo(processor, name, current[0], y, z);
    }

    @Override
    protected void applyCustomAnimations(AnimationProcessor<?> processor, PoseInputs inputs) {
        CaterKillerPose entity = inputs.subject(CaterKillerPose.class);
        float limbSwingAmount = inputs.limbSwingAmount();
        float ageInTicks = inputs.ageInTicks();
        // CaterKillerModel.poseFrom (the body of the classic setupAnim) verbatim, the float chain left to right.
        boolean attacking = entity.getAttacking() != 0;

        float jawAngle = attacking
                ? Mth.cos(ageInTicks * 1.7f * WINGSPEED) * (float) Math.PI * 0.07f
                : Mth.cos(ageInTicks * 1.3f * WINGSPEED) * (float) Math.PI * 0.025f;
        rotateZ(processor, "ljaw", 0.139f + jawAngle);
        rotateZ(processor, "rjaw", -0.139f - jawAngle);

        float headoff = attacking
                ? Mth.cos(ageInTicks * 1.7f * WINGSPEED) * 8.0f
                : Mth.cos(ageInTicks * 0.3f * WINGSPEED) * 2.0f;

        moveY(processor, "Head", -8.0f + headoff);
        moveY(processor, "falsehead", -8.0f + headoff);
        moveY(processor, "ltusk1", -25.0f + headoff);
        moveY(processor, "ltusk2", -25.0f + headoff);
        moveY(processor, "rtusk1", -25.0f + headoff);
        moveY(processor, "rtusk2", -25.0f + headoff);
        moveY(processor, "ljaw", -1.0f + headoff);
        moveY(processor, "rjaw", -1.0f + headoff);

        float tuskAngle = Mth.cos(ageInTicks * 2.11f * WINGSPEED) * (float) Math.PI * 0.08f;
        rotateY(processor, "ltusk2", 0.802f + tuskAngle);
        tuskAngle = Mth.cos(ageInTicks * 2.3f * WINGSPEED) * (float) Math.PI * 0.08f;
        rotateY(processor, "rtusk2", -0.802f + tuskAngle);

        for (int i = 0; i < 3; i++) {
            float yOff = headoff / (float) (i + 1) + 8.0f * i;

            moveYZ(processor, "seg1_" + i, -8.0f + yOff, -12.0f + 14.0f * i);
            moveYZ(processor, "seg1lspike_" + i, -32.0f + yOff, -6.0f + 14.0f * i);
            moveYZ(processor, "seg1rspike_" + i, -32.0f + yOff, -6.0f + 14.0f * i);
            moveYZ(processor, "seg1ltopspike_" + i, -39.0f + yOff, -6.0f + 14.0f * i);
            moveYZ(processor, "seg1rtopspike_" + i, -39.0f + yOff, -6.0f + 14.0f * i);
            moveYZ(processor, "seg1lleg_" + i, -8.0f + yOff, -5.0f + 14.0f * i);
            moveYZ(processor, "seg1rleg_" + i, -8.0f + yOff, -5.0f + 14.0f * i);

            float spikeAngle = Mth.cos((float) (ageInTicks * 0.91f * WINGSPEED + Math.PI / 8.0 * i)) * (float) Math.PI * 0.08f;
            rotateZ(processor, "seg1lspike_" + i, spikeAngle);
            rotateZ(processor, "seg1rspike_" + i, -spikeAngle);

            float legAngle = attacking
                    ? Mth.cos((float) (ageInTicks * 2.91f * WINGSPEED + Math.PI / 8.0 * i)) * (float) Math.PI * 0.15f
                    : Mth.cos((float) (ageInTicks * 0.35f * WINGSPEED + Math.PI / 8.0 * i)) * (float) Math.PI * 0.04f;
            rotateX(processor, "seg1lleg_" + i, legAngle);
            rotateX(processor, "seg1rleg_" + i, -legAngle);
        }

        float segZ = 0.0f;
        for (int i = 0; i < 6; i++) {
            float zdist = Mth.cos(ageInTicks * 1.7f * WINGSPEED + (float) (Math.PI / 4.0) * i) * 1.5f * limbSwingAmount;
            segZ = 39.0f + (16.0f + zdist) * i;

            moveZ(processor, "seg2_" + i, segZ);
            moveZ(processor, "seg2lfoot_" + i, segZ);
            moveZ(processor, "seg2rfoot_" + i, segZ);
            moveZ(processor, "seg2ltopspike_" + i, segZ);
            moveZ(processor, "seg2rtopspike_" + i, segZ);
            moveZ(processor, "seg2lspike_" + i, segZ);
            moveZ(processor, "seg2rspike_" + i, segZ);

            float seg2SpikeAngle = Mth.cos((float) (ageInTicks * 0.4f * WINGSPEED - Math.PI / 8.0 * i)) * (float) Math.PI * 0.07f;
            rotateZ(processor, "seg2lspike_" + i, seg2SpikeAngle);
            rotateZ(processor, "seg2rspike_" + i, -seg2SpikeAngle);
        }

        // this.seg2rspike[5].z + 16.0f: the sixth segment's z the loop just wrote
        float seg3Z = segZ + 16.0f;
        moveZ(processor, "seg3", seg3Z);
        moveZ(processor, "seg3lfoot", seg3Z);
        moveZ(processor, "seg3rfoot", seg3Z);
        moveZ(processor, "seg3lspike", seg3Z);
        moveZ(processor, "seg3rspike", seg3Z);
        moveZ(processor, "seg3ltopspike", seg3Z);
        moveZ(processor, "seg3rtopspike", seg3Z);
        moveZ(processor, "seg3lbackspike", seg3Z + 6.0f);
        moveZ(processor, "seg3rbackspike", seg3Z + 6.0f);

        float seg3SpikeAngle = Mth.cos((float) (ageInTicks * 0.4f * WINGSPEED - Math.PI / 8.0 * 6)) * (float) Math.PI * 0.07f;
        rotateZ(processor, "seg3lspike", seg3SpikeAngle);
        rotateZ(processor, "seg3rspike", -seg3SpikeAngle);

        float backAngle = Mth.cos(ageInTicks * 0.81f * WINGSPEED) * (float) Math.PI * 0.04f;
        rotateX(processor, "seg3lbackspike", -0.977f + backAngle);
        backAngle = Mth.cos(ageInTicks * 0.87f * WINGSPEED) * (float) Math.PI * 0.04f;
        rotateX(processor, "seg3rbackspike", -0.977f + backAngle);

        backAngle = Mth.cos(ageInTicks * 1.11f * WINGSPEED) * (float) Math.PI * 0.04f;
        rotateY(processor, "seg3lbackspike", 0.28f + backAngle);
        backAngle = Mth.cos(ageInTicks * 1.3f * WINGSPEED) * (float) Math.PI * 0.04f;
        rotateY(processor, "seg3rbackspike", -0.28f + backAngle);
    }

    public static final class Renderer extends OreSpawnGeoReplacedEntityRenderer<EntityCaterKiller, CaterKillerGeoReplacement> {
        public Renderer(EntityRendererProvider.Context context) {
            super(context, new CaterKillerGeoReplacement());
        }
    }
}
